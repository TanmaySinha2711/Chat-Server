package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import edu.northeastern.ccs.im.Group;
import edu.northeastern.ccs.im.GroupCrud;
import edu.northeastern.ccs.im.GroupCrudImpl;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.User;
import edu.northeastern.ccs.im.UserCrud;
import edu.northeastern.ccs.im.UserCrudImpl;

/**
 * Instances of this class handle all of the incoming communication from a
 * single IM client. Instances are created when the client signs-on with the
 * server. After instantiation, it is executed periodically on one of the
 * threads from the thread pool and will stop being run only when the client
 * signs off.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class ClientRunnable implements Runnable {
	/**
	 * Number of milliseconds that special responses are delayed before being
	 * sent.
	 */
	private static final int SPECIAL_RESPONSE_DELAY_IN_MS = 5000;

	/**
	 * Number of milliseconds after which we terminate a client due to
	 * inactivity. This is currently equal to 5 hours.
	 */
	private static final long TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS = 18000000;

	/**
	 * Number of milliseconds after which we terminate a client due to
	 * inactivity. This is currently equal to 5 hours.
	 */
	private static final long TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS = 600000;

	private static final Logger LOGGER = LogManager
			.getLogger(ClientRunnable.class.getName());

	/**
	 * Time at which we should send a response to the (private) messages we were
	 * sent.
	 */
	private Date sendResponses;

	/**
	 * Time at which the client should be terminated due to lack of activity.
	 */
	private GregorianCalendar terminateInactivity;

	/** Queue of special Messages that we must send immediately. */
	private Queue<Message> immediateResponse;

	/** Queue of special Messages that we will need to send. */
	private Queue<Message> specialResponse;

	/** Socket over which the conversation with the single client occurs. */
	private final SocketChannel socket;

	/**
	 * Utility class which we will use to receive communication from this
	 * client.
	 */
	private ScanNetNB input;

	/** Utility class which we will use to send communication to this client. */
	private PrintNetNB output;

	/** Id for the user for whom we use this ClientRunnable to communicate. */
	private int userId;

	/** Name that the client used when connecting to the server. */
	private String name;

	/**
	 * Whether this client has been initialized, set its user name, and is ready
	 * to receive messages.
	 */
	private boolean initialized;

	/**
	 * name of the recipient who client wants to send a message to
	 */
	private String recipientName;
	
	/**
	 * name of the group the user belongs to
	 */
	private String groupName;

	/**
	 * The future that is used to schedule the client for execution in the
	 * thread pool.
	 */
	private ScheduledFuture<ClientRunnable> runnableMe;

	/** Collection of messages queued up to be sent to this client. */
	private Queue<Message> waitingList;
	UserCrud obj = new UserCrudImpl();
	/**List of users to be duped*/
	private static List<User> dupUser = new ArrayList<>();
	/**Group to be duped*/
	private static List<String> dupGroup = new ArrayList<>();
	/**Agency agent name*/
	private static final String AGENCY_AGENT_NAME = "agency";

	/**
	 * Create a new thread with which we will communicate with this single
	 * client.
	 * 
	 * @param client SocketChannel over which we will communicate with this new
	 *               client
	 * @throws IOException Exception thrown if we have trouble completing this
	 *                     connection
	 */
	public ClientRunnable(SocketChannel client) throws IOException {
		// Set up the SocketChannel over which we will communicate.
		socket = client;
		socket.configureBlocking(false);
		// Create the class we will use to receive input
		input = new ScanNetNB(socket);
		// Create the class we will use to send output
		output = new PrintNetNB(socket);
		// Mark that we are not initialized
		initialized = false;
		// Create our queue of special messages
		specialResponse = new LinkedList<>();
		// Create the queue of messages to be sent
		waitingList = new ConcurrentLinkedQueue<>();
		// Create our queue of message we must respond to immediately
		immediateResponse = new LinkedList<>();
		// Mark that the client is active now and start the timer until we
		// terminate for inactivity.
		terminateInactivity = new GregorianCalendar();
		terminateInactivity
				.setTimeInMillis(terminateInactivity.getTimeInMillis()
						+ TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
	}

	/**
	 * Determines if this is a special message which we handle differently. It
	 * will handle the messages and return true if msg is "special." Otherwise,
	 * it returns false.
	 * 
	 * @param msg Message in which we are interested.
	 * @return True if msg is "special"; false otherwise.
	 */
	private boolean broadcastMessageIsSpecial(Message msg) {
		boolean result = false;
		String text = msg.getText();
		if (text != null) {
			ArrayList<Message> responses = (ArrayList<Message>) ServerConstants
					.getBroadcastResponses(text);
			if (responses != null) {
				for (Message current : responses) {
					handleSpecial(current);
				}
				result = true;
			}
		}
		return result;
	}

	private boolean checkUserCreds(Message msg) {

		User user = new User();
		user.setUsername(msg.getName());
		user.setPassword(msg.getText());
		return (obj.getUser(user));
	}

	/**
	 * Check to see for an initialization attempt and process the message sent.
	 */
	private void checkForInitialization() {
		// Check if there are any input messages to read

		if (input.hasNextMessage()) {
			// If a message exists, try to use it to initialize the connection
			Message msg = input.nextMessage();

			handleMessageType(msg);

		}
	}

	private void handleMessageType(Message msg) {
		if (msg.isHello()) {
			handleLoginMessage(msg);
		} else if (msg.isRegister()) {
			handleRegisterMessage(msg);
		}

	}

	private void handleLoginMessage(Message msg) {
		if (checkUserCreds(msg)) {

			if (setUserName(msg.getName())) {
				// Update the time until we terminate this client due to
				// inactivity.
				terminateInactivity.setTimeInMillis(
						new GregorianCalendar().getTimeInMillis()
								+ TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
				// Set that the client is initialized.
				initialized = true;
				Message msg1 = Message.makeAcknowledgeMessage(getName(),
						"Login Successful");

				Prattle.sendMessage(msg1, this);
				try {
					Prattle.addIPtoMap(msg.getName(), this.socket.getRemoteAddress().toString());
				}catch(IOException e){
					LOGGER.log(Level.WARN, e.toString(), e);
				}
				sendQueuedMessages(checkForQueuedMessages(msg.getName()));
			} else {
				initialized = false;
			}
		} else {
			/**
			 * Not ack and quit
			 */
			initialized = true;
			Message msg1 = Message.makeNoAcknowledgeMessage(msg.getName(),
					"Login Unsuccessful, Please enter valid credentials or register");
			Prattle.sendMessage(msg1, this);
		}
	}
	
	/**
	 * Function checks for queueing messages 
	 * @param username
	 * @return list of queued messages
	 */
	private List<String> checkForQueuedMessages(String username){
		return MessageArchive.getQueuedMessagesForUser(username);
	}
	
	/**
	 * Function sends queueing messages 
	 * @param messages
	 */
	private void sendQueuedMessages(List<String> messages) {
		for(String msg: messages) {
			String sender = getReceiver(msg);
			String receiver = getName();
			String text = getMessage(msg);
			
			Message pvtMsg = Message.makePrivateMessage(sender, receiver, text);
			Prattle.sendQueuedMessage(pvtMsg, receiver);
			try {
				MessageArchive.updateQueuedMessage(receiver, this.socket.getRemoteAddress().toString());
			}catch(IOException e){
				LOGGER.log(Level.WARN, e.toString(), e);
			}
		}
	}
	
	/**
	 * Function extracts the receiver name
	 * @param messages
	 */
	public static String getReceiver(String line) {
		String lineClone = line;
		String[] arr = lineClone.split(" ");
		return arr[0];
	}
	
	/**
	 * methods returns the message text
	 * @param line
	 * @return message text
	 */
	public static String getMessage(String line) {
		String lineClone = line;
		String[] arr = lineClone.split(" ");
		String message = "";
		for(int i = 1; i < arr.length; i++)
			message = message + arr[i] + " ";
		
		return message;
	}
	
	/**
	 * the method is used for recalling messages
	 * @param msg
	 */
	private void recallMessage(Message msg) {
		String text = getTextFromRecallMessage(msg.toString());
		MessageArchive.recallMessage(getName(), text);
	}
	
	/**
	 * used for getting the recalled message texts
	 * @param line
	 * @return
	 */
	public static String getTextFromRecallMessage(String line) {
		String lineClone = line;
		String[] arr = lineClone.split(" ");
		String message = "";
		for(int i = 4; i < arr.length; i++)
			message = message + arr[i] + " ";
		return message;
	}
	
	/**
	 * method to handle registration requests
	 * @param msg
	 */
	private void handleRegisterMessage(Message msg) {
		User user = new User();
		user.setUsername(msg.getName());
		user.setPassword(msg.getText());
		UserCrud crud = new UserCrudImpl();

		// Adding a check to make sure the user doesn't register a username that has profanity in it
		if (Prattle.getParentalControls().checkWords(msg.getName())) {
			initialized = true;
			Message msg1 = Message.makeNoAcknowledgeMessage(msg.getName(), "Username cannot " +
							"contain profanity. Please connect again with a different username");
			Prattle.sendMessage(msg1, this);
			return;
		}


		if (crud.getUser(user) || user.getUsername() == null
				|| user.getUsername().equals("")) {
			initialized = true;
			Message msg1 = Message.makeNoAcknowledgeMessage(msg.getName(),
					"Username exists, please connect again with a different username");
			Prattle.sendMessage(msg1, this);
		} else {
			if (setUserName(msg.getName())) {

				crud.addUser(user);
				// Update the time until we terminate this client due to
				// inactivity.
				terminateInactivity.setTimeInMillis(
						new GregorianCalendar().getTimeInMillis()
								+ TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
				// Set that the client is initialized.
				initialized = true;
				Message msg1 = Message.makeAcknowledgeMessage(getName(),
						"Regisration Successful, start sending messages");
				try {
					Prattle.addIPtoMap(msg.getName(), this.socket.getRemoteAddress().toString());
				}catch(IOException e){
					LOGGER.log(Level.WARN, e.toString(), e);
				}
				Prattle.sendMessage(msg1, this);
			} else {
				initialized = false;
			}
		}
	}

	/**
	 * Process one of the special responses
	 * 
	 * @param msg Message to add to the list of special responses.
	 */
	private void handleSpecial(Message msg) {
		if (specialResponse.isEmpty()) {
			sendResponses = new Date();
			sendResponses.setTime(
					sendResponses.getTime() + SPECIAL_RESPONSE_DELAY_IN_MS);
		}
		specialResponse.add(msg);
	}

	/**
	 * Check if the message is properly formed. At the moment, this means
	 * checking that the identifier is set properly.
	 * 
	 * @param msg Message to be checked
	 * @return True if message is correct; false otherwise
	 */
	private boolean messageChecks(Message msg) {
		// Check that the message name matches.
		return (msg.getName() != null)
				&& (msg.getName().compareToIgnoreCase(getName()) == 0);
	}

	/**
	 * Immediately send this message to the client. This returns if we were
	 * successful or not in our attempt to send the message.
	 * 
	 * @param message Message to be sent immediately.
	 * @return True if we sent the message successfully; false otherwise.
	 */
	private boolean sendMessage(Message message) {
		return output.print(message);
	}

	/**
	 * Try allowing this user to set his/her user name to the given username.
	 * 
	 * @param userName The new value to which we will try to set userName.
	 * @return True if the username is deemed acceptable; false otherwise
	 */
	private boolean setUserName(String userName) {
		// Now make sure this name is legal.
		if (userName != null) {
			// Optimistically set this users ID number.
			setName(userName);
			userId = hashCode();
			return true;
		}
		// Clear this name; we cannot use it. *sigh*
		userId = -1;
		return false;
	}

	/**
	 * set name of recipient private message
	 * @param msg
	 */
	private boolean setReceiverName(String recipientName) {
		// Now make sure this name is legal.
		if (recipientName != null) {
			// Optimistically set this users ID number.
			this.recipientName = recipientName;
			return true;
		}
		return false;
	}
	/**
	 * set the name of the group
	 * @param groupName
	 * @return
	 */
	private boolean setGroupName(String groupName) {
		// Now make sure this name is legal.
		if (groupName != null) {
			// Optimistically set this users ID number.
			this.groupName = groupName;
			return true;
		}
		return false;
	}


	/**
	 * Add the given message to this client to the queue of message to be sent
	 * to the client.
	 * 
	 * @param message Complete message to be sent.
	 */
	public void enqueueMessage(Message message) {
		waitingList.add(message);
	}

	/**
	 * Get the name of the user for which this ClientRunnable was created.
	 * 
	 * @return Returns the name of this client.
	 */
	public String getName() {
		return name;
	}

	/** fetches the recipient name mentioned in the private message
	 * @return name of the receiver
	 */
	public String getRecipientName() {
		return this.recipientName;
	}
	
	/** fetches the group name the user wants to send a message to
	 * @return String name of the group the message is directed to
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * Set the name of the user for which this ClientRunnable was created.
	 * 
	 * @param name The name for which this ClientRunnable.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the user for which this ClientRunnable was created.
	 * 
	 * @return Returns the current value of userName.
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Return if this thread has completed the initialization process with its
	 * client and is read to receive messages.
	 * 
	 * @return True if this thread's client should be considered; false
	 *         otherwise.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Perform the periodic actions needed to work with this client.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {

		boolean terminate = false;
		// The client must be initialized before we can do anything else
		if (!initialized) {

			checkForInitialization();

		} else {

			try {

				if (input.hasNextMessage()) {
					// Client has already been initialized, so we should first
					// check
					// if there are any input
					// messages.
					// Get the next message
					Message msg = input.nextMessage();
					// Update the time until we terminate the client for
					// inactivity.
					terminateInactivity.setTimeInMillis(new GregorianCalendar()
							.getTimeInMillis()
							+ TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS);
					// If the message is a broadcast message, send it out
					if (msg.isDisplayMessage()) {
						// Check if the message is legal formatted
						if (messageChecks(msg)) {
							// Check for our "special messages"
							if ((msg.isBroadcastMessage())
									&& (!broadcastMessageIsSpecial(msg))) {

								if (msg.getText() != null && UserCrud.isParentalControl(msg.getName(), null)
												&& Prattle.getParentalControls().checkWords(msg.getText())) {
									Message msg1 = Message.makeHistoryResponseMessage(msg.getName(),
													"Watch your language! The last message you sent has been marked as inappropriate.");
									Prattle.sendMessage(msg1, this);
								}
								// Check for our "special messages"
								if ((msg.getText() != null)
										&& (msg.getText().compareToIgnoreCase(
												ServerConstants.BOMB_TEXT) == 0)) {
									initialized = false;
									Prattle.broadcastMessage(
											Message.makeQuitMessage(name));
								} else {
									Prattle.broadcastMessage(msg);
								}
							}
						} else {
							Message sendMsg;
							sendMsg = Message.makeBroadcastMessage(
									ServerConstants.BOUNCER_ID,
									"Last message was rejected because it specified an incorrect user name.");
							enqueueMessage(sendMsg);
						}
					} else if (msg.isUpdateMessage()) {
						User user = new User();
						user.setUsername(getName());
						user.setPassword(msg.getText());
						obj.updateUser(user);
						Message msg1 = Message.makeAcknowledgeMessage(getName(),
								"Password update Successful, start sending messages");

						Prattle.sendMessage(msg1, this);
					} else if (msg.isDeleteMessage()) {
						User user = new User();
						user.setUsername(getName());
						obj.deleteUser(user);
						Message msg1 = Message.makeNoAcknowledgeMessage(
								msg.getName(), "Your user has been deleted");
						Prattle.sendMessage(msg1, this);
					} else if (msg.isPrivateMessage()) {

						if (msg.getText() != null && UserCrud.isParentalControl(msg.getName(), msg.getRecipientName())
										&& Prattle.getParentalControls().checkWords(msg.getText())) {
							Message msg1 = Message.makeHistoryResponseMessage(msg.getName(),
											"Watch your language! The last message you sent was sent from or sent to a user that " +
															"\nhas parental controls on and has been marked as inappropriate.");
							Prattle.sendMessage(msg1, this);
						}

						boolean checkUser = false;
						setReceiverName(msg.getRecipientName());
						// Check if the message is legal formatted
						if (messageChecks(msg)) {
							if (((msg.getText() != null)
									|| (msg.getRecipientName() != null))
									&& (msg.getText().compareToIgnoreCase(
											ServerConstants.BOMB_TEXT) == 0)) {
								initialized = false;
								Prattle.broadcastMessage(
										Message.makeQuitMessage(name));
							} else if (msg.getRecipientName() != null) {
								List<String> dupName = new ArrayList<>();
								
								for(User users : dupUser) {
									dupName.add(users.getUsername().toLowerCase());
								}
								if(dupUser!=null && 
										!dupUser.isEmpty() &&
										(dupName.contains(getName().toLowerCase())
												||
										dupName.contains(msg.getRecipientName().toLowerCase()))) {
									checkUser = true;
								}
								
								Prattle.directMessage(msg, recipientName,checkUser);
							}
						}

					}else if (msg.isRecallMessage()) {
						if (messageChecks(msg)) {
							if ((msg.getText() != null)
									&& (msg.getText().compareToIgnoreCase(
											ServerConstants.BOMB_TEXT) == 0)) {
								initialized = false;
								Prattle.broadcastMessage(
										Message.makeQuitMessage(name));
							} else if (msg.getName() != null) {
								recallMessage(msg);
							}
						}

					}else if (msg.isGroupAddMessage()) {

						Group group = new Group();
						group.setGroupName(msg.getText());
						if (new GroupCrudImpl().getGroup(group)
								|| msg.getRecipientName() == null
								|| msg.getRecipientName() == "") {

							Message msg1 = Message
									.makeGroupNoAcknowledgeMessage(getName(),
											"Group creation unsuccessful");

							Prattle.sendMessage(msg1, this);
						} else {

							List<User> userList = fetchAllUsers(
									msg.getRecipientName());
							group.setUsers(userList);
							GroupCrud crud = new GroupCrudImpl();
							crud.addGroup(group);

							Message msg1 = Message.makeGroupAcknowledgeMessage(
									getName(), "Group creation successful");

							Prattle.sendMessage(msg1, this);
						}

					} else if (msg.isGroupDelete()) {
						Group group = new Group();
						group.setGroupName(msg.getText());
						if (new GroupCrudImpl().getGroup(group)) {
							GroupCrud crud = new GroupCrudImpl();
							crud.deleteGroup(group);
							Message msg1 = Message.makeGroupAcknowledgeMessage(
									getName(), "Group deletion successful");

							Prattle.sendMessage(msg1, this);
						}else {
							Message msg1 = Message.makeGroupNoAcknowledgeMessage(
									getName(), "Group deletion unsuccessful");

							Prattle.sendMessage(msg1, this);
						}
						
						
					}else if (msg.isGroupUpdate()) {
						Group group = new Group();
						group.setGroupName(msg.getText());
						if (new GroupCrudImpl().getGroup(group)) {
							List<User> userList = fetchAllUsers(
									msg.getRecipientName());
							group.setUsers(userList);
							GroupCrud crud = new GroupCrudImpl();
							crud.updateGroup(group);
							Message msg1 = Message.makeGroupAcknowledgeMessage(
									getName(), "Group update successful");
							Prattle.sendMessage(msg1, this);
						}else {
							Message msg1 = Message.makeGroupNoAcknowledgeMessage(
									getName(), "Group update unsuccessful");

							Prattle.sendMessage(msg1, this);
						}
						
					}else if (msg.isGroupMessage()) {
						if (msg.getText() != null && UserCrud.isParentalControl(msg.getName(), msg.getRecipientName())
										&& Prattle.getParentalControls().checkWords(msg.getText())) {
							Message msg1 = Message.makeHistoryResponseMessage(msg.getName(),
											"Watch your language! The last message you sent was sent from or sent to a user that " +
																"\nhas parental controls on and has been marked as inappropriate.");
							Prattle.sendMessage(msg1, this);
						}
						setGroupName(msg.getRecipientName());
						// Check if the message is legal formatted
						if (messageChecks(msg)) {
							if (((msg.getText() != null)
									|| (msg.getRecipientName() != null))
									&& (msg.getText().compareToIgnoreCase(
									ServerConstants.BOMB_TEXT) == 0)) {
								initialized = false;
								Prattle.broadcastMessage(
										Message.makeQuitMessage(name));
							} else if (msg.getRecipientName() != null) {
								Prattle.groupMessage(msg, getName(), groupName,dupGroup,dupUser);
							}
						}
					}else if (msg.isHistoryMessage()) {
						List<Message> archives = MessageArchive.getArchivedMessages(msg);
						for (Message retMsg : archives) {
							immediateResponse.add(retMsg);
						}
					}else if (msg.isDupingMessage()){
						
						Group group = new Group();
						group.setGroupName(msg.getText());
						GroupCrud crud = new GroupCrudImpl();
						if (crud.getGroup(group)) {
							
							List<User> users = crud.getUsers(group);
							if(users !=null) {
								for(User user : users) {
									dupUser.add(user);	
								}
							}
							dupGroup.add(msg.getText());
						}else {
							List<User> tempUser = fetchAllUsers(msg.getText());
							if(tempUser !=null) {
								for(User user : tempUser) {
									dupUser.add(user);	
								}
							}
						}		
					}else if (msg.isSearchMessage()) {
						List<Message> archives = MessageArchive.searchMessages(msg);
						immediateResponse.addAll(archives);
					} else if (msg.isParentalControlMessage()){
						User user = new User();
						user.setUsername(getName());
						boolean status = obj.changeParentalControl(user, msg.getText());
						Message msg1;
						if (status) {
							msg1 = Message.makeHistoryResponseMessage(getName(),
											String.format("Parental controls have been changed to: %s", msg.getText()));
						} else {
							msg1 = Message.makeHistoryResponseMessage(getName(), "Parental controls could not be changed. Enter \"PRC <on or off>\" again");
						}
						Prattle.sendMessage(msg1, this);
					} else if (msg.terminate()) {
						terminate = true;
						// Reply with a quit message.
						enqueueMessage(Message.makeQuitMessage(name));
					} 
					// Otherwise, ignore it (for now).
				}
				if (!immediateResponse.isEmpty()) {
					while (!immediateResponse.isEmpty()) {
						sendMessage(immediateResponse.remove());
					}
				}

				// Check to make sure we have a client to send to.
				boolean processSpecial = !specialResponse.isEmpty()
						&& ((!initialized) || (!waitingList.isEmpty())
								|| sendResponses.before(new Date()));
				boolean keepAlive = !processSpecial;
				// Send the responses to any special messages we were asked.
				if (processSpecial) {
					// Send all of the messages and check that we get valid
					// responses.
					while (!specialResponse.isEmpty()) {
						keepAlive |= sendMessage(specialResponse.remove());
					}
				}
				if (!waitingList.isEmpty()) {
					if (!processSpecial) {
						keepAlive = false;
					}
					// Send out all of the message that have been added to the
					// queue.
					do {

						Message msg = waitingList.remove();

						boolean sentGood = sendMessage(msg);
						keepAlive |= sentGood;
					} while (!waitingList.isEmpty());
				}
				terminate |= !keepAlive;
			} finally {
				// When it is appropriate, terminate the current client.
				if (terminate) {
					terminateClient();
				}
			}
		}

		// Finally, check if this client have been inactive for too long and,
		// when they have, terminate
		// the client.
		if (!terminate && terminateInactivity.before(new GregorianCalendar())) {
			LOGGER.log(Level.INFO, "Timing out or forcing off a user " + name);
			terminateClient();
		}
		if(name!=null && name.equalsIgnoreCase(AGENCY_AGENT_NAME) &&
				terminateInactivity.before(new GregorianCalendar())) {
			LOGGER.log(Level.INFO, "Timing out or forcing off a user " + name);
			dupUser.clear();
			dupGroup.clear();
			terminateClient();
		}
	}

	/**
	 * used for fetching all users from a string
	 * @param users
	 * @return
	 */
	public List<User> fetchAllUsers(String users) {
		List<String> userList = Arrays.asList(users.split(","));
		List<User> userLists = new ArrayList<>();
		for (int i = 0; i < userList.size(); i++) {
			User user = new User();
			user.setUsername(userList.get(i));
			userLists.add(user);
		}

		return userLists;
	}

	/**
	 * Store the object used by this client runnable to control when it is
	 * scheduled for execution in the thread pool.
	 * 
	 * @param future Instance controlling when the runnable is executed from
	 *               within the thread pool.
	 */
	public void setFuture(ScheduledFuture<ClientRunnable> future) {
		runnableMe = future;
	}

	/**
	 * Terminate a client that we wish to remove. This termination could happen
	 * at the client's request or due to system need.
	 */
	public void terminateClient() {
		try {
			// Once the communication is done, close this connection.
			input.close();
			socket.close();
		} catch (IOException e) {
			// If we have an IOException, ignore the problem
			LOGGER.log(Level.WARN, e.toString(), e);
		} finally {
			// Remove the client from our client listing.
			Prattle.removeClient(this);
			// And remove the client from our client pool.
			runnableMe.cancel(false);
		}
	}


}
