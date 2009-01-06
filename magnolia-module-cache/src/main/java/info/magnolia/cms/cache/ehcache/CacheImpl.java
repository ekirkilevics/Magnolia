/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.cache.ehcache;

import info.magnolia.cms.beans.config.ConfigurationException;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheableEntry;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.Path;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>Cache</code> implementation using <a href="http://ehcache.sf.net/">EHCACHE</a>.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @since 3.0
 *
 * @deprecated since 3.6, replaced by info.magnolia.module.cache.ehcache.EhCacheWrapper
 */
public class CacheImpl implements info.magnolia.cms.cache.Cache {

    private static final Logger log = LoggerFactory.getLogger(CacheImpl.class);

    private CacheConfig config;

    private Cache ehcache;

    private CacheManager ehcacheManager;

    public void cacheRequest(String key, CacheableEntry entry, boolean canCompress) {
        this.ehcache.put(new Element(key, entry));

        if (canCompress) {

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream out = new GZIPOutputStream(baos);
                out.write(entry.getOut());
                out.finish();

                CacheableEntry compressedEntry = new CacheableEntry(baos.toByteArray());
                compressedEntry.setContentType(entry.getContentType());
                compressedEntry.setCharacterEncoding(entry.getCharacterEncoding());
                this.ehcache.put(new Element(compressedKey(key), compressedEntry));
            }
            catch (IOException e) {
                log.warn("Failed to cache " + key, e);
            }
        }

    }

    public void flush() {
        this.ehcache.removeAll();
    }

    /**
     * Remove the entry
     */
    public void remove(String key) {
        this.ehcache.remove(key);
    }

    public long getCreationTime(String key) {
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

    public boolean isCached(String request) {
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

    public boolean streamFromCache(String key, HttpServletResponse response, boolean canCompress) {

        String actualKey = canCompress ? compressedKey(key) : key;
        try {
            Element element = this.ehcache.get(actualKey);
            if (element == null) {
                return false;
            }

            byte[] buffer = ((CacheableEntry) element.getValue()).getOut();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            response.setContentLength(buffer.length);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setDateHeader("Last-Modified", this.getCreationTime(key));

            if (canCompress) {
                response.setHeader("Content-Encoding", "gzip");
                response.setHeader("Vary", "Accept-Encoding"); // needed for proxies
            }
            try {
                OutputStream out = response.getOutputStream();
                IOUtils.copy(in, out);
                out.flush();
                IOUtils.closeQuietly(out);
            }
            catch (IOException e) {
                // usually a ClientAbortException
                log.debug("Error while reading cache for: {}: {} {}", new Object[]{
                    key,
                    e.getClass().getName(),
                    e.getMessage()});
                return false;
            }
            finally {
                IOUtils.closeQuietly(in);
            }

            return true;
        }
        catch (CacheException e) {
            log.warn("Failed to stream from cache " + key, e);
            return false;
        }
    }

    private Cache createCache() throws ConfigurationException {
        try {
            Content configNode = this.config.getContent("ehcache");
            String name = configNode.getNodeData("name").getString();
            int maxElements = (int) configNode.getNodeData("maxElementsInMemory").getLong();

            MemoryStoreEvictionPolicy evictionPolicy = MemoryStoreEvictionPolicy.fromString(configNode.getNodeData(
                "memoryStoreEvictionPolicy").getString());
            boolean overflow = configNode.getNodeData("overflowToDisk").getBoolean();

            String diskStore = configNode.getNodeData("diskStorePath").getString();
            boolean eternal = configNode.getNodeData("eternal").getBoolean();
            long ttl = configNode.getNodeData("timeToLiveSeconds").getLong();
            long tti = configNode.getNodeData("timeToIdleSeconds").getLong();
            boolean persistent = configNode.getNodeData("diskPersistent").getBoolean();
            long expiryInterval = configNode.getNodeData("diskExpiryThreadIntervalSeconds").getLong();
            RegisteredEventListeners listeners = null;
            BootstrapCacheLoader bootstrapCacheLoader = null;

            int maxElementsOnDisk = (int) configNode.getNodeData("maxElementsOnDisk").getLong();
            int diskSpoolBufferSizeMB = (int) configNode.getNodeData("diskSpoolBufferSizeMB").getLong();

            if (StringUtils.isBlank(diskStore)) {
                diskStore = Path.getCacheDirectoryPath();
            }

            return new Cache(
                name,
                maxElements,
                evictionPolicy,
                overflow,
                diskStore,
                eternal,
                ttl,
                tti,
                persistent,
                expiryInterval,
                listeners,
                bootstrapCacheLoader,
                maxElementsOnDisk,
                diskSpoolBufferSizeMB);

        }
        catch (RepositoryException e) {
            throw new ConfigurationException(e);
        }
    }

    private String compressedKey(String key) {
        return key + ".$.gzip";
    }

}
