package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beans.User;

/**
 *  DAO to access user information on the DB
 */
public class UserDAO {
	
	private Connection connection;

	/**
	 * Initialize DAO for the given DB connection
	 */
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Register the user with the given credentials to the DB
	 * Will throw an exception when called with an email already in the DB, hence it assumes that is checked before
	 */
	public void registerUser(String name, String email, String password) throws SQLException {
		
		String action = "Registering the user with name:" + name + " email: " + email + "password: " + password;
		String query = "INSERT INTO user (name,email,password) VALUES(?,?,?)";
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, email);
			preparedStatement.setString(3, password);
			preparedStatement.executeUpdate();
		}catch(SQLException e) {
			throw new SQLException("DB ACCESS ERROR. Action: " + action);
		}
	}
	
	/**
	 * Check if the email corresponds to a registered user.
	 * Returns the user if found, or null if it does not exist.
	 */
	public User getUserByEmail(String email) throws SQLException {
		
		User user = null;
		String action = "Getting user with email: " + email;
		String query = "SELECT * FROM user WHERE email = ?";
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setString(1, email);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt("id"));
				user.setName(resultSet.getString("name"));
				user.setEmail(resultSet.getString("email"));
			}
		}catch(SQLException e) {
			throw new SQLException("DB ACCESS ERROR. Action: " + action);
		}
		return user;
	}

	/**
	 * Check if the email and password correspond to a registered user.
	 * Returns the user if found, or null if it does not exist.
	 */
	public User checkCredentials(String email, String password) throws SQLException {

		String action = "Checking credentials. email: " + email + " password: " + password;
		String query = "SELECT * FROM user WHERE email = ? AND password = ?";
		User user = null;
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setString(1, email);
			preparedStatement.setString(2, password);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next()) {
				user = new User();
				user.setId(resultSet.getInt("id"));
				user.setName(resultSet.getString("name"));
				user.setEmail(resultSet.getString("email"));
			}
		}catch(SQLException e) {
			throw new SQLException("DB ACCESS ERROR. Action: " + action);
		}
		return user;
	}
	
}
