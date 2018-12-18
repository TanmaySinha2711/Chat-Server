package edu.northeastern.ccs.im;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

public class GroupCrudImpl implements GroupCrud {

	private static final Logger LOGGER = LogManager
			.getLogger(GroupCrudImpl.class.getName());

	@Override
	public void addGroup(Group group) {

		String sql = "INSERT INTO Groups(groupname) VALUES(?)";
		try (Connection conn = Database.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql);) {

			statement.setString(1, group.getGroupName());
			statement.executeUpdate();
		} catch (SQLException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		}

		updateGroup(group);
	}

	@Override
	public Boolean getGroup(Group group) {
		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {

			con = Database.getConnection();

			stmt = con.createStatement();

			String sql;
			sql = "SELECT groupname FROM Groups";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String name = rs.getString("groupname");

				if (group.getGroupName().equalsIgnoreCase(name)) {

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
	public void updateGroup(Group group) {
		for (int i = 0; i < group.getUsers().size(); i++) {
			String sql = "INSERT INTO Group_membership(user_id,group_id) VALUES" +
					"((SELECT id FROM Users WHERE username = ? LIMIT 1), (SELECT id FROM Groups WHERE groupname = ? LIMIT 1))";
			try (Connection conn = Database.getConnection();
				 PreparedStatement statement = conn.prepareStatement(sql)) {
				statement.setString(1, group.getUsers().get(i).getUsername());
				statement.setString(2, group.getGroupName());
				statement.executeUpdate();
			} catch (SQLException e) {
				LOGGER.log(Level.WARN, e.toString(), e);
			}
		}
	}

	@Override
	public void deleteGroup(Group group) {
		// delete all Messages entries related to this group
		String sqlMessages = "DELETE FROM Messages WHERE to_group_id=(SELECT id FROM Groups WHERE groupname=?);";
		try(Connection conn = Database.getConnection();
		PreparedStatement pstmt = conn.prepareStatement(sqlMessages)){
			pstmt.setString(1, group.getGroupName());
			pstmt.executeUpdate();
		}catch (SQLException e){
			LOGGER.log(Level.WARN, e.toString(), e);
		}

		String sqlGroup = "DELETE FROM Groups WHERE groupname = ?";
		String sqlGroupMember = "DELETE FROM Group_membership WHERE group_id = ?";
		String sqlGroupId = "SELECT id FROM Groups WHERE groupname = ?";
		ResultSet groupId = null;
		try (Connection conn = Database.getConnection();
				PreparedStatement statement = conn.prepareStatement(sqlGroupId);
				PreparedStatement statement1 = conn.prepareStatement(sqlGroup);
				PreparedStatement statement2 = conn
						.prepareStatement(sqlGroupMember);) {

			statement.setString(1, group.getGroupName());
			groupId = statement.executeQuery();

			while (groupId.next()) {
				int id = groupId.getInt("id");
				statement2.setInt(1, id);
				statement2.executeUpdate();
			}
			
			statement1.setString(1, group.getGroupName());
			statement1.executeUpdate();

		} catch (SQLException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		} finally {
			if (groupId != null) {
				try {
					groupId.close();
				} catch (SQLException e) {
					LOGGER.log(Level.WARN, e.toString(), e);
				}
			}

		}

	}

	@Override
	public List<User> getUsers(Group group) {
		List<User> userList = new ArrayList<>();
		String sql = "SELECT username FROM Users WHERE id IN "
			+ "(SELECT user_id FROM Group_membership WHERE group_id = "
			+ "(SELECT id FROM Groups WHERE groupname = ?))";
		ResultSet usr = null;
		try (Connection conn = Database.getConnection();
				PreparedStatement statement = conn.prepareStatement(sql);){
			statement.setString(1, group.getGroupName());
			usr = statement.executeQuery();
			while (usr.next()) {
				String name = usr.getString("username");
				User obj = new User ();
				obj.setUsername(name);
				userList.add(obj);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		} finally {
			if (usr != null) {
				try {
					usr.close();
				} catch (SQLException e) {
					LOGGER.log(Level.WARN, e.toString(), e);
				}
			}
		}
		return userList;
	}
	

}
