package edu.northeastern.ccs.im;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import java.util.ArrayList;
import java.util.List;



public class UserCrudImpl implements UserCrud {

	private String encryptionKey = "SecretString";

	private static final Logger LOGGER = LogManager
			.getLogger(UserCrudImpl.class.getName());

	@Override
	public void addUser(User user) {

		String sql = "INSERT INTO Users(username,password) " + "VALUES(?,AES_ENCRYPT(?,?))";
		try (Connection conn = Database.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql);) {

			statement.setString(1, user.getUsername());
			statement.setString(2, user.getPassword());
			statement.setString(3, this.encryptionKey);
			statement.executeUpdate();
		} catch (SQLException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		}

	}

	@Override
	public Boolean getUser(User user) {

		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {

			con = Database.getConnection();

			stmt = con.createStatement();

			String sql;
			sql = "SELECT username, AES_DECRYPT(password,'"+this.encryptionKey+"') AS password FROM Users";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String name = rs.getString("username");
				String userPassword = rs.getString("password");

				if (user.getUsername().equalsIgnoreCase(name)
						&& user.getPassword().equals(userPassword)) {
					return true;
				}
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LOGGER.log(Level.WARN, e.toString(), e);
			}
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				LOGGER.log(Level.WARN, e.toString(), e);
			}
			if (con != null) {
				Database.closeConnection(con);
			}
		} // end try

		return false;
	}

	@Override
	public void updateUser(User user) {

		String sql = "UPDATE Users "
                + "SET password = AES_ENCRYPT(?,?) "
                + "WHERE username = ?";
 
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, this.encryptionKey);
            pstmt.setString(3, user.getUsername());
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
        	LOGGER.log(Level.WARN, e.toString(), e);
        }
	}

	@Override
	public void deleteUser(User user) {
		// delete all Messages entries related to this user
		String sqlMessages = "DELETE FROM Messages WHERE to_group_id=(SELECT id FROM Users WHERE username=?);";
		try(Connection conn = Database.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sqlMessages)){
			pstmt.setString(1, user.getUsername());
			pstmt.executeUpdate();
		}catch (SQLException e){
			LOGGER.log(Level.WARN, e.toString(), e);
		}
		
		String sql = "DELETE FROM Users WHERE username = ?";
 
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setString(1, user.getUsername());
            pstmt.executeUpdate();
 
        } catch (SQLException e) {
        	LOGGER.log(Level.WARN, e.toString(), e);
        }
	}
	
	/* (non-Javadoc)
	 * @see edu.northeastern.ccs.im.UserCrud#getAllUsernames()
	 */
	public List<String> getAllUsernames(){
		List<String> usernames = new ArrayList<>();
		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {

			con = Database.getConnection();

			stmt = con.createStatement();

			String sql;
			sql = "SELECT username FROM Users";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String name = rs.getString("username");
				usernames.add(name);
			}

		} catch (SQLException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LOGGER.log(Level.WARN, e.toString(), e);
			}
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				LOGGER.log(Level.WARN, e.toString(), e);
			}
			if (con != null) {
				Database.closeConnection(con);
			}
		} // end try

		return usernames;
	}

	@Override
	public boolean changeParentalControl(User user, String newStatus) {
		if (newStatus.equalsIgnoreCase("on") || newStatus.equalsIgnoreCase("off")) {
			String sqlMessages = "UPDATE Users "
							+ "SET parentalcontrol=?"
							+ "WHERE username = ?";
			try (Connection conn = Database.getConnection();
					 PreparedStatement pstmt = conn.prepareStatement(sqlMessages)) {
				pstmt.setString(1, newStatus);
				pstmt.setString(2, user.getUsername());
				pstmt.executeUpdate();
			} catch (SQLException e) {
				LOGGER.log(Level.WARN, e.toString(), e);
			}
			return true;
		} else {
			return false;
		}
	}
}
