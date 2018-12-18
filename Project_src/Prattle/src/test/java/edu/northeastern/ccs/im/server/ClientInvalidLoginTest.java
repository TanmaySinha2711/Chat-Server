package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.junit.jupiter.api.*;

import edu.northeastern.ccs.im.chatter.IMConnection;

class ClientInvalidLoginTest {

	ServerSocketChannel serverSocket;
	IMConnection clientTerminal;

	@BeforeEach
	public final void setUp() {
		// set up server sockets
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(4989));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// set up chatter client to send and receive valid messages to our
		// server
		clientTerminal = new IMConnection("127.0.0.1", 4989, "avik", "temp");
		clientTerminal.connect("Login");
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
	void testinvalidMessage() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);

			clientTerminal.sendMessage("hi temp");
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
