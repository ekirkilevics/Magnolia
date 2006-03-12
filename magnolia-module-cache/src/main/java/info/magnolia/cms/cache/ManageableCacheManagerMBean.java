package info.magnolia.cms.cache;

/**
 * The JMX Standard MBean management interface for <code>ManageableCacheManager</code>. TODO maybe add persistent
 * enable() and disable() methods that modify the "active" property in the repository.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public interface ManageableCacheManagerMBean {

    void flushAll();

    int getCacheHits();

    int getCacheMisses();

    int getCachePuts();

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
