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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;


/**
 * User: Sameer Charles Date: Mar 22, 2004 Time: 10:55:12 AM
 * @author Sameer Charles
 */
public class SecureURI {

    private static final String RESTRICTED_ACCESS_NODE = "magnoliaRestrictedAccess";

    private static Logger log = Logger.getLogger(SecureURI.class);

    private static Hashtable cachedContent;

    private static HierarchyManager hierarchyManager;

    public SecureURI() {
    }

    public static void init() {
        SecureURI.cachedContent = new Hashtable();
        SecureURI.cachedContent.clear();
        hierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);
        try {
            createList(hierarchyManager.getRootPage());
        }
        catch (RepositoryException re) {
            log.error("failed to load secure URI list");
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() throws PathNotFoundException, RepositoryException {
        init();
    }

    /**
     * <p>
     * Recursively create a list of all protected pages
     * </p>
     * @param startPage
     */
    private static void createList(Content startPage) throws PathNotFoundException, RepositoryException {
        boolean isSecured = startPage.getNodeData(RESTRICTED_ACCESS_NODE).getBoolean();
        if (isSecured) {
            addToList(startPage.getHandle());
            addToList(startPage.getHandle() + "/*");
            return;
        }
        Collection children = startPage.getChildren();
        if (children.size() > 0) {
            Iterator it = children.iterator();
            while (it.hasNext()) {
                createList((Content) it.next());
            }
        }
    }

    /**
     * @param handle
     */
    private synchronized static void addToList(String handle) {
        String stringPattern = RegexWildcardPattern.getEncodedString(handle);
        Pattern pattern1 = Pattern.compile(stringPattern);
        SecureURI.cachedContent.put(handle, pattern1);
    }

    /**
     * @param handle
     */
    private synchronized static void deleteFromList(String handle) {
        SecureURI.cachedContent.remove(handle);
    }

    /**
     * @param handle
     */
    public static void add(String handle) {
        SecureURI.addToList(handle);
    }

    /**
     * @param request
     */
    public static void add(HttpServletRequest request, String handle) {
        try {
            HierarchyManager hm = SessionAccessControl.getHierarchyManager(request);
            Content page = hm.getPage(handle);
            if (page == null)
                return;
            if (page.getNodeData(RESTRICTED_ACCESS_NODE).getBoolean()) {
                SecureURI.addToList(handle);
                SecureURI.addToList(handle + "/*");
            }
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
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
        Enumeration e = SecureURI.cachedContent.keys();
        while (e.hasMoreElements()) {
            Pattern p = (Pattern) SecureURI.cachedContent.get((String) e.nextElement());
            if (p.matcher(uri).matches())
                return true;
        }
        return false;
    }
}
