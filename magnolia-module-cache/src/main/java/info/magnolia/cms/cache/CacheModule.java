package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class CacheModule implements ModuleLifecycle {

    private static final Logger log = LoggerFactory.getLogger(CacheModule.class);

    private final CacheManager cacheManager = CacheManagerFactory.getCacheManager();

    /**
     * {@inheritDoc}
     */
    public void start(ModuleLifecycleContext moduleLifecycleContext) {

        // @todo should be refactored, it's a step in removing the old module configuration
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        Content configNode;
        try {
            configNode = hm.getContent("/modules/cache/config");
            this.cacheManager.init(configNode);
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        if (this.cacheManager.isRunning()) {
            this.cacheManager.stop();
        }
    }

}
