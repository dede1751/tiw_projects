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
 * Servlet implementation class RenameCategory
 */
@WebServlet("/RenameCategory")
@MultipartConfig
public class RenameCategory extends DBHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RenameCategory() {
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
		
		String id = StringEscapeUtils.escapeJava(request.getParameter("id"));
		String newName = StringEscapeUtils.escapeJava(request.getParameter("newName"));
		
		int categoryID = 0;
		
		// Error on empty fields
		if (id == null || newName == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Missing parameter");
			return;
		}
		
		// Error for names that are too long
		if (newName.length() > 45) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("New name is too long!");
			return;
		}
		
		// Error on non-numeric values for id
		try {
			categoryID = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("ID must be a number!");
			return;
		}
		
		// If the category exists in the DB, rename it
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		try {
			if (categoryDAO.getChildCount(categoryID) == -1) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
				response.getWriter().println("Category to rename does not exist!");
				return;
			};
			categoryDAO.renameCategory(categoryID, newName);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;	
		}
		
		// Everything executed correctly
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
