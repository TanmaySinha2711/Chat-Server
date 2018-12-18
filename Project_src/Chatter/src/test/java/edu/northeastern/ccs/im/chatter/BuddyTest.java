package edu.northeastern.ccs.im.chatter;

import static org.junit.jupiter.api.Assertions.*;

import edu.northeastern.ccs.im.chatter.Buddy;
import org.junit.jupiter.api.Test;

/** class to test for Buddy class
 * @author Tanmay
 *
 */
class BuddyTest {

	@Test
	void testGetBuddy() {
		//test valid buddy
		Buddy testBuddy = Buddy.getBuddy("test bud");
		//null buddy
		Buddy testBuddy2 = Buddy.getBuddy(null);
		
		//check if user is created in the instance map
		assertTrue(testBuddy.equals(Buddy.getInstanceMap().get("test bud")), 
				"Buddy not present in instance map");
		assertNull(testBuddy2, "if name is not provided then buddy is null");
		
	}

	@Test
	void testGetEmptyBuddy() {
		//test valid buddy
		Buddy testBuddy = Buddy.getEmptyBuddy("test bud2");
		//null buddy
		Buddy testBuddy2 = Buddy.getEmptyBuddy(null);
				
		assertNull(Buddy.getInstanceMap().get(testBuddy.getUserName()), 
					"empty Buddy should be created but not put in instance map");
		assertNull(testBuddy2, "if name is not provided then buddy is null");
	}

	@Test
	void testRemoveBuddy() {
		//test empty buddy
		Buddy testBuddy = Buddy.getEmptyBuddy("test bud2");
		//test buddy
		Buddy testBuddy2 = Buddy.getBuddy("test bud");
		
		//remove a buddy not in map
		Buddy.removeBuddy(testBuddy.getUserName());
		
		//remove buddy in map
		Buddy.removeBuddy(testBuddy2.getUserName());
						
		assertNull(Buddy.getInstanceMap().get(testBuddy.getUserName()), 
							"empty Buddy should not exist in map");
		assertNull(Buddy.getInstanceMap().get(testBuddy2.getUserName()),
				"Buddy shoul dbe removed from map");
	}

	@Test
	void testMakeTestBuddy() {
		//test valid buddy
		Buddy testBuddy = Buddy.makeTestBuddy("test bud2");
		//null buddy
		Buddy testBuddy2 = Buddy.makeTestBuddy(null);
						
		assertNull(Buddy.getInstanceMap().get(testBuddy.getUserName()), 
							"test Buddy should be created but not put in instance map");
		assertNull(testBuddy2, "if name is not provided then buddy is null");
	}

	@Test
	void testGetUserName() {
		//test valid buddy
		Buddy testBuddy = Buddy.getBuddy("test bud");
				
		//check if user's name is equal to the user's name in the instance map
		assertTrue(testBuddy.getUserName().
				equals(Buddy.getInstanceMap().get("test bud").getUserName()), 
				"buddy's username should be same as in the map");
	}

}
