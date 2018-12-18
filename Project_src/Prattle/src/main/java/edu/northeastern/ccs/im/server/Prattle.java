package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import edu.northeastern.ccs.im.ParentalControls;
import edu.northeastern.ccs.im.Group;
import edu.northeastern.ccs.im.GroupCrud;
import edu.northeastern.ccs.im.GroupCrudImpl;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.User;
import edu.northeastern.ccs.im.UserCrud;
import edu.northeastern.ccs.im.UserCrudImpl;

/**
 * A network server that communicates with IM clients that connect to it. This
 * version of the server spawns a new thread to handle each client that connects
 * to it. At this point, messages are broadcast to all of the other clients. It
 * does not send a response when the user has gone off-line.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public abstract class Prattle {

	/** Amount of time we should wait for a signal to arrive. */
	private static final int DELAY_IN_MS = 50;

	/** Number of threads available in our thread pool. */
	private static final int THREAD_POOL_SIZE = 20;

	/** Delay between times the thread pool runs the client check. */
	private static final int CLIENT_CHECK_DELAY = 200;

	private static ParentalControls parentalControls = new ParentalControls();

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LogManager
			.getLogger(Prattle.class.getName());

	/** Map of socket ip adresses to user names */
	private static Map<String, String> user_ip_map;

	/** Collection of threads that are currently being used. */
	private static ConcurrentLinkedQueue<ClientRunnable> active;

	/** All of the static initialization occurs in this "method" */
	static {
		// Create the new queue of active threads.
		active = new ConcurrentLinkedQueue<>();
		user_ip_map = new HashMap<>();
	}

	/** Used for testing, will cause break out of main run loop if >= 0 */
	private static int breakLoopAfter = -1;

	/**
	 * Add a user/ip address pair to the server map (used in message archival)
	 * @param username
	 * @param ipAddress
	 */
	public static void addIPtoMap(String username, String ipAddress){
		if (user_ip_map.containsKey(username)){
			user_ip_map.replace(username, ipAddress);
		}else{
			user_ip_map.put(username, ipAddress);
		}
	}

	/**
	 * Remove a user from the server ip map
	 * @param username
	 */
	public static void removeFromIPMap(String username){
		if(user_ip_map.containsKey(username)){
			user_ip_map.remove(username);
		}
	}

	/**
	 * Broadcast a given message to all the other IM clients currently on the
	 * system. This message _will_ be sent to the client who originally sent it.
	 * 
	 * @param message Message that the client sent.
	 */
	public static void broadcastMessage(Message message) {
		// Loop through all of our active threads

		for (ClientRunnable tt : active) {
			// Do not send the message to any clients that are not ready to
			// receive it.
			if (tt.isInitialized()) {

				tt.enqueueMessage(message);
			}
		}
	}

	/**
	 * @param message
	 * @param tt
	 */
	public static void sendMessage(Message message, ClientRunnable tt) {

		tt.enqueueMessage(message);

	}

	/**
	 * sends the message to active client threads with a particular user name
	 * 
	 * @param message       the message to be sent
	 * @param recipientName the name of the recipient the message has to be sent
	 */
	public static void directMessage(Message message, String recipientName, boolean checkUser) {
		UserCrud uc = new UserCrudImpl();
		int flag = 0;
		// Loop through all of our active threads
		for (ClientRunnable tt : active) {
			// Do not send the message to any clients that are not ready to
			// receive it.
			if ((tt.isInitialized()
					&& tt.getName().equalsIgnoreCase(recipientName))
				|| (tt.isInitialized()
						&& tt.getName().equalsIgnoreCase("agency")
						&& checkUser)){
				flag = 1;
				tt.enqueueMessage(message);
			}
		}
		// also archive this direct message (only once, not for each thread)
		if(flag == 1)
			MessageArchive.archiveMessage(message, true, user_ip_map.get(message.getName()), user_ip_map.get(recipientName));
		if(flag == 0) {
			for(String username: uc.getAllUsernames())
				if(username.equalsIgnoreCase(recipientName))
					MessageArchive.archiveMessage(message, false, user_ip_map.get(message.getName()), "");
		}
	}
	
	/**
	 * sends queued messages to active client threads with a particular user name
	 *  without archiving them again
	 * @param message       the message to be sent
	 * @param recipientName the name of the recipient the message has to be sent
	 */
	public static void sendQueuedMessage(Message message, String recipientName) {
		// Loop through all of our active threads
		for (ClientRunnable tt : active) {
			// Do not send the message to any clients that are not ready to
			// receive it.
			if (tt.isInitialized()
					&& tt.getName().equalsIgnoreCase(recipientName)) {
				tt.enqueueMessage(message);
			}
		}
	}

	/**
	 * sends the message to active client threads with a particular group name
	 * 
	 * @param message   the message to be sent
	 * @param groupName the name of the group the message has to be sent
	 */
	public static void groupMessage(Message message, String sender, String groupName,
				List<String> dupGroup,List<User> dupUser) {
		List<String> dupName = new ArrayList<>();
		
		if(dupGroup!=null && 
				dupGroup.isEmpty() && 
				dupUser!=null &&
				!dupUser.isEmpty()) {
			for(User users : dupUser) {
				dupName.add(users.getUsername().toLowerCase());
			}
		}

		Group group = new Group();
		group.setGroupName(groupName);
		GroupCrud crud = new GroupCrudImpl();
		List<User> users = crud.getUsers(group);
		List<String> name = new ArrayList<>();
		List<String> activeUsers = new ArrayList<>();

		if (users != null) {
			for(User user : users) {
				name.add(user.getUsername().toLowerCase());
			}
			for (ClientRunnable tt : active) {
				// Do not send the message to any clients that are not ready to
				// receive it.
				if ((tt.isInitialized() && name.contains(tt.getName()))
						|| (dupGroup!=null &&
							dupName!=null &&
							tt.isInitialized()
								&& tt.getName().equalsIgnoreCase("agency")
								&& (dupGroup.contains(groupName)
										|| 
									checkUser(dupName,name)))) {
					activeUsers.add(tt.getName());
					tt.enqueueMessage(message);
				}
			}
			// also archive this group message (only once, not for each thread)
			MessageArchive.archiveMessage(message, true);


			Message grpToPvtMsg;
			String filteredContent = parentalControls.replaceWords(message.getText());

			for(String s: name) {
				if(!activeUsers.contains(s)) {
					if (UserCrud.isParentalControl(sender, s)) {
						grpToPvtMsg = Message.makePrivateMessage(sender, s,
										filteredContent);
						directMessage(grpToPvtMsg, s, false);
					} else {
						grpToPvtMsg = Message.makePrivateMessage(sender, s,
										getMessage(message.toString()));
						directMessage(grpToPvtMsg, s, false);
					}
				}
			}
		}
	}
	
	/**
	 * Check if user is part of duping user list
	 * @param dupUser
	 * @param grpUser
	 * @return true if part of duping user
	 */
	public static boolean checkUser(List<String> dupUser,List<String> grpUser) {
		
		for(String groupUser : grpUser) {
			for(String dupliUser : dupUser) {
				if(groupUser.equals(dupliUser)) {
					return true;
				}
			}
		}
		
		return false;
		
	}
	
	private static String getMessage(String line) {
		String lineClone = line;
		String[] arr = lineClone.split(" ");
		String message = "";
		for(int i = 6; i < arr.length; i++)
			message = message + arr[i] + " ";
		
		return message;
	}

	/**
	 * Start up the threaded talk server. This class accepts incoming
	 * connections on a specific port specified on the command-line. Whenever it
	 * receives a new connection, it will spawn a thread to perform all of the
	 * I/O with that client. This class relies on the server not receiving too
	 * many requests -- it does not include any code to limit the number of
	 * extant threads.
	 * 
	 * @param args String arguments to the server from the command line. At
	 *             present, none.
	 * @throws IOException Exception thrown if the server cannot connect to the
	 *                     port to which it is supposed to listen.
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		ServerSocketChannel serverSocket = null;
		try {
			// Connect to the socket on the appropriate port to which this
			// server connects.
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.socket()
					.bind(new InetSocketAddress(ServerConstants.PORT));
			// Create the Selector with which our channel is registered.
			Selector selector = SelectorProvider.provider().openSelector();
			// Register to receive any incoming connection messages.
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			// Create our pool of threads on which we will execute.
			ScheduledExecutorService threadPool = Executors
					.newScheduledThreadPool(THREAD_POOL_SIZE);
			// Listen on this port until ...
			boolean done = false;
			int loopIts = 0;
			while (!done) {
				if (breakLoopAfter > 0) {
					// if in a testing context, set breakLoopAfter via
					// reflection
					// Otherwise, this loop will run forever.
					loopIts++;
					done = loopIts > breakLoopAfter;
				}

				// force the logger configuration to update in case someone updated the logging level or file
				((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();

				// Check if we have a valid incoming request, but limit the time we may wait.
				while (selector.select(DELAY_IN_MS) != 0) {
					// Get the list of keys that have arrived since our last
					// check
					Set<SelectionKey> acceptKeys = selector.selectedKeys();
					// Now iterate through all of the keys
					Iterator<SelectionKey> it = acceptKeys.iterator();
					while (it.hasNext()) {
						// Get the next key; it had better be from a new
						// incoming connection
						SelectionKey key = it.next();
						it.remove();
						// Assert certain things I really hope is true
						assert key.isAcceptable();
						assert key.channel() == serverSocket;
						// Create a new thread to handle the client for which we
						// just received a
						// request.
						try {
							// Accept the connection and create a new thread to
							// handle this client.
							SocketChannel socket = serverSocket.accept();
							// Make sure we have a connection to work with.
							if (socket != null) {
								String ip = socket.getRemoteAddress().toString();
								LOGGER.log(Level.INFO, "user has logged on from " + ip);
								ClientRunnable tt = new ClientRunnable(socket);
								// Add the thread to the queue of active threads
								active.add(tt);
								// Have the client executed by our pool of
								// threads.
								@SuppressWarnings("rawtypes")
								ScheduledFuture clientFuture = threadPool
										.scheduleAtFixedRate(tt,
												CLIENT_CHECK_DELAY,
												CLIENT_CHECK_DELAY,
												TimeUnit.MILLISECONDS);
								tt.setFuture(clientFuture);
							}
						} catch (AssertionError ae) {
							LOGGER.log(Level.WARN, ae.toString(), ae);
						} catch (Exception e) {
							LOGGER.log(Level.WARN, e.toString(), e);
						}
					}
				}
			}
			// clean up if the main loop has ended
		} catch (Exception e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		} finally {
			serverSocket.close();
		}
	}

	/**
	 * Returns this ParentalControls instance.
	 *
	 * @return ParentalControls class
	 */
	public static ParentalControls getParentalControls() {
		return parentalControls;
	}

	/**
	 * Remove the given IM client from the list of active threads.
	 * 
	 * @param dead Thread which had been handling all the I/O for a client who
	 *             has since quit.
	 */
	public static void removeClient(ClientRunnable dead) {
		// Test and see if the thread was in our list of active clients so that
		// we can remove it.
		removeFromIPMap(dead.getName());
		if (!active.remove(dead)) {
			LOGGER.log(Level.INFO,
					"Could not find a thread that I tried to remove!\\n");
		}
	}
}

