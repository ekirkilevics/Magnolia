package info.magnolia.cms.filters;

import info.magnolia.cms.security.SecurityFilter;

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
 * A single filter that in turn executed a chain of other filters not configured in web.xml. At the momoent it simply
 * delegates to a list of filters hardcoded here, but it could be modified in order to read filter definions from the
 * config jcr (avoid clutter in user's web.xml, easier update to new versions...).
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class MagnoliaManagedFilter implements Filter {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ContentTypeFilter.class);

    private FilterConfig filterConfig;

    // private Filter[] filterChain = new Filter[0];
    private Filter[] filterChain = new Filter[]{
        new ContentTypeFilter(),
        new MultipartRequestFilter(),
        new SecurityFilter(),
        new MgnlContextFilter(),
        new MgnlVirtualUriFilter(),
        new MgnlInterceptFilter(),
        new MgnlCmsFilter()};

    public void destroy() {
        for (int j = 0; j < filterChain.length; j++) {
            Filter filter = filterChain[j];
            filter.destroy();

        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        FilterChain fullchain = new CustomFilterChain(chain, filterChain);

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

        for (int j = 0; j < filterChain.length; j++) {
            Filter filter = filterChain[j];
            filter.init(filterConfig);
        }
    }

    private class CustomFilterChain implements FilterChain {

        private Filter[] filters;

        private int position = 0;

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
    }
}
