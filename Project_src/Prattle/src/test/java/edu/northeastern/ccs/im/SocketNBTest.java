package edu.northeastern.ccs.im;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Sarah Lichtman
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SocketNBTest {
    int workingPort = 4547;
    ServerSocket serverSocket;
    private static final Logger LOGGER = Logger.getLogger(SocketNBTest.class.getName());

    @BeforeAll
    void createDummyListener(){
        try {
            this.serverSocket = new ServerSocket(workingPort);
        }catch(Exception e){
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
        }
    }

    @Test
    void constructors(){
        // test invalid inputs: negative ports, bad IP addresses
        // note that empty string is a valid hostname!
        Executable constr = () -> new SocketNB("127.0.0.1", -1);
        assertThrows(Exception.class, constr);
        constr = () -> new SocketNB("127.0.0.0.0.0", workingPort);
        assertThrows(Exception.class, constr);

        // Test a connection that cannot complete because the host does not exist
        constr = () -> new SocketNB("randomName123", workingPort);
        assertThrows(Exception.class, constr);

        // Note: could not find a way to reach inside the IOException if statement,
        // may be an exception that will only occur e.g: during a host disconnect
        // that we are unable to emulate in tests

        // Test with valid inputs (localhost should always work)
        constr = () -> {
            SocketNB sut = new SocketNB("127.0.0.1", workingPort);
            assertNotNull(sut.getSocket());
            sut.close();
        };
        assertDoesNotThrow(constr);
    }

    @Test
    void getSocket() {
        // test correct object through reflection
        try {
            Field channelField = SocketNB.class.getDeclaredField("channel");
            channelField.setAccessible(true);

            SocketNB sut = new SocketNB("127.0.0.1", workingPort);
            assertTrue(channelField.get(sut) == sut.getSocket());
            sut.close();
        }catch (Exception e){
        	LOGGER.log(Level.SEVERE, e.toString(), e);
            fail();
        }
    }

    @Test
    void close() {
        Executable closer = () -> {
            SocketNB sut = new SocketNB("127.0.0.1", workingPort);
            sut.close();
        };
        assertDoesNotThrow(closer);
    }
}
