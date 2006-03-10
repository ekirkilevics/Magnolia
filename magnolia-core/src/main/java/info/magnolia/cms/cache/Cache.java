package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;

import javax.servlet.http.HttpServletResponse;


/**
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public interface Cache {

    // ~ Instance fields
    // --------------------------------------------------------------------------------------------------------------------

    int UNKNOWN_CREATION_TIME = -1;

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    void cacheRequest(CacheRequest request);

    void flushAll();

    long getCreationTime(CacheRequest request);

    void start(CacheConfig config) throws ConfigurationException;

    boolean isCached(CacheRequest request);

    void stop();

    boolean streamFromCache(CacheRequest request, HttpServletResponse response);
}
