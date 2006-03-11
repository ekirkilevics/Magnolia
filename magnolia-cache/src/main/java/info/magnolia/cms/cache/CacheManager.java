package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;

import javax.servlet.http.HttpServletResponse;


/**
 * @author Andreas Brenk
 * @since 3.0
 */
public interface CacheManager {

    boolean cacheRequest(CacheRequest request);

    void flushAll();

    long getCreationTime(CacheRequest request);

    String getDomain();

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

    boolean streamFromCache(CacheRequest request, HttpServletResponse response);
}
