package edu.northeastern.ccs.im;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;


/**
 * Each instance of this class represents a single transmission by our IM
 * clients.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class Message {

	/**
	 * List of the different possible message types.
	 */
	protected enum MessageType {
	/**
	 * Message sent by the user attempting to login using a specified username.
	 */
	HELLO("HLO"),
	/** Message sent by the server acknowledging a successful log in. */
	ACKNOWLEDGE("ACK"),
	/** Message sent by the server rejecting a login attempt. */
	NO_ACKNOWLEDGE("NAK"),
	/**
	 * Message sent by the user to start the logging out process and sent by the
	 * server once the logout process completes.
	 */
	QUIT("BYE"),
	/** Message whose contents is broadcast to all connected users. */
	BROADCAST("BCT"),
	/** Message type for register **/
	REGISTER("REG"),
	/** Message for update **/
	UPDATE("UPD"),
	/** Delete account **/
	DELETE("DEL"),
	/** Send PVT message **/
	PRIVATE("PVT"),
	/** Message type for requesting archived message history */
	HISTORY("HST"),
	/** Message type for sending archived message history */
	HISTORY_RESPONSE("HSR"),
	/** Message type for creating a group with multiple users **/
	GROUPADD("GAD"), GROUP_ACKNOWLEDGE("GAK"), GROUP_NO_ACKNOWLEDGE("GNK"),
	/** group delete message**/
	GROUPDELETE("GDL"),
	/** group update message**/
	GROUPUPDATE("GUP"),
	/**send message to a particular group of users**/
	GROUP("GRP"),
	/** recall a previously sent message **/
	RECALL("RCL"),
	/** duping message**/
	DUPING("DUP"),
	/** search for messages that have already been sent **/
	SEARCH("SRC"),
	/** Toggles parental control on and off for the current user **/
    PARENTAL_CONTROL("PRC");

		/** Store the short name of this message type. */
		private String tla;

		/**
		 * Define the message type and specify its short name.
		 * 
		 * @param abbrev Short name of this message type, as a String.
		 */
		private MessageType(String abbrev) {
			tla = abbrev;
		}

		/**
		 * Return a representation of this Message as a String.
		 * 
		 * @return Three letter abbreviation for this type of message.
		 */
		@Override
		public String toString() {
			return tla;
		}
	}

	/** The string sent when a field is null. */
	private static final String NULL_OUTPUT = "--";

	/** The handle of the message. */
	private MessageType msgType;

	/**
	 * The first argument used in the message. This will be the sender's
	 * identifier.
	 */
	private String msgSender;

	/** The second argument used in the message. */
	private String msgText;

	/** recipient name for private messages **/
	private String recipientName;

	private static final Logger LOGGER = LogManager
          .getLogger(Message.class.getName());

	/**
	 * start time criteria needed for a search message
	 **/
	private String startTime;
	/**
	 * end time criteria needed for a search message
	 **/
	private String endTime;
	/**
	 * criteria needed for a search message
	 **/
	private String sentReceivedBoth;

  /**
   * Create a new message that contains actual IM text. The type of
   * distribution is defined by the handle and we must also set the name of
   * the message sender and the text to send.
   *
   * @param handle  Handle for the type of message being created.
   * @param srcName Name of the individual sending this message
   * @param text    Text of the instant message
   */
  private Message(MessageType handle, String srcName, String text) {
    this(handle, srcName, text, null);
  }
	
	/**
	 * Creae a duping message
	 * @param myName
	 * @param text
	 * @return nstance of Message that specifies the process of duping.
	 */
	public static Message makeDupingMessage(String myName, String text) {
		if (myName == null)
			return null;
		return new Message(MessageType.DUPING, myName, text);
	}

	
	/**
	 * Create a message to recall a previously sent message
	 * 
	 * @param myName Name of the sender of this very important missive.
	 * @param text   Text of the message that will match the previuosly sent message's text
	 * @return Instance of Recall message
	 */
	public static Message makeRecallMessage(String myName, String text) {
		if (myName == null)
			return null;
		return new Message(MessageType.RECALL, myName, text);
	}


	/**
	 * Given a handle, name, text, password, and recipient name return the
	 * appropriate message instance or an instance from a subclass of message.
	 *

	 * @param handle        Handle of the message to be generated.
	 * @param srcName       Name of the originator of the message (may be null)
	 * @param text          Text sent in this message (may be null)
	 * @param password      Password of the user (may be null)
	 * @param recipientName Name of the receiver of the message (may be null)
	 * @return Instance of Message (or its subclasses) representing the handle,
	 *         name, & text.
	 */
	protected static Message makeMessage(String handle, String srcName,
			String text, String password, String recipientName) {
		Message result = null;
		if (handle.compareTo(MessageType.QUIT.toString()) == 0) {
			result = makeQuitMessage(srcName);
		} else if (handle.compareTo(MessageType.HELLO.toString()) == 0) {
			result = makeSimpleLoginMessage(srcName, password);
		} else if (handle.compareTo(MessageType.BROADCAST.toString()) == 0) {
			result = makeBroadcastMessage(srcName, text);
		} else if (handle.compareTo(MessageType.ACKNOWLEDGE.toString()) == 0) {
			result = makeAcknowledgeMessage(srcName, text);
		} else if (handle
				.compareTo(MessageType.NO_ACKNOWLEDGE.toString()) == 0) {
			result = makeNoAcknowledgeMessage(srcName, text);
		} else if (handle.compareTo(MessageType.REGISTER.toString()) == 0) {
			result = makeRegisterMessage(srcName, password);
		} else if (handle.compareTo(MessageType.UPDATE.toString()) == 0) {
			result = makeUpdateMessage(srcName, password);
		} else if (handle.compareTo(MessageType.DELETE.toString()) == 0) {
			result = makeDeleteMessage(srcName);
		} else if (handle.compareTo(MessageType.PRIVATE.toString()) == 0) {
			result = makePrivateMessage(srcName, recipientName, text);
		}else if (handle.compareTo(MessageType.HISTORY.toString())==0) {
			result = makeHistoryMessage(srcName, recipientName, text);
		}else if (handle.compareTo(MessageType.HISTORY_RESPONSE.toString())==0){
			result = makeHistoryResponseMessage(recipientName, text);
		} else if (handle.compareTo(MessageType.GROUPADD.toString()) == 0) {
			result = makeGroupAdd(srcName, recipientName, text);
		} else if (handle.compareTo(MessageType.GROUPDELETE.toString()) == 0) {
			result = makeGroupDelete(srcName, text);
		}else if (handle.compareTo(MessageType.GROUPUPDATE.toString()) == 0) {
			result = makeGroupUpdate(srcName, recipientName, text);
		}else if (handle.compareTo(MessageType.GROUP.toString()) == 0) {
			result = makeGroupMessage(srcName, recipientName, text);
		}else if (handle.compareTo(MessageType.RECALL.toString()) == 0) {
			result = makeRecallMessage(srcName, text);
		}else if (handle.compareTo(MessageType.DUPING.toString()) == 0) {
			result = makeDupingMessage(srcName, text);
		}
		
		return result;
	}


  private Message(MessageType handle, String srcName, String text, String receiver) {

    msgType = handle;
    // Save the properly formatted identifier for the user sending the
    // message.
    msgSender = srcName;
    // Save the text of the message.
    msgText = text;

    recipientName = receiver;
  }

  /**
   * Create a new message that contains a command sent the server that
   * requires a single argument. This message contains the given handle and
   * the single argument.
   *
   * @param handle  Handle for the type of message being created.
   * @param srcName Argument for the message; at present this is the name used
   *                to log-in to the IM server.
   */
  private Message(MessageType handle, String srcName) {
    this(handle, srcName, null);
  }

  /**
   * Create a new message that contains a command sent the server that
   * requires a single argument. This message contains the given handle and
   * the single argument.
   *
   * @param handle        Handle for the type of message being created.
   * @param srcName       Argument for the message; at present this is the name used
   *                      to log-in to the IM server.
   * @param recipientName Name of the user to search messages for
   * @param startTime     Earliest *sent* time allowed when searching for messages
   * @param endTime       Latest *sent* time allowed when searching for messages
   */
  private Message(MessageType handle, String srcName, String sentReceivedBoth, String recipientName, String startTime, String endTime) {
    msgType = handle;
    // Save the properly formatted identifier for the user sending the
    // message.
    msgSender = srcName;
    this.sentReceivedBoth = sentReceivedBoth;
    this.recipientName = recipientName;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  /**
   * Create a new message to continue the logout process.
   *
   * @return Instance of Message that specifies the process is logging out.
   */
  public static Message makeQuitMessage(String myName) {
    if (myName == null)
      return null;
    return new Message(MessageType.QUIT, myName, null);
  }

  /**
   * Create a new message to add a group with users
   *
   * @return Instance of Message creates a new group
   */
  public static Message makeGroupAdd(String myName, String groupName,
                                     String users) {
    return new Message(MessageType.GROUPADD, myName, groupName, users);
  }

  public static Message makeGroupUpdate(String myName, String groupName,
                                        String users) {
    return new Message(MessageType.GROUPUPDATE, myName, groupName, users);
  }

  public static Message makeGroupDelete(String myName, String groupName) {
    return new Message(MessageType.GROUPDELETE, myName, groupName);
  }

  /**
   * Create a new message broadcasting an announcement to the world.
   *
   * @param myName Name of the sender of this very important missive.
   * @param text   Text of the message that will be sent to all users
   * @return Instance of Message that transmits text to all logged in users.
   */
  public static Message makeBroadcastMessage(String myName, String text) {
    if (myName == null)
      return null;
    return new Message(MessageType.BROADCAST, myName, text);
  }

  /**
   * Create a new message group message to be sent to particular
   * group of users
   *
   * @param myName    Name of the sender of this very important missive.
   * @param groupName Name of the group the user wants to send a message to
   * @param text      Text of the message that will be sent to all users
   * @return Instance of Message that transmits text to all logged in users.
   */
  public static Message makeGroupMessage(String myName, String groupName, String text) {
    if (myName == null || groupName == null)
      return null;
    return new Message(MessageType.GROUP, myName, text, groupName);
  }

  /**
   * Create a new private message from the given myName to the given recipientName with the given
   * text.
   *
   * @param myName        sender's username
   * @param recipientName receiver's username
   * @param text          message text
   * @return a new Message
   */
  public static Message makePrivateMessage(String myName,
                                           String recipientName, String text) {
    if (myName == null || recipientName == null)
      return null;
    return new Message(MessageType.PRIVATE, myName, text, recipientName);
  }

  /**
   * Create a new message stating the name with which the user would like to
   * login.
   *
   * @param text Name the user wishes to use as their screen name.
   * @return Instance of Message that can be sent to the server to try and
   * login.
   */
  protected static Message makeHelloMessage(String text) {
    if (text == null)
      return null;
    return new Message(MessageType.HELLO, null, text);
  }

  /**
   * Create a message to retrieve historical messages with a particular user or group
   *
   * @param myName      The name of the requestor
   * @param withName    The name of the requestor's buddy whos history is desired
   * @param numMessages The number of past messages to retrieve
   */
  public static Message makeHistoryMessage(String myName, String withName, String numMessages) {
    if (myName == null || withName == null) {
      return null;
    }
    int histSize = 10; // default to last 10 messages
    try {
      histSize = Integer.parseInt(numMessages);
    } catch (Exception e) {
      LOGGER.log(Level.WARN, "invalid integer number of messages");
    }
    return new Message(MessageType.HISTORY, myName, Integer.toString(histSize), withName);
  }

  /**
   * Create a message containing a retrieved archived message
   *
   * @param user Who the message is directed to (the original requestor)
   * @param msg  The archive text
   */
  public static Message makeHistoryResponseMessage(String user, String msg) {
    if (user == null) {
      return null;
    }
    return new Message(MessageType.HISTORY_RESPONSE, user, msg);
  }

  /**
   * Create a new message to delete a user.
   *
   * @param myName Name of the user to delete.
   * @return Instance of Message that is used for deleting.
   */
  public static Message makeDeleteMessage(String myName) {
    if (myName == null)
      return null;
    return new Message(MessageType.DELETE, myName);
  }

  /**
   * Update password of a user.
   *
   * @param myName   User's name
   * @param password User's password
   * @return Instance of Message that is used for updating.
   */
  public static Message makeUpdateMessage(String myName, String password) {
    if (myName == null) {
      return null;
    }
    return new Message(MessageType.UPDATE, myName, password);
  }

  /**
   * Create a new message to register a user.
   *
   * @param myName   User's desired name
   * @param password User's password
   * @return Instance of Message that is used for registering.
   */
  public static Message makeRegisterMessage(String myName, String password) {
    if (myName == null) {
      return null;
    }
    return new Message(MessageType.REGISTER, myName, password);
  }

  /**
   * Create a new message to reject the bad login attempt.
   *
   * @param srcName name of the sender of the message
   * @param text    message text
   * @return Instance of Message that rejects the bad login attempt.
   */
  public static Message makeNoAcknowledgeMessage(String srcName,
                                                 String text) {
    return new Message(MessageType.NO_ACKNOWLEDGE, srcName, text);
  }

  public static Message makeGroupNoAcknowledgeMessage(String srcName,
                                                      String text) {
    return new Message(MessageType.GROUP_NO_ACKNOWLEDGE, srcName, text);
  }

  public static Message makeGroupAcknowledgeMessage(String srcName,
                                                    String text) {
    return new Message(MessageType.GROUP_ACKNOWLEDGE, srcName, text);
  }

  /**
   * Create a new message to acknowledge that the user successfully logged as
   * the name <code>srcName</code>.
   *
   * @param srcName Name the user was able to use to log in.
   * @param text    text of the message
   * @return Instance of Message that acknowledges the successful login.
   */
  public static Message makeAcknowledgeMessage(String srcName, String text) {
    if (srcName == null) {
      return null;
    }
    return new Message(MessageType.ACKNOWLEDGE, srcName, text);
  }

  /**
   * Create a new message for the early stages when the user logs in without
   * all the special stuff.
   *
   * @param myName   Name of the user who has just logged in.
   * @param password The password of the user.
   * @return Instance of Message specifying a new friend has just logged in.
   */
  public static Message makeSimpleLoginMessage(String myName,
                                               String password) {
    if (myName == null)
      return null;
    return new Message(MessageType.HELLO, myName, password);
  }

  /**
   * Create new message to request a search for messages from/to the given withName between the given
   * start and endTime.
   *
   * @param myName    The name of the requestor
   * @param withName  The name of the requestor's buddy whos history is desired
   * @param startTime the earliest *sent* time for messages in this search
   * @param endTime   the latest *sent* time for messages in this search
   * @return a new Message
   */
  public static Message makeSearchMessage(String myName, String sentReceivedBoth, String withName, String startTime, String endTime) {
    if (myName == null || withName == null) {
      return null;
    }
    return new Message(MessageType.SEARCH, myName, sentReceivedBoth, withName, startTime, endTime);
  }

  /**
   * Create a new message stating the name with which the user would like to
   * login.
   *
   * @param myName Name of the user that made the request
   * @return Instance of Message that can be sent to the server to try and
   *         login.
   */
  public static Message makeParentalControlMessage(String myName, String onOff) {
    if (myName == null || onOff == null)
      return null;
    return new Message(MessageType.PARENTAL_CONTROL, myName, onOff);
  }

  /**
   * Return the name of the sender of this message.
   *
   * @return String specifying the name of the message originator.
   */
  public String getName() {
    return msgSender;
  }

  /**
   * Return the name of the recipient of this message.
   *
   * @return String specifying the name of the message sender.
   */
  public String getRecipientName() {
    return this.recipientName;
  }

  /**
   * Return the text of this message.
   *
   * @return String equal to the text sent by this message.
   */
  public String getText() {
    return msgText;
  }

  /**
   * Return the start time of this Message.
   *
   * @return String equal to the start time criteria for this SEARCH Message.
   */
  public String getStartTime() {
    return startTime;
  }

  /**
   * Return the end time of this Message.
   *
   * @return String equal to the end time criteria for this SEARCH Message.
   */
  public String getEndTime() {
    return endTime;
  }

  /**
   * Return the sentReceivedBoth string of this Message
   *
   * @return String used to determine the search criteria from a message
   */
  public String getSentReceivedBoth() {
    return this.sentReceivedBoth;
  }

  /**
   * Determine if this message is an acknowledgement message.
   *
   * @return True if the message is an acknowledgement message; false
   * otherwise.
   */
  public boolean isAcknowledge() {
    return (msgType == MessageType.ACKNOWLEDGE);
  }

  /**
   * Determine if this message is an hello message.
   *
   * @return True if the message is an hello message; false otherwise.
   */
  public boolean isHello() {
    return (msgType == MessageType.HELLO);
  }

  /**
   * Determine if this message is an non-acknowledgement message.
   *
   * @return True if the message is an non-acknowledgement message; false
   * otherwise.
   */
  public boolean isNakAcknowledge() {
    return (msgType == MessageType.NO_ACKNOWLEDGE);
  }

  /**
   * Determine if this message is an register message.
   *
   * @return True if the message is an register message; false otherwise.
   */
  public boolean isRegister() {
    return (msgType == MessageType.REGISTER);
  }

  /**
   * Determine if this message is broadcasting text to everyone.
   *
   * @return True if the message is a broadcast message; false otherwise.
   */
  public boolean isBroadcastMessage() {
    return (msgType == MessageType.BROADCAST);
  }

  /**
   * Determine if this message contains text which the recipient should
   * display.
   *
   * @return True if the message is an actual instant message; false if the
   * message contains data
   */
  public boolean isDisplayMessage() {
    return (msgType == MessageType.BROADCAST);
  }

  public boolean isGroupDelete() {
    return (msgType == MessageType.GROUPDELETE);
  }

  /**
   * Determine if the message is for updating a user's account.
   *
   * @return True if the message is of type UPDATE; false otherwise
   */
  public boolean isUpdateMessage() {
    return (msgType == MessageType.UPDATE);
  }

  /**
   * Determine if this message is a delete message.
   *
   * @return True if the message is of message type DELETE; false otherwise
   */
  public boolean isDeleteMessage() {
    return (msgType == MessageType.DELETE);
  }

  public boolean isGroupUpdate() {
    return (msgType == MessageType.GROUPUPDATE);
  }

  /**
   * Determine if this message is a private/direct message.
   *
   * @return True if the message is of type PRIVATE; false otherwise.
   */
  public boolean isPrivateMessage() {
    return (msgType == MessageType.PRIVATE);
  }

  /**
   * Determine if this message is a group message.
   *
   * @return True if the message is of type GROUP; false otherwise.
   */
  public boolean isGroupMessage() {
    return (msgType == MessageType.GROUP);
  }

  /**
   * Determine if this message is a history retrieval message
   *
   * @return True if the message is of type HISTORY, false otherwise
   */
  public boolean isHistoryMessage() {
    return (msgType == MessageType.HISTORY);
  }

  /**
   * Determine if this message is a group add message.
   *
   * @return True if the message is of type GROUP ADD; false otherwise.
   */
  public boolean isGroupAddMessage() {
    return (msgType == MessageType.GROUPADD);
  }

  /**
   * Determine if this message is sent by a new client to log-in to the
   * server.
   *
   * @return True if the message is an initialization message; false otherwise
   */
  public boolean isInitialization() {
    return (msgType == MessageType.HELLO);
  }

  /**
   * Determine if this message is a message signing off from the IM server.
   *
   * @return True if the message is sent when signing off; false otherwise
   */
  public boolean terminate() {
    return (msgType == MessageType.QUIT);
  }

  /**
   * Determine if this message is a search message
   *
   * @return True if the message is of type SEARCH, false otherwise
   */
  public boolean isSearchMessage() {
    return (msgType == MessageType.SEARCH);
  }

  /**
   * Determine if this message is a parental control message
   *
   * @return True if the message is of type PARENTAL_CONTROL, false otherwise
   */
  public boolean isParentalControlMessage() {
    return (msgType == MessageType.PARENTAL_CONTROL);
  }

  public boolean isHistoryResponseMessage() {
    return (msgType == MessageType.HISTORY_RESPONSE);
  }

  public MessageType getMsgType() {
    return this.msgType;
  }

  /**
   * Representation of this message as a String. This begins with the message
   * handle and then contains the length (as an integer) and the value of the
   * next two arguments.
   *
   * @return Representation of this message as a String.
   */
  @Override
  public String toString() {
    String result = msgType.toString();
    if (msgType.equals(MessageType.PRIVATE)
            || msgType.equals(MessageType.HISTORY)
            || msgType.equals(MessageType.GROUPADD)
            || msgType.equals(MessageType.GROUPUPDATE)
            || msgType.equals(MessageType.GROUP)) {
      // msgSender and msgRecipient cannot be null in a Private message.
      result += " " + msgSender.length() + " " + msgSender;
      result += " " + recipientName.length() + " " + recipientName;
      if (msgText != null) {
        result += " " + msgText.length() + " " + msgText;
      } else {
        result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
      }
    } else if (msgType.equals(MessageType.SEARCH)) {
      result += " " + msgSender.length() + " " + msgSender;
      result += " " + sentReceivedBoth.length() + " " + sentReceivedBoth;
      result += " " + recipientName.length() + " " + recipientName;
      result += " " + startTime.length() + " " + startTime;
      result += " " + endTime.length() + " " + endTime;
    } else {
      if (msgSender != null) {
        result += " " + msgSender.length() + " " + msgSender;
      } else {
        result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
      }
      if (msgText != null) {
        result += " " + msgText.length() + " " + msgText;
      } else {
        result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
      }
    }
    return result;
  }
  
  	/** checks if message type is RECALL
  	 * @return true if message type is RECALL, else false   
  	 */
  public boolean isRecallMessage() {
	  return (msgType == MessageType.RECALL);
  }
  
  /** checks if message type is DUPING
	 * @return true if message type is DUPING, else false
	 */
  public boolean isDupingMessage() {
	  return (msgType == MessageType.DUPING);
  }
}


