package edu.northeastern.ccs.im.server;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import edu.northeastern.ccs.im.User;
import edu.northeastern.ccs.im.UserCrud;
import edu.northeastern.ccs.im.UserCrudImpl;
import edu.northeastern.ccs.im.chatter.IMConnection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing that when a registration message is entered with a username that contains profanity,
 * the username is not added to the database and the user is asked to register under a different
 * name.
 */
class ClientInvalidRegistrationProfanityTest {
  private ServerSocketChannel serverSocket;
  private IMConnection clientTerminal;

  @BeforeEach
  final void setUp() {
    // set up server sockets
    try {
      serverSocket = ServerSocketChannel.open();
      serverSocket.configureBlocking(false);
      serverSocket.socket().bind(new InetSocketAddress(4789));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // set up chatter client to send and receive valid messages to our server
    // The new username contains profanity
    clientTerminal = new IMConnection("127.0.0.1", 4789, "profanityTestFuck", "password");
    clientTerminal.connect("Register");
  }

  @AfterEach
  final void tearDown() {
    try {
      if (clientTerminal.connectionActive()) {
        // clientTerminal.sendDeleteMessage();
        clientTerminal.disconnect();
      }
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Testing that when a registration message is entered with a username that contains profanity,
   * the username is not added to the database and the user is asked to register under a different
   * name.
   */
  @Test
  void testInvalidMessage() {
    SocketChannel socket;
    try {
      socket = serverSocket.accept();
      ClientRunnable runnable = new ClientRunnable(socket);

      try {
        for (int i = 0; i < 10; i++) {
          runnable.run();
        }
      } catch (NullPointerException e) {
        // expect to get a null pointer exception when terminating a
        // connection!
        // because we never added this ClientRunnable to Prattle.active
      } catch (Exception e) {
        e.printStackTrace();
        fail("unexpected exception type, should only throw null pointer exception");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    User user = new User();
    user.setUsername("profanityTestFuck");
    user.setPassword("password");
    UserCrud crud = new UserCrudImpl();
    //User was not added to the database
    assertFalse(crud.getUser(user));
  }

}