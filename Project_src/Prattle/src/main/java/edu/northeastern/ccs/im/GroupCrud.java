package edu.northeastern.ccs.im;

import java.util.List;

public interface GroupCrud {

	/**
	 * writes the group data to database group table
	 * 
	 * @param group
	 */
	void addGroup(Group group);

	/**
	 * Checks if the group exists
	 * 
	 * @param group
	 * @return true if group is present
	 */
	Boolean getGroup(Group group);

	/**
	 * updates the members in a group
	 * 
	 * @param group
	 */
	void updateGroup(Group group);

	/**
	 * delete the group 
	 * 
	 * @param group
	 */
	void deleteGroup(Group group);
	
	/**
	 * return list of users in a group
	 * 
	 * @param group
	 */	
	List<User> getUsers(Group group);
}
