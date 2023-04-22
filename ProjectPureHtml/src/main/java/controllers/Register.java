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
import dao.UserDAO;
import beans.User;

/**
 * Register the new user to the DB
 * Register responds to post requests on the register.html page
 */
@WebServlet("/Register")
public class Register extends DBTemplateHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
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
	 * Tries to register the user and if registration is succesfull passes control to the GoToHome servlet
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String name = request.getParameter("name");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		// Error on empty fields
		if(name == null || email == null || password == null) {
			renderError(request, response, "Registration fields empty!");
			return;
		}
		
		// Render a warning if the email already exists in the DB
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		try {
			user = userDAO.getUserByEmail(email);
		} catch (SQLException e) {
			renderError(request, response, e.getMessage());
			return;	
		}
		if (user != null) {
			request.setAttribute("registerWarningMsg", "Chosen email already exists!");
			renderPage(request, response, PathUtils.pathToLoginPage);
			return;
		}
		
		// Register the user to the DB
		try {
			userDAO.registerUser(name, email, password);
		} catch (SQLException e) {
			renderError(request, response, e.getMessage());
			return;	
		}
		
		// Fetch the user from the DB and save it to the session, then redirect to home servlet
		try {
			user = userDAO.getUserByEmail(email);
		} catch (SQLException e) {
			renderError(request, response, e.getMessage());
			return;	
		}
		HttpSession session = request.getSession();
		session.setAttribute("user", user);
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToHomeServlet);
	}

}
