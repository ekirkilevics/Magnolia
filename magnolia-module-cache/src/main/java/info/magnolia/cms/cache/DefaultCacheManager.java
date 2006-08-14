package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.cache.noop.NoOpCache;
import info.magnolia.cms.util.ClassUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class DefaultCacheManager extends BaseCacheManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultCacheManager.class);

    private static final Cache NOOP_CACHE = new NoOpCache();

    private Cache cache = NOOP_CACHE;

    /**
     * @see info.magnolia.cms.cache.CacheManager#getCacheKey(javax.servlet.http.HttpServletRequest)
     */
    public CacheKey getCacheKey(HttpServletRequest request) {
        return new CacheKey(request);
    }

    /**
     * @see info.magnolia.cms.cache.CacheManager#isCacheable(javax.servlet.http.HttpServletRequest)
     */
    public boolean isCacheable(HttpServletRequest request) {

        if (StringUtils.equalsIgnoreCase(request.getMethod(), "POST")) {
            return false; // don't cache POSTs
        }

        if (!request.getParameterMap().isEmpty()) {
            return false; // don't cache requests with parameters
        }

        if (StringUtils.isEmpty(MIMEMapping.getMIMEType(request))) {
            return false; // check for MIMEMapping, extension must exist
        }

        return getConfig().isUriCacheable(request);
    }

    /**
     * @see info.magnolia.cms.cache.CacheManager#canCompress(javax.servlet.http.HttpServletRequest)
     */
    public boolean canCompress(HttpServletRequest request) {

        String extension = StringUtils.substringAfter(request.getRequestURI(), ".");
        return getConfig().canCompress(extension);
    }

    public boolean cacheRequest(CacheKey key, CacheableEntry entry, boolean canCompress) {
        cache.cacheRequest(key, entry, canCompress);
        return true;
    }

    protected void doFlushAll() {
        this.cache.flush();
    }

    protected long doGetCreationTime(CacheKey request) {
        return this.cache.getCreationTime(request);
    }

    protected void doStart() {
        try {
            CacheConfig config = getConfig();

            Cache cache = newCache(config.getCacheImplementation());
            cache.start(config);

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

    protected boolean doStreamFromCache(CacheKey key, HttpServletResponse response, boolean canCompress) {
        if (!isRunning()) {
            return false;
        }

        boolean didUseCache = this.cache.streamFromCache(key, response, canCompress);

        if (log.isDebugEnabled()) {
            if (didUseCache) {
                log.debug("Used cache for: '{}'.", key);
            }
            else {
                log.debug("Not found in cache: '{}'.", key);
            }
        }

        return didUseCache;
    }

    protected Cache getCache() {
        return this.cache;
    }

    private boolean isCached(CacheKey request) {
        return this.cache.isCached(request);
    }

    private Cache newCache(String cacheImplementation) throws ConfigurationException {
        try {
            return (Cache) ClassUtil.newInstance(cacheImplementation);
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

}
