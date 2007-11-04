package info.magnolia.cms.cache;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;


/**
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0 $Id$
 */
class CacheConfigListener implements EventListener {

    private CacheManager cacheManager;

    private CacheConfig cacheConfig;

    protected CacheConfigListener(CacheManager cacheManager, CacheConfig cacheConfig) {
        this.cacheManager = cacheManager;
        this.cacheConfig = cacheConfig;
    }

    public void onEvent(EventIterator events) {
        try {
            this.cacheConfig.reload();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        // this.cacheManager.setConfigNode(this.content); // FIXME
        this.cacheManager.restart();
    }
}
