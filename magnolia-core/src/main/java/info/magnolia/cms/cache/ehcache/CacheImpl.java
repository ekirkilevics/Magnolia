package info.magnolia.cms.cache.ehcache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;
import java.net.URLConnection;

import java.text.MessageFormat;

import javax.jcr.RepositoryException;

import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.cache.AbstractCache;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheRequest;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.SecureURI;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;


/**
 * A <code>Cache</code> implementation using <a href="http://ehcache.sf.net/">EHCACHE</a>.
 * @author Andreas Brenk
 * @since 06.02.2006
 */
public class CacheImpl extends AbstractCache {

    // ~ Static fields/initializers
    // ---------------------------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(CacheImpl.class);

    // ~ Instance fields
    // --------------------------------------------------------------------------------------------------------------------

    private CacheConfig config;

    private Cache ehcache;

    private CacheManager ehcacheManager;

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------------------------

    public void cacheRequest(CacheRequest request) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean success = streamRequest(request, out);
        if (success) {
            this.ehcache.put(new Element(request, out.toByteArray()));
        }
    }

    public void flushAll() {
        try {
            this.ehcache.removeAll();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long getCreationTime(CacheRequest request) {
        try {
            Element element = this.ehcache.get(request);

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

    public boolean isCached(CacheRequest request) {
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

    public boolean streamFromCache(CacheRequest request, HttpServletResponse response) {
        try {
            Element element = this.ehcache.get(request);
            if (element == null) {
                return false;
            }

            byte[] buffer = (byte[]) element.getValue();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            response.setContentLength(buffer.length);
            try {
                stream(in, response.getOutputStream());
            }
            catch (IOException e) {
                log.error("Error while reading cache for: '" + request.getURI() + "'.", e);
                return false;
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

    private boolean streamRequest(CacheRequest request, OutputStream out) {

        // if (StringUtils.isEmpty(domain)) {
        // domain = getAppURL(request);
        // }

        final String uri = request.getURI();
        try {
            URL url = new URL(this.config.getDomain() + uri);
            if (log.isDebugEnabled()) {
                log.debug("Streaming uri:" + url.toExternalForm());
            }

            URLConnection urlConnection = url.openConnection();
            if (SecureURI.isProtected(uri)) {
                urlConnection.setRequestProperty("Authorization", request.getAuthorization());
            }

            stream(urlConnection.getInputStream(), out);

            return true;
        }
        catch (IOException e) {
            log.error(MessageFormat.format("Failed to stream [{0}] due to a {1}: {2}", new Object[]{
                uri,
                e.getClass().getName(),
                e.getMessage()}), e);
        }

        return false;
    }
}
