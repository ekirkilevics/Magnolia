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
    private static Map secureURIs = new Hashtable();

    private static Map unsecureURIs = new Hashtable();

    /**
     * Utility class, don't instantiate.
     */
    private SecureURI() {
        // unused
    }

    public static Map listSecureURIs() {
        Hashtable copy = new Hashtable();
        copy.putAll(secureURIs);
        return copy;
    }

    public static Map listUnsecureURIs() {
        Hashtable copy = new Hashtable();
        copy.putAll(unsecureURIs);
        return copy;
    }

    /**
     * Initialize the Secure URI list.
     */
    public static void init() {
        SecureURI.secureURIs.clear();
        SecureURI.unsecureURIs.clear();

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
    private static synchronized void addToList(String handle, Map map) {
        UrlPattern pattern1 = new SimpleUrlPattern(handle);
        map.put(handle, pattern1);
    }

    /**
     * @param handle
     */
    private static synchronized void deleteFromList(String handle, Map map) {
        map.remove(handle);
    }

    /**
     * @param handle
     */
    public static void addSecure(String handle) {
        SecureURI.addToList(handle, SecureURI.secureURIs);
    }

    /**
     * @param handle
     */
    public static void deleteSecure(String handle) {
        SecureURI.deleteFromList(handle, SecureURI.secureURIs);
    }

    public static void addUnsecure(String handle) {
        SecureURI.addToList(handle, SecureURI.unsecureURIs);
    }

    /**
     * @param handle
     */
    public static void deleteUnsecure(String handle) {
        SecureURI.deleteFromList(handle, SecureURI.unsecureURIs);
    }

    /**
     * Check if a request URI matches a configured pattern.
     * @param uri request URI to check
     * @return <code>true</code> if the given uri matches one of the configured patterns.
     */
    public static boolean isProtected(String uri) {
        return hasPattern(uri, SecureURI.secureURIs);
    }

    public static boolean isUnsecure(String uri) {
        return hasPattern(uri, SecureURI.unsecureURIs);
    }

    /**
     * Check if the uri is a pattern that has a mapping in the given map
     * @param uri the uri to search for
     * @param map the map where the <code>UrlPattern</code> are stored
     * @return <code>true</code> if the uri has match one of the pattern stored in the map, false otherwise
     */
    public static boolean hasPattern(String uri, Map map) {
        Iterator e = map.keySet().iterator();
        while (e.hasNext()) {
            UrlPattern p = (UrlPattern) map.get(e.next());
            if (p.match(uri)) {
                return true;
            }
        }
        return false;
    }
}
