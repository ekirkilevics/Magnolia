package info.magnolia.cms.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A single filter which in turn executes a chain of other filters.
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class CompositeFilter extends AbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(CompositeFilter.class);

    private MgnlFilter[] filters = new MgnlFilter[0];

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        FilterChain fullchain = new MgnlFilterChain(chain, filters);

        if (log.isDebugEnabled()) {
            String pathInfo = request.getPathInfo();
            String requestURI = request.getRequestURI();

            if (pathInfo == null || !requestURI.startsWith("/.")) {
                log.debug("handling: {}   path info: {}", requestURI, pathInfo);
            }
        }

        fullchain.doFilter(request, response);
    }

    public MgnlFilter[] getFilters() {
        return filters;
    }

    public void addFilter(MgnlFilter filter) {
        this.filters = (MgnlFilter[]) ArrayUtils.add(this.filters, filter);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        initFilters(filterConfig);
    }

    /**
     * The first time called by the main filter.
     */
    public void initFilters(FilterConfig filterConfig) {
        for (int j = 0; j < filters.length; j++) {
            MgnlFilter filter = filters[j];

            try {
                log.info("Initializing filter [{}]", filter.getName());
                filter.init(filterConfig);
            }
            catch (ServletException e) {
                log.error("Error initializing filter [" + filter.getName() + "]", e);
            }
        }
    }

    public void destroy() {
        for (int j = 0; j < filters.length; j++) {
            Filter filter = filters[j];
            filter.destroy();
        }
        filters = new MgnlFilter[0];
    }

}
