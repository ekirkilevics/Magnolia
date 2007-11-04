package info.magnolia.cms.cache.noop;

import info.magnolia.cms.cache.Cache;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheableEntry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A <code>Cache</code> implementation that does nothing.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class NoOpCache implements Cache {

    /**
     * Does nothing.
     */
    public void cacheRequest(HttpServletRequest request) {
    }

    /**
     * Does nothing.
     */
    public void flush() {
    }

    /**
     * Does nothing.
     * @return <code>Cache.UNKNOWN_CREATION_TIME</code>
     */
    public long getCreationTime(HttpServletRequest request) {
        return Cache.UNKNOWN_CREATION_TIME;
    }

    /**
     * Does nothing.
     */
    public void start(CacheConfig config) {
    }

    /**
     * Does nothing.
     * @return <code>false</code>
     */
    public boolean isCached(String request) {
        return false;
    }

    /**
     * Does nothing.
     */
    public void stop() {
    }

    /**
     * Does nothing.
     * @return <code>false</code>
     */
    public boolean streamFromCache(HttpServletRequest request, HttpServletResponse response, boolean canCompress) {
        return false;
    }

    /**
     * @see info.magnolia.cms.cache.Cache#cacheRequest(info.magnolia.cms.cache.CacheKey,
     * info.magnolia.cms.cache.CacheableEntry, boolean)
     */
    public void cacheRequest(String key, CacheableEntry out, boolean canCompress) {

    }

    /**
     * @see info.magnolia.cms.cache.Cache#getCreationTime(info.magnolia.cms.cache.CacheKey)
     */
    public long getCreationTime(String request) {
        return 0;
    }

    /**
     * @see info.magnolia.cms.cache.Cache#streamFromCache(info.magnolia.cms.cache.CacheKey,
     * javax.servlet.http.HttpServletResponse, boolean)
     */
    public boolean streamFromCache(String key, HttpServletResponse response, boolean canCompress) {
        return false;
    }

    /**
     * @see info.magnolia.cms.cache.Cache#flushEntry(info.magnolia.cms.cache.CacheKey)
     */
    public void remove(String key) {

    }

}
