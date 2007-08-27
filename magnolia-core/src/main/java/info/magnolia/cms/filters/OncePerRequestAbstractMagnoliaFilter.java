package info.magnolia.cms.filters;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A base abstract class for filters that should not be executed more than once for each request.
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public abstract class OncePerRequestAbstractMagnoliaFilter extends AbstractMagnoliaFilter {

    private String requestKeyName = "__" + getClass() + "_FILTERED";

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {

        request.setAttribute(requestKeyName, Boolean.TRUE);
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }
    
    public boolean bypasses(HttpServletRequest request) {
        return MgnlContext.getAttribute(requestKeyName, Context.LOCAL_SCOPE) != null;
    }
}
