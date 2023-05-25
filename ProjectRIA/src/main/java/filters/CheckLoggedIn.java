package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import utils.PathUtils;

/**
 * Filter requests that require the user to be logged in
 */
@WebFilter(urlPatterns = {"/home.html", "/GetTaxonomy", "/CreateCategory", "/RenameCategory", "/CopyCategory"})
public class CheckLoggedIn extends HttpFilter implements Filter {
       
    private static final long serialVersionUID = 1L;

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
		
		res.sendRedirect(req.getServletContext().getContextPath() + PathUtils.pathToLoginPage);
	}

}
