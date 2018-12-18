package edu.northeastern.ccs.im;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface UserCrud {

	/**
	 * writes the user data to database user table
	 * 
	 * @param user
	 */
	void addUser(User user);

	/**
	 * Checks if the user exists
	 * 
	 * @param user
	 * @return true if user is present
	 */
	Boolean getUser(User user);

	/**
	 * updates the password of the user specified
	 * 
	 * @param user
	 */
	void updateUser(User user);

	/**
	 * delete user 
	 * 
	 * @param user
	 */
	void deleteUser(User user);

	/** fetches all the names of users registered
	 * @return A list of string containing all usernames
	 */
	List<String> getAllUsernames();
	/**
	 * Switch parental control from on to off or from off to on.
	 *
	 * @param user user
	 * @param newStatus on or off
	 * @return true if the change was a success, false otherwise
	 */
	boolean changeParentalControl(User user, String newStatus);

	/**
	 * Determines if the given user has parental controls on or off.
	 *
	 * @param srcName user to check
	 * @return true if parental controls are set to "on", false otherwise
	 */
	static boolean isParentalControl(String srcName, String recipientName) {
		final Logger LOGGER = Logger.getLogger(UserCrud.class.getName());
		String fieldname = "parentalcontrol";
		String statusOn = "on";

		if (srcName == null) {
			return false;
		}

		String parentalControlSender = "";
		String parentalControlReceiver = "";
		String sql = "SELECT fieldname FROM Users WHERE username=? OR username=?;";
		sql = sql.replaceFirst("fieldname", fieldname);
		ResultSet rs = null;
		try (Connection dbConn = Database.getConnection();
				 PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
			pstmt.setString(1, srcName);
			pstmt.setString(2, recipientName);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				parentalControlSender = rs.getString(fieldname);
			}
			if (rs.next()) {
				parentalControlReceiver = rs.getString(fieldname);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.toString(), e);
			}
		}


		return (parentalControlSender != null && parentalControlSender.equalsIgnoreCase(statusOn)) ||
						(parentalControlReceiver != null && parentalControlReceiver.equalsIgnoreCase(statusOn));
	}
	
}
