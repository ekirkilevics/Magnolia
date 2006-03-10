package info.magnolia.cms.cache;

/**
 * @author Andreas Brenk
 * @since 06.02.2006
 */
class CacheRunnable implements Runnable {

    // ~ Instance fields
    // --------------------------------------------------------------------------------------------------------------------

    private final ThreadedCacheManager manager;

    private final CacheRequest request;

    // ~ Constructors
    // -----------------------------------------------------------------------------------------------------------------------

    public CacheRunnable(ThreadedCacheManager manager, CacheRequest request) {
        this.manager = manager;
        this.request = request;
    }

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    public int hashCode() {
        return this.request.hashCode();
    }

    public void run() {
        this.manager.notifyStart(this, this.request);
        this.manager.getCache().cacheRequest(this.request);
        this.manager.notifyFinish(this);
    }

    public String toString() {
        return "CacheThread for '" + this.request.getURI() + "'";
    }
}
