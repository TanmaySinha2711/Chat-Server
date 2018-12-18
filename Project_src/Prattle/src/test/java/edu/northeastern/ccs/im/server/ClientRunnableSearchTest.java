package edu.northeastern.ccs.im.server;


import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.chatter.IMConnection;
import edu.northeastern.ccs.im.chatter.MessageScanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ClientRunnableSearchTest {


  private ServerSocketChannel serverSocket;
  private IMConnection clientTerminal;
  private IMConnection clientTerminal2;
  private ServerSocketChannel serverSocket2;

  @BeforeEach
  public final void setUp() {
    // set up server sockets
    try {
      serverSocket = ServerSocketChannel.open();
      serverSocket.configureBlocking(false);
      serverSocket.socket().bind(new InetSocketAddress(4544));
      serverSocket2 = ServerSocketChannel.open();
      serverSocket2.configureBlocking(false);
      serverSocket2.socket().bind(new InetSocketAddress(4543));
    } catch (IOException e) {
      e.printStackTrace();
    }

    //set up chatter client to send and receive valid messages to our server
    // this must be a user and password in our database, or the connection will be rejected!
    clientTerminal = new IMConnection("127.0.0.1", 4544, "tim", "pass321");
    clientTerminal.connect("Login");
    clientTerminal2 = new IMConnection("127.0.0.1", 4543, "cole", "password");
    clientTerminal2.connect("Login");
  }

  @AfterEach
  public final void tearDown() {
    try {
      if(clientTerminal.connectionActive()){
        //clientTerminal.sendDeleteMessage();
        clientTerminal.disconnect();
      }
      if (clientTerminal2.connectionActive()) {
        clientTerminal2.disconnect();
      }
      serverSocket.close();
      serverSocket2.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Search messages test.
   */
  @Test
  void testSearchRequest(){
    SocketChannel socket;
    SocketChannel socket2;
    try{
      socket = serverSocket.accept();
      socket2 = serverSocket2.accept();
      ClientRunnable runnable = new ClientRunnable(socket);
      ClientRunnable runnable2 = new ClientRunnable(socket2);
      // now manually archive some messages
      // (Prattle would archive them automatically, but it's not running here!)
      Message[] messages = new Message[7];
      messages[0] = Message.makePrivateMessage(clientTerminal.getUserName(), "Cole", "hello there 1");
      messages[1] = Message.makePrivateMessage(clientTerminal.getUserName(), "Cole", "hello there 2");
      messages[2] = Message.makePrivateMessage(clientTerminal.getUserName(), "Cole", "hello there 3");
      messages[3] = Message.makePrivateMessage("Cole", clientTerminal.getUserName(), "Cole to tim");
      messages[4] = Message.makePrivateMessage("Cole", clientTerminal.getUserName(), "Cole to tim 2");
      messages[5] = Message.makeGroupMessage(clientTerminal.getUserName(), "team", "tim to team");
      messages[6] = Message.makeGroupMessage("cole", "team", "cole to team");

      // Server runs on GMT time, hence the timezone change in the timestamp below
      SimpleDateFormat dateTimeStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateTimeStart.setTimeZone(TimeZone.getTimeZone("GMT"));
      String startTimeStamp = dateTimeStart.format(new Date(System.currentTimeMillis() - 5000));
      System.out.println(startTimeStamp);

      // Archive messages
      MessageArchive.archiveMessage(messages[0], true);
      MessageArchive.archiveMessage(messages[1], true);
      MessageArchive.archiveMessage(messages[2], true);
      MessageArchive.archiveMessage(messages[3], true);
      MessageArchive.archiveMessage(messages[4], true);
      MessageArchive.archiveMessage(messages[5], true);


      // Get end time
      SimpleDateFormat dateTimeEnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateTimeEnd.setTimeZone(TimeZone.getTimeZone("GMT"));
      String endTimeStamp = dateTimeEnd.format(new Date(System.currentTimeMillis() + 5000));
      System.out.println(endTimeStamp);



      // check that a search returns the archived messages
      clientTerminal.sendSearchMessage("sentTo","Cole", startTimeStamp, endTimeStamp);
      for (int i = 0; i < 10; i++) {
        runnable.run();
      }
      // Sleep so IMConnection has time to deal with the new messages
      TimeUnit.MILLISECONDS.sleep(1000);
      // Test 1 for "sentTo"
      MessageScanner ms = clientTerminal.getMessageScanner();
      assertEquals(3, helper(ms, messages));

      // Test 2 for "receivedFrom"
      clientTerminal.sendSearchMessage("receivedFrom","Cole", startTimeStamp, endTimeStamp);
      for (int i = 0; i < 10; i++) {
        runnable.run();
      }
      // Sleep so IMConnection has time to deal with the new messages
      TimeUnit.MILLISECONDS.sleep(1000);
      MessageScanner ms2 = clientTerminal.getMessageScanner();
      assertEquals(2, helper(ms2, messages));

      // Test 3 for "both"
      clientTerminal.sendSearchMessage("both","Cole", startTimeStamp, endTimeStamp);
      for (int i = 0; i < 10; i++) {
        runnable.run();
      }
      // Sleep so IMConnection has time to deal with the new messages
      TimeUnit.MILLISECONDS.sleep(1000);
      MessageScanner ms3 = clientTerminal.getMessageScanner();
      assertEquals(5, helper(ms3, messages));

      // Test 4 for group messages
      clientTerminal.sendSearchMessage("both","Team", startTimeStamp, endTimeStamp);
      for (int i = 0; i < 10; i++) {
        runnable.run();
      }
      // Sleep so IMConnection has time to deal with the new messages
      TimeUnit.MILLISECONDS.sleep(1000);
      MessageScanner ms4 = clientTerminal.getMessageScanner();
      assertEquals(0, helper(ms4, messages));

      // Test 4 for group messages
      clientTerminal2.sendSearchMessage("both","Team", startTimeStamp, endTimeStamp);
      for (int i = 0; i < 10; i++) {
        runnable2.run();
      }
      // Sleep so IMConnection has time to deal with the new messages
      TimeUnit.MILLISECONDS.sleep(1000);
      MessageScanner ms5 = clientTerminal.getMessageScanner();
      assertEquals(1, helper(ms5, messages));

      clientTerminal.disconnect();
      try{runnable.run();}
      catch(NullPointerException e) {
        // expect to get a null pointer exception when terminating a connection!
        // because we never added this ClientRunnable to Prattle.active)
      }
    } catch (IOException | InterruptedException e){
      e.printStackTrace();
    }
  }

  /**
   * Returns the number of messages in MessageScanner that have message text equal to the at least
   * one of the messages in the given message array.
   *
   * @param ms MessageScanner to check
   * @param messages Message to check against
   * @return an integer
   */
  private int helper(MessageScanner ms, Message[] messages) {
    int matches = 0;
    while (ms.hasNext()) {
      String nextText = ms.next().getText();
      for (Message aMessage : messages) {
        if (nextText.contains(aMessage.getText())) {
          matches++;
          break;
        }
      }
    }
    return matches;
  }

  /**
   * Parental Control message test.
   */
  @Test
  void testParentalControlRequest(){
    SocketChannel socket;
    try{
      socket = serverSocket.accept();
      ClientRunnable runnable = new ClientRunnable(socket);

      // now manually archive some messages
      // (Prattle would archive them automatically, but it's not running here!)
      Message m1 = Message.makeParentalControlMessage(clientTerminal.getUserName(), "on");
      Message m2 = Message.makeParentalControlMessage(clientTerminal.getUserName(), "off");
      Message m3 = Message.makeParentalControlMessage(clientTerminal.getUserName(), "invalid");

      // sleep so messages are archived with different timestamps
      // and check that the archive contains the correct messages
      clientTerminal.sendParentalControlMessage(m1.getText());
      clientTerminal.sendParentalControlMessage(m2.getText());
      clientTerminal.sendParentalControlMessage(m3.getText());
      for (int i = 0; i < 10; i++) {
        runnable.run();
      }
      // Sleep so IMConnection has time to deal with the new messages
      TimeUnit.MILLISECONDS.sleep(1000);
      MessageScanner ms = clientTerminal.getMessageScanner();
      //int matches = 0;
      // Bypass the 1st message as it will be a login successful message
      ms.next();

      // Make sure the parental controls have been changed
      assertEquals("Parental controls have been changed to: on", ms.next().getText());
      assertEquals("Parental controls have been changed to: off", ms.next().getText());
      assertEquals("Parental controls could not be changed. Enter \"PRC <on or off>\" again", ms.next().getText());

      clientTerminal.disconnect();
      try{runnable.run();}
      catch(NullPointerException e) {
        // expect to get a null pointer exception when terminating a connection!
        // because we never added this ClientRunnable to Prattle.active)
      }
    } catch (IOException | InterruptedException e){
      e.printStackTrace();
    }
  }
}
