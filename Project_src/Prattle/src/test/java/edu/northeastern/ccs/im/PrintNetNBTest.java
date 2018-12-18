package edu.northeastern.ccs.im;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Testing the PrintNetNB class
 *
 * @author Sarah Lichtman and Cole Clark
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PrintNetNBTest {

  private Message messageHello;
  private Message messageQuit;
  private Message messageBroadcast;
  private Message messageAcknowledge;
  private Message messageNoAcknowledge;
  private Message messageSimpleLogin;
  private Message messageCustom;

  private PrintNetNB printNetNB1;
  private PrintNetNB printNetNB2;
  private ServerSocket serverSocket;
  private SocketNB socket;
  
  private static final Logger LOGGER = Logger.getLogger(PrintNetNBTest.class.getName());

  @BeforeAll
  void createDummyListener(){
    int workingPort = 5001;
    try {
      this.serverSocket = new ServerSocket(workingPort);
    }catch(Exception e){
    	LOGGER.log(Level.SEVERE, e.toString(), e);
      fail();
    }

    messageHello = Message.makeHelloMessage("Clark's message");
    messageQuit = Message.makeQuitMessage("Cole");
    messageBroadcast = Message.makeBroadcastMessage("Mike W", "This is Mike W.");
    messageAcknowledge = Message.makeAcknowledgeMessage("Client 2.0","Client 2.0");
    messageNoAcknowledge = Message.makeNoAcknowledgeMessage("Cole","");
    messageSimpleLogin = Message.makeSimpleLoginMessage("Cole","Password");
    messageCustom = Message.makeMessage("BCT", "Cole Clark", "hello from outer space","","");

    try {
      socket = new SocketNB("127.0.0.1", 5001);
      // Constructed using the
      printNetNB1 = new PrintNetNB(socket);
      printNetNB2 = new PrintNetNB(socket.getSocket());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
      fail();
    }

  }

  @AfterAll
  void closeDummyListener(){
    try {
      this.serverSocket.close();
    }catch (Exception e){
    	LOGGER.log(Level.SEVERE, e.toString(), e);
      fail();
    }
  }


  /**
   * Testing that when the socket is closed, an IOException is caught in the print method, and
   * false is returned.
   */
  @Test
  void testIOExceptionReturnsFalse1() {
    try {
      socket.close();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
      fail();
    }

    assertFalse(printNetNB1.print(messageHello));
    assertFalse(printNetNB1.print(messageQuit));
    assertFalse(printNetNB1.print(messageBroadcast));
    assertFalse(printNetNB1.print(messageAcknowledge));
    assertFalse(printNetNB1.print(messageNoAcknowledge));
    assertFalse(printNetNB1.print(messageSimpleLogin));
    assertFalse(printNetNB1.print(messageCustom));
    assertFalse(printNetNB2.print(messageHello));
    assertFalse(printNetNB2.print(messageQuit));
    assertFalse(printNetNB2.print(messageBroadcast));
    assertFalse(printNetNB2.print(messageAcknowledge));
    assertFalse(printNetNB2.print(messageNoAcknowledge));
    assertFalse(printNetNB2.print(messageSimpleLogin));
    assertFalse(printNetNB2.print(messageCustom));

    try {
      socket = new SocketNB("127.0.0.1", 5001);
      // Constructed using the
      printNetNB1 = new PrintNetNB(socket);
      printNetNB2 = new PrintNetNB(socket.getSocket());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
      fail();
    }
  }


  /**
   * Testing the print function for different types of messages after creating an PrintNetNB using
   * the constructor that takes a socket.
   */
  @Test
  void printMessageSocket() {
    assertTrue(printNetNB1.print(messageHello));
    assertTrue(printNetNB1.print(messageQuit));
    assertTrue(printNetNB1.print(messageBroadcast));
    assertTrue(printNetNB1.print(messageAcknowledge));
    assertTrue(printNetNB1.print(messageNoAcknowledge));
    assertTrue(printNetNB1.print(messageSimpleLogin));
    assertTrue(printNetNB1.print(messageCustom));
  }

  /**
   * Testing the print function for different types of messages after creating an PrintNetNB using
   * the constructor that takes a channel.
   */
  @Test
  void printMessageChannel() {
    assertTrue(printNetNB2.print(messageHello));
    assertTrue(printNetNB2.print(messageQuit));
	  assertTrue(printNetNB2.print(messageBroadcast));
    assertTrue(printNetNB2.print(messageAcknowledge));
    assertTrue(printNetNB2.print(messageNoAcknowledge));
    assertTrue(printNetNB2.print(messageSimpleLogin));
    assertTrue(printNetNB2.print(messageCustom));
  }

  /**
   * Testing the inappropriateLanguage method to make sure any profanity is replaced by asterisks.
   */
  @Test
  void testInappropriateLanguage() {
    Message msg1 = Message.makePrivateMessage("pottyMouth", "Cole", "oh shit");
    changeParentalControls("pottyMouth", "password", "on");
    changeParentalControls("pottyMouth2", "password", "on");
    changeParentalControls("Cole", "password", "off");
    changeParentalControls("Avik", "password", "off");

    try {
      Method method = PrintNetNB.class.getDeclaredMethod("inappropriateLanguage", Message.class);
      method.setAccessible(true);
      Message output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh ****", output.getText());
      assertEquals("pottyMouth", output.getName());
      assertEquals("Cole", output.getRecipientName());

      msg1 = Message.makePrivateMessage("Cole", "pottyMouth", "oh shit");
      output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh ****", output.getText());
      assertEquals("Cole", output.getName());
      assertEquals("pottyMouth", output.getRecipientName());

      msg1 = Message.makePrivateMessage("Cole", "Avik", "oh shit");
      output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh shit", output.getText());
      assertEquals("Cole", output.getName());
      assertEquals("Avik", output.getRecipientName());

      msg1 = Message.makeGroupMessage("Cole", "Team", "oh shit");
      output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh shit", output.getText());
      assertEquals("Cole", output.getName());
      assertEquals("Team", output.getRecipientName());

      msg1 = Message.makeGroupMessage("pottyMouth", "Team", "oh shit");
      output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh ****", output.getText());
      assertEquals("pottyMouth", output.getName());

      msg1 = Message.makePrivateMessage("pottyMouth", "pottyMouth2", "oh shit");
      output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh ****", output.getText());
      assertEquals("pottyMouth", output.getName());
      assertEquals("pottyMouth2", output.getRecipientName());

      msg1 = Message.makeHistoryResponseMessage("pottyMouth", "oh shit");
      output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh ****", output.getText());
      assertEquals("pottyMouth", output.getName());
      assertEquals(null, output.getRecipientName());

      msg1 = Message.makeHistoryResponseMessage("Cole", "oh shit");
      output = (Message) method.invoke(printNetNB1, msg1);
      assertEquals("oh shit", output.getText());
      assertEquals("Cole", output.getName());
      assertEquals(null, output.getRecipientName());
    } catch (Exception e) {

      //Empty

    }
  }

  /**
   * Turns the parental controls on or off for the given user.
   *
   * @param srcName user name
   * @param srcPassword password for the user
   * @param onOrOff "on" or "off" string
   */
  private void changeParentalControls(String srcName, String srcPassword, String onOrOff) {
    User user = new User();
    user.setUsername(srcName);
    user.setPassword(srcPassword);
    UserCrud crud = new UserCrudImpl();
    crud.changeParentalControl(user, onOrOff);
  }




}