package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.cache.noop.NoOpCache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.DirectExecutor;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.QueuedExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;


/**
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public class ThreadedCacheManager extends BaseCacheManager {

    // ~ Static fields/initializers
    // ---------------------------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ThreadedCacheManager.class);

    private static final Cache NOOP_CACHE = new NoOpCache();

    // ~ Instance fields
    // --------------------------------------------------------------------------------------------------------------------

    private final Map activeThreads = Collections.synchronizedMap(new HashMap());

    private Cache cache = NOOP_CACHE;

    private Executor executor;

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    protected boolean doCacheRequest(CacheRequest request) {
        if (!isRunning() || isCached(request) || hasActiveThread(request) || !mayCache(request)) {
            return false;
        }

        try {
            this.executor.execute(new CacheRunnable(this, request));
        }
        catch (InterruptedException e) {
            log.error("Unexpected thread interruption!", e);
            return false;
        }

        return true;
    }

    protected void doFlushAll() {
        this.cache.flushAll();
    }

    protected long doGetCreationTime(CacheRequest request) {
        return this.cache.getCreationTime(request);
    }

    protected void doStart() {
        try {
            CacheConfig config = getConfig();

            Executor executor = newExecutor(config.getThreadStrategy());

            Cache cache = newCache(config.getCacheImplementation());
            cache.start(config);

            this.executor = executor;
            this.cache = cache;
        }
        catch (ConfigurationException e) {
            // FIXME
        }
    }

    protected void doStop() {
        this.cache.stop();
        this.cache = NOOP_CACHE;
    }

    protected boolean doStreamFromCache(CacheRequest request, HttpServletResponse response) {
        if (!isRunning() || hasActiveThread(request)) {
            return false;
        }

        boolean didUseCache = this.cache.streamFromCache(request, response);

        if (log.isDebugEnabled()) {
            if (didUseCache) {
                log.debug("Used cache for: '" + request.getURI() + "'.");
            }
            else {
                log.debug("Not found in cache: '" + request.getURI() + "'.");
            }
        }

        return didUseCache;
    }

    protected Cache getCache() {
        return this.cache;
    }

    protected void notifyFinish(CacheRunnable runnable) {
        if (log.isDebugEnabled()) {
            log.debug("Finished " + runnable);
        }

        this.activeThreads.remove(runnable);
    }

    protected void notifyStart(CacheRunnable runnable, CacheRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Started " + runnable);
        }

        this.activeThreads.put(runnable, request);
    }

    private boolean hasActiveThread(CacheRequest request) {
        return this.activeThreads.containsValue(request);
    }

    private boolean isCached(CacheRequest request) {
        return this.cache.isCached(request);
    }

    private boolean mayCache(CacheRequest request) {
        if (StringUtils.equalsIgnoreCase(request.getMethod(), "POST") || (request.getParameterCount() > 0)) {
            return false; // don't cache POSTs or requests with parameters
        }

        if (StringUtils.isEmpty(MIMEMapping.getMIMEType(request.getExtension()))) {
            return false; // check for MIMEMapping, extension must exist
        }

        return getConfig().isAllowed(request.getURI());
    }

    private Cache newCache(String cacheImplementation) throws ConfigurationException {
        try {
            Class cacheClass = Class.forName(cacheImplementation);
            return (Cache) cacheClass.newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new ConfigurationException("Cache class not found!", e);
        }
        catch (InstantiationException e) {
            throw new ConfigurationException("Could not instantiate Cache class!", e);
        }
        catch (IllegalAccessException e) {
            throw new ConfigurationException("Could not access Cache class!", e);
        }
        catch (ClassCastException e) {
            throw new ConfigurationException("Class does not implement Cache!", e);
        }
    }

    /**
     * Create an Executor for the specified strategy. Supports "threaded","queued", "pooled", "direct".
     */
    private Executor newExecutor(String threadStrategy) {
        final Executor executor;

        if (StringUtils.equalsIgnoreCase(threadStrategy, "threaded")) {
            executor = new ThreadedExecutor();
        }
        else if (StringUtils.equalsIgnoreCase(threadStrategy, "queued")) {
            executor = new QueuedExecutor();
        }
        else if (StringUtils.equalsIgnoreCase(threadStrategy, "pooled")) {
            executor = new PooledExecutor();
            // TODO further pool configuration
        }
        else if (StringUtils.equalsIgnoreCase(threadStrategy, "direct")) {
            executor = new DirectExecutor();
        }
        else {
            executor = null;
        }

        return executor;
    }
}
