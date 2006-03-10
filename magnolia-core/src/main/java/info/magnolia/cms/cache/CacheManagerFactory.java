package info.magnolia.cms.cache;

/**
 * A Factory for a singleton <code>CacheManager</code> object. Creates a <code>ManageableCacheManager</code>
 * wrapping a <code>ThreadedCacheManager</code> and stores it in its cacheManager instance variable. Supposed to be
 * later replaced with a more sophisticated, configurable method like e.g. dependency injection using the Spring
 * Framework.
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public class CacheManagerFactory {

    // ~ Static fields/initializers
    // ---------------------------------------------------------------------------------------------------------

    private static CacheManager cacheManager;

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    public static synchronized CacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = new ManageableCacheManager(new ThreadedCacheManager());
        }

        return cacheManager;
    }
}
