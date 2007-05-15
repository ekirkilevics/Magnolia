package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * TODO : we should probably use the AggregationState instead of the HttpServletRequest
 * in the various methods of this interface.
 *
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public interface CacheManager {

    boolean cacheRequest(CacheKey key, CacheableEntry entry, boolean canCompress);

    CacheKey getCacheKey(HttpServletRequest request);

    boolean isCacheable(HttpServletRequest request);

    boolean canCompress(HttpServletRequest request);

    void flushAll();

    long getCreationTime(CacheKey request);

    void init(Content content) throws ConfigurationException;

    boolean isEnabled();

    boolean isPaused();

    boolean isRunning();

    boolean isStarted();

    void pause();

    void restart();

    void resume();

    void start();

    void stop();

    boolean streamFromCache(CacheKey request, HttpServletResponse response, boolean canCompress);
}
