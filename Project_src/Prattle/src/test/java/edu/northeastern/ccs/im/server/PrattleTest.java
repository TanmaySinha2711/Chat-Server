package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.chatter.IMConnection;
import edu.northeastern.ccs.im.chatter.MessageScanner;
import org.junit.jupiter.api.*;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sarah Lichtman
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PrattleTest {

    ConcurrentLinkedQueue<ClientRunnable> active;
    ServerSocketChannel serverSocket;
    List<IMConnection> clientTerminals;

    int serverPort = 6005;
    int numThreads = 1;


    @SuppressWarnings("unchecked")
	@BeforeEach
    void setup(){
        // set up server
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(serverPort));
        } catch (IOException e) {
            e.printStackTrace();
            fail("couldn't set up local server");
        }

        // use reflection to access the private list of threads for the class-under-test
        try {
            Field f = Prattle.class.getDeclaredField("active");
            f.setAccessible(true);
            active = (ConcurrentLinkedQueue<ClientRunnable>) f.get(null);
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }


        // set up server-side sockets to clients, each in own thread
        //clientSockets = new ArrayList<>();
        //clientSocketReaders = new ArrayList<>();
        clientTerminals = new ArrayList<>();
        try {
            for(int i=0; i<numThreads; i++) {
                setupClient(this.serverPort);

                // tell the server to accept this latest client request
                SocketChannel socket = serverSocket.accept();
                ClientRunnable runnable = new ClientRunnable(socket);
                runnable.run();
                active.add(runnable);

            }
        }catch (Exception e){
            fail("couldn't get server to accept clients");
        }

    }

    void setupClient(int serverPort){
        // the username and password must be in our database for this to work!
        IMConnection clientTerminal = new IMConnection("127.0.0.1", serverPort, "avik", "password");
        clientTerminal.connect("Login");
        clientTerminals.add(clientTerminal);
    }

    @AfterEach
    void teardown(){
        try{
            for(IMConnection t : clientTerminals){
                if(t.connectionActive()) {
                    t.disconnect();
                }
            };
        }catch(Exception e){
            e.printStackTrace();
        }
        for(ClientRunnable a : active){
            Prattle.removeClient(a);
        }
        try{
            serverSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    void addRemoveIPMap() {
        // use reflection to look at the IP map after changing it
        try {
            Field f = Prattle.class.getDeclaredField("user_ip_map");
            f.setAccessible(true);
            // remove the user we set up in BeforeEach
            Prattle.removeFromIPMap("avik");

            Prattle.addIPtoMap("me", "127.0.0.1");
            HashMap<String, String> res = (HashMap<String, String>) f.get(null);
            assertEquals(1, res.size());
            assertEquals("127.0.0.1", res.get("me"));

            Prattle.addIPtoMap("you", "127.0.0.1");
            assertEquals(2, res.size());

            Prattle.removeFromIPMap("you");
            assertEquals(1, res.size());
            assertFalse(res.containsKey("you"));
            assertTrue(res.containsKey("me"));

            Prattle.removeFromIPMap("someone");
            assertEquals(1, res.size());

            Prattle.addIPtoMap("me", "127.4.4.4");
            assertEquals(1, res.size());
            assertEquals("127.4.4.4", res.get("me"));

            Prattle.removeFromIPMap("me");
            assertEquals(0, res.size());

        }catch(Exception e){
            e.printStackTrace();
            fail("couldn't access Prattle ip map for testing");
        }

    }

    @Test
    void broadcastMessage() {
        Message testMsg = Message.makeBroadcastMessage("god", "hello all");
        Prattle.broadcastMessage(testMsg);
        // ensure all the client sockets received this!
        try {
            int cntr = 0;
            Iterator threaditerator = active.iterator();
            while(threaditerator.hasNext() && cntr < numThreads) {
                MessageScanner ms = clientTerminals.get(cntr).getMessageScanner();
                ClientRunnable thisthread = (ClientRunnable) threaditerator.next();
                thisthread.run(); thisthread.run();
                // sleep a little to let the messageScanner deal with the messages
                try{
                    TimeUnit.MILLISECONDS.sleep(400);}catch (Exception e){e.printStackTrace();}
                int matches = 0;
                while(ms.hasNext()){
                    if (ms.next().getText().equals(testMsg.getText())){
                        matches++;
                    }
                }
                assertEquals(1, matches);
                cntr++;
            }
        } catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void directMessage() {
        Message testMsg = Message.makePrivateMessage("cole", "avik", "hi avik");
        Prattle.directMessage(testMsg, testMsg.getRecipientName(),false);
        // ensure all the client sockets received this!
        try {
            int cntr = 0;
            Iterator threaditerator = active.iterator();
            while(threaditerator.hasNext() && cntr < numThreads) {
                MessageScanner ms = clientTerminals.get(cntr).getMessageScanner();
                ClientRunnable thisthread = (ClientRunnable) threaditerator.next();
                thisthread.run(); thisthread.run();
                // sleep a little to let the messageScanner deal with the messages
                try{TimeUnit.MILLISECONDS.sleep(400);}catch (Exception e){e.printStackTrace();}
                int matches = 0;
                while(ms.hasNext()){
                    if (ms.next().getText().equals(testMsg.getText())){
                        matches++;
                    }
                }
                assertEquals(1, matches);
                cntr++;
            }
        } catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void checkUser() {
    	List<String> list1 = new ArrayList<>();
    	List<String> list2 = new ArrayList<>();
    	List<String> list3 = new ArrayList<>();
    	
    	list1.add("avik");
    	list2.add("avik");
    	list3.add("tanmay");
    	
    	assertTrue(Prattle.checkUser(list1, list2));
    	assertFalse(Prattle.checkUser(list1, list3));
    }
    @Test
    void groupMessage() {
        // note: this test will only work if both Cole and Avik are in the "team" group in our database!
        Message testMsg = Message.makeGroupMessage("Cole", "team", "hi test users");
        Prattle.groupMessage(testMsg, "Cole", testMsg.getRecipientName(),null,null);
        // ensure all the client sockets received this!
        // TODO: solve duplicate threads setup issue in this test class
        /*try {
            int cntr = 0;
            Iterator threaditerator = active.iterator();
            while(threaditerator.hasNext() && cntr < numThreads) {
                MessageScanner ms = clientTerminals.get(cntr).getMessageScanner();
                ClientRunnable thisthread = (ClientRunnable) threaditerator.next();
                thisthread.run(); thisthread.run();
                // sleep a little to let the messageScanner deal with the messages
                try{Thread.sleep(500);}catch (Exception e){e.printStackTrace();}
                int matches = 0;
                while(ms.hasNext()){
                    if (ms.next().getText().equals(testMsg.getText())){
                        matches++;
                    }
                }
                assertEquals(1, matches);
                cntr++;
            }
        } catch (Exception e){
            e.printStackTrace();
            fail();
        }
        */
    }
    
    @Test
    void sendQueuedMessage() {
        // note: this test will only work if both Cole and Avik are in the "team" group in our database!
    	Message testMsg = Message.makePrivateMessage("cole", "avik", "hi avik");
        Prattle.sendQueuedMessage(testMsg, testMsg.getRecipientName());
        // ensure all the client sockets received this!
        try {
            int cntr = 0;
            Iterator threaditerator = active.iterator();
            while(threaditerator.hasNext() && cntr < numThreads) {
                MessageScanner ms = clientTerminals.get(cntr).getMessageScanner();
                ClientRunnable thisthread = (ClientRunnable) threaditerator.next();
                thisthread.run(); thisthread.run();
                // sleep a little to let the messageScanner deal with the messages
                try{Thread.sleep(500);}catch (Exception e){e.printStackTrace();}
                int matches = 0;
                while(ms.hasNext()){
                    if (ms.next().getText().equals(testMsg.getText())){
                        matches++;
                    }
                }
                assertEquals(1, matches);
                cntr++;
            }
        } catch (Exception e){
            e.printStackTrace();
            fail();
        }

    }


    @Test
    void removeClient() {
        ClientRunnable[] crarr = new ClientRunnable[active.size()];
        active.toArray(crarr);
        // should not except when removing an existing thread
        assertDoesNotThrow(()->Prattle.removeClient(crarr[0]));
        // should not except when trying to remove thread that's not there
        assertDoesNotThrow(()->Prattle.removeClient(crarr[0]));
    }
}
