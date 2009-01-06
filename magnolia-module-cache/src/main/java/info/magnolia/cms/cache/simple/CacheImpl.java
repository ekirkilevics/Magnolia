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
package info.magnolia.cms.cache.simple;

import info.magnolia.cms.cache.Cache;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheableEntry;
import info.magnolia.cms.core.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.GZIPOutputStream;


/**
 * A <code>Cache</code> implementation based on the Magnolia 2.x filesystem cache.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @author Sameer Charles
 * @since 3.0 $Id:CacheImpl.java 6314 2006-09-11 08:24:51Z scharles $
 */
public class CacheImpl implements Cache {

    private static final Logger log = LoggerFactory.getLogger(CacheImpl.class);

    /**
     * We add this extions to directories to avoid conflicts with files not having an extension
     */
    public static final String CACHE_DIRECTORY_EXTENTION = ".cache";

    /**
     * Cached items: the key is the URI of the cached request and the entry is a Cache instance
     */
    private Map cachedURIList = new Hashtable();

    /**
     * This is used to syncronize between cache flushing and creating We cannot use syncronized blocks here because it
     * will block other threads even to cache simultaniusly
     */
    private static boolean currentlyRemoving;

    private CacheConfig config;

    /**
     * Cache this request in default and optimized stores
     * @param key
     * @param entry
     * @param canCompress
     */
    public void cacheRequest(String key, CacheableEntry entry, boolean canCompress) {
        // This implementation flushes entire cache, its safe to simply check on one flag, for other
        // implementations we need to check and synchronize on CacheKey
        if (currentlyRemoving) {
            return; // simply ignore this cache request
        }

        File file = getFile(key, false);

        if (file.isDirectory()) {
            // additional check, if file name is something like "/"
            return;
        }

        long fileSize = cacheSingleFile(key, entry, file, false);

        long compressedSize = 0;
        if (canCompress) {
            compressedSize = cacheSingleFile(key, entry, getFile(key, true), true);
        }

        if (fileSize > 0) {
            addToCachedURIList(key, System.currentTimeMillis(), (int) fileSize, (int) compressedSize);
        }
        else {
            log.debug("Not caching {}, empty file saved.", key.toString());
        }
    }

    /**
     * @param key
     * @param entry
     * @param file
     */
    private long cacheSingleFile(String key, CacheableEntry entry, File file, boolean compress) {
        // it should not cache again if the resource already existing
        // its a responsibility of a cache manager to call remove or flush if resource needs to be invalidated
        if (!file.exists()) {
            OutputStream out = null;
            if (log.isDebugEnabled()) {
                log.debug("creating file {}", file.getAbsolutePath());
            }
            try {
                boolean parentDirectoryReady = mkdirs(file.getParentFile());
                if (!parentDirectoryReady) {
                    log.warn("Failed to cache {}, unable to create parent directory {}", key, file
                        .getParentFile()
                        .getAbsolutePath());
                    return 0;
                }
                file.createNewFile();

                out = new FileOutputStream(file);
                if (compress) {
                    out = new GZIPOutputStream(out);
                }
                out.write(entry.getOut());
                out.flush();
            }
            catch (IOException e) {
                log.warn("Failed to cache " + key + " to " + file.getAbsolutePath(), e);
            }
            finally {
                IOUtils.closeQuietly(out);
            }
        }
        return file.length();
    }

    public synchronized void flush() {
        try {
            File cacheDir = getCacheDirectory();
            currentlyRemoving = true;
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                FileUtils.deleteDirectory(cacheDir);
            }

            // this will create cache start directory again
            boolean mainCacheDirCreated = mkdirs(cacheDir);

            if (!mainCacheDirCreated) {
                log
                    .warn("Unable to create cache dir {}. Will retry while caching entries.", cacheDir
                        .getAbsolutePath());
            }

            // clear in-memory cache also
            clearCachedURIList();
        }
        catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        finally {
            currentlyRemoving = false;
        }
    }

    public long getCreationTime(String key) {
        CachedItem item = (CachedItem) this.cachedURIList.get(key);
        if (item == null) {
            return -1;
        }

        return item.time;
    }

    public void start(CacheConfig config) {
        this.config = config;

        File cacheDir = getCacheDirectory();

        if (!cacheDir.exists()) {
            boolean result = mkdirs(cacheDir);
            if (!result) {
                log.error("Failed to create cache directory location {}", cacheDir.getAbsolutePath());
            }
        }
        else {
            updateInMemoryCache(cacheDir);
        }
    }

    /**
     * synchronize file system with in-memory cache list
     * @param cacheDir
     */
    private void updateInMemoryCache(File cacheDir) {
        File[] items = cacheDir.listFiles();
        for (int index = 0; index < items.length; index++) {
            File item = items[index];
            if (item.isDirectory()) {
                updateInMemoryCache(item);
            }
            else {
                if (item.getName().lastIndexOf("gzip") < 0) {
                    // use this as key
                    String cacheHome = getCacheDirectory().getPath();
                    String key = StringUtils.substringAfter(item.getPath(), cacheHome);
                    key = StringUtils.replace(key, CACHE_DIRECTORY_EXTENTION, "");
                    int size = (int) item.length();
                    File compressedFile = new File(item.getPath() + ".gzip");
                    int compressedSize = -1;
                    if (compressedFile.exists()) {
                        compressedSize = (int) compressedFile.length();
                    }
                    addToCachedURIList(key, item.lastModified(), size, compressedSize);
                }
            }
        }
    }

    public boolean isCached(String key) {
        return this.cachedURIList.get(key) != null;
    }

    public void stop() {
        // NOTE: it should not flush the cache here. otherwise on server stop all filesystem
        // cache is removed, only clean in-memory cache
        clearCachedURIList();
    }

    /**
     * Spools cached data back to the client. This only works if specified request is a GET request and does not have
     * any request parameter, else it wont write anything on the output stream.
     * @param key HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> is successful
     */
    public boolean streamFromCache(String key, HttpServletResponse response, boolean canCompress) {

        FileInputStream fin = null;
        OutputStream out = null;
        try {
            File file = getFile(key, canCompress);

            if (!file.exists() || file.isDirectory() || file.length() < 4) {
                return false;
            }

            if (log.isDebugEnabled()) {
                log.debug("Streaming from cache the file: {}", file.getAbsolutePath()); // $NON-NLS-1$
            }

            fin = new FileInputStream(file);
            out = response.getOutputStream();
            response.setStatus(HttpServletResponse.SC_OK);
            response.setDateHeader("Last-Modified", this.getCreationTime(key));
            if (canCompress) {
                log.debug("setting compression headers");
                response.setContentLength(getCompressedSize(key));
                response.setHeader("Content-Encoding", "gzip");
                response.setHeader("Vary", "Accept-Encoding"); // needed for proxies
            }
            else {
                response.setContentLength(getSize(key));
            }
            IOUtils.copy(fin, out);
            out.flush();
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
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fin);
        }

        return true;
    }

    /**
     * @param lastModified last modification time (ms from 1970)
     * @param size original size
     * @param compressedSize compressed size
     * @param lastModified
     */
    private void addToCachedURIList(String key, long lastModified, int size, int compressedSize) {
        CachedItem entry = new CachedItem(lastModified, size, compressedSize);

        if (log.isDebugEnabled()) {
            log.debug("Caching URI [{}]", key); // $NON-NLS-1$
        }

        this.cachedURIList.put(key, entry);
    }

    private void clearCachedURIList() {
        this.cachedURIList.clear();
    }

    /**
     * Empties the cache for the specified resource. Currenty it expects the entire path, including cache location. This
     * is never used for simple cache since there are no way to find out how to flush related items
     */
    public void remove(String key) {
        File file = this.getFile(key, false);
        try {
            if (file.isDirectory()) {
                FileUtils.deleteDirectory(file);
                clearCachedURIList();
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Flushing {}", file.getPath()); // $NON-NLS-1$
                }

                file.delete();
                removeFromCachedURIList(key);
            }
        }
        catch (Exception e) {
            log.error("Failed to flush [" + file.getPath() + "]: " + e.getMessage(), e); // $NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @return size as on disk
     */
    private int getCompressedSize(String key) {
        CachedItem item = (CachedItem) this.cachedURIList.get(key);
        if (item == null) {
            return -1;
        }

        return item.compressedSize;
    }

    private File getFile(String key, boolean compressed) {
        String fileName = key;
        // we add .cache extension to directories to distinguish them from files cached without extensions
        fileName = StringUtils.removeStart(
            StringUtils.replace(fileName, "/", CACHE_DIRECTORY_EXTENTION + "/"),
            CACHE_DIRECTORY_EXTENTION);
        File cacheFile;
        if (compressed) {
            cacheFile = new File(getCacheDirectory(), fileName + ".gzip");
        }
        else {
            cacheFile = new File(getCacheDirectory(), fileName);
        }
        return cacheFile;
    }

    /**
     * @return size as on disk
     */
    private int getSize(String key) {
        CachedItem item = (CachedItem) this.cachedURIList.get(key);
        if (item == null) {
            return -1;
        }

        return item.size;
    }

    /**
     * @param key
     */
    private void removeFromCachedURIList(String key) {
        this.cachedURIList.remove(key);
    }

    private File getCacheDirectory() {

        // add a fixed "mgnl-cache" subdir: the cache dir is removed on flush and if the user choose an existing
        // and not empty dir we should be sure to not remove everything!
        return new File(Path.getCacheDirectory(), "mgnl-cache");
    }

    /**
     * override File.mkdir to solve race condition check http://jira.magnolia.info/browse/MAGNOLIA-1446
     */
    private boolean mkdirs(File file) {
        if (file.exists()) {
            return file.isDirectory(); // return true only if it's a directory
        }
        if (file.mkdir() || (file.exists() && file.isDirectory())) { // the additional file.exists() check could
            // prevent concurrency problems
            return true;
        }
        File canonFile;
        try {
            canonFile = file.getCanonicalFile();
        }
        catch (IOException e) {
            log.warn("Can't get canonical file for path {}", file.getAbsolutePath());
            return false;
        }
        File parent = canonFile.getParentFile();
        if (null == parent) {
            log.warn("Parent of {} (canonical file for path {}) is null!", canonFile.getAbsoluteFile(), file
                .getAbsolutePath());
            return false;
        }

        if (mkdirs(parent)) {
            return canonFile.mkdir();
        }

        return false;
    }

    private static class CachedItem {

        /**
         * Compressed size.
         */
        protected int compressedSize;

        /**
         * Original size.
         */
        protected int size;

        /**
         * Time in milliseconds.
         */
        protected long time;

        public CachedItem(long time, int size, int compressedSize) {
            this.time = time;
            this.size = size;
            this.compressedSize = compressedSize;
        }
    }

}
