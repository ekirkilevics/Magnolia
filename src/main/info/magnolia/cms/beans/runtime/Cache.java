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


/**
 * @author Sameer Charles
 * @version 1.5
 */
public final class Cache {

    private static Map cachedURIList = new Hashtable();

    /**
     * holds all URI's which are being cached by cache process this list is updated by CacheHandler on start and end of
     * cache process
     */
    private static Map inProcessURIList = new Hashtable();

    private long time;

    private int size;

    private int compressedSize;

    /**
     * Utility class, don't instantiate.
     */
    private Cache() {
        // unused
    }

    /**
     * @return true is the request is cached
     */
    public static boolean isCached(HttpServletRequest request) {
        return isCached(Path.getURI(request));
    }

    /**
     * @return true is the request URI is cached
     */
    public static boolean isCached(String uri) {
        return (Cache.cachedURIList.get(uri) != null);
    }

    /**
     * @return true is the request URI is being cached
     */
    public static boolean isInCacheProcess(HttpServletRequest request) {
        return isInCacheProcess(Path.getURI(request));
    }

    /**
     * @return true is the request URI is being cached
     */
    public static boolean isInCacheProcess(String uri) {
        return (Cache.inProcessURIList.get(uri) != null);
    }

    /**
     * @param uri
     */
    public static void addToInProcessURIList(String uri) {
        Cache.inProcessURIList.put(uri, StringUtils.EMPTY);
    }

    /**
     * @param uri
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
     * @param uri
     * @param lastModified
     */
    public static void addToCachedURIList(String uri, long lastModified, int size, int compressedSize) {
        Cache cacheMap = new Cache();
        cacheMap.time = lastModified;
        cacheMap.size = size;
        cacheMap.compressedSize = compressedSize;
        Cache.cachedURIList.put(uri, cacheMap);
    }

    /**
     * @param uri
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
     * @return creation time in miliseconds
     */
    public static long getCreationTime(HttpServletRequest request) {
        return getCreationTime(Path.getURI(request));
    }

    /**
     * @return creation time in miliseconds
     */
    public static long getCreationTime(String uri) {
        Cache cacheMap = (Cache) cachedURIList.get(uri);
        if (cacheMap == null) {
            return -1;
        }
        return cacheMap.time;
    }

    /**
     * @return size as on disk
     */
    public static int getSize(HttpServletRequest request) {
        return getSize(Path.getURI(request));
    }

    /**
     * @return size as on disk
     */
    public static int getSize(String uri) {
        Cache cacheMap = (Cache) cachedURIList.get(uri);
        return cacheMap.size;
    }

    /**
     * @return size as on disk
     */
    public static int getCompressedSize(HttpServletRequest request) {
        return getCompressedSize(Path.getURI(request));
    }

    /**
     * @return size as on disk
     */
    public static int getCompressedSize(String uri) {
        Cache cacheMap = (Cache) cachedURIList.get(uri);
        return cacheMap.compressedSize;
    }
}
