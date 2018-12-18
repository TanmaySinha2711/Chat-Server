package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.chatter.IMConnection;

class ClientRunnableTestQueueMessages {

    @Test
	void testRun() {

		try {
            ServerSocketChannel  serverSocket;
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress(6001));
            IMConnection clientTerminal= new IMConnection("127.0.0.1", 6001, "avik", "password");
            
            clientTerminal.connect("Login");
            
            ClientRunnable runnable = new ClientRunnable(serverSocket.accept());
            
            /*clientTerminal.sendPrivateMessage("Tanmay", "come online");*/
            //clientTerminal.sendRecallMessage("come online");
            
            
            
            clientTerminal.sendPrivateMessage("cole", "test pvt queuing");
            clientTerminal.sendGroupMessage("team", "test grp queuing");
            
            /*ServerSocketChannel  serverSocket1;
            serverSocket1 = ServerSocketChannel.open();
            serverSocket1.configureBlocking(false);
            serverSocket1.socket().bind(new InetSocketAddress(6001));*/
            IMConnection clientTerminal2= new IMConnection("127.0.0.1", 6001, "cole", "password");
            clientTerminal2.connect("Login");
            
            ClientRunnable runnable1 = new ClientRunnable(serverSocket.accept());
            clientTerminal2.sendPrivateMessage("avik", "yo i m online");   
            try {
                for (int i = 0; i < 10; i++) {
                    runnable.run();
                    runnable1.run();
                }                
                clientTerminal2.disconnect();
                clientTerminal.disconnect(); 
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

}
