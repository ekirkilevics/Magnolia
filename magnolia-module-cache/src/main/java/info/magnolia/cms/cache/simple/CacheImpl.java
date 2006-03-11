package info.magnolia.cms.cache.simple;

import info.magnolia.cms.cache.AbstractCache;
import info.magnolia.cms.cache.CacheConfig;
import info.magnolia.cms.cache.CacheRequest;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.SecureURI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>Cache</code> implementation based on the Magnolia 2.x filesystem cache.
 * @author Andreas Brenk
 * @since 3.0
 */
public class CacheImpl extends AbstractCache {

    private static final Logger log = LoggerFactory.getLogger(CacheImpl.class);

    /**
     * Default cache files location under main cache directory
     */
    private static final String DEFAULT_STORE = "/default";

    /**
     * Optimized cache files location under main cache directory
     */
    private static final String COMPRESSED_STORE = "/optimized";

    /**
     * Cached items: the key is the URI of the cached request and the entry is a Cache instance
     */
    private Map cachedURIList = new Hashtable();

    private CacheConfig config;

    /**
     * Cache this request in default and optimized stores
     * @param request duplicate request created for cache
     */
    public void cacheRequest(CacheRequest request) {
        // if (Cache.isCached(request) || CacheManager.hasRedirect(request.getHttpServletRequest())) { if
        // (hasRedirect(request.getHttpServletRequest())) { log.warn("Not caching... request has redirect!"); return; }

        final String uri = request.getURI();

        FileOutputStream out = null;
        int compressedSize = 0;
        try {

            // this check is already performed by the caller!
            // if (!CacheConfiguration.isCacheable(request)) {
            // if (log.isDebugEnabled())
            // log.debug("Request:" + request.getURI() + " not cacheable");
            // return;
            // }
            File file = getDestinationFile(uri, DEFAULT_STORE);

            if (!file.exists()) {
                file.createNewFile();
                out = new FileOutputStream(file);
                boolean success = streamRequest(request, out);
                out.flush();
                out.close();
                if (!success) {

                    // don't leave bad or incomplete files!
                    file.delete();
                    log.error(MessageFormat.format("NOT Caching uri [{0}] due to a previous error", // $NON-NLS-1$
                        new Object[]{uri}));

                    // caching failed, return
                    return;
                }

                if (log.isInfoEnabled()) {
                    log.info(MessageFormat.format("Successfully cached URI [{0}]", // $NON-NLS-1$
                        new Object[]{uri}));
                }
            }

            if (this.config.canCompress(request.getExtension())) {
                File gzipFile = getDestinationFile(uri, COMPRESSED_STORE);
                if (!gzipFile.exists()) {
                    gzipFile.createNewFile();
                    out = new FileOutputStream(gzipFile);
                    GZIPOutputStream gzipOut = new GZIPOutputStream(out);
                    boolean success = streamRequest(request, gzipOut);
                    gzipOut.flush();
                    gzipOut.close();
                    if (!success) {

                        // don't leave bad or incomplete files!
                        gzipFile.delete();
                        log.error(MessageFormat.format("NOT Caching compressed uri [{0}] due to a previous error", // $NON-NLS-1$
                            new Object[]{uri}));

                        // caching failed, return
                        return;
                    }

                    if (log.isInfoEnabled()) {
                        log.info(MessageFormat.format("Successfully cached compressed URI [{0}]", // $NON-NLS-1$
                            new Object[]{uri}));
                    }
                }

                compressedSize = (new Long(gzipFile.length())).intValue();
            }

            addToCachedURIList(uri, new Date().getTime(), (int) file.length(), compressedSize);
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public void flushAll() {
        try {
            flushResource(Path.getCacheDirectoryPath());

            // this will create cache start directory again
            validatePath(Path.getCacheDirectoryPath());
            validatePath(Path.getCacheDirectoryPath() + DEFAULT_STORE);
            validatePath(Path.getCacheDirectoryPath() + COMPRESSED_STORE);

            // clear in-memory cache also
            clearCachedURIList();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public long getCreationTime(CacheRequest request) {
        CacheEntry entry = (CacheEntry) this.cachedURIList.get(request.getURI());
        if (entry == null) {
            return -1;
        }

        return entry.time;
    }

    public void start(CacheConfig config) {
        this.config = config;

        try {
            validatePath(Path.getCacheDirectoryPath());
        }
        catch (Exception e) {
            log.error("Failed to validate cache directory location: " + e.getMessage(), e);
        }
    }

    public boolean isCached(CacheRequest request) {
        return this.cachedURIList.get(request.getURI()) != null;
    }

    public void stop() {
        flushAll();
    }

    /**
     * Checks if the page has "redirectURL" property set
     * @param request HttpServletRequest
     * @return true if it has redirect
     */
    // private boolean hasRedirect(HttpServletRequest request) {
    // Object obj = request.getAttribute(Aggregator.ACTPAGE);
    // if (obj == null) {
    // return false; // some other resource
    // }
    //
    // Content page = (Content) obj;
    //
    // if (StringUtils.isEmpty(page.getNodeData("redirectURL").getString())) { // $NON-NLS-1$
    // return false;
    // }
    //
    // return true;
    //
    // }
    /**
     * Spools cached data back to the client. This only works if specified request is a GET request and does not have
     * any request parameter, else it wont write anything on the output stream.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException
     * @return <code>true</code> is successful
     * @throws IOException
     */
    public boolean streamFromCache(CacheRequest request, HttpServletResponse response) {
        boolean compress = request.useGZIP() && this.config.canCompress(request.getExtension());
        FileInputStream fin = null;
        try {
            File file;
            if (compress) {
                file = new File(Path.getCacheDirectoryPath() + COMPRESSED_STORE + request.getURI());
            }
            else {
                file = new File(Path.getCacheDirectoryPath() + DEFAULT_STORE + request.getURI());
            }

            if (!file.exists()) {
                return false;
            }

            if (file.length() < 4) {
                return false;
            }

            if (log.isDebugEnabled()) {
                log.debug("Streaming from cache the file:" + file.getAbsolutePath()); // $NON-NLS-1$
            }

            fin = new FileInputStream(file);
            if (compress) {
                response.setContentLength(getCompressedSize(request));
                response.setHeader("Content-Encoding", "gzip");
                stream(fin, response.getOutputStream());
            }
            else {
                response.setContentLength(getSize(request));
                stream(fin, response.getOutputStream());
            }
        }
        catch (IOException e) {
            log.error("Error while reading cache for: '" + request.getURI() + "'.", e);
            return false;
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                }
                catch (IOException e) {
                    // ignore
                }
            }
        }

        return true;
    }

    /**
     * @param uri request URI
     * @param lastModified last modification time (ms from 1970)
     * @param size original size
     * @param compressedSize compressed size
     * @param lastModified
     */
    private void addToCachedURIList(String uri, long lastModified, int size, int compressedSize) {
        CacheEntry entry = new CacheEntry(lastModified, size, compressedSize);

        if (log.isDebugEnabled()) {
            log.debug("Caching URI [" + uri + "]"); // $NON-NLS-1$
        }

        this.cachedURIList.put(uri, entry);
    }

    private void clearCachedURIList() {
        this.cachedURIList.clear();
    }

    /**
     * Recursively deletes all files under the specified directory
     * @param directory directory where files should be deleted
     */
    private void emptyDirectory(File directory) {
        File[] children = directory.listFiles();

        // children can be null if File is not a directory or if it has been already deleted
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if ((children[i] != null) && children[i].isDirectory()) {
                    emptyDirectory(children[i]);
                    children[i].delete();
                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug("Flushing - " + children[i].getPath()); // $NON-NLS-1$
                    }

                    String path = StringUtils.substringAfter(children[i].getPath(), Path.getCacheDirectoryPath());
                    removeFromCachedURIList(path);
                    children[i].delete();
                }
            }
        }
    }

    /**
     * Empties the cache for the specified resource. Currenty it expects the entire path, including cache location.
     * @param uri request URI
     */
    private void flushResource(String uri) {
        File file = new File(uri);
        try {
            if (file.isDirectory()) {
                emptyDirectory(file);
                file.delete();
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Flushing - " + uri); // $NON-NLS-1$
                }

                file.delete();
                removeFromCachedURIList(uri);
            }
        }
        catch (Exception e) {
            log.error("Failed to flush [" + uri + "]: " + e.getMessage(), e); // $NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @return size as on disk
     */
    private int getCompressedSize(CacheRequest request) {
        CacheEntry entry = (CacheEntry) this.cachedURIList.get(request.getURI());
        if (entry == null) {
            return -1;
        }

        return entry.compressedSize;
    }

    /**
     * Creates file hierarchy for the given URI in cache store
     * @param uri request uri
     * @param type could be either CacheHandler.DEFAULT_STORE or CacheHandler.COMPRESSED_STORE
     * @return newly created file
     */
    private File getDestinationFile(String uri, String type) {
        validatePath(Path.getCacheDirectoryPath());
        validatePath(Path.getCacheDirectoryPath() + type);
        String[] items = uri.split("/"); // $NON-NLS-1$
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (; i < (items.length - 1); i++) {
            if (StringUtils.isEmpty(items[i])) {
                continue;
            }

            buffer.append("/" + items[i]); // $NON-NLS-1$
            validatePath(Path.getCacheDirectoryPath() + type + buffer.toString());
        }

        buffer.append("/" + items[i]); // $NON-NLS-1$
        return (new File(Path.getCacheDirectoryPath() + type + buffer.toString()));
    }

    /**
     * @return size as on disk
     */
    private int getSize(CacheRequest request) {
        CacheEntry entry = (CacheEntry) this.cachedURIList.get(request.getURI());
        if (entry == null) {
            return -1;
        }

        return entry.size;
    }

    /**
     * @param uri request URI
     */
    private void removeFromCachedURIList(String uri) {
        this.cachedURIList.remove(uri);
    }

    /**
     * Stream given URI
     * @param uri to be streamed
     * @param out this could be any stream type inherited from java.io.OutputStream
     * @param request HttpServletRequest
     * @return <code>true</code> if resource is successfully returned to the client, <code>false</code> otherwise
     */
    private boolean streamRequest(CacheRequest request, OutputStream out) {

        // if (StringUtils.isEmpty(domain)) {
        // domain = getAppURL(request);
        // }

        final String uri = request.getURI();
        try {
            URL url = new URL(this.config.getDomain() + uri);
            if (log.isDebugEnabled()) {
                log.debug("Streaming uri:" + url.toExternalForm()); // $NON-NLS-1$
            }

            URLConnection urlConnection = url.openConnection();
            if (SecureURI.isProtected(uri)) {
                urlConnection.setRequestProperty("Authorization", request.getAuthorization()); // $NON-NLS-1$
                // //$NON-NLS-2$
            }

            stream(urlConnection.getInputStream(), out);

            return true;
        }
        catch (IOException e) {
            log.error(MessageFormat.format("Failed to stream [{0}] due to a {1}: {2}", // $NON-NLS-1$
                new Object[]{uri, e.getClass().getName(), e.getMessage()}), e);
        }

        return false;
    }

    /**
     * Create a directory specified by the path
     * @param path to the directory
     */
    private void validatePath(String path) {
        File file = new File(path);
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                log.error("Can not create directory {}", path); // $NON-NLS-1$
            }
        }
    }

    private static class CacheEntry {

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

        public CacheEntry(long time, int size, int compressedSize) {
            this.time = time;
            this.size = size;
            this.compressedSize = compressedSize;
        }
    }
}
