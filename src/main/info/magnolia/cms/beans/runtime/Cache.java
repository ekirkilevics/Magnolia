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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.core.Path;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version $Revision $ ($Author $)
 */
public final class Cache {

    /**
     * Cached items: the key is the URI of the cached request and the entry is a Cache instance
     */
    private static Map cachedURIList = new Hashtable();

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Cache.class);

    /**
     * holds all URI's which are being cached by cache process this list is updated by CacheHandler on start and end of
     * cache process
     */
    private static Map inProcessURIList = new Hashtable();

    /**
     * Time in milliseconds.
     */
    private long time;

    /**
     * Original size.
     */
    private int size;

    /**
     * Compressed size.
     */
    private int compressedSize;

    /**
     * Utility class, don't instantiate.
     */
    private Cache() {
        // unused
    }

    /**
     * @param request HttpServletRequest
     * @return true is the request is cached
     */
    public static boolean isCached(HttpServletRequest request) {
        return Cache.cachedURIList.get(Path.getURI(request)) != null;
    }

    /**
     * @return true is the request URI is being cached
     * @param request HttpServletRequest
     */
    public static boolean isInCacheProcess(HttpServletRequest request) {
        return Cache.inProcessURIList.get(Path.getURI(request)) != null;
    }

    /**
     * @param uri request URI
     */
    public static void addToInProcessURIList(String uri) {
        Cache.inProcessURIList.put(uri, StringUtils.EMPTY);
    }

    /**
     * @param uri request URI
     */
    public static void removeFromInProcessURIList(String uri) {
        Cache.inProcessURIList.remove(uri);
    }

    /**
     *
     */
    public static void clearInProcessURIList() {
        Cache.inProcessURIList.clear();
    }

    /**
     * @param uri request URI
     * @param lastModified last modification time (ms from 1970)
     * @param size original size
     * @param compressedSize compressed size
     * @param lastModified
     */
    public static void addToCachedURIList(String uri, long lastModified, int size, int compressedSize) {
        Cache cacheMap = new Cache();
        cacheMap.time = lastModified;
        cacheMap.size = size;
        cacheMap.compressedSize = compressedSize;
        if (log.isDebugEnabled()) {
            log.debug("Caching URI [" + uri + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        Cache.cachedURIList.put(uri, cacheMap);
    }

    /**
     * @param uri request URI
     */
    public static void removeFromCachedURIList(String uri) {
        Cache.cachedURIList.remove(uri);
    }

    /**
     *
     */
    public static void clearCachedURIList() {
        Cache.cachedURIList.clear();
    }

    /**
     * @param request HttpServletRequest
     * @return creation time in milliseconds
     */
    public static long getCreationTime(HttpServletRequest request) {
        Cache cacheMap = (Cache) cachedURIList.get(Path.getURI(request));
        if (cacheMap == null) {
            return -1;
        }
        return cacheMap.time;
    }

    /**
     * @param request HttpServletRequest
     * @return size as on disk
     */
    public static int getSize(HttpServletRequest request) {
        Cache cacheMap = (Cache) cachedURIList.get(Path.getURI(request));
        return cacheMap.size;
    }

    /**
     * @param request HttpServletRequest
     * @return size as on disk
     */
    public static int getCompressedSize(HttpServletRequest request) {
        Cache cacheMap = (Cache) cachedURIList.get(Path.getURI(request));
        return cacheMap.compressedSize;
    }

    /**
     * @param uri request URI
     * @return creation time in miliseconds
     * @deprecated use getCreationTime(HttpServletRequest). Cache could decide to handle requests internally using not
     * only the request URI
     */
    public static long getCreationTime(String uri) {
        Cache cacheMap = (Cache) cachedURIList.get(uri);
        if (cacheMap == null) {
            return -1;
        }
        return cacheMap.time;
    }

    /**
     * @param uri request URI
     * @return size as on disk
     * @deprecated use getSize(HttpServletRequest). Cache could decide to handle requests internally using not only the
     * request URI
     */
    public static int getSize(String uri) {
        Cache cacheMap = (Cache) cachedURIList.get(uri);
        return cacheMap.size;
    }

    /**
     * @param uri request URI
     * @return size as on disk
     * @deprecated use getCompressedSize(HttpServletRequest). Cache could decide to handle requests internally using not
     * only the request URI
     */
    public static int getCompressedSize(String uri) {
        Cache cacheMap = (Cache) cachedURIList.get(uri);
        return cacheMap.compressedSize;
    }

    /**
     * @param uri request URI
     * @return true is the request URI is cached
     * @deprecated use isCached(HttpServletRequest). Cache could decide to handle requests internally using not only the
     * request URI
     */
    public static boolean isCached(String uri) {
        return Cache.cachedURIList.get(uri) != null;
    }

    /**
     * @param uri request URI
     * @return true is the request URI is being cached
     * @deprecated use isInCacheProcess(HttpServletRequest). Cache could decide to handle requests internally using not
     * only the request URI
     */
    public static boolean isInCacheProcess(String uri) {
        return Cache.inProcessURIList.get(uri) != null;
    }

}
