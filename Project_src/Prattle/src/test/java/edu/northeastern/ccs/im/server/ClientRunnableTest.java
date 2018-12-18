package edu.northeastern.ccs.im.server;


import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

import edu.northeastern.ccs.im.chatter.MessageScanner;
import org.junit.jupiter.api.*;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.SocketNB;
import edu.northeastern.ccs.im.chatter.IMConnection;

class ClientRunnableTest {

	ServerSocketChannel serverSocket;
	IMConnection clientTerminal;

	@BeforeEach
	public final void setUp() {
		// set up server sockets
		try {
			serverSocket = ServerSocketChannel.open();
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(4544));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//set up chatter client to send and receive valid messages to our server
		// this must be a user and password in our database, or the connection will be rejected!
		clientTerminal = new IMConnection("127.0.0.1", 4544, "tim", "pass321");
		clientTerminal.connect("Login");
	} 

	@AfterEach
	public final void tearDown() {
		try {
			if(clientTerminal.connectionActive()){ 
				//clientTerminal.sendDeleteMessage();
				clientTerminal.disconnect(); }
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testConstructor() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			assertNotEquals(null, runnable);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	void testRunWithoutMessage() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			runnable.run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Private Message Test
	 */
	@Test
	void testRunMessage() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			clientTerminal.sendPrivateMessage("Tanmay", "come online");
			clientTerminal.sendRecallMessage("come online");
			IMConnection clientTerminal2 = new IMConnection("127.0.0.1", 4544, "Tanmay", "password");
			clientTerminal2.connect("Login");
			clientTerminal.sendPrivateMessage("Tanmay", "hey u're online");
			clientTerminal.sendPrivateMessage("Tanmay", "Prattle says everyone log off");
			clientTerminal.disconnect();
			clientTerminal2.disconnect();
			try {
				for (int i = 0; i < 10; i++) {
					runnable.run();
				}
			}catch(NullPointerException e){
				// expect to get a null pointer exception when terminating a connection!
				// because we never added this ClientRunnable to Prattle.active
			}catch (Exception e){
				e.printStackTrace();
				fail("unexpected exception type, should only throw null pointer exception");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Broadcast Message Test
	 */
	@Test
	void testRunBroadcastMessage() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			clientTerminal.sendMessage("Hello");
			clientTerminal.sendMessage("hello");
			clientTerminal.sendMessage("Prattle says everyone log off");
			clientTerminal.disconnect();
			try {
				for (int i = 0; i < 10; i++) {
					runnable.run();
				}
			}catch(NullPointerException e){
				// expect to get a null pointer exception when terminating a connection!
				// because we never added this ClientRunnable to Prattle.active
			}catch (Exception e){
				e.printStackTrace();
				fail("unexpected exception type, should only throw null pointer exception");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Message history test.
	 */
	@Test
	void testHistoryRequest(){
		SocketChannel socket;
		try{
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);

			// now manually archive some messages
			// (Prattle would archive them automatically, but it's not running here!)
			Message m1 = Message.makePrivateMessage(clientTerminal.getUserName(), "Tanmay", "hi 1");
			Message m2 = Message.makePrivateMessage(clientTerminal.getUserName(), "Tanmay", "hi 2");
			Message m3 = Message.makePrivateMessage(clientTerminal.getUserName(), "Tanmay", "hi 3");
			MessageArchive.archiveMessage(m1, true);
			// sleep so messages are archived with different timestamps
			try{
				TimeUnit.MILLISECONDS.sleep(1000);}catch(Exception e){}
			MessageArchive.archiveMessage(m2, true);
			try{TimeUnit.MILLISECONDS.sleep(1000);}catch(Exception e){}
			MessageArchive.archiveMessage(m3, true);
			// and check that the archive contains the correct messages
			clientTerminal.sendHistoryMessage("Tanmay", "2");
			for (int i = 0; i < 30; i++) {
				runnable.run();
			}
			// Sleep so IMConnection has time to deal with the new messages
			try{TimeUnit.MILLISECONDS.sleep(500);}catch(Exception e){}
			MessageScanner ms = clientTerminal.getMessageScanner();
			int matches = 0;
			while(ms.hasNext()){
				String nextText = ms.next().getText();
				if (nextText.contains(m3.getText()) || nextText.contains(m2.getText())) {
					matches++;
				}
			}
			assertEquals(2, matches);
			clientTerminal.disconnect();
			try{runnable.run();}
			catch(NullPointerException e) {
				// expect to get a null pointer exception when terminating a connection!
				// because we never added this ClientRunnable to Prattle.active)
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}



	/**
	 * ADD , Update , Delete Group Message Test
	 */
	@Test
	void testRunGroupMessage() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			clientTerminal.sendMessage("Hello");
			clientTerminal.sendMessage("hello");
			clientTerminal.sendGroupAddMessage("test1", "avik,tanmay,cole");
			clientTerminal.sendGroupAddMessage("test1", "avik,tanmay,cole");
			clientTerminal.sendGroupUpdateMessage("test1", "sarah");
			clientTerminal.sendGroupMessage("test1", "hello");
			clientTerminal.sendGroupDeleteMessage("test1");
			clientTerminal.sendGroupDeleteMessage("test1");
			clientTerminal.sendGroupUpdateMessage("test1", "sarah");
			clientTerminal.sendGroupMessage("team", "hello");
			clientTerminal.sendGroupMessage("team", "Prattle says everyone log off");
			clientTerminal.disconnect();
			try {
				for (int i = 0; i < 10; i++) {
					runnable.run();
				}
			}catch(NullPointerException e){
				// expect to get a null pointer exception when terminating a connection!
				// because we never added this ClientRunnable to Prattle.active
			}catch (Exception e){
				e.printStackTrace();
				fail("unexpected exception type, should only throw null pointer exception");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testName() {
		SocketChannel socket;
		try {
			SocketNB nb = new SocketNB("127.0.0.1", 4544);
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			runnable.setName("test");
			assertTrue(runnable.getName().equals("test"));
			nb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	void testUserId() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			assertTrue(runnable.getUserId() == 0);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	void testEnqueueMessage() {
		SocketChannel socket;
		try {
			socket = serverSocket.accept();
			ClientRunnable runnable = new ClientRunnable(socket);
			Message message = Message.makeBroadcastMessage("test", "hi");
			runnable.enqueueMessage(message);
			runnable.run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	void testTerminateClient(){
		ClientRunnable runnable = null;
		try {
			SocketChannel socket = serverSocket.accept();
			runnable = new ClientRunnable(socket);
			runnable.run();
			runnable.terminateClient();
		}catch(NullPointerException e) {
			// since we did not add this thread to Prattle.active,
			// expect this to throw a null pointer exceptions when
			// Prattle.removeClient() is called
		}catch (Exception e){
			// if we get a different exception, fail
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	void testGetReceiver(){
		ClientRunnable runnable = null;
		try {
			SocketChannel socket = serverSocket.accept();
			runnable = new ClientRunnable(socket);
			
			assert(ClientRunnable.getReceiver("avik").equals("avik"));
			
		}catch(NullPointerException e) {
			// since we did not add this thread to Prattle.active,
			// expect this to throw a null pointer exceptions when
			// Prattle.removeClient() is called
		}catch (Exception e){
			// if we get a different exception, fail
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	void testGetMessage(){
		ClientRunnable runnable = null;
		try {
			SocketChannel socket = serverSocket.accept();
			runnable = new ClientRunnable(socket);
			assert(ClientRunnable.getMessage("avik hello").trim().equalsIgnoreCase("hello"));
			
		}catch(NullPointerException e) {
			// since we did not add this thread to Prattle.active,
			// expect this to throw a null pointer exceptions when
			// Prattle.removeClient() is called
		}catch (Exception e){
			// if we get a different exception, fail
			e.printStackTrace();
			fail();
		}
	}

}
