package info.magnolia.context;

import info.magnolia.cms.util.FactoryUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContextFactory {
	public WebContext createWebContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		WebContext ctx = (WebContext) FactoryUtil.newInstance(WebContext.class);
        ctx.init(request, response, servletContext);
        return ctx;
	}
	
	public SystemContext getSystemContext() {
		return (SystemContext) FactoryUtil.getSingleton(SystemContext.class);
	}
    
    public static ContextFactory getInstance(){
        return (ContextFactory) FactoryUtil.getSingleton(ContextFactory.class);
    }
}
