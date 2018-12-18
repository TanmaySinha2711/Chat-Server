package edu.northeastern.ccs.im.chatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the Message class.
 *
 * @author Cole Clark
 */
class MessageTest {

  private Message quit;
  private Message broadcast;
  private Message broadCastNullText;
  private Message acknowledge;
  private Message noAcknowledge;
  private Message simpleLogin;
  private Message hello;

  private Message makeMessageHello;
  private Message makeMessageQuit;
  private Message makeMessageBCT;
  private Message makeMessageAck;
  private Message makeMessageNoAck;

  private Message register;
  private Message update;
  private Message delete;
  private Message privateMessage;

  private Message searchMessage1;
  private Message searchMessage2;
  private Message searchMessage3;


  /**
   * Creating object instances of Message for testing.
   */
  @BeforeEach
  void setUp() {
    // Created using the makeQuitMessage method
    quit = Message.makeQuitMessage("Cole");
    // Created using the makeBroadcastMessage method
    broadcast = Message.makeBroadcastMessage("Mike W", "This is Mike W.");
    broadCastNullText = Message.makeBroadcastMessage("Broadcast message", null);
    // Created using the makeAcknowledgeMessage method
    acknowledge = Message.makeAcknowledgeMessage("Client 2.0","");
    // Created using the makeNoAcknowledgeMessage method
    noAcknowledge = Message.makeNoAcknowledgeMessage("Cole","");
    // Created using the makeSimpleLoginMessage method
    simpleLogin = Message.makeLoginMessage("Cole","Password");
    // Created using the makeHelloMessage method
    hello = Message.makeHelloMessage("Clark's message");

    // Created using te makeMessage method
    makeMessageHello = Message.makeMessage("HLO", "Client 1", "hello","Password","");
    makeMessageQuit = Message.makeMessage("BYE", "Sarah", "This is a quit message.","","");
    makeMessageBCT = Message.makeMessage("BCT", "Avik", "This is a broadcast message.","","");
    makeMessageAck = Message.makeMessage("ACK", "Tanmay", "This is a ack message.","","");
    makeMessageNoAck = Message.makeMessage("NAK", "Cole Clark", "This is a no ack message.","","");

    // New message types (register, update, delete, and private)
    register = Message.makeMessage("REG", "Cole", "This is a register message.", "registerPassword", "");
    update = Message.makeMessage("UPD", "Tanmay", "This is an update message.", "updatePassword", "");
    delete = Message.makeMessage("DEL", "Avik", "This is a delete message.", "deletePassword", "");
    privateMessage = Message.makeMessage("PVT", "Sarah", "This is a private message.", "MyPassword", "Cole");

    searchMessage1 = Message.makeSearchMessage("Cole", "sentTo", "Avik", "2018-11-23 00:00:00", "2018-12-31 00:00:00");
    searchMessage2 = Message.makeSearchMessage("Cole", "receivedFrom", "Avik", "2018-11-23 00:00:00", "2018-12-31 00:00:00");
    searchMessage3 = Message.makeSearchMessage("Cole", "both", "Avik", "2018-11-23 00:00:00", "2018-12-31 00:00:00");
  }

  /**
   * Tests that the getSender function returns the correct sender for each message.
   */
  @Test
  void getSender() {
    assertEquals("Cole",quit.getSender());
    assertEquals("Mike W",broadcast.getSender());
    assertEquals("Client 2.0",acknowledge.getSender());
    assertEquals("Cole",noAcknowledge.getSender());
    assertEquals("Cole",simpleLogin.getSender());
    assertEquals(null, hello.getSender());
    assertEquals("Broadcast message", broadCastNullText.getSender());

    assertEquals("Client 1", makeMessageHello.getSender());
    assertEquals("Sarah", makeMessageQuit.getSender());
    assertEquals("Avik", makeMessageBCT.getSender());
    assertEquals("Tanmay", makeMessageAck.getSender());
    assertEquals("Cole Clark", makeMessageNoAck.getSender());


    assertEquals("Cole", register.getSender());
    assertEquals("Tanmay", update.getSender());
    assertEquals("Avik", delete.getSender());
    assertEquals("Sarah", privateMessage.getSender());


    assertEquals("Cole", searchMessage1.getSender());
    assertEquals("Cole", searchMessage2.getSender());
    assertEquals("Cole", searchMessage3.getSender());
  }

  /**
   * Tests that the getText function returns the correct text from each message.
   */
  @Test
  void getText() {
    assertEquals(null, quit.getText());
    assertEquals("This is Mike W.", broadcast.getText());
    assertEquals("", acknowledge.getText());
    assertEquals("", noAcknowledge.getText());
    assertEquals("Password", simpleLogin.getText());
    assertEquals("Clark's message", hello.getText());
    assertEquals(null, broadCastNullText.getText());

    assertEquals("Password", makeMessageHello.getText());
    assertEquals(null, makeMessageQuit.getText());
    assertEquals("This is a broadcast message.", makeMessageBCT.getText());
    assertEquals("This is a ack message.", makeMessageAck.getText());
    assertEquals("This is a no ack message.", makeMessageNoAck.getText());

    // getText() for register and update messages is the password
    assertEquals("registerPassword", register.getText());
    assertEquals("updatePassword", update.getText());
    assertEquals(null, delete.getText());
    assertEquals("This is a private message.", privateMessage.getText());

    assertEquals(null, searchMessage1.getText());
    assertEquals(null, searchMessage2.getText());
    assertEquals(null, searchMessage3.getText());
  }

  /**
   * Tests that the getType function works properly.
   */
  @Test
  void getType() {
    assertEquals(Message.MessageType.QUIT,quit.getType());
    assertEquals(Message.MessageType.BROADCAST,broadcast.getType());
    assertEquals(Message.MessageType.ACKNOWLEDGE,acknowledge.getType());
    assertEquals(Message.MessageType.NO_ACKNOWLEDGE,noAcknowledge.getType());
    assertEquals(Message.MessageType.HELLO,simpleLogin.getType());
    assertEquals(Message.MessageType.HELLO, hello.getType());
    assertEquals(Message.MessageType.BROADCAST, broadCastNullText.getType());

    assertEquals(Message.MessageType.HELLO, makeMessageHello.getType());
    assertEquals(Message.MessageType.QUIT, makeMessageQuit.getType());
    assertEquals(Message.MessageType.BROADCAST, makeMessageBCT.getType());
    assertEquals(Message.MessageType.ACKNOWLEDGE, makeMessageAck.getType());
    assertEquals(Message.MessageType.NO_ACKNOWLEDGE, makeMessageNoAck.getType());


    assertEquals(Message.MessageType.REGISTER, register.getType());
    assertEquals(Message.MessageType.UPDATE, update.getType());
    assertEquals(Message.MessageType.DELETE, delete.getType());
    assertEquals(Message.MessageType.PRIVATE, privateMessage.getType());

    assertEquals(Message.MessageType.SEARCH, searchMessage1.getType());
    assertEquals(Message.MessageType.SEARCH, searchMessage2.getType());
    assertEquals(Message.MessageType.SEARCH, searchMessage3.getType());

  }


  /**
   * Tests that the isAcknowledge method appropriately evaluates each message. True is
   * returned only if the message is of type ACKNOWLEDGE.
   */
  @Test
  void isAcknowledge() {
    assertFalse(quit.isAcknowledge());
    assertFalse(broadcast.isAcknowledge());
    assertTrue(acknowledge.isAcknowledge());
    assertFalse(noAcknowledge.isAcknowledge());
    assertFalse(simpleLogin.isAcknowledge());
    assertFalse(hello.isAcknowledge());
    assertFalse(broadCastNullText.isAcknowledge());


    assertFalse(makeMessageHello.isAcknowledge());
    assertFalse(makeMessageQuit.isAcknowledge());
    assertFalse(makeMessageBCT.isAcknowledge());
    assertTrue(makeMessageAck.isAcknowledge());
    assertFalse(makeMessageNoAck.isAcknowledge());

    assertFalse(searchMessage1.isAcknowledge());
    assertFalse(searchMessage3.isAcknowledge());
    assertFalse(searchMessage2.isAcknowledge());
  }

  /**
   * Tests that the isBroadcastMessage method appropriately evaluates each message. True is
   * returned only if the message is of type BROADCAST.
   */
  @Test
  void isBroadcastMessage() {
    assertFalse(quit.isBroadcastMessage());
    assertTrue(broadcast.isBroadcastMessage());
    assertFalse(acknowledge.isBroadcastMessage());
    assertFalse(noAcknowledge.isBroadcastMessage());
    assertFalse(simpleLogin.isBroadcastMessage());
    assertFalse(hello.isBroadcastMessage());
    assertTrue(broadCastNullText.isBroadcastMessage());

    assertFalse(makeMessageHello.isBroadcastMessage());
    assertFalse(makeMessageQuit.isBroadcastMessage());
    assertTrue(makeMessageBCT.isBroadcastMessage());
    assertFalse(makeMessageAck.isBroadcastMessage());
    assertFalse(makeMessageNoAck.isBroadcastMessage());

    assertFalse(searchMessage1.isBroadcastMessage());
    assertFalse(searchMessage2.isBroadcastMessage());
    assertFalse(searchMessage3.isBroadcastMessage());
  }

  /**
   * Tests that the isDisplayMessage method appropriately evaluates each message. True is
   * returned only if the message is of type BROADCAST.
   */
  @Test
  void isDisplayMessage() {
    assertFalse(quit.isDisplayMessage());
    assertTrue(broadcast.isDisplayMessage());
    assertFalse(acknowledge.isDisplayMessage());
    assertFalse(noAcknowledge.isDisplayMessage());
    assertFalse(simpleLogin.isDisplayMessage());
    assertFalse(hello.isDisplayMessage());
    assertTrue(broadCastNullText.isDisplayMessage());

    assertFalse(makeMessageHello.isDisplayMessage());
    assertFalse(makeMessageQuit.isDisplayMessage());
    assertTrue(makeMessageBCT.isDisplayMessage());
    assertFalse(makeMessageAck.isDisplayMessage());
    assertFalse(makeMessageNoAck.isDisplayMessage());

    assertFalse(searchMessage1.isDisplayMessage());
    assertFalse(searchMessage2.isDisplayMessage());
    assertFalse(searchMessage3.isDisplayMessage());
  }

  /**
   * Tests that the isInitialization method appropriately evaluates each message. True is
   * returned only if the message is of type HELLO.
   */
  @Test
  void isInitialization() {
    assertFalse(quit.isInitialization());
    assertFalse(broadcast.isInitialization());
    assertFalse(acknowledge.isInitialization());
    assertFalse(noAcknowledge.isInitialization());
    assertTrue(simpleLogin.isInitialization());
    assertTrue(hello.isInitialization());
    assertFalse(broadCastNullText.isInitialization());


    assertTrue(makeMessageHello.isInitialization());
    assertFalse(makeMessageQuit.isInitialization());
    assertFalse(makeMessageBCT.isInitialization());
    assertFalse(makeMessageAck.isInitialization());
    assertFalse(makeMessageNoAck.isInitialization());

    assertFalse(searchMessage1.isInitialization());
    assertFalse(searchMessage2.isInitialization());
    assertFalse(searchMessage3.isInitialization());
  }

  /**
   * Tests that the terminate method appropriately evaluates each message. True is
   * returned only if the message is of type QUIT.
   */
  @Test
  void terminate() {
    assertTrue(quit.terminate());
    assertFalse(broadcast.terminate());
    assertFalse(acknowledge.terminate());
    assertFalse(noAcknowledge.terminate());
    assertFalse(simpleLogin.terminate());
    assertFalse(hello.terminate());
    assertFalse(broadCastNullText.terminate());

    assertFalse(makeMessageHello.terminate());
    assertTrue(makeMessageQuit.terminate());
    assertFalse(makeMessageBCT.terminate());
    assertFalse(makeMessageAck.terminate());
    assertFalse(makeMessageNoAck.terminate());

    assertFalse(searchMessage1.terminate());
    assertFalse(searchMessage2.terminate());
    assertFalse(searchMessage3.terminate());
  }

  /**
   * Tests that the toString method returns the correct String for each message.
   */
  @Test
  void toStringMethod() {
    assertEquals("BYE 4 Cole 2 --", quit.toString());
    assertEquals("BCT 6 Mike W 15 This is Mike W.", broadcast.toString());
    assertEquals("ACK 10 Client 2.0 0 ", acknowledge.toString());
    assertEquals("NAK 4 Cole 0 ", noAcknowledge.toString());
    assertEquals("HLO 4 Cole 8 Password", simpleLogin.toString());
    assertEquals("HLO 2 -- 15 Clark's message", hello.toString());
    assertEquals("BCT 17 Broadcast message 2 --", broadCastNullText.toString());

    assertEquals("HLO 8 Client 1 8 Password", makeMessageHello.toString());
    assertEquals("BYE 5 Sarah 2 --", makeMessageQuit.toString());
    assertEquals("BCT 4 Avik 28 This is a broadcast message.", makeMessageBCT.toString());
    assertEquals("ACK 6 Tanmay 22 This is a ack message.", makeMessageAck.toString());
    assertEquals("NAK 10 Cole Clark 25 This is a no ack message.", makeMessageNoAck.toString());

    assertEquals("REG 4 Cole 16 registerPassword", register.toString());
    assertEquals("UPD 6 Tanmay 14 updatePassword", update.toString());
    assertEquals("DEL 4 Avik 2 --", delete.toString());
    assertEquals("PVT 5 Sarah 4 Cole 26 This is a private message.", privateMessage.toString());

    Message nullPVTText = Message.makeMessage("PVT", "Tanmay", null, null, "Avik");
    assertEquals("PVT 6 Tanmay 4 Avik 2 --", nullPVTText.toString());

    assertEquals("SRC 4 Cole 6 sentTo 4 Avik 19 2018-11-23 00:00:00 19 2018-12-31 00:00:00", searchMessage1.toString());
    assertEquals("SRC 4 Cole 12 receivedFrom 4 Avik 19 2018-11-23 00:00:00 19 2018-12-31 00:00:00", searchMessage2.toString());
    assertEquals("SRC 4 Cole 4 both 4 Avik 19 2018-11-23 00:00:00 19 2018-12-31 00:00:00", searchMessage3.toString());
  }

  /**
   * Tests for appropriate behavior when the name or text field is null in certain types of
   * messages.
   */
  @Test
  void testNullValues() {
    makeMessageQuit = Message.makeQuitMessage(null);
    makeMessageBCT = Message.makeBroadcastMessage(null, "hey everyone");
    makeMessageHello = Message.makeHelloMessage(null);
    simpleLogin = Message.makeLoginMessage(null,null);
    makeMessageAck = Message.makeAcknowledgeMessage(null,null);
    makeMessageNoAck = Message.makeMessage("Not correct", null, null,null,null);

    assertNull(makeMessageQuit);
    assertNull(makeMessageBCT);
    assertNull(makeMessageHello);
    assertNull(simpleLogin);
    assertNull(makeMessageAck);
    assertNull(makeMessageNoAck);
    assertNull(Message.makeUpdateMessage(null, "updatepassword"));
    assertNull(Message.makeRegisterMessage(null, "password"));
    assertNull(Message.makeDeleteMessage(null));
    assertNull(Message.makePrivateMessage(null, "Cole", "this is a private message."));
    assertNull(Message.makePrivateMessage("Cole", null, "this is a private message."));

    assertNull(Message.makeSearchMessage(null, "sentTo","Avik", "2018-11-24 00:00:00", "2018-11-25 00:00:00"));
    assertNull(Message.makeSearchMessage("Cole", "receivedFrom",null, "2018-11-24 00:00:00", "2018-11-25 00:00:00"));
    assertNull(Message.makeSearchMessage(null, "both",null, "2018-11-24 00:00:00", "2018-11-25 00:00:00"));
  }

  /**
   * Tests that the getRecipientName function returns the receiving person's username for
   * private messages and null for all other types of messages.
   */
  @Test
  void testGetRecipientName() {
    assertEquals("Cole", privateMessage.getRecipientName());
    assertEquals(null, update.getRecipientName());
    assertEquals(null, delete.getRecipientName());
    assertEquals(null, register.getRecipientName());
    assertEquals(null, makeMessageNoAck.getRecipientName());
    assertEquals(null, makeMessageAck.getRecipientName());
    assertEquals(null, simpleLogin.getRecipientName());
    assertEquals(null, makeMessageHello.getRecipientName());
    assertEquals(null, makeMessageBCT.getRecipientName());
    assertEquals(null, makeMessageQuit.getRecipientName());
    assertEquals(null, quit.getRecipientName());
    assertEquals(null, broadcast.getRecipientName());
    assertEquals(null, hello.getRecipientName());
    assertEquals(null, acknowledge.getRecipientName());
    assertEquals(null, noAcknowledge.getRecipientName());


    assertEquals("Avik", searchMessage1.getRecipientName());
    assertEquals("Avik", searchMessage2.getRecipientName());
    assertEquals("Avik", searchMessage3.getRecipientName());
  }



  /**
   * Tests that the isNonAcknowledge method appropriately evaluates each message. True is
   * returned only if the message is of type NO_ACKNOWLEDGE.
   */
  @Test
  void isNonAcknowledge() {
    assertFalse(quit.isNonAcknowledge());
    assertFalse(broadcast.isNonAcknowledge());
    assertFalse(acknowledge.isNonAcknowledge());
    assertTrue(noAcknowledge.isNonAcknowledge());
    assertFalse(simpleLogin.isNonAcknowledge());
    assertFalse(hello.isNonAcknowledge());
    assertFalse(broadCastNullText.isNonAcknowledge());

    assertFalse(makeMessageHello.isNonAcknowledge());
    assertFalse(makeMessageQuit.isNonAcknowledge());
    assertFalse(makeMessageBCT.isNonAcknowledge());
    assertFalse(makeMessageAck.isNonAcknowledge());

    assertFalse(register.isNonAcknowledge());
    assertFalse(update.isNonAcknowledge());
    assertFalse(delete.isNonAcknowledge());
    assertFalse(privateMessage.isNonAcknowledge());

    assertFalse(searchMessage1.isNonAcknowledge());
    assertFalse(searchMessage2.isNonAcknowledge());
    assertFalse(searchMessage3.isNonAcknowledge());
  }


  /**
   * Tests that the isRegister method appropriately evaluates each message. True is
   * returned only if the message is of type REGISTER.
   */
  @Test
  void isRegister() {
    assertFalse(quit.isRegister());
    assertFalse(broadcast.isRegister());
    assertFalse(acknowledge.isRegister());
    assertFalse(noAcknowledge.isRegister());
    assertFalse(simpleLogin.isRegister());
    assertFalse(hello.isRegister());
    assertFalse(broadCastNullText.isRegister());

    assertFalse(makeMessageHello.isRegister());
    assertFalse(makeMessageQuit.isRegister());
    assertFalse(makeMessageBCT.isRegister());
    assertFalse(makeMessageAck.isRegister());

    assertTrue(register.isRegister());
    assertFalse(update.isRegister());
    assertFalse(delete.isRegister());
    assertFalse(privateMessage.isRegister());

    assertFalse(searchMessage1.isRegister());
    assertFalse(searchMessage2.isRegister());
    assertFalse(searchMessage3.isRegister());
  }

  /**
   * Tests that the isUpdateMessage method appropriately evaluates each message. True is
   * returned only if the message is of type UPDATE.
   */
  @Test
  void isUpdateMessage() {
    assertFalse(quit.isUpdateMessage());
    assertFalse(broadcast.isUpdateMessage());
    assertFalse(acknowledge.isUpdateMessage());
    assertFalse(noAcknowledge.isUpdateMessage());
    assertFalse(simpleLogin.isUpdateMessage());
    assertFalse(hello.isUpdateMessage());
    assertFalse(broadCastNullText.isUpdateMessage());

    assertFalse(makeMessageHello.isUpdateMessage());
    assertFalse(makeMessageQuit.isUpdateMessage());
    assertFalse(makeMessageBCT.isUpdateMessage());
    assertFalse(makeMessageAck.isUpdateMessage());

    assertFalse(register.isUpdateMessage());
    assertTrue(update.isUpdateMessage());
    assertFalse(delete.isUpdateMessage());
    assertFalse(privateMessage.isUpdateMessage());

    assertFalse(searchMessage1.isUpdateMessage());
    assertFalse(searchMessage2.isUpdateMessage());
    assertFalse(searchMessage3.isUpdateMessage());
  }

  /**
   * Tests that the isDeleteMessage method appropriately evaluates each message. True is
   * returned only if the message is of type DELETE.
   */
  @Test
  void isDeleteMessage() {
    assertFalse(quit.isDeleteMessage());
    assertFalse(broadcast.isDeleteMessage());
    assertFalse(acknowledge.isDeleteMessage());
    assertFalse(noAcknowledge.isDeleteMessage());
    assertFalse(simpleLogin.isDeleteMessage());
    assertFalse(hello.isDeleteMessage());
    assertFalse(broadCastNullText.isDeleteMessage());

    assertFalse(makeMessageHello.isDeleteMessage());
    assertFalse(makeMessageQuit.isDeleteMessage());
    assertFalse(makeMessageBCT.isDeleteMessage());
    assertFalse(makeMessageAck.isDeleteMessage());

    assertFalse(register.isDeleteMessage());
    assertFalse(update.isDeleteMessage());
    assertTrue(delete.isDeleteMessage());
    assertFalse(privateMessage.isDeleteMessage());

    assertFalse(searchMessage1.isDeleteMessage());
    assertFalse(searchMessage2.isDeleteMessage());
    assertFalse(searchMessage3.isDeleteMessage());
  }


  /**
   * Tests that the isPrivateMessage method appropriately evaluates each message. True is
   * returned only if the message is of type PRIVATE.
   */
  @Test
  void isPrivateMessage() {
    assertFalse(quit.isPrivateMessage());
    assertFalse(broadcast.isPrivateMessage());
    assertFalse(acknowledge.isPrivateMessage());
    assertFalse(noAcknowledge.isPrivateMessage());
    assertFalse(simpleLogin.isPrivateMessage());
    assertFalse(hello.isPrivateMessage());
    assertFalse(broadCastNullText.isPrivateMessage());

    assertFalse(makeMessageHello.isPrivateMessage());
    assertFalse(makeMessageQuit.isPrivateMessage());
    assertFalse(makeMessageBCT.isPrivateMessage());
    assertFalse(makeMessageAck.isPrivateMessage());

    assertFalse(register.isPrivateMessage());
    assertFalse(update.isPrivateMessage());
    assertFalse(delete.isPrivateMessage());
    assertTrue(privateMessage.isPrivateMessage());

    assertFalse(searchMessage1.isPrivateMessage());
    assertFalse(searchMessage2.isPrivateMessage());
    assertFalse(searchMessage3.isPrivateMessage());
  }

  /**
   * Tests that the isSearchMessage method appropriately evaluates each message. True is
   * returned only if the message is of type SEARCH.
   */
  @Test
  void isSearchMessage() {
    assertFalse(quit.isSearchMessage());
    assertFalse(broadcast.isSearchMessage());
    assertFalse(acknowledge.isSearchMessage());
    assertFalse(noAcknowledge.isSearchMessage());
    assertFalse(simpleLogin.isSearchMessage());
    assertFalse(hello.isSearchMessage());
    assertFalse(broadCastNullText.isSearchMessage());

    assertFalse(makeMessageHello.isSearchMessage());
    assertFalse(makeMessageQuit.isSearchMessage());
    assertFalse(makeMessageBCT.isSearchMessage());
    assertFalse(makeMessageAck.isSearchMessage());

    assertFalse(register.isSearchMessage());
    assertFalse(update.isSearchMessage());
    assertFalse(delete.isSearchMessage());
    assertFalse(privateMessage.isSearchMessage());

    assertTrue(searchMessage1.isSearchMessage());
    assertTrue(searchMessage2.isSearchMessage());
    assertTrue(searchMessage3.isSearchMessage());
  }
}

