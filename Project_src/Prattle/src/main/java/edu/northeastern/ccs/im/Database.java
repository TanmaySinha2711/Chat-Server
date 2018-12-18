package edu.northeastern.ccs.im;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import java.util.Properties;

public class Database {
	private static final Logger LOGGER = LogManager
			.getLogger(Database.class.getName());
	
	private static final String PROPERTY_FILE="/database.properties";


	private Database() {

	}
	
	public static Connection getConnection() {
		Connection con = null;
		InputStream inputStream =null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Properties prop = new Properties();
			inputStream = Connection.class.getResourceAsStream(PROPERTY_FILE);
			prop.load(inputStream);
			// get the property value and print it out
			String user = prop.getProperty("user");
			String password = prop.getProperty("password");
			String db = prop.getProperty("db");
			String port = prop.getProperty("port");
			String hostname = prop.getProperty("hostname");
			String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/"
					+ db + "?user=" + user + "&password=" + password;
			con = DriverManager.getConnection(jdbcUrl);
		} catch (SQLException | ClassNotFoundException | IOException  e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		}finally {
			if(inputStream!=null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOGGER.log(Level.WARN, e.toString(), e);
				}
			}
		}
		return con;
	}

	public static void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
		}

	}
}
