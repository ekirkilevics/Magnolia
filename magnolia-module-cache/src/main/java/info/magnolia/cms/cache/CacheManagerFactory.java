package info.magnolia.cms.cache;

/**
 * A Factory for a singleton <code>CacheManager</code> object. Creates a <code>ManageableCacheManager</code>
 * wrapping a <code>ThreadedCacheManager</code> and stores it in its cacheManager instance variable. Supposed to be
 * later replaced with a more sophisticated, configurable method like e.g. dependency injection using the Spring
 * Framework.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class CacheManagerFactory {

    private static CacheManager cacheManager;

    public static synchronized CacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = new ManageableCacheManager(new DefaultCacheManager());
        }

        return cacheManager;
    }
}
