package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.Path;

import java.util.ArrayList;

import javax.management.*;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>CacheManager</code> that is JMX manageable.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class ManageableCacheManager implements CacheManager, ManageableCacheManagerMBean {

    private static final Logger log = LoggerFactory.getLogger(ManageableCacheManager.class);

    private int cacheHits;

    private final CacheManager cacheManager;

    private int cacheMisses;

    private int cachePuts;

    public ManageableCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean cacheRequest(CacheKey key, CacheableEntry entry, boolean canCompress) {
        boolean didCache = this.cacheManager.cacheRequest(key, entry, canCompress);

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

    public long getCreationTime(CacheKey request) {
        return this.cacheManager.getCreationTime(request);
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

    public boolean streamFromCache(HttpServletRequest request, ServletResponse response) {
        return false;
    }

    /**
     * @see info.magnolia.cms.cache.CacheManager#isCacheable(javax.servlet.http.HttpServletRequest)
     */
    public boolean isCacheable(HttpServletRequest request) {
        return this.cacheManager.isCacheable(request);
    }

    /**
     * @see info.magnolia.cms.cache.CacheManager#canCompress(javax.servlet.http.HttpServletRequest)
     */
    public boolean canCompress(HttpServletRequest request) {
        return this.cacheManager.canCompress(request);
    }

    public CacheKey getCacheKey(HttpServletRequest request) {
        return this.cacheManager.getCacheKey(request);
    }

    public boolean streamFromCache(CacheKey key, HttpServletResponse response, boolean canCompress) {
        boolean didUseCache = this.cacheManager.streamFromCache(key, response, canCompress);

        if (didUseCache) {
            this.cacheHits++;
        }
        else if (isRunning()) {
            this.cacheMisses++;
        }

        return didUseCache;
    }

    private void registerMBean() {
        String appName = Path.getAppRootDir().getName();
        // not totally failsafe, but it will work unless you run two instances in directories with the same name
        final String id = "Magnolia:type=CacheManager,domain=" + appName;
        try {
            final ObjectName mbeanName = new ObjectName(id);
            ArrayList list = MBeanServerFactory.findMBeanServer(null);
            final MBeanServer mbeanServer;
            if (list != null && list.size() > 0) {
                mbeanServer = (MBeanServer) list.get(0);
            }
            else {
                mbeanServer = MBeanServerFactory.createMBeanServer();
            }
            if (!mbeanServer.isRegistered(mbeanName)) {
                mbeanServer.registerMBean(this, mbeanName);
            }
        } catch (InstanceAlreadyExistsException e) {
            log.info("MBean '{}' exist", id);
        } catch (Throwable t) {
            log.error("Could not register JMX MBean '" + id + "'", t);
        }
    }
}
