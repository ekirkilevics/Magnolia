package info.magnolia.cms.cache;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.RegisterException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class Engine extends AbstractModule {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(Engine.class);

    private CacheManager cacheManager = CacheManagerFactory.getCacheManager();

    /**
     * Init cache manager
     */
    public void init(Content configNode) throws InvalidConfigException, InitializationException {
        this.cacheManager.init(configNode);
        this.setInitialized(true);
    }

    /**
     * @see info.magnolia.cms.module.Module#destroy()
     */
    public void destroy() {
        this.cacheManager.stop();
    }

    /**
     * @see info.magnolia.cms.module.AbstractModule#onRegister(int)
     */
    protected void onRegister(int registerState) throws RegisterException {
        // nothing todo
    }

}
