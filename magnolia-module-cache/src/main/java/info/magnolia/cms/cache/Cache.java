package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;

import javax.servlet.http.HttpServletResponse;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public interface Cache {

    int UNKNOWN_CREATION_TIME = -1;

    void start(CacheConfig config) throws ConfigurationException;

    void stop();

    boolean isCached(CacheKey key);

    void cacheRequest(CacheKey key, CacheableEntry out, boolean canCompress);

    boolean streamFromCache(CacheKey key, HttpServletResponse response, boolean canCompress);

    long getCreationTime(CacheKey key);

    void remove(CacheKey key);
    
    void flush();

}
