package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the database-based user create, read, update, delete functions
 * @author Sarah Lichtman
 */
class UserCrudImplTest {

    @Test
    void testAllCrud(){
        // we'll put all the tests in one method since the order of operations matters

        // set up test fixtures
        UserCrud sut = new UserCrudImpl();
        User testUserValid = new User();
        testUserValid.setUsername("testusername");
        testUserValid.setPassword("testpassword");
        User testUserInvalid = new User();
        testUserInvalid.setUsername("me");
        testUserInvalid.setPassword("");

        // add a user, then ensure we can get it
        // useless comment 2
        sut.addUser(testUserValid); 
        assertTrue(sut.getUser(testUserValid));
        // ensure we can't get a user that's not there
        assertFalse(sut.getUser(testUserInvalid));

        // update the user's password and ensure we can get it
        String newPswd = "betterPassword";
        User updatedUser = new User();
        updatedUser.setUsername(testUserValid.getUsername());
        updatedUser.setPassword(newPswd);
        sut.updateUser(updatedUser);
        assertTrue(sut.getUser(updatedUser));
        // also ensure we can't get it with the old password
        assertFalse(sut.getUser(testUserValid));

        // try to update a user that's not in the table
        // (shouldn't have any effect)
        assertDoesNotThrow(()->{sut.updateUser(testUserInvalid);});

        // Delete the user we added and ensure it's not there
        sut.deleteUser(updatedUser);
        assertFalse(sut.getUser(updatedUser));

        // Also try deleting as user that's not there
        // (shouldn't do any harm)
        assertDoesNotThrow(() -> {sut.deleteUser(testUserInvalid);});
        
        // retrieve all users from database
        assertTrue(sut.getAllUsernames().size() != 0);

    }

}