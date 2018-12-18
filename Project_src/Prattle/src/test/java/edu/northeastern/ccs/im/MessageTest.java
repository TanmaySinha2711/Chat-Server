package edu.northeastern.ccs.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the Message class.
 *
 * @author Cole Clark
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
  private Message makeMessageRcl;

  private Message register;
  private Message update;
  private Message delete;
  private Message privateMessage;
  private Message historyMessage;
  private Message groupMessage;
  private Message dupMessage;

  private Message searchMessageSent;
  private Message searchMessageReceived;
  private Message searchMessageBoth;

  private Message parentalControlsOn;
  private Message parentalControlsOff;
  private Message parentalControlsInvalid;

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
    simpleLogin = Message.makeSimpleLoginMessage("Cole","Password");
    // Created using the makeHelloMessage method
    hello = Message.makeHelloMessage("Clark's message");

    // Created using hte makeMessage method
    makeMessageHello = Message.makeMessage("HLO", "Client 1", "hello","Password","");
    makeMessageQuit = Message.makeMessage("BYE", "Sarah", "This is a quit message.","","");
    makeMessageBCT = Message.makeMessage("BCT", "Avik", "This is a broadcast message.","","");
    makeMessageAck = Message.makeMessage("ACK", "Tanmay", "This is a ack message.","","");
    makeMessageNoAck = Message.makeMessage("NAK", "Cole Clark", "This is a no ack message.","","");
    makeMessageRcl = Message.makeMessage("RCL", "Tanmay", "This is a RCL message","","");

    // New message types (register, update, delete, and private)
    register = Message.makeMessage("REG", "Cole", "This is a register message.", "registerPassword", "");
    update = Message.makeMessage("UPD", "Tanmay", "This is an update message.", "updatePassword", "");
    delete = Message.makeMessage("DEL", "Avik", "This is a delete message.", "deletePassword", "");
    privateMessage = Message.makeMessage("PVT", "Sarah", "This is a private message.", "MyPassword", "Cole");
    historyMessage = Message.makeMessage("HST", "Sarah", "10", null, "all");
    groupMessage = Message.makeMessage("GRP", "Sarah", "This is a group message.", "MyPassword", "Cole");
    dupMessage = Message.makeMessage("DUP","agency","avik","MyPassword",null);
    
    // New message type, search
    searchMessageSent = Message.makeSearchMessage("Cole", "sentto", "Avik", "2018-11-23 00:00:00", "2018-11-25 00:00:00");
    searchMessageReceived = Message.makeSearchMessage("Sarah", "receivedfrom", "Tanmay", "2018-09-23 11:23:35", "2018-11-30 00:00:00");
    searchMessageBoth = Message.makeSearchMessage("Avik", "both", "Sarah", "2018-11-23 00:00:00", "2019-01-25 02:10:10");

    parentalControlsOn = Message.makeParentalControlMessage("Cole", "ON");
    parentalControlsOff = Message.makeParentalControlMessage("Avik", "off");
    parentalControlsInvalid = Message.makeParentalControlMessage("Tanmay", "invalid");
  }

  /**
   * Tests that the getName function returns the correct sender for each message.
   */
  @Test
  void getName() {
    assertEquals("Cole",quit.getName());
    assertEquals("Mike W",broadcast.getName());
    assertEquals("Client 2.0",acknowledge.getName());
    assertEquals("Cole",noAcknowledge.getName());
    assertEquals("Cole",simpleLogin.getName());
    assertEquals(null, hello.getName());
    assertEquals("Broadcast message", broadCastNullText.getName());

    assertEquals("Client 1", makeMessageHello.getName());
    assertEquals("Sarah", makeMessageQuit.getName());
    assertEquals("Avik", makeMessageBCT.getName());
    assertEquals("Tanmay", makeMessageAck.getName());
    assertEquals("Cole Clark", makeMessageNoAck.getName());


    assertEquals("Cole", register.getName());
    assertEquals("Tanmay", update.getName());
    assertEquals("Avik", delete.getName());
    assertEquals("Sarah", privateMessage.getName());
    assertEquals("Sarah", historyMessage.getName());
    assertEquals("Sarah", groupMessage.getName());

    assertEquals("Cole", searchMessageSent.getName());
    assertEquals("Sarah", searchMessageReceived.getName());
    assertEquals("Avik", searchMessageBoth.getName());

    assertEquals("Cole", parentalControlsOn.getName());
    assertEquals("Avik", parentalControlsOff.getName());
    assertEquals("Tanmay", parentalControlsInvalid.getName());
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
    assertEquals("10", historyMessage.getText());
    assertEquals("This is a group message.", groupMessage.getText());


    assertEquals(null, searchMessageSent.getText());
    assertEquals(null, searchMessageReceived.getText());
    assertEquals(null, searchMessageBoth.getText());

    assertEquals("ON", parentalControlsOn.getText());
    assertEquals("off", parentalControlsOff.getText());
    assertEquals("invalid", parentalControlsInvalid.getText());
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
    assertFalse(privateMessage.isAcknowledge());
    assertFalse(groupMessage.isAcknowledge());

    assertFalse(makeMessageHello.isAcknowledge());
    assertFalse(makeMessageQuit.isAcknowledge());
    assertFalse(makeMessageBCT.isAcknowledge());
    assertTrue(makeMessageAck.isAcknowledge());
    assertFalse(makeMessageNoAck.isAcknowledge());

    assertFalse(searchMessageSent.isAcknowledge());
    assertFalse(searchMessageReceived.isAcknowledge());
    assertFalse(searchMessageBoth.isAcknowledge());
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

    assertFalse(searchMessageSent.isBroadcastMessage());
    assertFalse(searchMessageReceived.isBroadcastMessage());
    assertFalse(searchMessageBoth.isBroadcastMessage());
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

    assertFalse(searchMessageSent.isDisplayMessage());
    assertFalse(searchMessageReceived.isDisplayMessage());
    assertFalse(searchMessageBoth.isDisplayMessage());
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

    assertFalse(searchMessageSent.isInitialization());
    assertFalse(searchMessageReceived.isInitialization());
    assertFalse(searchMessageBoth.isInitialization());
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

    assertFalse(searchMessageSent.terminate());
    assertFalse(searchMessageReceived.terminate());
    assertFalse(searchMessageBoth.terminate());
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
    
    assertEquals("GRP 5 Sarah 4 Cole 24 This is a group message.", groupMessage.toString());

    Message nullGRPText = Message.makeMessage("GRP", "Tanmay", null, null, "Avik");
    assertEquals("GRP 6 Tanmay 4 Avik 2 --", nullGRPText.toString());

    assertEquals("HST 5 Sarah 3 all 2 10", historyMessage.toString());
    Message nullHSTtext = Message.makeHistoryMessage("Sarah", "Tanmay", null);
    assertEquals("HST 5 Sarah 6 Tanmay 2 10", nullHSTtext.toString());

    assertEquals("SRC 4 Cole 6 sentto 4 Avik 19 2018-11-23 00:00:00 19 2018-11-25 00:00:00",searchMessageSent.toString());
    assertEquals("SRC 5 Sarah 12 receivedfrom 6 Tanmay 19 2018-09-23 11:23:35 19 2018-11-30 00:00:00", searchMessageReceived.toString());
    assertEquals("SRC 4 Avik 4 both 5 Sarah 19 2018-11-23 00:00:00 19 2019-01-25 02:10:10", searchMessageBoth.toString());

    assertEquals("PRC 4 Cole 2 ON",parentalControlsOn.toString());
    assertEquals("PRC 4 Avik 3 off", parentalControlsOff.toString());
    assertEquals("PRC 6 Tanmay 7 invalid", parentalControlsInvalid.toString());


  }

  /**
   * Tests for appropriate behavior when the name or text field is null in certain types of
   * messages.
   */
  @Test
  void testNullValues() {
    assertNull(Message.makeQuitMessage(null));
    assertNull(Message.makeBroadcastMessage(null, "hey everyone"));
    assertNull(Message.makeHelloMessage(null));
    assertNull(Message.makeSimpleLoginMessage(null,null));
    assertNull(Message.makeAcknowledgeMessage(null,null));
    assertNull(Message.makeMessage("Not correct", null, null,null,null));
    assertNull(Message.makeSearchMessage(null, "both", "Cole", "2018-11-24 00:00:00", "2018-11-25 00:00:00"));
    assertNull(Message.makeSearchMessage("Cole", "both", null, "2018-11-24 00:00:00", "2018-11-25 00:00:00"));
    assertNull(Message.makeUpdateMessage(null, "updatepassword"));
    assertNull(Message.makeRegisterMessage(null, "password"));
    assertNull(Message.makeDeleteMessage(null));
    assertNull(Message.makePrivateMessage(null, "Cole", "this is a private message."));
    assertNull(Message.makePrivateMessage("Cole", null, "this is a private message."));
    assertNull(Message.makeHistoryMessage(null, "Cole", "1"));
    assertNull(Message.makeHistoryMessage("Cole", null, "1"));
    assertNull(Message.makeGroupMessage("Cole", null, "this is a group message."));
    assertNull(Message.makeGroupMessage(null, null, "this is a group message."));
    assertNull(Message.makeParentalControlMessage(null, "on"));
    assertNull(Message.makeParentalControlMessage("Cole", null));

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

    assertEquals("Avik", searchMessageSent.getRecipientName());
    assertEquals("Tanmay", searchMessageReceived.getRecipientName());
    assertEquals("Sarah", searchMessageBoth.getRecipientName());
  }

  /**
   * Tests that the isHello method appropriately evaluates each message. True is
   * returned only if the message is of type HELLO.
   */
  @Test
  void isHello() {
    assertFalse(quit.isHello());
    assertFalse(broadcast.isHello());
    assertFalse(acknowledge.isHello());
    assertFalse(noAcknowledge.isHello());
    assertTrue(simpleLogin.isHello());
    assertTrue(hello.isHello());
    assertFalse(broadCastNullText.isHello());

    assertTrue(makeMessageHello.isHello());
    assertFalse(makeMessageQuit.isHello());
    assertFalse(makeMessageBCT.isHello());
    assertFalse(makeMessageAck.isHello());

    assertFalse(register.isHello());
    assertFalse(update.isHello());
    assertFalse(delete.isHello());
    assertFalse(privateMessage.isHello());
    assertFalse(historyMessage.isHello());
    assertFalse(groupMessage.isHello());


    assertFalse(searchMessageReceived.isHello());
    assertFalse(searchMessageSent.isHello());
    assertFalse(searchMessageBoth.isHello());
  }
  
  @Test
  void isRecall() {
    assertFalse(quit.isRecallMessage());
    assertFalse(broadcast.isRecallMessage());
    assertFalse(acknowledge.isRecallMessage());
    assertFalse(noAcknowledge.isRecallMessage());
    assertFalse(simpleLogin.isRecallMessage());
    assertFalse(hello.isRecallMessage());
    assertFalse(broadCastNullText.isRecallMessage());

    assertFalse(makeMessageHello.isRecallMessage());
    assertFalse(makeMessageQuit.isRecallMessage());
    assertFalse(makeMessageBCT.isRecallMessage());
    assertFalse(makeMessageAck.isRecallMessage());

    assertFalse(register.isRecallMessage());
    assertFalse(update.isRecallMessage());
    assertFalse(delete.isRecallMessage());
    assertFalse(privateMessage.isRecallMessage());
    assertFalse(historyMessage.isRecallMessage());
    assertFalse(groupMessage.isRecallMessage());
    assertTrue(makeMessageRcl.isRecallMessage());
  }
  
  @Test
  void isDupMessage() {
    assertFalse(quit.isDupingMessage());
    assertFalse(broadcast.isDupingMessage());
    assertFalse(acknowledge.isDupingMessage());
    assertFalse(noAcknowledge.isDupingMessage());
    assertFalse(simpleLogin.isDupingMessage());
    assertFalse(hello.isDupingMessage());
    assertFalse(broadCastNullText.isDupingMessage());

    assertFalse(makeMessageHello.isDupingMessage());
    assertFalse(makeMessageQuit.isDupingMessage());
    assertFalse(makeMessageBCT.isDupingMessage());
    assertFalse(makeMessageAck.isDupingMessage());

    assertFalse(register.isDupingMessage());
    assertFalse(update.isDupingMessage());
    assertFalse(delete.isDupingMessage());
    assertFalse(privateMessage.isDupingMessage());
    assertFalse(historyMessage.isDupingMessage());
    assertFalse(groupMessage.isDupingMessage());
    assertTrue(dupMessage.isDupingMessage());
  }

  /**
   * Tests that the isNakAcknowledge method appropriately evaluates each message. True is
   * returned only if the message is of type NO_ACKNOWLEDGE.
   */
  @Test
  void isNakAcknowledge() {
    assertFalse(quit.isNakAcknowledge());
    assertFalse(broadcast.isNakAcknowledge());
    assertFalse(acknowledge.isNakAcknowledge());
    assertTrue(noAcknowledge.isNakAcknowledge());
    assertFalse(simpleLogin.isNakAcknowledge());
    assertFalse(hello.isNakAcknowledge());
    assertFalse(broadCastNullText.isNakAcknowledge());

    assertFalse(makeMessageHello.isNakAcknowledge());
    assertFalse(makeMessageQuit.isNakAcknowledge());
    assertFalse(makeMessageBCT.isNakAcknowledge());
    assertFalse(makeMessageAck.isNakAcknowledge());

    assertFalse(register.isNakAcknowledge());
    assertFalse(update.isNakAcknowledge());
    assertFalse(delete.isNakAcknowledge());
    assertFalse(privateMessage.isNakAcknowledge());
    assertFalse(historyMessage.isNakAcknowledge());
    assertFalse(groupMessage.isNakAcknowledge());

    assertFalse(searchMessageSent.isNakAcknowledge());
    assertFalse(searchMessageReceived.isNakAcknowledge());
    assertFalse(searchMessageBoth.isNakAcknowledge());
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
    assertFalse(historyMessage.isRegister());
    assertFalse(groupMessage.isRegister());

    assertFalse(searchMessageSent.isRegister());
    assertFalse(searchMessageReceived.isRegister());
    assertFalse(searchMessageBoth.isRegister());
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
    assertFalse(historyMessage.isUpdateMessage());
    assertFalse(groupMessage.isUpdateMessage());


    assertFalse(searchMessageSent.isUpdateMessage());
    assertFalse(searchMessageReceived.isUpdateMessage());
    assertFalse(searchMessageBoth.isUpdateMessage());
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
    assertFalse(historyMessage.isDeleteMessage());
    assertFalse(groupMessage.isDeleteMessage());


    assertFalse(searchMessageReceived.isDeleteMessage());
    assertFalse(searchMessageSent.isDeleteMessage());
    assertFalse(searchMessageBoth.isDeleteMessage());
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
    assertFalse(historyMessage.isPrivateMessage());
    assertFalse(groupMessage.isPrivateMessage());

    assertFalse(searchMessageSent.isPrivateMessage());
    assertFalse(searchMessageReceived.isPrivateMessage());
    assertFalse(searchMessageBoth.isPrivateMessage());
  }

  /**
   * Tests that the isHistoryMessage method appropriately evaluates each message. True is
   * returned only if the message is of type HISTORY.
   */
  @Test
  void isHistoryMessage() {
    assertFalse(quit.isHistoryMessage());
    assertFalse(broadcast.isHistoryMessage());
    assertFalse(acknowledge.isHistoryMessage());
    assertFalse(noAcknowledge.isHistoryMessage());
    assertFalse(simpleLogin.isHistoryMessage());
    assertFalse(hello.isHistoryMessage());
    assertFalse(broadCastNullText.isHistoryMessage());

    assertFalse(makeMessageHello.isHistoryMessage());
    assertFalse(makeMessageQuit.isHistoryMessage());
    assertFalse(makeMessageBCT.isHistoryMessage());
    assertFalse(makeMessageAck.isHistoryMessage());

    assertFalse(register.isHistoryMessage());
    assertFalse(update.isHistoryMessage());
    assertFalse(delete.isHistoryMessage());
    assertFalse(privateMessage.isHistoryMessage());
    assertTrue(historyMessage.isHistoryMessage());
    assertFalse(groupMessage.isHistoryMessage());

    assertFalse(searchMessageSent.isHistoryMessage());
    assertFalse(searchMessageReceived.isHistoryMessage());
    assertFalse(searchMessageBoth.isHistoryMessage());
  }

  /**
   * Tests that the isGroupMessage method appropriately evaluates each message. True is
   * returned only if the message is of type GROUP.
   */
  @Test
  void isGroupMessage() {
    assertFalse(quit.isGroupMessage());
    assertFalse(broadcast.isGroupMessage());
    assertFalse(acknowledge.isGroupMessage());
    assertFalse(noAcknowledge.isGroupMessage());
    assertFalse(simpleLogin.isGroupMessage());
    assertFalse(hello.isGroupMessage());
    assertFalse(broadCastNullText.isGroupMessage());

    assertFalse(makeMessageHello.isGroupMessage());
    assertFalse(makeMessageQuit.isGroupMessage());
    assertFalse(makeMessageBCT.isGroupMessage());
    assertFalse(makeMessageAck.isGroupMessage());

    assertFalse(register.isGroupMessage());
    assertFalse(update.isGroupMessage());
    assertFalse(delete.isGroupMessage());
    assertFalse(privateMessage.isGroupMessage());
    assertFalse(historyMessage.isGroupMessage());
    assertTrue(groupMessage.isGroupMessage());

    assertFalse(searchMessageSent.isGroupMessage());
    assertFalse(searchMessageReceived.isGroupMessage());
    assertFalse(searchMessageBoth.isGroupMessage());
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
    assertFalse(historyMessage.isSearchMessage());
    assertFalse(groupMessage.isSearchMessage());

    assertTrue(searchMessageSent.isSearchMessage());
    assertTrue(searchMessageReceived.isSearchMessage());
    assertTrue(searchMessageBoth.isSearchMessage());
  }

  /**
   * Testing the getStartTime, getEndTime, and getSentReceivedBoth methods
   */
  @Test
  void testSearchmessages() {
    Message src1 = Message.makeSearchMessage("cole", "sentTo", "Avik", "2018-12-23 23:34:12", "2018-12-31 00:00:00");
    assertEquals("sentTo", src1.getSentReceivedBoth());
    assertEquals("2018-12-23 23:34:12", src1.getStartTime());
    assertEquals("2018-12-31 00:00:00", src1.getEndTime());

    src1 = Message.makeSearchMessage("cole", "Receivedfrom", "Avik", "2018-11-24 14:47:51", "2018-12-6 19:01:03");
    assertEquals("Receivedfrom", src1.getSentReceivedBoth());
    assertEquals("2018-11-24 14:47:51", src1.getStartTime());
    assertEquals("2018-12-6 19:01:03", src1.getEndTime());

    src1 = Message.makeSearchMessage("cole", "both", "Avik", "2018-11-23 09:03:00", "2018-11-25 09:03:00");
    assertEquals("both", src1.getSentReceivedBoth());
    assertEquals("2018-11-23 09:03:00", src1.getStartTime());
    assertEquals("2018-11-25 09:03:00", src1.getEndTime());

    assertEquals(null, quit.getSentReceivedBoth());
    assertEquals(null, hello.getStartTime());
    assertEquals(null, broadcast.getEndTime());

  }

  @Test
  void isParentalControlMessage() {
    assertTrue(parentalControlsOn.isParentalControlMessage());
    assertTrue(parentalControlsOff.isParentalControlMessage());
    assertTrue(parentalControlsInvalid.isParentalControlMessage());

    assertFalse(quit.isParentalControlMessage());
    assertFalse(broadcast.isParentalControlMessage());
    assertFalse(acknowledge.isParentalControlMessage());
    assertFalse(noAcknowledge.isParentalControlMessage());
    assertFalse(simpleLogin.isParentalControlMessage());
    assertFalse(hello.isParentalControlMessage());
    assertFalse(broadCastNullText.isParentalControlMessage());

    assertFalse(makeMessageHello.isParentalControlMessage());
    assertFalse(makeMessageQuit.isParentalControlMessage());
    assertFalse(makeMessageBCT.isParentalControlMessage());
    assertFalse(makeMessageAck.isParentalControlMessage());

    assertFalse(register.isParentalControlMessage());
    assertFalse(update.isParentalControlMessage());
    assertFalse(delete.isParentalControlMessage());
    assertFalse(privateMessage.isParentalControlMessage());
    assertFalse(historyMessage.isParentalControlMessage());
    assertFalse(groupMessage.isParentalControlMessage());

    assertFalse(searchMessageBoth.isParentalControlMessage());
    assertFalse(searchMessageReceived.isParentalControlMessage());
    assertFalse(searchMessageSent.isParentalControlMessage());

  }


}

