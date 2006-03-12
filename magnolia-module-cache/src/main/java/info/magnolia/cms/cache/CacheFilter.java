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

import org.apache.commons.lang.StringUtils;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class CacheFilter implements Filter {

    public static String ALREADY_FILTERED = CacheFilter.class.getName();

    /**
     * the cache manager.
     */
    private CacheManager cacheManager;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.cacheManager = CacheManagerFactory.getCacheManager();
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        this.cacheManager = null;
    }

    /**
     * @see javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
        ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        boolean cacheable = cacheManager.isCacheable(request);

        // check if the request is cachable before trying to stream from cache.
        // if the same page has already been requested before without parameters we should not fetch it from the cache
        // if this time there are parameters.
        if (cacheable) {
            HttpServletResponse response = (HttpServletResponse) res;

            CacheKey key = cacheManager.getCacheKey(request);

            boolean canCompress = cacheManager.canCompress(request);
            boolean usedCache = cacheManager.streamFromCache(key, response, canCompress && clientAcceptsGzip(request));

            if (!usedCache && !isAlreadyFiltered(request) && cacheManager.isCacheable(request)) {

                // mark the request as already filtered, avoid recursion
                request.setAttribute(ALREADY_FILTERED, Boolean.TRUE);

                CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response);

                chain.doFilter(request, responseWrapper);

                // if response has status 200 go cache it
                if (responseWrapper.getStatus() == HttpServletResponse.SC_OK) {
                    CacheableEntry cacheableEntry = responseWrapper.getCacheableEntry();
                    if (cacheableEntry != null) {
                        this.cacheManager.cacheRequest(key, cacheableEntry, canCompress);
                    }
                }
                return;
            }
        }

        // don't cache, just go on
        chain.doFilter(req, res);

    }

    private boolean clientAcceptsGzip(HttpServletRequest request) {
        return StringUtils.contains(request.getHeader("Accept-Encoding"), "gzip");
    }

    protected boolean isAlreadyFiltered(HttpServletRequest request) {
        return request.getAttribute(ALREADY_FILTERED) != null;
    }

}
