package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import dao.CategoryDAO;
import utils.DBHttpServlet;

/**
 * Create a new Category for the taxonomy
 * CreateCategory responds to post resquests to CreateCategory
 */
@WebServlet("/CreateCategory")
@MultipartConfig
public class CreateCategory extends DBHttpServlet {
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateCategory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String name = StringEscapeUtils.escapeJava(request.getParameter("name"));
		String parent = StringEscapeUtils.escapeJava(request.getParameter("parentID"));

		int parentID = 0;
		
		// Error on empty fields
		if (name == null || parent == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Missing parameter");
			return;
		}
		
		// Error for names that are too long
		if (name.length() > 45) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Name is too long!");
			return;
		}
		
		// Error on non-numeric values for parent id
		try {
			parentID = Integer.parseInt(parent);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("ParentID must be a number!");
			return;
		}
		
		// Lookup child count in DB
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		int childCount = 0;
		try {
			childCount = categoryDAO.getChildCount(parentID);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		// Error on non-exisiting parent nodes and ones with too many children
		if ( childCount == -1 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Chosen parent ID does not exist!");
			return;
		} else if ( childCount >= 9 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Chosen parent has too many children!");
			return;
		}
		
		// Compute new id and add it to the DB
		int newID = parentID * 10 + childCount + 1;
		try {
			categoryDAO.insertCategory(newID, name, parentID);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		// Everything executed correctly
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
}
