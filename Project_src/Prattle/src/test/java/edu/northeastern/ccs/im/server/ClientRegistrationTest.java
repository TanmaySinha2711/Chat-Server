package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.junit.jupiter.api.*;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.SocketNB;
import edu.northeastern.ccs.im.chatter.IMConnection;

class ClientRegistrationTest {

	ServerSocketChannel serverSocket;
	IMConnection clientTerminal;

	@BeforeEach
	public final void setUp() {
		// set up server sockets
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(4999));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// set up chatter client to send and receive valid messages to our
		// server
		clientTerminal = new IMConnection("127.0.0.1", 4999, "temp", "temp");
		clientTerminal.connect("Register");
	}

	@AfterEach
	public final void tearDown() {
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
	 * Registration Message,Update password and delete user Test
	 */
	@Test
	void testRegistrationMessage() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);

			clientTerminal.sendMessage("hi temp");
			clientTerminal.sendUpdateMessage("temp1");
			clientTerminal.sendDeleteMessage();
			clientTerminal.disconnect();
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
	}

}
