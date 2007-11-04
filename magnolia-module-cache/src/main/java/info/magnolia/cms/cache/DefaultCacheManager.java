package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.cache.noop.NoOpCache;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.voting.Voting;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
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
     * @see info.magnolia.cms.cache.CacheManager#getCacheKey(javax.servlet.http.HttpServletRequest) TODO : we should
     * probably use the AggregationState instead of the HttpServletRequest
     */
    public String getCacheKey(HttpServletRequest request) {
        return MgnlContext.getAggregationState().getOriginalURI();
    }

    /**
     * In case we the voting is negative the request is not cached. Otherwise the defined deny or allow uri are checked.
     */
    public boolean isCacheable(HttpServletRequest request) {
        if (Voting.Factory.getDefaultVoting().vote(voters, request) < 0) {
            return false;
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

    public boolean cacheRequest(String key, CacheableEntry entry, boolean canCompress) {
        cache.cacheRequest(key, entry, canCompress);
        return true;
    }

    protected void doFlushAll() {
        this.cache.flush();
    }

    protected long doGetCreationTime(String key) {
        return this.cache.getCreationTime(key);
    }

    protected void doStart() {
        try {
            CacheConfig config = getConfig();

            Cache cache = newCache(config.getCacheImplementation());
            cache.start(config);

            this.cache = cache;

            registerObservation();
        }
        catch (ConfigurationException e) {
            // FIXME
        }
    }

    protected void doStop() {
        this.cache.stop();
        this.cache = NOOP_CACHE;
    }

    protected boolean doStreamFromCache(String key, HttpServletResponse response, boolean canCompress) {
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

    private boolean isCached(String request) {
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

    /**
     * override this to register different workspaces
     */
    protected void registerObservation() {
        // TODO this should be more flexible in cases you want to handle events on other repositories
        // register to observe on any changes if configured
        ObservationUtil.registerDefferedChangeListener(ContentRepository.WEBSITE, "/", new EventListener() {

            public void onEvent(EventIterator events) {
                handleChangeEvents(events);
            }
        }, 5000, 30000);
    }

    /**
     * Called based on the observation. The default implementation flushes the whole cache.
     */
    protected void handleChangeEvents(EventIterator events) {
        flushAll();
    }

}
