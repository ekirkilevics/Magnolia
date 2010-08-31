package info.magnolia.cms.cache.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.commands.FlushCachesCommand;
import info.magnolia.module.cache.commands.FlushFromCachesByUUIDCommand;
import info.magnolia.module.cache.commands.FlushNamedCacheCommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tools page to show cache info and flush caches.
 * 
 * @author Bert Leunis
 */
public class CacheToolsPage extends TemplatedMVCHandler {

    private final CacheModule cacheModule;

    public CacheToolsPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        cacheModule = ModuleRegistry.Factory.getInstance().getModuleInstance(CacheModule.class);
    }

    public String refresh() {
        return this.show();
    }

    public String flushNamedCache() {
        try {
            AlertUtil.setMessage(flushNamedCacheCommand());
        }
        catch (Exception e) {
            AlertUtil.setMessage("Error when flushing cache: " + e.getMessage(), e);
        }
        return this.show();
    }

    private String flushNamedCacheCommand() {
        FlushNamedCacheCommand command = new FlushNamedCacheCommand();
        try {
            command.execute(MgnlContext.getInstance());
        } catch (Exception e) {
            return "Error occurred during command execution. (" + e.getMessage() +")";
        }

        return "Cache \"" + MgnlContext.getParameter(FlushNamedCacheCommand.CACHE_NAME) + "\" is flushed.";
    }

    public String flushByUUID() {
        try {
            AlertUtil.setMessage(flushByUUIDCommand());
        }
        catch (Exception e) {
            AlertUtil.setMessage("Error when flushing by uuid: " + e.getMessage(), e);
        }
        return this.show();
    }

    private String flushByUUIDCommand() {
        FlushFromCachesByUUIDCommand command = new FlushFromCachesByUUIDCommand();
        try {
            command.execute(MgnlContext.getInstance());
        } catch (Exception e) {
            return "Error occurred during command execution. (" + e.getMessage() +")";
        }
        return "UUID \"" + MgnlContext.getParameter(FlushFromCachesByUUIDCommand.UUID) + "\" is flushed from cache.";
    }

    public String flushAllCaches() {
        try {
            AlertUtil.setMessage(flushAllCacheCommand());
        }
        catch (Exception e) {
            AlertUtil.setMessage("Error when flushing all caches: " + e.getMessage(), e);
        }
        return this.show();
    }

    private String flushAllCacheCommand() {
        FlushCachesCommand command = new FlushCachesCommand();
        try {
            command.execute(MgnlContext.getInstance());
        } catch (Exception e) {
            return "Error occurred during command execution. (" + e.getMessage() +")";
        }
        return "All caches are flushed.";
    }

    public Map<String, Integer> getCacheSizes() {
        Map<String, Integer> cacheNames = new HashMap<String, Integer>();
        CacheFactory factory = cacheModule.getCacheFactory();
        Collection<CacheConfiguration> cacheConfigs = cacheModule.getConfigurations().values();
        for (CacheConfiguration config : cacheConfigs) {
            String cacheName = config.getName();
            cacheNames.put(cacheName, factory.getCache(cacheName).getSize());
        }
        return cacheNames;
    }

    public Iterator getRepositories() {
        return ContentRepository.getAllRepositoryNames();
    }

}
