package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import beans.Category;

/**
 * DAO to access category information on the DB
 */
public class CategoryDAO {
	
	private Connection connection;

	/**
	 * Initialize DAO for the given DB connection
	 */
	public CategoryDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Get the number of children for a given parent node
	 */
	public int getChildCount(int parentID) throws SQLException {
		
		String action = "Checking if category " + parentID + " exists";
		String queryExists = "SELECT childCount FROM category WHERE id = ?";
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryExists)){
			preparedStatement.setInt(1, parentID);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			// Return the child count. If the parent does not exist or an error occurs, it returns -1
			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		}catch(SQLException e) {
			throw new SQLException("DB ACCESS ERROR. Action: " + action);
		}
		return -1;
	}
	
	/**
	 * Fetch a linearized representation of the category tree.
	 * Since copies do not copy the entire subtree, this search must be recursive.
	 */
	public List<Category> getCategoryTree(int rootID) throws SQLException {
		
		String action = "Fetching category tree.";
		String recursiveCTE = 
				"WITH RECURSIVE tree AS ("
				+ "SELECT id, name, parentID, childCount, CAST(id AS CHAR(500)) AS stack " 					          // base case: add the tree root node (parent id is null)
				+ "FROM category "
				+ "WHERE id = ? " 				
				+ "UNION ALL "
				+ "SELECT child.id, child.name, child.parentID, child.childCount, CONCAT(tree.stack, ' ' , child.id)" // recursive call: look for all children of the current tree
				+ "FROM category child JOIN tree ON child.parentID = tree.id) "; 
		String query = "SELECT * FROM tree ORDER BY stack"; // stack is used to guarantee dfs ordering
		List<Category> tree = new ArrayList<>();
		
		try (PreparedStatement preparedStatement = connection.prepareStatement(recursiveCTE + query)){
			preparedStatement.setInt(1, rootID);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				Category category = new Category();
				category.setId(resultSet.getInt("id"));
				category.setName(resultSet.getString("name"));
				category.setParentID(resultSet.getInt("parentID"));
				category.setChildCount(resultSet.getInt("childCount"));
				category.setGeneration(resultSet.getString("stack").split(" ").length);
				category.setHighlighted(false);
				tree.add(category);
			}
		}catch(SQLException e) {
			throw new SQLException("DB ACCESS ERROR. Action: " + action);
		}
		return tree;
	}
	
	/**
	 * Insert a new category into the DB
	 */
	public void insertCategory(int newID, String name, int parentID) throws SQLException {
		
		String action = "Creating category with id: " + newID + " name: " + name + " parent id: " + parentID;
		String query = "INSERT INTO category (id,name,parentID, childCount) VALUES(?,?,?,0)";
		String update = "UPDATE category SET childCount = childCount + 1 WHERE id = ?";
		
		try (PreparedStatement preparedStatementQuery = connection.prepareStatement(query);
			 PreparedStatement preparedStatementUpdate = connection.prepareStatement(update))
		{
			connection.setAutoCommit(false);
			
			// Insert the new category
			preparedStatementQuery.setInt(1, newID);
			preparedStatementQuery.setString(2, name);
			preparedStatementQuery.setInt(3, parentID);
			preparedStatementQuery.executeUpdate();
			
			// Increment the parent's children count
			preparedStatementUpdate.setInt(1, parentID);
			preparedStatementUpdate.executeUpdate();
			
			connection.commit();
		} catch(SQLException e) {
			connection.rollback();
			throw new SQLException("DB ACCESS ERROR. Action: " + action);
		} finally {
			connection.setAutoCommit(true);
		}
	}
	
	/**
	 * Insert the subtree starting at root as a child to the parent node
	 * Returns the new ID of the copied subtree
	 */
	public int insertSubtree(int rootID, int parentID, int parentChildCount) throws SQLException {
		String action = "Inserting subtree located at root id: " + rootID + " under parent id: " + parentID;
		
		// First fetch the subtree containing the node to copy
		List<Category> subtree = getCategoryTree(rootID);
				
		// Relabel id and parentID for the subtree obtained (inefficient, but it's ok)
		for (int i = 0; i < subtree.size(); ++i) {
			Category parent = subtree.get(i);
			int newID;
			
			// Root depends on where it's being copied, the rest rely on their previous ID's last digit
			if ( i == 0 ) {
				parent.setParentID(parentID);
				newID = parentID * 10 + parentChildCount + 1;
			} else {
				newID = parent.getParentID() * 10 + parent.getId() % 10;
			}
			
			for ( int j = i; j < subtree.size(); ++j ) {
				Category child = subtree.get(j);
				
				if ( child.getParentID() == parent.getId() ) {
					child.setParentID(newID);
				}
			}
			parent.setId(newID);
		}
		
		String query = "INSERT INTO category (id,name,parentID, childCount) VALUES(?,?,?,?)";
		String update = "UPDATE category SET childCount = childCount + 1 WHERE id = ?";
		
		try (PreparedStatement preparedStatementQuery = connection.prepareStatement(query);
			 PreparedStatement preparedStatementUpdate = connection.prepareStatement(update))
		{
			connection.setAutoCommit(false);
			
			// Insert the whole subtree
			for (Category cat: subtree) {
				preparedStatementQuery.setInt(1, cat.getId());
				preparedStatementQuery.setString(2, cat.getName());
				preparedStatementQuery.setInt(3, cat.getParentID());
				preparedStatementQuery.setInt(4, cat.getChildCount()); // this does not change when copying
				preparedStatementQuery.addBatch();
			}
			preparedStatementQuery.executeBatch();
			
			// Increment the parent's children count
			preparedStatementUpdate.setInt(1, parentID);
			preparedStatementUpdate.executeUpdate();
			
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw new SQLException("DB ACCESS ERROR. Action: " + action);
		} finally {
			connection.setAutoCommit(true);
		}
	
		return subtree.get(0).getId();
	}
	
}
