package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for custom exception used in Prattle
 * @author Sarah Lichtman
 */
class NextDoesNotExistExceptionTest {

    @Test
    void testException(){
        // test construction of custom exception
        // test syntax drawn from junit5 doc user guide
        String testMsg = "this is a message";
        Throwable except = assertThrows(NextDoesNotExistException.class,
                () -> {
                    throw new NextDoesNotExistException(testMsg);
                });
        assertEquals(testMsg, except.getMessage());

        // ensure it also works with empty string
        except = assertThrows(NextDoesNotExistException.class,
                () -> {
                    throw new NextDoesNotExistException(null);
                });
        assertNull(except.getMessage());

    }
}