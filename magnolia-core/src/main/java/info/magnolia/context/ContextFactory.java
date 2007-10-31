package info.magnolia.context;

import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.util.FactoryUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContextFactory {
	public WebContext createWebContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		WebContext ctx = (WebContext) FactoryUtil.newInstance(WebContext.class);
        ctx.init(request, response, servletContext);
        if (Authenticator.isAuthenticated(request)) {
        	//set logged in repository
        } else {
        	//set anonym repository
        }
        return ctx;
	}
	
	public void createSystemContext() {
		
	}
}
