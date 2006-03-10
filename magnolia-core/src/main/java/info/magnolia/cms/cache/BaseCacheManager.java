package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.core.Content;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * An abstract base implementation of <code>CacheManager</code> implementing its lifecycle and enforcing a no
 * exception policy after initialization. Also does INFO level logging.
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public abstract class BaseCacheManager implements CacheManager {

    // ~ Static fields/initializers
    // ---------------------------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(BaseCacheManager.class);

    // ~ Instance fields
    // --------------------------------------------------------------------------------------------------------------------

    private CacheConfig config;

    private boolean initialized;

    private boolean paused;

    private boolean started;

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    public final boolean cacheRequest(CacheRequest request) {
        try {
            return doCacheRequest(request);
        }
        catch (Exception e) {
            handleNonFatalException(e);
            return false;
        }
    }

    public final void flushAll() {
        log.info("Flushing Cache...");

        try {
            doFlushAll();
        }
        catch (Exception e) {
            handleNonFatalException(e);
        }

        log.info("Cache was flushed.");
    }

    /**
     * @return the config created by a previous call to init(Content) or null if not yet initialized.
     */
    public CacheConfig getConfig() {
        return this.config;
    }

    public final long getCreationTime(CacheRequest request) {
        try {
            return doGetCreationTime(request);
        }
        catch (Exception e) {
            handleNonFatalException(e);
            return Cache.UNKNOWN_CREATION_TIME;
        }
    }

    /**
     * @return the domain as specified in the cache configuration
     * @throws NullPointerException if called before init(Content)
     */
    public String getDomain() {
        return this.config.getDomain();
    }

    /**
     * This method must be called once and only once. It loads the configuration from the repository and starts the
     * cache if it is enabled. Normally called by ConfigLoader.
     * @see info.magnolia.cms.beans.config.ConfigLoader
     * @throws ConfigurationException if the configuration is invalid
     * @throws IllegalStateException if called more than once
     */
    public final void init(Content content) throws ConfigurationException {
        if (isInitialized()) {
            throw new IllegalStateException("CacheManager is already initialized!");
        }

        log.info("Initializing CacheManager...");

        createCacheConfig(content);
        createShutdownTask();

        try {
            doInit();
        }
        catch (Exception e) {
            handleFatalException(e);
        }

        this.initialized = true;

        log.info("CacheManager is now initialized.");

        if (isEnabled()) {
            start();
        }
    }

    /**
     * @throws NullPointerException if called before init(Content)
     */
    public boolean isEnabled() {
        return this.config.isActive();
    }

    public final boolean isInitialized() {
        return this.initialized;
    }

    public final boolean isPaused() {
        return this.paused;
    }

    public final boolean isRunning() {
        return this.started && !this.paused;
    }

    public final boolean isStarted() {
        return this.started;
    }

    /**
     * @throws IllegalStateException if called before init(Content)
     */
    public final void pause() {
        if (!isInitialized()) {
            throw new IllegalStateException("CacheManager is not initialized!");
        }

        if (!isRunning()) {
            return;
        }

        log.info("Pausing CacheManager...");

        try {
            doPause();
        }
        catch (Exception e) {
            handleFatalException(e);
        }

        this.paused = true;

        log.info("CacheManager is now paused.");
    }

    /**
     * Do a complete stop / start cycle.
     * @see #stop()
     * @see #start()
     * @throws IllegalStateException if called before init(Content)
     */
    public void restart() {
        if (!isInitialized()) {
            throw new IllegalStateException("CacheManager is not initialized!");
        }

        try {
            stop();
            start();
        }
        catch (Exception e) {
            handleFatalException(e);
        }
    }

    /**
     * @throws IllegalStateException if called before init(Content)
     */
    public final void resume() {
        if (!isInitialized()) {
            throw new IllegalStateException("CacheManager is not initialized!");
        }

        if (isRunning()) {
            return;
        }

        log.info("Resuming CacheManager...");

        try {
            doResume();
        }
        catch (Exception e) {
            handleFatalException(e);
        }

        this.paused = false;

        log.info("CacheManager is now running.");
    }

    /**
     * @throws IllegalStateException if called before init(Content)
     */
    public final void start() {
        if (!isInitialized()) {
            throw new IllegalStateException("CacheManager is not initialized!");
        }

        if (isStarted()) {
            return;
        }

        log.info("Starting CacheManager...");

        try {
            doStart();
        }
        catch (Exception e) {
            handleFatalException(e);
        }

        this.paused = false;
        this.started = true;

        log.info("CacheManager is now running.");
    }

    /**
     * @throws IllegalStateException if called before init(Content)
     */
    public final void stop() {
        if (!isInitialized()) {
            throw new IllegalStateException("CacheManager is not initialized!");
        }

        if (!isStarted()) {
            return;
        }

        log.info("Stopping CacheManager...");

        try {
            doStop();
        }
        catch (Exception e) {
            handleFatalException(e);
        }

        this.paused = false;
        this.started = false;

        log.info("CacheManager is now stopped.");
    }

    public final boolean streamFromCache(CacheRequest request, HttpServletResponse response) {
        try {
            return doStreamFromCache(request, response);
        }
        catch (Exception e) {
            handleNonFatalException(e);
            return false;
        }
    }

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as an error but are not propagated.
     * @see BaseCacheManager#handleNonFatalException(Exception)
     */
    protected abstract boolean doCacheRequest(CacheRequest request);

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as an error but are not propagated.
     * @see BaseCacheManager#handleNonFatalException(Exception)
     */
    protected abstract void doFlushAll();

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as an error but are not propagated.
     * @see BaseCacheManager#handleNonFatalException(Exception)
     */
    protected abstract long doGetCreationTime(CacheRequest request);

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as an error but are not propagated.
     * @see BaseCacheManager#handleNonFatalException(Exception)
     */
    protected abstract boolean doStreamFromCache(CacheRequest request, HttpServletResponse response);

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as a fatal error but are not propagated. The state is reset to "stopped". This implementation
     * is empty.
     * @see BaseCacheManager#handleFatalException(Exception)
     */
    protected void doInit() {
        // empty template method
    }

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as a fatal error but are not propagated. The state is reset to "stopped". This implementation
     * is empty.
     * @see BaseCacheManager#handleFatalException(Exception)
     */
    protected void doPause() {
        // empty template method
    }

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as a fatal error but are not propagated. The state is reset to "stopped". This implementation
     * is empty.
     * @see BaseCacheManager#handleFatalException(Exception)
     */
    protected void doResume() {
        // empty template method
    }

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as a fatal error but are not propagated. The state is reset to "stopped". This implementation
     * is empty.
     * @see BaseCacheManager#handleFatalException(Exception)
     */
    protected void doStart() {
        // empty template method
    }

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as a fatal error but are not propagated. The state is reset to "stopped". This implementation
     * is empty.
     * @see BaseCacheManager#handleFatalException(Exception)
     */
    protected void doStop() {
        // empty template method
    }

    private void createCacheConfig(Content content) throws ConfigurationException {
        this.config = new CacheConfig(this, content);
    }

    private void createShutdownTask() {
        ShutdownManager.addShutdownTask(new CacheShutdownTask(this));
    }

    /**
     * Log the <code>Exception</code> as a fatal error and stop operation.
     */
    private void handleFatalException(Exception e) {
        this.paused = false;
        this.started = false;
        log.fatal("Unexpected exception! Stopping operation.", e);
    }

    /**
     * Log the <code>Exception</code> as an error and continue operation.
     */
    private void handleNonFatalException(Exception e) {
        log.error("Unexpected exception! Continueing operation.", e);
    }
}
