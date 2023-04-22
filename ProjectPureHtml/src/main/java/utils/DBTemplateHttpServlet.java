package utils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Utility class for servlets needing templating support and a database connection
 */
public class DBTemplateHttpServlet extends TemplateHttpServlet {
	private static final long serialVersionUID = 1L;
	protected Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DBTemplateHttpServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
		this.connection = ConnectionHandler.getConnection(servletContext);
    }
    
    @Override
    public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
