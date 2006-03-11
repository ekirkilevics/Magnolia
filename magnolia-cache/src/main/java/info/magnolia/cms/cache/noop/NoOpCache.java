package info.magnolia.cms.cache.noop;

import info.magnolia.cms.cache.Cache;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheRequest;

import javax.servlet.http.HttpServletResponse;


/**
 * A <code>Cache</code> implementation that does nothing.
 * @author Andreas Brenk
 * @since 3.0
 */
public class NoOpCache implements Cache {

    /**
     * Does nothing.
     */
    public void cacheRequest(CacheRequest request) {
        return;
    }

    /**
     * Does nothing.
     */
    public void flushAll() {
        return;
    }

    /**
     * Does nothing.
     * @return <code>Cache.UNKNOWN_CREATION_TIME</code>
     */
    public long getCreationTime(CacheRequest request) {
        return Cache.UNKNOWN_CREATION_TIME;
    }

    /**
     * Does nothing.
     */
    public void start(CacheConfig config) {
        return;
    }

    /**
     * Does nothing.
     * @return <code>false</code>
     */
    public boolean isCached(CacheRequest request) {
        return false;
    }

    /**
     * Does nothing.
     */
    public void stop() {
        return;
    }

    /**
     * Does nothing.
     * @return <code>false</code>
     */
    public boolean streamFromCache(CacheRequest request, HttpServletResponse response) {
        return false;
    }
}
