package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageArchiveSearchTest {

  User me;
  User you;
  User other;
  UserCrud uc;
  GroupCrud gc;
  Group us;


  @BeforeAll
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
    List<User> usUsers = new ArrayList<>(); usUsers.add(me); usUsers.add(you);
    us.setUsers(usUsers);
    gc.addGroup(us);

  }

  @AfterAll
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


  /**
   * Tests for searching archived messages based on the sender, receiver, and timestamp.
   */
  @Test
  void searchMessagesPVT() {
    try {
      SimpleDateFormat dateTimeStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateTimeStart.setTimeZone(TimeZone.getTimeZone("GMT"));
      String startTimeStamp = dateTimeStart.format(new Date(System.currentTimeMillis() - 5000));

      Message m1 = Message.makePrivateMessage(me.getUsername(), you.getUsername(), "sup 1");
      MessageArchive.archiveMessage(m1, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message m2 = Message.makePrivateMessage(you.getUsername(), me.getUsername(), "sup 2");
      MessageArchive.archiveMessage(m2, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message m3 = Message.makePrivateMessage(me.getUsername(), you.getUsername(), "sup 3");
      MessageArchive.archiveMessage(m3, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      // Group message mixed in with private messages
      Message g1 = Message.makeGroupMessage(me.getUsername(), us.getGroupName(), "hi group");
      MessageArchive.archiveMessage(g1, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message m4 = Message.makePrivateMessage(me.getUsername(), you.getUsername(), "sup 4");
      MessageArchive.archiveMessage(m4, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message m5 = Message.makePrivateMessage(you.getUsername(), me.getUsername(), "sup 5");
      MessageArchive.archiveMessage(m5, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message g2 = Message.makeGroupMessage(you.getUsername(), us.getGroupName(), "hello group again");
      MessageArchive.archiveMessage(g2, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message m6 = Message.makePrivateMessage(me.getUsername(), other.getUsername(), "sup other");
      MessageArchive.archiveMessage(m6, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message m7 = Message.makePrivateMessage(other.getUsername(), me.getUsername(), "other sup");
      MessageArchive.archiveMessage(m7, true);
      TimeUnit.MILLISECONDS.sleep(1000);

      Message g3 = Message.makeGroupMessage(me.getUsername(), us.getGroupName(), "whats up group");
      MessageArchive.archiveMessage(g3, true);
      TimeUnit.MILLISECONDS.sleep(3000);

      SimpleDateFormat dateTimeEnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateTimeEnd.setTimeZone(TimeZone.getTimeZone("GMT"));
      String endTimeStamp = dateTimeStart.format(new Date(System.currentTimeMillis() + 5000));

      // wrong message type should not throw error
      assertDoesNotThrow(()-> MessageArchive.searchMessages(m1));

      // get all messages I sent to you
      List<Message> mlist;
      Message sm = Message.makeSearchMessage(me.getUsername(),"sentTo", you.getUsername(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(4, mlist.size());
      assertTrue(mlist.get(0).getText().contains(m4.getText()));
      assertTrue(mlist.get(1).getText().contains(m3.getText()));
      assertTrue(mlist.get(2).getText().contains(m1.getText()));
      assertTrue(mlist.get(3).getText().contains("end archive retrieval"));

      // get all messages I received from you
      sm = Message.makeSearchMessage(me.getUsername(), "receivedFrom", you.getUsername(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(3, mlist.size());
      assertTrue(mlist.get(0).getText().contains(m5.getText()));
      assertTrue(mlist.get(1).getText().contains(m2.getText()));
      assertTrue(mlist.get(2).getText().contains("end archive retrieval"));

      // get all messages I sent to and received from you
      sm = Message.makeSearchMessage(me.getUsername(), "both", you.getUsername(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(6, mlist.size());
      assertTrue(mlist.get(0).getText().contains(m5.getText()));
      assertTrue(mlist.get(1).getText().contains(m4.getText()));
      assertTrue(mlist.get(2).getText().contains(m3.getText()));
      assertTrue(mlist.get(3).getText().contains(m2.getText()));
      assertTrue(mlist.get(4).getText().contains(m1.getText()));
      assertTrue(mlist.get(5).getText().contains("end archive retrieval"));

      // get all messages I sent (to everyone)
      sm = Message.makeSearchMessage(me.getUsername(), "sentTo", "*", startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(7, mlist.size());
      assertTrue(mlist.get(0).getText().contains(g3.getText()));
      assertTrue(mlist.get(1).getText().contains(m6.getText()));
      assertTrue(mlist.get(2).getText().contains(m4.getText()));
      assertTrue(mlist.get(3).getText().contains(g1.getText()));
      assertTrue(mlist.get(4).getText().contains(m3.getText()));
      assertTrue(mlist.get(5).getText().contains(m1.getText()));
      assertTrue(mlist.get(6).getText().contains("end archive retrieval"));

      // get all messages I received (from everyone)
      sm = Message.makeSearchMessage(me.getUsername(), "receivedFrom", "*", startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(4, mlist.size());
      assertTrue(mlist.get(0).getText().contains(m7.getText()));
      assertTrue(mlist.get(1).getText().contains(m5.getText()));
      assertTrue(mlist.get(2).getText().contains(m2.getText()));
      assertTrue(mlist.get(3).getText().contains("end archive retrieval"));

      // get all messages I sent and received
      sm = Message.makeSearchMessage(me.getUsername(), "both", "*", startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(10, mlist.size());
      assertTrue(mlist.get(0).getText().contains(g3.getText()));
      assertTrue(mlist.get(1).getText().contains(m7.getText()));
      assertTrue(mlist.get(2).getText().contains(m6.getText()));
      assertTrue(mlist.get(3).getText().contains(m5.getText()));
      assertTrue(mlist.get(4).getText().contains(m4.getText()));
      assertTrue(mlist.get(5).getText().contains(g1.getText()));
      assertTrue(mlist.get(6).getText().contains(m3.getText()));
      assertTrue(mlist.get(7).getText().contains(m2.getText()));
      assertTrue(mlist.get(8).getText().contains(m1.getText()));
      assertTrue(mlist.get(9).getText().contains("end archive retrieval"));

      sm = Message.makeSearchMessage(me.getUsername(),"sentTo", us.getGroupName(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(3, mlist.size());
      assertTrue(mlist.get(0).getText().contains(g3.getText()));
      assertTrue(mlist.get(1).getText().contains(g1.getText()));
      assertTrue(mlist.get(2).getText().contains("end archive retrieval"));

      sm = Message.makeSearchMessage(me.getUsername(), "receivedFrom", us.getGroupName(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(2, mlist.size());
      assertTrue(mlist.get(0).getText().contains(g2.getText()));
      assertTrue(mlist.get(1).getText().contains("end archive retrieval"));

      sm = Message.makeSearchMessage(me.getUsername(), "both", us.getGroupName(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(4, mlist.size());
      assertTrue(mlist.get(0).getText().contains(g3.getText()));
      assertTrue(mlist.get(1).getText().contains(g2.getText()));
      assertTrue(mlist.get(2).getText().contains(g1.getText()));
      assertTrue(mlist.get(3).getText().contains("end archive retrieval"));

      sm = Message.makeSearchMessage(me.getUsername(), "both", us.getGroupName(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(4, mlist.size());
      assertTrue(mlist.get(0).getText().contains(g3.getText()));
      assertTrue(mlist.get(1).getText().contains(g2.getText()));
      assertTrue(mlist.get(2).getText().contains(g1.getText()));
      assertTrue(mlist.get(3).getText().contains("end archive retrieval"));

      sm = Message.makeSearchMessage(me.getUsername(), "both", us.getGroupName(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(4, mlist.size());
      assertTrue(mlist.get(0).getText().contains(g3.getText()));
      assertTrue(mlist.get(1).getText().contains(g2.getText()));
      assertTrue(mlist.get(2).getText().contains(g1.getText()));
      assertTrue(mlist.get(3).getText().contains("end archive retrieval"));

      sm = Message.makeSearchMessage(other.getUsername(), "both", us.getGroupName(), startTimeStamp, endTimeStamp);
      mlist = MessageArchive.searchMessages(sm);
      assertEquals(2, mlist.size());
      assertTrue(mlist.get(0).getText().contains("Cannot access because you are not member of that group"));
      assertTrue(mlist.get(1).getText().contains("end archive retrieval"));
    }catch (InterruptedException e) {
      fail();

    }
  }
}
