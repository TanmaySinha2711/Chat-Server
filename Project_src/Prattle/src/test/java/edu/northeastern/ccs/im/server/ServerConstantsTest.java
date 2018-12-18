package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.Message;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sarah Lichtman
 */
class ServerConstantsTest {

    @Test
    void getBroadcastResponses() {
        // send invalid input (get no messages back)
        assertNull(ServerConstants.getBroadcastResponses(""));

        // ask for the date, should get single broadcast message
        List<Message> result;
        result = ServerConstants.getBroadcastResponses(ServerConstants.DATE_COMMAND.toUpperCase());
        assertEquals(1, result.size());
        assertTrue(result.get(0).isBroadcastMessage());

        // ask for the time, should get single broadcast message
        result = ServerConstants.getBroadcastResponses(ServerConstants.TIME_COMMAND.toUpperCase());
        assertEquals(1, result.size());
        assertTrue(result.get(0).isBroadcastMessage());

        // Ask what time is it Mr Fox, should get two broadcast messages
        result = ServerConstants.getBroadcastResponses(ServerConstants.IMPATIENT_COMMAND.toUpperCase());
        assertEquals(2, result.size());
        assertTrue(result.get(0).isBroadcastMessage());
        assertTrue(result.get(1).isBroadcastMessage());

        // Note: these messages must match constant string literal capitalization, unlike the previous tests
        // send cool command, should get one broadcast message
        result = ServerConstants.getBroadcastResponses(ServerConstants.COOL_COMMAND);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isBroadcastMessage());

        // send Query command, should get two broadcast messages
        result = ServerConstants.getBroadcastResponses(ServerConstants.QUERY_COMMAND);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isBroadcastMessage());
        assertTrue(result.get(1).isBroadcastMessage());
    }
}