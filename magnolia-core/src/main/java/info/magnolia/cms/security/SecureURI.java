/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.security;

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Sameer Charles
 */
public final class SecureURI {

    /**
     * Map of protected url patterns.
     */
    private static Map cachedContent = new Hashtable();

    /**
     * Utility class, don't instantiate.
     */
    private SecureURI() {
        // unused
    }

    /**
     * Initialize the Secure URI list.
     */
    public static void init() {
        SecureURI.cachedContent.clear();
    }

    /**
     * Reload the secure URI list.
     */
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
     * Check if a request URI matches a configured pattern.
     * @param uri request URI to check
     * @return <code>true</code> if the given uri matches one of the configured patterns.
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
