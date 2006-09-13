package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.FilterManager;
import info.magnolia.cms.beans.config.FilterManager.FilterDefinition;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
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
public class MagnoliaManagedFilter implements Filter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentTypeFilter.class);

    /**
     * FilterConfig. ServletContext will be passed over to managed filters.
     */
    private FilterConfig filterConfig;

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
        FilterChain fullchain = new CustomFilterChain(chain, filterManager.getFilters());

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
        this.filterConfig = filterConfig;

        for (int j = 0; j < filterManager.getFilterDefinitions().length; j++) {
            FilterDefinition filter = filterManager.getFilterDefinitions()[j];
            CustomFilterConfig customFilterConfig = new CustomFilterConfig(filterConfig, filter.getParameters());

            filterManager.getFilters()[j].init(customFilterConfig);

            log.info("Initializing filter {}", filter.getClassName());
        }
    }

    public class CustomFilterChain implements FilterChain {

        private Filter[] filters;

        private int position;

        private FilterChain originalChain;

        public CustomFilterChain(FilterChain originalChain, Filter[] filters) {
            this.filters = filters;
            this.originalChain = originalChain;
        }

        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if (position == filters.length) {
                originalChain.doFilter(request, response);
            }
            else {
                position++;
                filters[position - 1].doFilter(request, response, this);
            }
        }

        public void reset() {
            position = 0;
        }
    }

    private class CustomFilterConfig implements FilterConfig {

        private Map parameters;

        private FilterConfig parent;

        /**
         * @param parameters
         */
        public CustomFilterConfig(FilterConfig parent, Map parameters) {
            super();
            this.parent = parent;
            if (parameters != null) {
                this.parameters = parameters;
            }
            else {
                this.parameters = new HashMap();
            }
        }

        /**
         * @see javax.servlet.FilterConfig#getFilterName()
         */
        public String getFilterName() {
            return parent.getFilterName();
        }

        /**
         * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
         */
        public String getInitParameter(String name) {
            return (String) parameters.get(name);
        }

        /**
         * @see javax.servlet.FilterConfig#getInitParameterNames()
         */
        public Enumeration getInitParameterNames() {
            return new Hashtable(parameters).keys();
        }

        /**
         * @see javax.servlet.FilterConfig#getServletContext()
         */
        public ServletContext getServletContext() {
            return parent.getServletContext();
        }

    }
}
