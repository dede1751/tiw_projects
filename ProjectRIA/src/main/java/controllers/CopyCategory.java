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
 * Copy a taxonomy subtree as a child of an existing node.
 * Accessed at CopyCategory
 */
@WebServlet("/CopyCategory")
@MultipartConfig
public class CopyCategory extends DBHttpServlet {
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
		
		String copySrc = StringEscapeUtils.escapeJava(request.getParameter("copySrcID"));
		String copyTgt = StringEscapeUtils.escapeJava(request.getParameter("copyTgtID"));
		
		// Error on empty fields
		if (copySrc == null || copyTgt == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Missing parameter");
			return;
		}
		
		// Error on non-numeric values for ids
		int copySrcID;
		int copyTgtID;
		try {
			copySrcID = Integer.parseInt(copySrc);
			copyTgtID = Integer.parseInt(copyTgt);	
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Copy parameters must be numbers!");
			return;
		}
		
		// Error when copying root
		if ( copySrcID == 0 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("Root may not be copied!");
			return;
		}
		
		// Error when copying to own subtree
		if ( copyTgt.startsWith(copySrc) ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
			response.getWriter().println("A subtree cannot be copied to one of its nodes!");
			return;
		}
		
		// Sanitization requiring DB access + subtree insertion
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		try {
			int childCountSrc = categoryDAO.getChildCount(copySrcID);
			int childCountTgt = categoryDAO.getChildCount(copyTgtID);
			if ( childCountSrc == -1 || childCountTgt == -1 ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
				response.getWriter().println("Copy parameters must belong to a taxonomy in the tree!");
				return;
			}
			if ( childCountTgt >= 9 ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);		
				response.getWriter().println("Target category has too many children!");
				return;
			}
			
			categoryDAO.insertSubtree(copySrcID, copyTgtID, childCountTgt);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		// Everything executed correctly
		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
