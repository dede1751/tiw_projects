package controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import utils.PathUtils;
import utils.TemplateHttpServlet;

/**
 * Display the login page
 * Servlet is reached at the app's main page, or when redirected by CheckLoggedIn filter
 */
@WebServlet("")
public class GoToLogin extends TemplateHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToLogin() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * Uses the template engine to render the login.html page
	 * Handle warning message parameters.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Convert login warning message query to request attribute.
		String loginWarning = StringEscapeUtils.escapeJava(request.getParameter("loginWarningMsg"));
		if ( loginWarning != null ) {
			request.setAttribute("loginWarningMsg", "Email or Password incorrect!");
		}
		
		// Convert register warning message query to request attribute.
		String registerWarning = StringEscapeUtils.escapeJava(request.getParameter("registerWarningMsg"));
		if ( registerWarning != null ) {
			request.setAttribute("registerWarningMsg", "Chosen email already exists!");
		}
		
		renderPage(request, response, PathUtils.pathToLoginPage);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
