package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.voting.Voter;
import info.magnolia.voting.voters.AuthenticatedVoter;
import info.magnolia.voting.voters.ExtensionVoter;
import info.magnolia.voting.voters.OnAdminVoter;
import info.magnolia.voting.voters.WithParametersVoter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract base implementation of <code>CacheManager</code> implementing its lifecycle and enforcing a no
 * exception policy after initialization. Also does INFO level logging.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0 $Id$
 */
public abstract class BaseCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(BaseCacheManager.class);

    private CacheConfig config;

    protected Voter[] voters = new Voter[0];

    private boolean initialized;

    private boolean paused;

    private boolean started;

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

    public final long getCreationTime(CacheKey key) {
        try {
            return doGetCreationTime(key);
        }
        catch (Exception e) {
            handleNonFatalException(e);
            return Cache.UNKNOWN_CREATION_TIME;
        }
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

        List cacheVotersList = new ArrayList();
        Content votersNode = content.getChildByName("voters");

        if (votersNode == null) {
            log.info("Missing voters configuration in CacheManager. Adding default config.");
            try {
                votersNode = content.createContent("voters");
                Content voter = votersNode.createContent("notWithParametersVoter", ItemType.CONTENTNODE);
                voter.createNodeData("class", WithParametersVoter.class.getName());
                voter.createNodeData("enabled", Boolean.TRUE);
                voter.createNodeData("trueValue", new Long(-1));

                voter = votersNode.createContent("extensionVoter", ItemType.CONTENTNODE);
                voter.createNodeData("class", ExtensionVoter.class.getName());
                voter.createNodeData("enabled", Boolean.TRUE);
                voter.createNodeData("falseValue", new Long(-1));
                voter.createNodeData("trueValue", new Long(0));

                voter = votersNode.createContent("notOnAdminVoter", ItemType.CONTENTNODE);
                voter.createNodeData("class", OnAdminVoter.class.getName());
                voter.createNodeData("enabled", Boolean.TRUE);
                voter.createNodeData("trueValue", new Long(-1));

                voter = votersNode.createContent("notAuthenticatedVoter", ItemType.CONTENTNODE);
                voter.createNodeData("class", AuthenticatedVoter.class.getName());
                voter.createNodeData("enabled", Boolean.FALSE);
                voter.createNodeData("trueValue", new Long(-1));
            }
            catch (RepositoryException e) {
                log.error("Unable to create default cache manager configuration.", e);
            }
            finally {
                try {
                    content.save();
                }
                catch (RepositoryException e) {
                    log.error("Unable to save changes to default cache manager configuration.", e);
                }
            }
        }

        Collection childrens = votersNode.getChildren(ItemType.CONTENTNODE);
        Iterator it = childrens.iterator();
        while (it.hasNext()) {
            Content cnt = (Content) it.next();
            try {
                Voter voter = (Voter) Content2BeanUtil.toBean(cnt, true);
                cacheVotersList.add(voter);
            }
            catch (Content2BeanException e) {
                log.error("Unable to unmarshall config at " + cnt.getHandle(), e);
            }
            catch (ClassCastException e) {
                log.error(
                    "Invalid class configured at " + cnt.getHandle() + ". Expected " + Voter.class.getName(),
                    e);
            }
        }

        this.voters = (Voter[]) cacheVotersList.toArray(new Voter[cacheVotersList.size()]);

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
     * Return true only if it has been inited AND the config is active
     */
    public boolean isEnabled() {
        return this.initialized && this.config != null && this.config.isActive();
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

    public final boolean streamFromCache(CacheKey key, HttpServletResponse response, boolean canCompress) {
        if (log.isDebugEnabled()) {
            log.debug("Streaming from cache {}, {}", key, canCompress ? "compressed" : "not compressed");
        }
        try {
            return doStreamFromCache(key, response, canCompress);
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
    protected abstract void doFlushAll();

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as an error but are not propagated.
     * @see BaseCacheManager#handleNonFatalException(Exception)
     */
    protected abstract long doGetCreationTime(CacheKey request);

    /**
     * Template method to be implemented by subclasses. Should not throw any exceptions. All exceptions thrown by this
     * method are logged as an error but are not propagated.
     * @see BaseCacheManager#handleNonFatalException(Exception)
     */
    protected abstract boolean doStreamFromCache(CacheKey request, HttpServletResponse response, boolean canCompress);

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

    /**
     * Log the <code>Exception</code> as a fatal error and stop operation.
     */
    private void handleFatalException(Exception e) {
        this.paused = false;
        this.started = false;
        log.error("Unexpected exception! Stopping operation.", e);
    }

    /**
     * Log the <code>Exception</code> as an error and continue operation.
     */
    private void handleNonFatalException(Exception e) {
        log.error("Unexpected exception! Continueing operation.", e);
    }
}
