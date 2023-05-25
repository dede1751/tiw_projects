package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import beans.User;
import utils.DBTemplateHttpServlet;
import utils.PathUtils;
import dao.UserDAO;

/**
 * Login the user to the application
 * Login responds to post requests on the login.html page
 */
@WebServlet("/Login")
public class Login extends DBTemplateHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
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
	 * Check the received credentials. If login is succesfull, forward to GoToHome servlet
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		String password = StringEscapeUtils.escapeJava(request.getParameter("password"));
		
		// Error on empty fields
		if(email == null || password == null) {
			renderError(request, response, "Login fields empty!");
			return;
		}
		
		// Error for names that are too long
		if (email.length() > 45 || password.length() > 45) {
			renderError(request, response, "Login input is too long!");
			return;
		}
			
		// Attempt login
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		try {
			user = userDAO.checkCredentials(email, password);
		} catch (SQLException e) {
			renderError(request, response, e.getMessage());
			return;
		}
		
		// Warning if credentials do not match any existing user
		if(user == null) {
			String warningMsgQuery = "?loginWarningMsg";
			response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToLoginServlet + warningMsgQuery);
			return;
		}
		
		// Save the current user to the session and redirect to the home servlet
		HttpSession session = request.getSession();
		session.setAttribute("user", user);
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToHomeServlet);	
	}

}
