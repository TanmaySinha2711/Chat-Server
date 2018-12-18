package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test message archiving and retrieving from SQL database
 *
 * @author Sarah Lichtman and Cole Clark
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageArchiveTest {
    User me;
    User you;
    User other;
    UserCrud uc;
    GroupCrud gc;
    Group us;

    @BeforeEach
    void setup(){
        // create some users and groups to test with
        // (add them to the database)
        uc = new UserCrudImpl();

        me = new User();
        me.setUsername("me"); me.setPassword("pwd");
        uc.addUser(me);

        you = new User();
        you.setUsername("you"); you.setPassword("passwd");
        uc.addUser(you);

        other = new User();
        other.setUsername("other"); other.setPassword("p");
        uc.addUser(other);


        gc = new GroupCrudImpl();
        us = new Group();
        us.setGroupName("us");
        List<User> usUsers = new ArrayList<>(); 
        usUsers.add(me); 
        usUsers.add(you);
        us.setUsers(usUsers);
        gc.addGroup(us);

    }

    @AfterEach
    void teardown(){
        // remove our added messages from the database
        Connection conn = Database.getConnection();
        String sql = "DELETE FROM Messages WHERE "
        +"(to_user_id=(SELECT id FROM Users WHERE username='me') "
        +" OR to_user_id=(SELECT id FROM Users WHERE username='you') "
        +" OR to_user_id=(SELECT id FROM Users WHERE username='other') "
        +" OR to_group_id=(SELECT id FROM Groups WHERE groupname='us'));";

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }

        // remove our test users and groups from the database
        gc.deleteGroup(us);
        uc.deleteUser(me);
        uc.deleteUser(you);
        uc.deleteUser(other);
    }

    @Test
    void getIDName(){
        // test protected functions for retriever usernames/ids
        int id = MessageArchive.getID(me.getUsername(), "username", "Users");
        assertNotEquals(-1, id);

        int badid = MessageArchive.getID("badname", "username", "Users");
        assertEquals(-1, badid);

        badid = MessageArchive.getID("badname", "groupname", "Groups");
        assertEquals(-1, badid);

        String name = MessageArchive.getName(id, "username", "Users");
        assertEquals(me.getUsername(), name);

        name = MessageArchive.getName(-1, "username", "Users");
        assertEquals("NULL", name);

        // give a poorly formated sql query to test exception handling
        assertDoesNotThrow(()->MessageArchive.getID(me.getUsername(), "bad;field", "badtable"));
        assertDoesNotThrow(()->MessageArchive.getName(12, "bad;field", "badtable"));
    }

    /**
     * Combine tests for archiveMessage and getArchivedMessages into a single function,
     * since order of execution matters
     */
    @Test
    void archiveMessageAndGet() {
        Message m1 = Message.makePrivateMessage(me.getUsername(), you.getUsername(), "hi friend");
        MessageArchive.archiveMessage(m1, true);
        Message m2 = Message.makePrivateMessage(you.getUsername(), me.getUsername(), "hello 2!");
        MessageArchive.archiveMessage(m2, true);
        Message m3 = Message.makePrivateMessage(me.getUsername(), other.getUsername(), "ugh");
        MessageArchive.archiveMessage(m3, true);

        // wrong message type should not throw error
        assertDoesNotThrow(()-> MessageArchive.getArchivedMessages(m1));

        // Should only retrieve m1 and m2 (not m3)
        List<Message> mlist;
        Message hm = Message.makeHistoryMessage(me.getUsername(), you.getUsername(), "2");
        mlist = MessageArchive.getArchivedMessages(hm);
        assertEquals(3, mlist.size());
        assertTrue(mlist.get(0).getText().contains(m2.getText()));
        assertTrue(mlist.get(1).getText().contains(m1.getText()));


        // Should only retrieve m3, even though the user asked for more
        hm = Message.makeHistoryMessage(me.getUsername(), other.getUsername(), "2");
        mlist = MessageArchive.getArchivedMessages(hm);
        assertEquals(2, mlist.size());
        assertTrue(mlist.get(0).getText().contains(m3.getText()));


        // Should only retrieve m2 (which is most recent!)
        hm = Message.makeHistoryMessage(me.getUsername(), you.getUsername(), "1");
        mlist = MessageArchive.getArchivedMessages(hm);
        assertEquals(2, mlist.size());
        assertTrue(mlist.get(0).getText().contains(m2.getText()));

        // Test group messages
        MessageArchive.archiveMessage(Message.makeGroupMessage(me.getUsername(), us.getGroupName(), "hello all"), true);
        hm = Message.makeHistoryMessage(me.getUsername(), us.getGroupName(), "2");
        mlist = MessageArchive.getArchivedMessages(hm);
        assertEquals(2, mlist.size());

        // Test bad history requests (results in bad sql statements)
        assertDoesNotThrow(()->MessageArchive.archiveMessage(Message.makePrivateMessage("non;user", "someone", "bad news"), true));
        assertDoesNotThrow(()->MessageArchive.getArchivedMessages(Message.makeHistoryMessage(me.getUsername(), you.getUsername(), "-1")));
    }
    
    @Test
    void testGetQueuedMessages() {
    	Message m1 = Message.makePrivateMessage(me.getUsername(), you.getUsername(), "hi friend");
        MessageArchive.archiveMessage(m1, false);
        
        List<String> msgs = MessageArchive.getQueuedMessagesForUser(you.getUsername());
        assertTrue(msgs.size() != 0);
    }
    
    @Test
    void testUpdateQueuedMessages() {
    	Message m1 = Message.makePrivateMessage(me.getUsername(), you.getUsername(), "hi friend");
        MessageArchive.archiveMessage(m1, false);
        
        MessageArchive.updateQueuedMessage(you.getUsername(), "");
        
        List<String> msgs = MessageArchive.getQueuedMessagesForUser(you.getUsername());
        assertTrue(msgs.size() == 0);
    }

    @Test
    void testRecallMessages() {
    	Message m1 = Message.makePrivateMessage(me.getUsername(), you.getUsername(), "hi friend");
        MessageArchive.archiveMessage(m1, false);
        String[] s = m1.toString().split(" ");
        
        String text = "";
        for(int i = 6; i < s.length; i++)
        	text += s[i] + " ";
        
        MessageArchive.recallMessage(me.getUsername(), text);
        
        List<String> msgs = MessageArchive.getQueuedMessagesForUser(you.getUsername());
        assertTrue(msgs.size() == 0);
    }
}