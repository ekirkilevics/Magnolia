package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ShutdownManager.ShutdownTask;

import javax.servlet.ServletContextEvent;


/**
 * A <code>ShutdownTask</code> to stop the <code>CacheManager</code>.
 * @see info.magnolia.cms.cache.CacheManager#stop()
 * @author Andreas Brenk
 * @since 06.02.2006
 */
class CacheShutdownTask implements ShutdownTask {

    private final CacheManager cacheManager;

    public CacheShutdownTask(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Calls <code>CacheManager</code>'s stop() method.
     */
    public void execute(ServletContextEvent sce) {
        this.cacheManager.stop();
    }
}
