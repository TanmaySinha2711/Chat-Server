package edu.northeastern.ccs.im.server;


import edu.northeastern.ccs.im.Database;
import edu.northeastern.ccs.im.Group;
import edu.northeastern.ccs.im.GroupCrud;
import edu.northeastern.ccs.im.GroupCrudImpl;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.User;
import edu.northeastern.ccs.im.UserCrud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

/**
 * Archive messages and retrieve archived messages from a SQL database
 *
 * @author Sarah Lichtman
 */
public class MessageArchive {

  private MessageArchive() {
  }

  private static String nullStr = "NULL";

  private static final Logger LOGGER = LogManager
          .getLogger(MessageArchive.class.getName());

  private static final String ORDERBYSENTTIMEDESC = "ORDER BY sent_time DESC;";

  /**
   * Get the integer ID for the specified value, fieldname, and table
   *
   * @param name      The string value of the fieldname column to get an ID for
   * @param fieldname field name in the input table in the SQL database
   * @param table     table name in the SQL database
   * @return value in id field in specified table for specified name, or -1 if no match
   */
  protected static int getID(String name, String fieldname, String table) {
    int id = -1;
    String sql = "SELECT id FROM tablename WHERE fieldname =?;";
    sql = sql.replaceFirst("tablename", table);
    sql = sql.replaceFirst("fieldname", fieldname);
    ResultSet rs = null;
    try (Connection dbConn = Database.getConnection();
         PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
      pstmt.setString(1, name);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        id = rs.getInt("id");
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARN, e.toString(), e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARN, e.toString(), e);
      }
    }
    return id;
  }

  /**
   * Get the string value specified for the input id, fieldname and table
   *
   * @param id        ID in the SQL database for the desired entry
   * @param fieldname field name in the input table in the SQL database
   * @param table     table name in the SQL database
   * @return String representing the value in the field specified by fieldname, or "NULL" if no match
   */
  protected static String getName(int id, String fieldname, String table) {
    String name = MessageArchive.nullStr;
    String sql = "SELECT fieldname FROM tablename WHERE id=?;";
    sql = sql.replaceFirst("fieldname", fieldname);
    sql = sql.replaceFirst("tablename", table);
    ResultSet rs = null;
    try (Connection dbConn = Database.getConnection();
         PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
      pstmt.setInt(1, id);
      rs = pstmt.executeQuery();
      if (rs.next()) {
        name = rs.getString(fieldname);
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARN, e.toString(), e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARN, e.toString(), e);
      }
    }
    return name;
  }


    /**
     * Place the provided message into the  Messages table in the server database
     * @param m the Message to archive
     * @param receivedStatus true if the message was received by the sender, false otherwise
     * @param sender_ip a string representing the ip address of the message sender when sent
     * @param receiver_ip a string representing the ip address of the message recipient when received
     */
    public static void archiveMessage(Message m, boolean receivedStatus, String sender_ip, String receiver_ip){
      boolean flagged = false;
      if (UserCrud.isParentalControl(m.getName(), m.getRecipientName()) &&
              Prattle.getParentalControls().checkWords(m.getText())) {
        flagged = true;
      }

        String sql = "INSERT INTO Messages(sent_time, from_id, to_user_id, to_group_id, message_type, text, received, flagged, sent_ip, received_ip) "
                + "VALUES(NOW(), (SELECT id FROM Users WHERE username=?), "
                +"(SELECT id FROM Users WHERE username=?), (SELECT id FROM Groups WHERE groupname=?), ?, ?, ?, ?, ?, ?); ";

        try (Connection dbConn = Database.getConnection();
             PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
            pstmt.setString(1, m.getName());
            pstmt.setString(2, m.getRecipientName());
            pstmt.setString(3, m.getRecipientName());
            pstmt.setString(4, m.toString().substring(0,3));
            pstmt.setString(5, m.getText());
            pstmt.setBoolean(6, receivedStatus);
            pstmt.setBoolean(7, flagged);
            pstmt.setString(8, sender_ip);
            pstmt.setString(9, sender_ip);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.WARN, e.toString(), e);
        }
	}

    /**
     * Place the provided message into the  Messages table in the server database
     * @param m the Message to archive
     * @param receivedStatus boolean indicating whether the recipient has gotten the message
     */
    public static void archiveMessage(Message m, boolean receivedStatus){
        archiveMessage(m, receivedStatus, "", "");
    }

    /**
     * Retrieve archived messages based on a history message sent to the server
     * @param hm Message which contains the history request
     * @return a list of messages (in ascending order, 0th element is most recent)
     */
    public static List<Message> getArchivedMessages(Message hm){
        if(!hm.isHistoryMessage()){
            LOGGER.log(Level.WARN, "tried to get an archived message with a non history message!");
            return new ArrayList<Message>();
        }
        List<Message> retMessages = new ArrayList<>();

        int toGroupID = getID(hm.getRecipientName(), "groupname", "Groups");

        String sql = "";
        if (toGroupID != -1){
            // group message, get all messages from anyone to the group
            sql = "SELECT sent_time, message_type, from_id, text FROM Messages "
                    + "WHERE to_group_id=(SELECT id FROM Groups WHERE groupname=?) "
                    + "ORDER BY sent_time DESC LIMIT ?;";
        }else{
            // direct message, get all messages between the two users
            sql = "SELECT sent_time, message_type, from_id, text FROM ("
                    +"SELECT * FROM Messages WHERE (to_user_id=(SELECT id FROM Users WHERE username=?) AND from_id=(SELECT id FROM Users WHERE username=?)) "
                    + "AND message_type != \"RCL\""
                    + " UNION "
                    +"SELECT * FROM Messages WHERE (from_id=(SELECT id FROM Users WHERE username=?) AND to_user_id=(SELECT id FROM Users WHERE username=?)) "
                    + "AND message_type != \"RCL\" "
                    + ") AS T ORDER BY sent_time DESC LIMIT ?;";
        }
        ResultSet rs = null;
        try (Connection dbConn = Database.getConnection();
             PreparedStatement pstmt = dbConn.prepareStatement(sql)){

            if (toGroupID != -1) {
                // group message
                pstmt.setString(1, hm.getRecipientName());
                pstmt.setInt(2, Integer.parseInt(hm.getText()));
            }else{
                // direct message
                pstmt.setString(1, hm.getName());
                pstmt.setString(2, hm.getRecipientName());
                pstmt.setString(3, hm.getName());
                pstmt.setString(4, hm.getRecipientName());
                pstmt.setInt(5, Integer.parseInt(hm.getText()));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                // for each entry pulled from the archive, construct a new HSR message to return to the requestor
                String msgText = MessageArchive.constructArchiveText(
                        rs.getTimestamp("sent_time").toString(),
                        rs.getString("message_type"),
                        MessageArchive.getName(rs.getInt("from_id"), "username", "Users"),
                        rs.getString("text"));
                retMessages.add(Message.makeHistoryResponseMessage(hm.getName(), msgText));
            }
            retMessages.add(Message.makeHistoryResponseMessage(hm.getName(), "end archive retrieval"));
        }catch (SQLException e){
            LOGGER.log(Level.WARN, e.toString(), e);
        }finally {
            try{
                if(rs!=null){rs.close();}
            }catch (Exception e){
                LOGGER.log(Level.WARN, e.toString(), e);
            }
        }	

        return retMessages;
    }

  /**
   * Retrieve archived messages based on a search message sent to the server
   *
   * @param sm Message which contains the history request
   * @return a list of messages (in ascending order, 0th element is most recent)
   */
  public static List<Message> searchMessages(Message sm) {
    if (!sm.isSearchMessage()){
      return new ArrayList<>();
    }
    List<String> parameters = new ArrayList<>();

    int toGroupID = getID(sm.getRecipientName(), "groupname", "Groups");

    if (sm.getRecipientName().equalsIgnoreCase("*")) {
      return allMessages(sm);
    } else if (toGroupID != -1) {
      // user is part of group
      if (isMember(sm)) {
        return groupMessages(sm);
      }
      // user is not part of group
      return executeQuery("", parameters, sm);
    } else {
      return privateMessages(sm);
    }
  }

  /**
   * Retrieve archived messages based on a search message sent to the server. This method is used
   * when the search message recipient is "*" (everyone).
   *
   * @param sm SEARCH message
   * @return list of Messages
   */
  private static List<Message> allMessages(Message sm) {
    String sql;
    List<String> parameters = new ArrayList<>();
    int type = getType(sm.getSentReceivedBoth());
    String selectSentTypeFromToUserGroupFromMessages = "SELECT sent_time, message_type, from_id, text, to_user_id, to_group_id FROM Messages ";
    String alias = ") AS T ";


    if (type == 0) {
      // select all messages both sent by and received by current user
      sql = "SELECT sent_time, message_type, from_id, text, to_user_id, to_group_id FROM (" +
              "SELECT * FROM Messages WHERE (to_user_id=(SELECT id FROM Users WHERE username=?) AND sent_time > ? AND sent_time < ?) " +
              "UNION " +
              "SELECT * FROM Messages WHERE (from_id=(SELECT id FROM Users WHERE username=?) AND sent_time > ? AND sent_time < ?) " +
              alias + ORDERBYSENTTIMEDESC;
      parameters.add(sm.getName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
      parameters.add(sm.getName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    } else if (type == 1) {
      // select all messages received by current user
      sql = selectSentTypeFromToUserGroupFromMessages
              + "WHERE to_user_id=(SELECT id FROM Users WHERE username=?) AND sent_time > ? AND sent_time < ? "
              + ORDERBYSENTTIMEDESC;
      parameters.add(sm.getName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    } else {
      // select all messages sent by current user
      sql = selectSentTypeFromToUserGroupFromMessages
              + "WHERE from_id=(SELECT id FROM Users Where username = ?) AND sent_time > ? AND sent_time < ? "
              + ORDERBYSENTTIMEDESC;
      parameters.add(sm.getName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    }
      return executeQuery(sql, parameters, sm);
  }

    
    /** fetches the queued up messages for a user
     * @param username the user for whom the non received messages have to be fetched
     * @return A list of all non received/ queued up messages
     */
    public static List<String> getQueuedMessagesForUser(String username){
    	List<String> messages = new ArrayList<>();
    	String sql = "SELECT Users.username, Messages.text from Users, Messages"
    			+ " WHERE Messages.received = 0 AND"
    			+ " Messages.to_user_id = "
    			+ "(SELECT id FROM Users WHERE username=?)"
    			+ " AND Messages.from_id = Users.id";
    	
    	ResultSet rs = null;
    	try (Connection dbConn = Database.getConnection();
                PreparedStatement pstmt = dbConn.prepareStatement(sql)){
    		pstmt.setString(1, username);
        		rs = pstmt.executeQuery();
            while (rs.next()) {
            	String sender = rs.getString("Users.username");
            	String message = rs.getString("Messages.text");
            	messages.add(sender + " " + message);
            }
    	} catch (SQLException e) {
    		LOGGER.log(Level.WARN, e.toString(), e);
		}finally {
			try{
                if(rs!=null){
                	rs.close();
                }
            }catch (Exception e){
                LOGGER.log(Level.WARN, e.toString(), e);
            }
    	}
    	return messages;
    }
    
    /** sets the received status of queued messages to 1 after they have been sent
     * @param username user for which received status is set to 1
     */
    public static void updateQueuedMessage(String username, String user_ip) {
    	String sql = "UPDATE Messages "
                + "SET received = 1, sent_time = NOW(), received_ip = ? "
                + "WHERE to_user_id = (SELECT id FROM Users WHERE username=?)"
                + " AND received = 0";

        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user_ip);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
        	LOGGER.log(Level.WARN, e.toString(), e);
        }
    }
    
    /** recalls a given message based on username and text
     * @param username the name of the user who requuested a recall
     * @param text the text of the message which has to be recalled
     */
    public static void recallMessage(String username, String text) {
    	String sql = "UPDATE Messages "
                + "SET received = 1, sent_time = NOW(), message_type = \"RCL\" "
                + "WHERE from_id = (SELECT id FROM Users WHERE username=?)"
                + " AND received = 0 "
                + "AND text=?";
 
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setString(1, username);
            pstmt.setString(2, text);
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
        	LOGGER.log(Level.WARN, e.toString(), e);
        }
    }

  /**
   * Retrieve archived messages based on a search message sent to the server. This method is used
   * when the search message recipient is a group.
   *
   * @param sm SEARCH message
   * @return list of Messages
   */
  private static List<Message> groupMessages(Message sm) {
    List<String> parameters = new ArrayList<>();
    String sql;
    String selectSentTypeFromToUserGroupFromMessages = "SELECT sent_time, message_type, from_id, text, to_user_id, to_group_id FROM Messages ";

    int type = getType(sm.getSentReceivedBoth());
    if (type == 0) {
      // Both messages received and sent to group
      sql = selectSentTypeFromToUserGroupFromMessages
              + "WHERE to_group_id=(SELECT id FROM Groups WHERE groupname=?) AND sent_time > ? AND sent_time < ? "
              + ORDERBYSENTTIMEDESC;
      // received from and sent to group
      parameters.add(sm.getRecipientName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    } else if (type == 1) {
      sql = selectSentTypeFromToUserGroupFromMessages
              + "WHERE to_group_id=(SELECT id FROM Groups WHERE groupname=?) AND from_id!=(Select id FROM Users WHERE username=?) AND sent_time > ? AND sent_time < ? "
              + ORDERBYSENTTIMEDESC;
      //received from group
      parameters.add(sm.getRecipientName());
      parameters.add(sm.getName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    } else {
      // Messages sent to the group from the current user
      sql = selectSentTypeFromToUserGroupFromMessages
              + "WHERE from_id=(SELECT id FROM Users Where username = ?) AND to_group_id=(SELECT id FROM Groups WHERE groupname=?) AND sent_time > ? AND sent_time < ? "
              + ORDERBYSENTTIMEDESC;
      parameters.add(sm.getName());
      parameters.add(sm.getRecipientName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    }

    return executeQuery(sql, parameters, sm);
  }

  /**
   * Retrieve archived messages based on a search message sent to the server. This method is used
   * when the search message recipient is a user.
   *
   * @param sm SEARCH message
   * @return list of Messages
   */
  private static List<Message> privateMessages(Message sm) {
    if (!sm.isSearchMessage()){
      return new ArrayList<>();
    }
    List<String> parameters = new ArrayList<>();
    String sql;

    String selectSentTypeFromToUserFrom = "SELECT sent_time, message_type, from_id, text, to_user_id FROM (";
    String alias = ") AS T ";
    int type = getType(sm.getSentReceivedBoth());

    // Searching direct messages between two users
    if (type == 0) {
      // direct message, get all messages between the two users
      sql = selectSentTypeFromToUserFrom
              + "SELECT * FROM Messages WHERE (to_user_id=(SELECT id FROM Users WHERE username=?) AND from_id=(SELECT id FROM Users WHERE username=?) AND sent_time >? AND sent_time <? AND message_type != \"RCL\")"
              + " UNION "
              + "SELECT * FROM Messages WHERE (from_id=(SELECT id FROM Users WHERE username=?) AND to_user_id=(SELECT id FROM Users WHERE username=?) AND sent_time >? AND sent_time <? AND message_type != \"RCL\")"
              + alias + ORDERBYSENTTIMEDESC;
      parameters.add(sm.getName());
      parameters.add(sm.getRecipientName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
      parameters.add(sm.getName());
      parameters.add(sm.getRecipientName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    } else if (type == 1) {
      sql = selectSentTypeFromToUserFrom
              + "SELECT * FROM Messages WHERE (from_id=(SELECT id FROM Users WHERE username=?) AND to_user_id=(SELECT id FROM Users WHERE username=?) AND sent_time >? AND sent_time <? AND message_type != \"RCL\") "
              + alias + ORDERBYSENTTIMEDESC;
      parameters.add(sm.getRecipientName());
      parameters.add(sm.getName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    } else {
      sql = selectSentTypeFromToUserFrom
              + "SELECT * FROM Messages WHERE (to_user_id=(SELECT id FROM Users WHERE username=?) AND from_id=(SELECT id FROM Users WHERE username=?) AND sent_time >? AND sent_time <? AND message_type != \"RCL\") "
              + alias + ORDERBYSENTTIMEDESC;
      parameters.add(sm.getRecipientName());
      parameters.add(sm.getName());
      parameters.add(sm.getStartTime());
      parameters.add(sm.getEndTime());
    }

    return  executeQuery(sql, parameters, sm);
  }

  /**
   * Returns an int that represents the type of message.
   * @param sentReceivedBoth string
   * @return an integer representing receivedFrom (1), sentTo (2), or both (0)
   */
  private static int getType(String sentReceivedBoth) {
    if (sentReceivedBoth.equalsIgnoreCase("both")) {
      return 0;
    } else if (sentReceivedBoth.equalsIgnoreCase("receivedFrom")) {
      return 1;
    } else {
      return 2;
    }
  }

  /**
   * Determines if the sender of this message is a member of the group within the message.
   * @param sm SEARCH message
   * @return true if the sender is a member, false otherwise.
   */
  private static boolean isMember(Message sm) {
    Group group = new Group();
    group.setGroupName(sm.getRecipientName());
    GroupCrud crud = new GroupCrudImpl();
    List<User> users = crud.getUsers(group);
    for (User x : users) {
      if (x.getUsername().equalsIgnoreCase(sm.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Executes the given query with the given parameters and creates a list of response messages
   * that are returned.
   *
   * @param sql query
   * @param parameters arguments for query
   * @param sm SEARCH message
   * @return list of Messages
   */
  private static List<Message> executeQuery(String sql, List<String> parameters, Message sm) {
    ResultSet rs = null;
    List<Message> retMessages = new ArrayList<>();

    if (sql.equalsIgnoreCase("")) {
      retMessages.add(Message.makeHistoryResponseMessage(sm.getName(), "Cannot access because you are not member of that group"));
      retMessages.add(Message.makeHistoryResponseMessage(sm.getName(), "end archive retrieval"));
      return retMessages;
    }

    try (Connection dbConn = Database.getConnection();
         PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
      for (int i = 1; i <= parameters.size(); i++) {
        pstmt.setString(i, parameters.get(i-1));
      }
      rs = pstmt.executeQuery();
      while (rs.next()) {
        // for each entry pulled from the archive, construct a new SR message to return to the requestor
        if (rs.getString("message_type").equalsIgnoreCase("GRP")) {
          String msgText = MessageArchive.constructArchiveText(
                  rs.getTimestamp("sent_time").toString(),
                  rs.getString("message_type"),
                  MessageArchive.getName(rs.getInt("from_id"), "username", "Users"),
                  MessageArchive.getName(rs.getInt("to_group_id"), "groupname", "Groups"),
                  rs.getString("text"));
          retMessages.add(Message.makeHistoryResponseMessage(sm.getName(), msgText));
        } else {
          String msgText = MessageArchive.constructArchiveText(
                  rs.getTimestamp("sent_time").toString(),
                  rs.getString("message_type"),
                  MessageArchive.getName(rs.getInt("from_id"), "username", "Users"),
                  MessageArchive.getName(rs.getInt("to_user_id"), "username", "Users"),
                  rs.getString("text"));
          retMessages.add(Message.makeHistoryResponseMessage(sm.getName(), msgText));
        }
      }
    } catch (SQLException e) {
      LOGGER.log(Level.WARN, e.toString(), e);
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARN, e.toString(), e);
      }
    }
    retMessages.add(Message.makeHistoryResponseMessage(sm.getName(), "end archive retrieval"));
    return retMessages;
  }

  /**
   * Construct a string containing information about the requested message from the archive
   *
   * @param sentTime    SQL TIMESTAMP of when the message was received by the server
   * @param messageType Three-character string indicating the type of the original message
   * @param fromUser    Username that the archived message was sent by
   * @param text        Text in the archived message
   * @return A string describing the archived message
   */
  public static String constructArchiveText(String sentTime, String messageType, String fromUser, String text) {
    String result = "time: " + sentTime;
    result += " type: " + messageType;
    result += " from: " + fromUser;
    result += " text: " + text;
    return result;
  }

  /**
   * Construct a string containing information about the requested message from the archive
   *
   * @param sentTime    SQL TIMESTAMP of when the message was received by the server
   * @param messageType Three-character string indicating the type of the original message
   * @param fromUser    Username that the archived message was sent by
   * @param toUser      Username that the archived message was sent to
   * @param text        Text in the archived message
   * @return A string describing the archived message
   */
  private static String constructArchiveText(String sentTime, String messageType, String fromUser, String toUser, String text) {
    String result = "time: " + sentTime;
    result += " type: " + messageType;
    result += " from: " + fromUser;
    result += " to: " + toUser;
    result += " text: " + text;
    return result;
  }

}
