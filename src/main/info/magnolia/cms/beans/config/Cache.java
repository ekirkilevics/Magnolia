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




package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.CacheHandler;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Path;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import info.magnolia.cms.Aggregator;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;


/**
 * Date: Jul 7, 2004
 * Time: 5:50:58 PM
 *
 * @author Sameer Charles
 * @version 2.0
 */



public class Cache {


    private static final String CONFIG_PATH = "server/cache/level1";
    private static final String CACHE_MAPPING_NODE = "URI";
    private static final String COMPRESSION_LIST_NODE = "compression";

    private static final String ALLOW_LIST = "allow";
    private static final String DENY_LIST = "deny";
    private static final String ACTIVE = "active";
    private static final String DOMAIN = "domain";


    private static Logger log = Logger.getLogger(Cache.class);
    private static Hashtable cachedCacheableURIMapping = new Hashtable();

    /**
     * compression wont work for these pre compressed formats
     *
     * */
    private static final Hashtable compressionList = new Hashtable();


    private static boolean isCacheable;
    private static String domain;



    protected static void init() {
        cachedCacheableURIMapping.clear();
        compressionList.clear();
        log.info("Config : loading cache mapping");
        try {
            Content startPage
                    = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getPage(CONFIG_PATH);
            isCacheable = startPage.getNodeData(ACTIVE).getBoolean();
            if (isCacheable) {
                domain = startPage.getNodeData(DOMAIN).getString();
                ContentNode contentNode = startPage.getContentNode(CACHE_MAPPING_NODE+"/"+ALLOW_LIST);
                cacheCacheableURIMappings(contentNode, true);
                contentNode = startPage.getContentNode(CACHE_MAPPING_NODE+"/"+DENY_LIST);
                cacheCacheableURIMappings(contentNode, false);
                ContentNode compressionListNode = startPage.getContentNode(COMPRESSION_LIST_NODE);
                updateCompressionList(compressionListNode);
                // todo sort assending so there wont be too much work on comparing
            }
            log.info("Config : cache mapping loaded");
        } catch (RepositoryException re) {
            log.error("Config : Failed to load cache mapping or no mapping defined");
            log.error(re.getMessage(), re);
        }
    }



    public static void reload() {
        log.info("Config : reloading cache mapping");
        Cache.init();
    }



   /**
     *
     * @param nodeList to be added in cache
     */
    private static void cacheCacheableURIMappings(ContentNode nodeList, boolean allow)
           throws AccessDeniedException {
        if (nodeList == null)
            return;
        Iterator it = nodeList.getChildren().iterator();
        while (it.hasNext()) {
            ContentNode container = (ContentNode)it.next();
            NodeData URI = container.getNodeData("URI");
            String pattern = RegexWildcardPattern.getEncodedString(URI.getString());
            Pattern p = Pattern.compile(pattern);
            cachedCacheableURIMapping.put(p,new Boolean(allow));
        }
        try {
            CacheHandler.validatePath(CacheHandler.CACHE_DIRECTORY);
        } catch(Exception e) {
            log.error("Failed to validate cache directory location");
            log.error(e.getMessage(), e);
        }
    }



    private static void updateCompressionList(ContentNode list) throws AccessDeniedException {
        if (list == null)
            return;
        Iterator it = list.getChildren().iterator();
        while (it.hasNext()) {
            ContentNode node = (ContentNode) it.next();
            compressionList.put(node.getNodeData("extension").getString(),
                    node.getNodeData("type").getString());
        }
    }



    public static boolean applyCompression(String key) {
        return (compressionList.get(key.trim().toLowerCase())!=null);
    }



    /**
     * <p>
     * if this instance can be cached
     * todo check for Level1 and Level2 caching
     * </p>
     * */
    public static boolean isCacheable() {
        return isCacheable;
    }



    public static String getDomain() {
        return domain;
    }


    /**
      * @return true if the requested URI can be added to cache
      */
     public static boolean isCacheable(HttpServletRequest request) {
         /**
          * first check for MIMEMappings, extension must exist otherwise its
          * a fake request
          * */
         if (MIMEMapping.getMIMEType((String)request.getAttribute(Aggregator.EXTENSION)) == null) {
             return false;
         } else {
             Enumeration listEnum = cachedCacheableURIMapping.keys();
             String uri = Path.getURI(request);
             boolean isAllowed = false;
             int lastMatchedPatternlength = 0;
             while (listEnum.hasMoreElements()) {
                 Pattern p = (Pattern) listEnum.nextElement();
                 if (p.matcher(uri).matches()) {
                     // todo this wont work if pattern has more than one windcards
                     int patternLength = getPatternLength(p.pattern());
                     if (lastMatchedPatternlength < patternLength) {
                         lastMatchedPatternlength = patternLength;
                         isAllowed = ((Boolean)cachedCacheableURIMapping.get(p)).booleanValue();
                     }
                 }
             }
             return isAllowed;
         }
     }


     private static int getPatternLength(String pattern) {
         int length = pattern.length();
         if (pattern.indexOf(RegexWildcardPattern.getMultipleCharPattern())>-1)
             return length - RegexWildcardPattern.getMultipleCharPattern().length();

         return length;
     }



}
