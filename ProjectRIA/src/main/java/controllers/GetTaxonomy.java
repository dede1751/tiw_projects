package controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import beans.Category;
import beans.Category.PacketCategory;
import dao.CategoryDAO;
import utils.DBHttpServlet;

/**
 * Fetch the taxonomy tree
 */
@WebServlet("/GetTaxonomy")
public class GetTaxonomy extends DBHttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetTaxonomy() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Fetch the taxonomy from the DB
		CategoryDAO categoryDAO = new CategoryDAO(connection);
		List<Category> tree = null;
		try {
			tree = categoryDAO.getCategoryTree(0);
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		// Serialize and send the taxonomy
		List<PacketCategory> packetTree = new ArrayList<>(
				tree.stream()
					.map(c -> 
						new PacketCategory(c.getId(), c.getName(), c.getParentID(), c.getChildCount(), c.getGeneration()))
					.toList()
		);
		String taxonomy = new Gson().toJson(packetTree);
		response.setStatus(HttpServletResponse.SC_OK);	
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(taxonomy);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
