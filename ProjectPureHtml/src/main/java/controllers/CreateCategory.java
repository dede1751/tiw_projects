package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import utils.DBTemplateHttpServlet;
import utils.PathUtils;
import dao.CategoryDAO;

/**
 * Create a new Category for the taxonomy
 * CreateCategory responds to post resquests on the home.html page
 */
@WebServlet("/CreateCategory")
public class CreateCategory extends DBTemplateHttpServlet {
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
		
		String name = request.getParameter("name");
		String parent = request.getParameter("parentID");
		int parentID = 0;
		
		// Error on empty fields
		if (name == null || parent == null) {
			renderError(request, response, "Category creation fields empty!");
			return;
		}
		
		// Warning for names that are too long
		if (name.length() >= 45) {
			HttpSession session = request.getSession();
			session.setAttribute("createWarningMsg", "Please choose a shorter name!");
			response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToHomeServlet);
			return;
		}
		
		// Error on non-numeric values for parent id
		try {
			parentID = Integer.parseInt(parent);
		} catch (NumberFormatException e) {
			renderError(request, response, "Parent ID must be a number!");
			return;
		}
		
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		int childCount = 0;
		try {
			childCount = categoryDAO.getChildCount(parentID);
		} catch (SQLException e) {
			renderError(request, response, e.getMessage());
			return;	
		}
		
		// Warning on non-exisiting parent nodes and ones with too many children
		// The warning message must be saved to the session since a forward would leave us on this servlet,
		// and a redirect would lose the message.
		if ( childCount == -1 ) {
			HttpSession session = request.getSession();
			session.setAttribute("createWarningMsg", "Chosen parent ID does not exist!");
			response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToHomeServlet);
			return;
		} else if ( childCount >= 9 ) {
			HttpSession session = request.getSession();
			session.setAttribute("createWarningMsg", "Chosen parent has too many children!");
			response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToHomeServlet);
			return;
		}
		
		// Compute new id and add it to the DB
		int newID = parentID * 10 + childCount + 1;
		try {
			categoryDAO.insertCategory(newID, name, parentID);
		} catch (SQLException e) {
			renderError(request, response, e.getMessage());
			return;	
		}
		
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToHomeServlet);	
	}

}
