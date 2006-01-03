/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.beans.runtime.Cache;
import info.magnolia.cms.security.SecureURI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * CacheHandle checks if the data is already cached, if yes it spools the data back to the requester either compressed
 * or as is. If not it caches that request in default and optimized (only gzip for now) stores.
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public class CacheHandler extends Thread {

    /**
     * Cache directory file system path
     */
    public static final String CACHE_DIRECTORY = Path.getCacheDirectoryPath();

    /**
     * Default cache files location under main cache directory
     */
    private static final String DEFAULT_STORE = "/default"; //$NON-NLS-1$

    /**
     * Optimized cache files location under main cache directory
     */
    private static final String COMPRESSED_STORE = "/optimized"; //$NON-NLS-1$

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(CacheHandler.class);

    /**
     * Cache this request in default and optimized stores
     * @param request duplicate request created for cache
     */
    public static synchronized void cacheURI(HttpServletRequest request) {

        if (Cache.isCached(request) || CacheHandler.hasRedirect(request)) {
            return;
        }

        String uri = request.getRequestURI();

        // strip the context path
        if (uri.startsWith(request.getContextPath())) {
            uri = StringUtils.substringAfter(uri, request.getContextPath());
        }

        String repositoryURI = Path.getURI(request);

        FileOutputStream out = null;
        int size = 0;
        int compressedSize = 0;
        try {
            if (!info.magnolia.cms.beans.config.Cache.isCacheable(request)) {
                if (log.isDebugEnabled())
                    log.debug("Request:" + request.getServletPath() + " not cacheable");
                return;
            }
            File file = getDestinationFile(repositoryURI, DEFAULT_STORE);

            if (!file.exists()) {
                file.createNewFile();
                out = new FileOutputStream(file);
                boolean success = streamURI(uri, out, request);
                out.flush();
                IOUtils.closeQuietly(out);
                if (!success) {
                    // don't leave bad or incomplete files!
                    file.delete();
                    log.error(MessageFormat.format("NOT Caching uri [{0}] due to a previous error", //$NON-NLS-1$
                        new Object[]{uri}));

                    // caching failed, return
                    return;
                }
                if (log.isInfoEnabled()) {
                    log.info(MessageFormat.format("Successfully cached URI [{0}]", //$NON-NLS-1$
                        new Object[]{uri}));
                }
            }

            size = (int) file.length();

            if (info.magnolia.cms.beans.config.Cache.applyCompression(Path.getExtension(request))) {
                File gzipFile = getDestinationFile(repositoryURI, COMPRESSED_STORE);
                if (!gzipFile.exists()) {
                    gzipFile.createNewFile();
                    out = new FileOutputStream(gzipFile);
                    GZIPOutputStream gzipOut = new GZIPOutputStream(out);
                    boolean success = streamURI(uri, gzipOut, request);
                    gzipOut.flush();
                    IOUtils.closeQuietly(gzipOut);
                    if (!success) {
                        // don't leave bad or incomplete files!
                        gzipFile.delete();
                        log.error(MessageFormat.format("NOT Caching compressed uri [{0}] due to a previous error", //$NON-NLS-1$
                            new Object[]{uri}));

                        // caching failed, return
                        return;
                    }
                    if (log.isInfoEnabled()) {
                        log.info(MessageFormat.format("Successfully cached compressed URI [{0}]", //$NON-NLS-1$
                            new Object[]{uri}));
                    }
                }
                compressedSize = (new Long(gzipFile.length())).intValue();
            }
            Cache.addToCachedURIList(repositoryURI, new Date().getTime(), size, compressedSize);
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(out);
            Cache.removeFromInProcessURIList(repositoryURI);
        }
    }

    /**
     * Checks if the page has "redirectURL" property set
     * @param request HttpServletRequest
     * @return true if it has redirect
     */
    private static boolean hasRedirect(HttpServletRequest request) {
        Object obj = request.getAttribute(Aggregator.ACTPAGE);
        if (obj == null) {
            return false; // some other resource
        }
        Content page = (Content) obj;

        if (StringUtils.isEmpty(page.getNodeData("redirectURL").getString())) { //$NON-NLS-1$
            return false;
        }
        return true;

    }

    /**
     * Stream given URI
     * @param uri to be streamed
     * @param out this could be any stream type inherited from java.io.OutputStream
     * @param request HttpServletRequest
     * @return <code>true</code> if resource is successfully returned to the client, <code>false</code> otherwise
     */
    private static boolean streamURI(String uri, OutputStream out, HttpServletRequest request) {

        String domain = info.magnolia.cms.beans.config.Cache.getDomain();

        if (StringUtils.isEmpty(domain)) {
            domain = getAppURL(request);
        }
        // if misconfigured or webapp is configured as root
        domain = StringUtils.removeEnd(domain, "/");
        try {
            URL url = new URL(domain + uri);
            if (log.isDebugEnabled()) {
                log.debug("Streaming uri:" + url.toExternalForm()); //$NON-NLS-1$
            }
            URLConnection urlConnection = url.openConnection();
            if (SecureURI.isProtected(uri)) {
                urlConnection.setRequestProperty("Authorization", request.getHeader("Authorization")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            byte[] buffer = new byte[8192];
            int read = 0;
            InputStream in = urlConnection.getInputStream();
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return true;
        }
        catch (IOException e) {
            log.error(MessageFormat.format("Failed to stream [{0}] due to a {1}: {2}", //$NON-NLS-1$
                new Object[]{uri, e.getClass().getName(), e.getMessage()}), e);
        }

        return false;
    }

    /**
     * Returns the server url for the web application. Used when a cache domain is not configured.
     * @param request HttpServletRequest
     * @return the root webapp url [scheme]://[server]:[port]/[context]
     */
    private static String getAppURL(HttpServletRequest request) {
        StringBuffer url = new StringBuffer();
        int port = request.getServerPort();
        if (port < 0) {
            port = 80; // Work around java.net.URL bug
        }
        String scheme = request.getScheme();
        url.append(scheme);
        url.append("://"); //$NON-NLS-1$
        url.append(request.getServerName());
        if ((scheme.equals("http") && (port != 80)) || (scheme.equals("https") && (port != 443))) { //$NON-NLS-1$ //$NON-NLS-2$
            url.append(':');
            url.append(port);
        }
        url.append(request.getContextPath());

        return url.toString();
    }

    /**
     * Creates file hierarchy for the given URI in cache store
     * @param uri request uri
     * @param type could be either CacheHandler.DEFAULT_STORE or CacheHandler.COMPRESSED_STORE
     * @return newly created file
     */
    private static File getDestinationFile(String uri, String type) {
        validatePath(CACHE_DIRECTORY);
        validatePath(CACHE_DIRECTORY + type);
        String[] items = uri.split("/"); //$NON-NLS-1$
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (; i < (items.length - 1); i++) {
            if (StringUtils.isEmpty(items[i])) {
                continue;
            }
            buffer.append("/" + items[i]); //$NON-NLS-1$
            validatePath(CACHE_DIRECTORY + type + buffer.toString());
        }
        buffer.append("/" + items[i]); //$NON-NLS-1$
        return (new File(CACHE_DIRECTORY + type + buffer.toString()));
    }

    /**
     * Create a directory specified by the path
     * @param path to the directory
     */
    public static void validatePath(String path) {

        File file = new File(path);
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                log.error("Can not create directory - " + path); //$NON-NLS-1$
            }
        }
    }

    /**
     * Spools cached data back to the client. This only works if specified request is a GET request and does not have
     * any request parameter, else it wont write anything on the output stream.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> is successful
     */
    public static boolean streamFromCache(HttpServletRequest request, HttpServletResponse response) {
        // make sure not to stream anything from cache if it's a POST request or if it has parameters
        if (request.getMethod().toLowerCase().equals("post") || !request.getParameterMap().isEmpty()) { //$NON-NLS-1$
            return false;
        }

        boolean compress = canCompress(request);
        FileInputStream fin = null;
        try {
            File file;
            if (compress) {
                file = new File(CACHE_DIRECTORY + COMPRESSED_STORE + Path.getURI(request));
            }
            else {
                file = new File(CACHE_DIRECTORY + DEFAULT_STORE + Path.getURI(request));
            }
            if (!file.exists()) {
                return false;
            }
            if (file.length() < 4) {
                return false;
            }

            if (log.isDebugEnabled()) {
                log.debug("Streaming from cache the file:" + file.getAbsolutePath()); //$NON-NLS-1$
            }

            fin = new FileInputStream(file);
            if (compress) {
                response.setContentLength(Cache.getCompressedSize(request));
                sendCompressed(fin, response);
            }
            else {
                response.setContentLength(Cache.getSize(request));
                send(fin, response);
            }
        }
        catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while reading cache for " + e.getMessage(), e); //$NON-NLS-1$
            }
        }
        finally {
            IOUtils.closeQuietly(fin);
        }
        return true;
    }

    /**
     * Send data as GZIP output stream
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws IOException for errors while writing content to the output stream
     */
    private static void sendCompressed(InputStream is, HttpServletResponse res) throws IOException {
        res.setHeader("Content-Encoding", "gzip"); //$NON-NLS-1$ //$NON-NLS-2$
        send(is, res);
    }

    /**
     * Send data as is
     * @param is Input stream for the resource
     * @param res HttpServletResponse as received by the service method
     * @throws IOException for errors while writing content to the output stream
     */
    private static void send(InputStream is, HttpServletResponse res) throws IOException {
        ServletOutputStream os = res.getOutputStream();
        byte[] buffer = new byte[8192];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        os.flush();
        IOUtils.closeQuietly(os);
    }

    /**
     * Checks if the client from whom this request originated accept GZIP compression
     * @param request HttpServletRequest
     * @return true if client sends value "gzip" in Accept-Encoding header
     */
    private static boolean canCompress(HttpServletRequest request) {
        if (!info.magnolia.cms.beans.config.Cache.applyCompression(Path.getExtension(request))) {
            return false;
        }
        String encoding = request.getHeader("Accept-Encoding"); //$NON-NLS-1$
        if (encoding != null) {
            return StringUtils.contains(encoding.toLowerCase(), "gzip"); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Empties the cache for the specified resource. Currenty it expects the entire path, including cache location.
     * @param uri request URI
     */
    public static void flushResource(String uri) {
        File file = new File(uri);
        try {
            if (file.isDirectory()) {
                emptyDirectory(file);
                file.delete();
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Flushing - " + uri); //$NON-NLS-1$
                }
                file.delete();
                Cache.removeFromCachedURIList(uri);
                Cache.removeFromInProcessURIList(uri);
            }
        }
        catch (Exception e) {
            log.error("Failed to flush [" + uri + "]: " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Recursively deletes all files under the specified directory
     * @param directory directory where files should be deleted
     */
    private static void emptyDirectory(File directory) {
        File[] children = directory.listFiles();
        // children can be null if File is not a directory or if it has been already deleted
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i] != null && children[i].isDirectory()) {
                    emptyDirectory(children[i]);
                    children[i].delete();
                }
                else {
                    if (log.isDebugEnabled()) {
                        log.debug("Flushing - " + children[i].getPath()); //$NON-NLS-1$
                    }
                    String path = StringUtils.substringAfter(children[i].getPath(), Path.getCacheDirectoryPath());
                    Cache.removeFromCachedURIList(path);
                    Cache.removeFromInProcessURIList(path);
                    children[i].delete();
                }
            }
        }
    }

    /**
     * Flushes entire cache
     */
    public static void flushCache() {
        log.debug("Flushing entire cache"); //$NON-NLS-1$
        try {
            CacheHandler.flushResource(CACHE_DIRECTORY);
            // this will create cache start directory again
            CacheHandler.validatePath(CACHE_DIRECTORY);
            CacheHandler.validatePath(CACHE_DIRECTORY + DEFAULT_STORE);
            CacheHandler.validatePath(CACHE_DIRECTORY + COMPRESSED_STORE);

            // clear in-memory cache also
            Cache.clearCachedURIList();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
