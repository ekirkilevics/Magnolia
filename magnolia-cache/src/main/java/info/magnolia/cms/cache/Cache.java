package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;

import javax.servlet.http.HttpServletResponse;


/**
 * @author Andreas Brenk
 * @since 3.0
 */
public interface Cache {

    int UNKNOWN_CREATION_TIME = -1;

    void cacheRequest(CacheRequest request);

    void flushAll();

    long getCreationTime(CacheRequest request);

    void start(CacheConfig config) throws ConfigurationException;

    boolean isCached(CacheRequest request);

    void stop();

    boolean streamFromCache(CacheRequest request, HttpServletResponse response);
}
