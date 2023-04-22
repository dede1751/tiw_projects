package utils;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

/**
 * Utility class for servlets needing Templating support
 */
public abstract class TemplateHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected TemplateEngine templateEngine;
 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TemplateHttpServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
    }

    protected void renderError(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException{
		request.setAttribute("errorMsg", error);
		renderPage(request, response, PathUtils.pathToErrorPage);
		return;
	}
	
	protected void renderPage(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}
	
}
