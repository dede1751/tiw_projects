package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import utils.PathUtils;
import utils.TemplateHandler;

/**
 * Filter requests that require the user to be logged in
 */
@WebFilter(urlPatterns = {"/GoToHome", "/CreateCategory", "/CopyCategory", "/Logout"})
public class CheckLoggedIn extends HttpFilter implements Filter {
       
    private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	/**
     * @see HttpFilter#HttpFilter()
     */
    public CheckLoggedIn() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession s = req.getSession(false);
		
		// Refuse any request where the session has no user attribute
		if(s != null) {
			Object user = s.getAttribute("user");
			if(user != null) {
				chain.doFilter(request, response);
				return;
			}
		} 
		
		req.setAttribute("errorMsg", "You need to be logged in to access this page!");
		renderPage(req, res, PathUtils.pathToErrorPage);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		ServletContext servletContext = fConfig.getServletContext();
		this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
	}
	
	public void renderPage(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException{
		ServletContext servletContext = request.getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
	}

}
