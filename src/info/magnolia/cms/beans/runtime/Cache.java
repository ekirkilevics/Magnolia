/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */





package info.magnolia.cms.beans.runtime;


import info.magnolia.cms.util.Path;

import javax.servlet.http.HttpServletRequest;
import java.util.Hashtable;



/**
 * User: sameercharles
 * Date: Jul 16, 2003
 * Time: 11:24:23 AM
 * @author Sameer Charles
 * @version 1.5
 */


public class Cache {


    public static Hashtable cachedURIList = new Hashtable();


    private long time;
    private int size;
    private int compressedSize;


    /**
     *
     * @return true is the request is cached
     */
    public static boolean isCached(HttpServletRequest request) {
        return isCached(Path.getURI(request));
    }



    /**
     *
     * @return true is the request URI is cached
     */
    public static boolean isCached(String URI) {
        return (Cache.cachedURIList.get(URI) != null);
    }



    /**
     *
     * @param URI
     * @param lastModified
     */
    public static void addToCachedURIList(String URI, long lastModified, int size, int compressedSize) {
        Cache cacheMap = new Cache();
        cacheMap.time = lastModified;
        cacheMap.size = size;
        cacheMap.compressedSize = compressedSize;
        Cache.cachedURIList.put(URI,cacheMap);
    }



    /**
     *
     * @param URI
     */
    public static void removeFromCachedURIList(String URI) {
        Cache.cachedURIList.remove(URI);
    }



    /**
     *
     */
    public static void clearCachedURIList() {
        Cache.cachedURIList.clear();
    }


    /**
     *
     * @return creation time in miliseconds
     * */
    public static long getCreationTime(HttpServletRequest request) {
        return getCreationTime(Path.getURI(request));
    }



    /**
     *
     * @return creation time in miliseconds
     * */
    public static long getCreationTime(String URI) {
        Cache cacheMap = (Cache) cachedURIList.get(URI);
        if (cacheMap == null)
            return -1;
        return cacheMap.time;
    }


    /**
     * @return size as on disk
     * */
    public static int getSize(HttpServletRequest request) {
        return getSize(Path.getURI(request));
    }



    /**
     * @return size as on disk
     * */
    public static int getSize(String URI) {
        Cache cacheMap = (Cache) cachedURIList.get(URI);
        return cacheMap.size;
    }


    /**
     * @return size as on disk
     * */
    public static int getCompressedSize(HttpServletRequest request) {
        return getCompressedSize(Path.getURI(request));
    }



    /**
     * @return size as on disk
     * */
    public static int getCompressedSize(String URI) {
        Cache cacheMap = (Cache) cachedURIList.get(URI);
        return cacheMap.compressedSize;
    }




}
