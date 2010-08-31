package info.magnolia.module.cache.commands;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;

import org.apache.commons.lang.StringUtils;

/**
 * Command to flush a cache by name.
 * @author Bert Leunis
 *
 */
public class FlushNamedCacheCommand extends MgnlCommand {

    public static final String CACHE_NAME = "cacheName";
    private final CacheModule cacheModule;

    public FlushNamedCacheCommand() {
        cacheModule = ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class);
    }

    @Override
    public boolean execute(Context context) throws Exception {
        String cacheName = (String) context.get(CACHE_NAME);
        if(StringUtils.isNotBlank(cacheName)) {
            CacheFactory factory = cacheModule.getCacheFactory();
            Cache cache = factory.getCache(cacheName);
            if(cache != null) {
                cache.clear();
            }
        }
        return true;
    }

}
