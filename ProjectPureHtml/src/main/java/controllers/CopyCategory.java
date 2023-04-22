package controllers;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.CategoryDAO;
import utils.DBTemplateHttpServlet;
import utils.PathUtils;

/**
 * Copy a taxonomy subtree as a child of an existing node.
 * Accessed through links in the home.html page.
 */
@WebServlet("/CopyCategory")
public class CopyCategory extends DBTemplateHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CopyCategory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Parameter sanitization
		String copySrc = request.getParameter("copySrc");
		String copyTgt = request.getParameter("copyTgt");
		if (copySrc == null || copyTgt == null) {
			renderError(request, response, "Copy parameters empty!");
			return;
		}
		
		int copySrcID;
		int copyTgtID;
		try {
			copySrcID = Integer.parseInt(copySrc);
			copyTgtID = Integer.parseInt(copyTgt);	
		} catch (NumberFormatException e) {
			renderError(request, response, "Copy parameters must be numbers!");
			return;
		}
		
		if ( copySrcID == 0 ) {
			renderError(request, response, "Root may not be copied!");
			return;
		}
		if ( copyTgt.startsWith(copySrc) ) {
			renderError(request, response, "A subtree cannot be copied to one of its nodes!");
			return;
		}
		
		// Sanitization requiring DB access + subtree insertion
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		int insertionID;
		try {
			int childCountSrc = categoryDAO.getChildCount(copySrcID);
			int childCountTgt = categoryDAO.getChildCount(copyTgtID);
			if ( childCountSrc == -1 || childCountTgt == -1 ) {
				renderError(request, response, "Copy parameters must belong to a taxonomy in the tree!");
				return;
			}
			if ( childCountTgt >= 9 ) {
				renderError(request, response, "Target category has too many children!");
				return;
			}
			
			insertionID = categoryDAO.insertSubtree(copySrcID, copyTgtID, childCountTgt);
		} catch (SQLException e) {
			renderError(request, response, e.getMessage());
			return;
		}
		
		HttpSession session = request.getSession();
		session.setAttribute("insertionID", Integer.toString(insertionID));
		response.sendRedirect(getServletContext().getContextPath() + PathUtils.pathToHomeServlet);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
