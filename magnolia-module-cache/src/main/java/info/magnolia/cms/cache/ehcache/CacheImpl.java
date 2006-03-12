package info.magnolia.cms.cache.ehcache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheKey;
import info.magnolia.cms.cache.CacheableEntry;
import info.magnolia.cms.core.Content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>Cache</code> implementation using <a href="http://ehcache.sf.net/">EHCACHE</a>.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 */
public class CacheImpl implements info.magnolia.cms.cache.Cache {

    private static final Logger log = LoggerFactory.getLogger(CacheImpl.class);

    private CacheConfig config;

    private Cache ehcache;

    private CacheManager ehcacheManager;

    public void cacheRequest(CacheKey key, CacheableEntry out, boolean canCompress) {
        this.ehcache.put(new Element(key, out));
    }

    public void flushAll() {
        try {
            this.ehcache.removeAll();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long getCreationTime(CacheKey key) {
        try {
            Element element = this.ehcache.get(key);

            if (element == null) {
                return -1;
            }

            return element.getCreationTime();
        }
        catch (CacheException e) {
            throw new RuntimeException(e);
        }
    }

    public void start(CacheConfig config) throws ConfigurationException {
        try {
            this.config = config;
            this.ehcache = createCache();
            this.ehcacheManager = CacheManager.getInstance();
            this.ehcacheManager.addCache(this.ehcache);
        }
        catch (CacheException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isCached(CacheKey request) {
        try {
            Element element = this.ehcache.getQuiet(request);

            return (element != null);
        }
        catch (CacheException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        this.ehcacheManager.shutdown();
    }

    public boolean streamFromCache(CacheKey key, HttpServletResponse response, boolean canCompress) {
        try {
            Element element = this.ehcache.get(key);
            if (element == null) {
                return false;
            }

            byte[] buffer = ((CacheableEntry) element.getValue()).getOut();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            response.setContentLength(buffer.length);

            try {
                OutputStream out = response.getOutputStream();
                IOUtils.copy(in, out);
                out.flush();
                IOUtils.closeQuietly(out);
            }
            catch (IOException e) {
                log.error("Error while reading cache for: '" + key + "'.", e);
                return false;
            }
            finally {
                IOUtils.closeQuietly(in);
            }

            return true;
        }
        catch (CacheException e) {
            throw new RuntimeException(e);
        }
    }

    private Cache createCache() throws ConfigurationException {

        // commented lines require ehcache 1.2
        try {
            Content configNode = this.config.getContent("ehcache");
            String name = configNode.getNodeData("name").getString();
            int maxElements = (int) configNode.getNodeData("maxElementsInMemory").getLong();

            // MemoryStoreEvictionPolicy evictionPolicy =
            // MemoryStoreEvictionPolicy.fromString(configNode.getNodeData("memoryStoreEvictionPolicy").getString());
            boolean overflow = configNode.getNodeData("overflowToDisk").getBoolean();

            // String diskStore = configNode.getNodeData("diskStorePath").getString();
            boolean eternal = configNode.getNodeData("eternal").getBoolean();
            long ttl = configNode.getNodeData("timeToLiveSeconds").getLong();
            long tti = configNode.getNodeData("timeToIdleSeconds").getLong();
            boolean persistent = configNode.getNodeData("diskPersistent").getBoolean();
            long expiryInterval = configNode.getNodeData("diskExpiryThreadIntervalSeconds").getLong();
            // RegisteredEventListeners listeners = null;

            // if (StringUtils.isBlank(diskStore)) { diskStore = Path.getCacheDirectoryPath(); }

            // return new Cache(name, maxElements, evictionPolicy, overflow, diskStore, eternal, ttl, tti, persistent,
            // expiryInterval,
            // listeners);
            return new Cache(name, maxElements, overflow, eternal, ttl, tti, persistent, expiryInterval);
        }
        catch (RepositoryException e) {
            throw new ConfigurationException(e);
        }
    }

}
