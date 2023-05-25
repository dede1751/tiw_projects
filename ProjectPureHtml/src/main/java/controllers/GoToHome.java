package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import beans.Category;
import dao.CategoryDAO;
import utils.DBTemplateHttpServlet;
import utils.PathUtils;

/**
 * Display the home page
 * Servlet is reached from the Login or Register servlets, or when redirected by the CheckLoggedOut filter
 */
@WebServlet("/GoToHome")
public class GoToHome extends DBTemplateHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToHome() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * Uses the template engine to render the home.html page
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Convert create warning message query to request attribute.
		String warning = StringEscapeUtils.escapeJava(request.getParameter("createWarningMsg"));
		if ( warning != null ) {
			request.setAttribute("createWarningMsg", CreateCategory.getWarningMessage(warning));
		}
		
		// Fetch the taxonomy from the DB
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		List<Category> tree = null;
		try {
			tree = categoryDAO.getCategoryTree(0);
		} catch(SQLException e) {
			renderError(request, response, e.getMessage());
			return;
		}
		
		// Handle first step of copy
		String copyRootID = StringEscapeUtils.escapeJava(request.getParameter("copyRootID"));
		if (copyRootID != null) {
			int rootID;
			// Error on non-numeric values for parent id
			try {
				rootID = Integer.parseInt(copyRootID);
			} catch (NumberFormatException e) {
				renderError(request, response, "Copy ID must be a number!");
				return;
			}
			
			// Error on values not in tree
			try {
				int childCount = categoryDAO.getChildCount(rootID);
				if ( childCount == -1 ) {
					renderError(request, response, "The copied category must belong to a taxonomy in the tree!");
					return;
				}
			} catch (SQLException e) {
				renderError(request, response, e.getMessage());
				return;
			}
			
			// Highlight the whole subtree before copy
			for (Category cat: tree) {
				if (Integer.toString(cat.getId()).startsWith(copyRootID)) {
					cat.setHighlighted(true);
				}
			}
			
			// Copy root id is saved to send the correct query to the CopyCategory servlet
			// (otherwise, we would not be able to tell when a tree is pasted onto itself)
			request.setAttribute("copyRootID", copyRootID);
		}
		
		// Handle final step of copy (same as copyRootID, without saving anything to the request)
		String insertionID = StringEscapeUtils.escapeJava(request.getParameter("insertionID"));
		if (insertionID != null) {
			int insertID;
			
			// Error on non-numeric values for insertion id
			try {
				insertID = Integer.parseInt(insertionID);
			} catch (NumberFormatException e) {
				renderError(request, response, "Insertion ID must be a number!");
				return;
			}
			
			// Error on values not in tree
			try {
				int childCount = categoryDAO.getChildCount(insertID);
				if ( childCount == -1 ) {
					renderError(request, response, "The insertion category must belong to a taxonomy in the tree!");
					return;
				}
			} catch (SQLException e) {
				renderError(request, response, e.getMessage());
				return;
			}
			
			// Highlight the whole subtree after it's been copied
			for (Category cat: tree) {
				if (Integer.toString(cat.getId()).startsWith(insertionID)) {
					cat.setHighlighted(true);
				}
			}
		}
		
		request.setAttribute("tree", tree);
		renderPage(request, response, PathUtils.pathToHomePage);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
