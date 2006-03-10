package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * A <code>CacheManager</code> that is JMX manageable.
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public class ManageableCacheManager implements CacheManager, ManageableCacheManagerMBean {

    // ~ Static fields/initializers
    // ---------------------------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ManageableCacheManager.class);

    // ~ Instance fields
    // --------------------------------------------------------------------------------------------------------------------

    private int cacheHits;

    private final CacheManager cacheManager;

    private int cacheMisses;

    private int cachePuts;

    // ~ Constructors
    // -----------------------------------------------------------------------------------------------------------------------

    public ManageableCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    public boolean cacheRequest(CacheRequest request) {
        boolean didCache = this.cacheManager.cacheRequest(request);

        if (didCache) {
            this.cachePuts++;
        }

        return didCache;
    }

    public void flushAll() {
        this.cacheManager.flushAll();
    }

    public int getCacheHits() {
        return this.cacheHits;
    }

    public int getCacheMisses() {
        return this.cacheMisses;
    }

    public int getCachePuts() {
        return this.cachePuts;
    }

    public long getCreationTime(CacheRequest request) {
        return this.cacheManager.getCreationTime(request);
    }

    public String getDomain() {
        return this.cacheManager.getDomain();
    }

    public void init(Content content) throws ConfigurationException {
        this.cacheManager.init(content);
        registerMBean();
    }

    public boolean isEnabled() {
        return this.cacheManager.isEnabled();
    }

    public boolean isPaused() {
        return this.cacheManager.isPaused();
    }

    public boolean isRunning() {
        return this.cacheManager.isRunning();
    }

    public boolean isStarted() {
        return this.cacheManager.isStarted();
    }

    public void pause() {
        this.cacheManager.pause();
    }

    public void resetStatistics() {
        this.cacheHits = 0;
        this.cacheMisses = 0;
        this.cachePuts = 0;
    }

    public void restart() {
        this.cacheManager.restart();
    }

    public void resume() {
        this.cacheManager.resume();
    }

    public void start() {
        this.cacheManager.start();
    }

    public void stop() {
        this.cacheManager.stop();
    }

    public boolean streamFromCache(CacheRequest request, ServletResponse response) {
        return false;
    }

    public boolean streamFromCache(CacheRequest request, HttpServletResponse response) {
        boolean didUseCache = this.cacheManager.streamFromCache(request, response);

        if (didUseCache) {
            this.cacheHits++;
        }
        else if (isRunning()) {
            this.cacheMisses++;
        }

        return didUseCache;
    }

    /**
     * @todo refactor
     */
    private void registerMBean() {
        String objectName = "Magnolia:type=CacheManager,domain=" + ObjectName.quote(getDomain());
        try {
            MBeanServer mbeanServer = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
            mbeanServer.registerMBean(this, new ObjectName(objectName));
        }
        catch (Exception e) {
            log.error("Could not register JMX MBean '" + objectName + "'.", e);
        }
    }

}
