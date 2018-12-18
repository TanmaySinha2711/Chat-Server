package edu.northeastern.ccs.im;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the database-based group create, read, update, delete functions
 * @author Sarah Lichtman
 */
class GroupCrudImplTest {

    @Test
    void testAllCrud(){
        // we'll put all the tests in one method since the order of operations matters


        // set up test fixtures
        UserCrud uc = new UserCrudImpl();
        User u1 = new User(); u1.setUsername("u1"); u1.setPassword("testpassword");
        User u2 = new User(); u2.setUsername("u2"); u2.setPassword("blah");
        User u3 = new User(); u3.setUsername("u3"); u3.setPassword("password");
        List<User> ulist = new ArrayList<>(); ulist.add(u1); ulist.add(u2); ulist.add(u3);
        uc.addUser(u1); uc.addUser(u2); uc.addUser(u3);

        // test getGroup
        GroupCrud sut = new GroupCrudImpl();
        Group g = new Group(); g.setGroupName("us"); g.setUsers(new ArrayList<>());
        Group invalid = new Group(); invalid.setGroupName("not_here"); invalid.setUsers(new ArrayList<>());
        sut.addGroup(g);
        assertTrue(sut.getGroup(g));
        assertFalse(sut.getGroup(invalid));

        // test updateGroup
        Group updatedGroup = new Group(); updatedGroup.setGroupName(g.getGroupName());
        updatedGroup.setUsers(ulist.subList(0,2));
        sut.updateGroup(updatedGroup);
        assertTrue(sut.getGroup(updatedGroup));

        // test getUsers
        List<User> dbUsers = sut.getUsers(updatedGroup);
        for(int i=0; i<updatedGroup.getUsers().size(); i++){
            assertEquals(dbUsers.get(i).getUsername(), updatedGroup.getUsers().get(i).getUsername());
        }
        assertEquals(0, sut.getUsers(invalid).size());

        // test deleteGroup
        sut.deleteGroup(updatedGroup);
        assertFalse(sut.getGroup(updatedGroup));
        assertDoesNotThrow(()->sut.deleteGroup(invalid));

        // clean up by removing our test users from the database
        uc.deleteUser(u1); uc.deleteUser(u2); uc.deleteUser(u3);


    }

}