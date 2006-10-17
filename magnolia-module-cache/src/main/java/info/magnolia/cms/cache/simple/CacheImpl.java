package info.magnolia.cms.cache.simple;

import info.magnolia.cms.cache.Cache;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheKey;
import info.magnolia.cms.cache.CacheableEntry;
import info.magnolia.cms.core.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>Cache</code> implementation based on the Magnolia 2.x filesystem cache.
 * @author Andreas Brenk
 * @author Fabrizio Giustina
 * @author Sameer Charles
 * @since 3.0
 * $Id:CacheImpl.java 6314 2006-09-11 08:24:51Z scharles $
 */
public class CacheImpl implements Cache {

    private static final Logger log = LoggerFactory.getLogger(CacheImpl.class);

    /**
     * Cached items: the key is the URI of the cached request and the entry is a Cache instance
     */
    private Map cachedURIList = new Hashtable();

    /**
     * This is used to syncronize between cache flushing and creating
     * We cannot use syncronized blocks here because it will block other threads even to cache simultaniusly
     * */
    private static boolean currentlyRemoving;

    private CacheConfig config;

    /**
     * Cache this request in default and optimized stores
     * @param key
     * @param entry
     * @param canCompress
     */
    public void cacheRequest(CacheKey key, CacheableEntry entry, boolean canCompress) {
        // This implementation flushes entire cache, its safe to simply check on one flag, for other
        // implementations we need to check and synchronize on CacheKey
        if (currentlyRemoving) {
            return; // simply ignore this cache request
        }
        int compressedSize = 0;
        File file = getFile(key, false);

        if (file.isDirectory()) {
            // additional check, if file name is something like "/"
            return;
        }

        // it should not cache again if the resource already existing
        // its a responsibility of a cache manager to call remove or flush if resource needs to be
        // invalidated
        if (!file.exists()) {
            FileOutputStream out = null;
            if (log.isDebugEnabled()) {
                log.debug("creating file {}", file.getAbsolutePath());
            }
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();

                out = new FileOutputStream(file);
                out.write(entry.getOut());
                out.flush();
            } catch (Exception e) {
                log.error("Failed to cache "+key.toString(), e);
            } finally {
                IOUtils.closeQuietly(out);
            }
        }

        if (canCompress) {
            File gzipFile = getFile(key, true);
            if (!gzipFile.exists()) {
                GZIPOutputStream gzipOut = null;
                try {
                    gzipFile.getParentFile().mkdirs();
                    gzipFile.createNewFile();
                    FileOutputStream out = new FileOutputStream(gzipFile);
                    gzipOut = new GZIPOutputStream(out);
                    gzipOut.write(entry.getOut());
                    gzipOut.flush();
                } catch (Exception e) {
                    log.error("Failed to create compressed entry for "+key.toString(), e);
                } finally {
                    IOUtils.closeQuietly(gzipOut);
                }
            }

            compressedSize = (new Long(gzipFile.length())).intValue();
        }

        addToCachedURIList(key, new Date().getTime(), (int) file.length(), compressedSize);
    }

    public synchronized void flush() {
        try {
            File cacheDir = getCacheDirectory();
            currentlyRemoving = true;
            if (cacheDir.exists() && cacheDir.isDirectory()) {
                FileUtils.deleteDirectory(cacheDir);
            }

            // this will create cache start directory again
            cacheDir.mkdirs();

            // clear in-memory cache also
            clearCachedURIList();
        }
        catch (Throwable t) {
            log.error(t.getMessage(), t);
        } finally {
            currentlyRemoving = false;
        }
    }

    public long getCreationTime(CacheKey key) {
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
            boolean result = cacheDir.mkdirs();
            if (!result) {
                log.error("Failed to create cache directory location {}", cacheDir.getAbsolutePath());
            }
        }
    }

    public boolean isCached(CacheKey key) {
        return this.cachedURIList.get(key) != null;
    }

    public void stop() {
        // NOTE: it should not flush the cache here. otherwise on server stop all filesystem
        // cache is removed
    }

    /**
     * Spools cached data back to the client. This only works if specified request is a GET request and does not have
     * any request parameter, else it wont write anything on the output stream.
     * @param key HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> is successful
     */
    public boolean streamFromCache(CacheKey key, HttpServletResponse response, boolean canCompress) {

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
            if (canCompress) {
                response.setContentLength(getCompressedSize(key));
                response.setHeader("Content-Encoding", "gzip");
                IOUtils.copy(fin, out);
            }
            else {
                response.setContentLength(getSize(key));
                IOUtils.copy(fin, out);
            }
            out.flush();
        }
        catch (IOException e) {
            log.error("Error while reading cache for: '" + key + "'.", e);
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
    private void addToCachedURIList(CacheKey key, long lastModified, int size, int compressedSize) {
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
     * Empties the cache for the specified resource. Currenty it expects the entire path, including cache location.
     * This is never used for simple cache since there are no way to find out how to flush related items
     */
    public void remove(CacheKey key) {
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
    private int getCompressedSize(CacheKey key) {
        CachedItem item = (CachedItem) this.cachedURIList.get(key);
        if (item == null) {
            return -1;
        }

        return item.compressedSize;
    }

    private File getFile(CacheKey key, boolean compressed) {

        String fileName = key.toString();
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
    private int getSize(CacheKey key) {
        CachedItem item = (CachedItem) this.cachedURIList.get(key);
        if (item == null) {
            return -1;
        }

        return item.size;
    }

    /**
     * @param key
     */
    private void removeFromCachedURIList(CacheKey key) {
        this.cachedURIList.remove(key);
    }

    private File getCacheDirectory() {

        // add a fixed "mgnl-cache" subdir: the cache dir is removed on flush and if the user choose an existing
        // and not empty dir we should be sure to not remove everything!
        return new File(Path.getCacheDirectory(), "mgnl-cache");
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
