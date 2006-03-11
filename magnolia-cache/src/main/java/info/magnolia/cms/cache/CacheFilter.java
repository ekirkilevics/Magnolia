package info.magnolia.cms.cache;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Andreas Brenk
 * @since 3.0
 */
public class CacheFilter implements Filter {

    private CacheManager cacheManager;

    public void destroy() {
        this.cacheManager = null;
    }

    /**
     * TODO set Last-Modified and other cache related HTTP headers
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
        ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        CacheRequest cacheRequest = new CacheRequest(request);
        boolean usedCache = this.cacheManager.streamFromCache(cacheRequest, response);

        if (!usedCache) {
            this.cacheManager.cacheRequest(cacheRequest);
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.cacheManager = CacheManagerFactory.getCacheManager();
    }
}
