package info.magnolia.cms.cache;

/**
 * The JMX Standard MBean management interface for <code>ManageableCacheManager</code>. TODO maybe add persistent
 * enable() and disable() methods that modify the "active" property in the repository.
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public interface ManageableCacheManagerMBean {

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    void flushAll();

    int getCacheHits();

    int getCacheMisses();

    int getCachePuts();

    String getDomain();

    boolean isEnabled();

    boolean isPaused();

    boolean isRunning();

    boolean isStarted();

    void pause();

    void resetStatistics();

    void restart();

    void resume();

    void start();

    void stop();
}
