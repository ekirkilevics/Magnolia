package info.magnolia.cms.cache;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.filters.MagnoliaMainFilter;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.ContentUtil;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        // order the filter
        Content filters = ContentUtil.getContent(ContentRepository.CONFIG, MagnoliaMainFilter.SERVER_FILTERS);
        try {
            filters.orderBefore("cache", "virtualURI");
        }
        catch (RepositoryException e) {
            log.error("can't reorder filter", e);
        }

    }

}
