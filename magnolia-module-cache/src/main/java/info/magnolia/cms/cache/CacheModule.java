package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.NodeDataUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.BooleanUtils;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(CacheModule.class);

    private final CacheManager cacheManager = CacheManagerFactory.getCacheManager();

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
        if (this.cacheManager.isRunning()) {
            this.cacheManager.stop();
        }
    }

    /**
     * @see info.magnolia.cms.module.AbstractModule#onRegister(int)
     */
    protected void onRegister(int registerState) throws RegisterException {
        // set the cache inactive if this is an admin instance (default)
        try {
            // TODO : Can't use Server.isAdmin() here, because it's only initialized after the modules
            boolean isAdmin = BooleanUtils.toBoolean(NodeDataUtil.getString(
                ContentRepository.CONFIG,
                "/server/admin",
                "true"));

            if (isAdmin) {
                NodeDataUtil.getOrCreate(this.getModuleNode().getContent("config"), "active").setValue(false);
            }
        } catch (Exception e) {
            log.error("can't set the cache's active flag properly", e);
        }
    }

}
