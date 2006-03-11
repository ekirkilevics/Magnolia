package info.magnolia.cms.cache;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.RegisterException;

import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class Engine implements Module {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(Engine.class);

    private CacheManager cacheManager = CacheManagerFactory.getCacheManager();

    /**
     * @see info.magnolia.cms.module.Module#init(info.magnolia.cms.module.ModuleConfig)
     */
    public void init(ModuleConfig moduleConfig) throws InvalidConfigException {
        this.cacheManager.init(moduleConfig.getLocalStore());
    }

    /**
     * @see info.magnolia.cms.module.Module#register(String, String, Content,JarFile, int)
     */
    public void register(String moduleName, String version, Content moduleNode, JarFile jar, int registerState)
        throws RegisterException {
        // nothing to do
    }

    /**
     * @see info.magnolia.cms.module.Module#destroy()
     */
    public void destroy() {
        this.cacheManager.stop();
    }

}
