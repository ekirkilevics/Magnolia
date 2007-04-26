package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.FilterManager;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A single filter that in turn executed a chain of other filters not configured in web.xml.
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class MagnoliaMainFilter implements Filter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentTypeFilter.class);

    /**
     * FilterManager instance. Holds filter configuration.
     */
    private FilterManager filterManager = FilterManager.getInstance();

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        for (int j = 0; j < filterManager.getFilters().length; j++) {
            Filter filter = filterManager.getFilters()[j];
            filter.destroy();

        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        FilterChain fullchain = new MagnoliaFilterChain(chain, filterManager.getFilters());

        if (log.isDebugEnabled()) {
            String pathInfo = ((HttpServletRequest) request).getPathInfo();
            String requestURI = ((HttpServletRequest) request).getRequestURI();

            if (pathInfo == null || !requestURI.startsWith("/.")) {
                log.debug("handling: {}   path info: {}", requestURI, pathInfo);
            }
        }

        fullchain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        filterManager.initFilters(filterConfig);
    }

}
