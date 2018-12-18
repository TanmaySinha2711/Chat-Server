package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Tanmay
 *
 */
class ScanNetNBTest {

	ServerSocketChannel socket;
	
	private static final Logger LOGGER = Logger.getLogger(ScanNetNBTest.class.getName());
	
	@BeforeEach
	public final void setUp() {
		try {
			socket = ServerSocketChannel.open();
			socket.configureBlocking(false);
			socket.socket().bind(new InetSocketAddress(4546));
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}

	}

	@AfterEach
	public final void tearDown() {
		try {
			socket.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}

	@Test
	void testScanNetNBSocketNB() {
		SocketNB sock = null;
		try {
			sock = new SocketNB("127.0.0.1", 4546);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		ScanNetNB tester = new ScanNetNB(sock);
		
		//check if socket channel created is not null
		assertNotNull(tester, "socket channel should be created");
		try {
			sock.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	@Test
	void testScanNetNBSocketChannel() {
		
	}

	@Test
	void testReadArgument() {
		CharBuffer testBuffer = CharBuffer.allocate(1024);
		int[] testStr = {5, 6, 7, 8, 1, 2, 3, 4};
		for(int i = 0; i < testStr.length; i++)
			testBuffer.put((char) testStr[i]);
		
		SocketNB sock = null;
		try {
			sock = new SocketNB("127.0.0.1", 4546);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		ScanNetNB tester = new ScanNetNB(sock);
		assertNotNull(tester);
		try {
			sock.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	@Test
	void testHasNextMessage() {
		SocketNB sock = null;
		PrintWriter w = null;
		try {
			sock = new SocketNB("127.0.0.1", 4546);
			w = new PrintWriter(sock.getSocket().socket().getOutputStream(), true);

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		ScanNetNB tester = new ScanNetNB(sock);

		//check if socket channel created is not null
		assertNotNull(tester, "socket channel should be created");
		//w.println("aaaaaahahhhahaha");
		//w.flush();
		tester.hasNextMessage();
		w.print(Message.makeBroadcastMessage("me", "hi"));

		assertFalse(tester.hasNextMessage(), 
				"since nothing is typed there should be no message in the buffer");
		
		try {
			sock.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}

	@Test
	void testNextMessage() {
		SocketNB sock = null;
		try {
			sock = new SocketNB("127.0.0.1", 4546);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		ScanNetNB tester = new ScanNetNB(sock);
		
		//check if socket channel created is not null
		assertNotNull(tester, "socket channel should be created");
		
		assertThrows(NextDoesNotExistException.class,() -> { tester.nextMessage();},
				"since nothing is typed there should be no next message");
		
		try {
			sock.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}

	@Test
	void testClose() {
		SocketNB sock = null;
		try {
			sock = new SocketNB("127.0.0.1", 4546);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
		ScanNetNB tester = new ScanNetNB(sock);
		
		assertDoesNotThrow(() -> {tester.close();}, 
				"closing channel should not throw exception");

	}

}
