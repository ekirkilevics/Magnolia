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
package info.magnolia.cms.security;

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


/**
 * User: Sameer Charles Date: Mar 22, 2004 Time: 10:55:12 AM
 * @author Sameer Charles
 */
public final class SecureURI {

    private static Map cachedContent;

    /**
     * Utility class, don't instantiate.
     */
    private SecureURI() {
        // unused
    }

    public static void init() {
        SecureURI.cachedContent = new Hashtable();
        SecureURI.cachedContent.clear();
    }

    public static void reload() {
        init();
    }

    /**
     * @param handle
     */
    private static synchronized void addToList(String handle) {
        UrlPattern pattern1 = new SimpleUrlPattern(handle);
        SecureURI.cachedContent.put(handle, pattern1);
    }

    /**
     * @param handle
     */
    private static synchronized void deleteFromList(String handle) {
        SecureURI.cachedContent.remove(handle);
    }

    /**
     * @param handle
     */
    public static void add(String handle) {
        SecureURI.addToList(handle);
    }

    /**
     * @param handle
     */
    public static void delete(String handle) {
        SecureURI.deleteFromList(handle);
    }

    /**
     * @param uri
     */
    public static boolean isProtected(String uri) {
        Iterator e = SecureURI.cachedContent.keySet().iterator();
        while (e.hasNext()) {
            UrlPattern p = (UrlPattern) SecureURI.cachedContent.get(e.next());
            if (p.match(uri)) {
                return true;
            }
        }
        return false;
    }
}
