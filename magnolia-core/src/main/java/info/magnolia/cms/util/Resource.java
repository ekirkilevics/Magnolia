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
package info.magnolia.cms.util;

import info.magnolia.cms.Aggregator;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public final class Resource {

    /**
     * Attribute used for enabling the preview mode.
     */
    public static final String MGNL_PREVIEW_ATTRIBUTE = "mgnlPreview"; //$NON-NLS-1$

    private static final String GLOBAL_CONTENT_NODE = "contentObjGlobal"; //$NON-NLS-1$

    private static final String LOCAL_CONTENT_NODE = "contentObj"; //$NON-NLS-1$

    private static final String LOCAL_CONTENT_NODE_COLLECTION_NAME = "localContentNodeCollectionName"; //$NON-NLS-1$

    /**
     * Utility class, don't instantiate.
     */
    private Resource() {
        // unused
    }

    /**
     * <p>
     * get Content object as requested from the URI
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @return currently active page, as requested from the URI
     */
    public static Content getActivePage(HttpServletRequest req) {
        return (Content) req.getAttribute(Aggregator.ACTPAGE);
    }

    /**
     * <p>
     * get file object associated with the requested atom
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @return currently atom
     */
    public static File getFile(HttpServletRequest req) {
        return (File) req.getAttribute(Aggregator.FILE);
    }

    /**
     * <p>
     * get Content object as requested from the URI
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @return currently active page, as requested from the URI
     */
    public static Content getCurrentActivePage(HttpServletRequest req) {
        Content currentActpage;
        currentActpage = (Content) req.getAttribute(Aggregator.CURRENT_ACTPAGE);
        if (currentActpage == null) {
            currentActpage = (Content) req.getAttribute(Aggregator.ACTPAGE);
        }
        return currentActpage;
    }

    /**
     * Get HierarchyManager object from the request OR from the user session this hierarchy manager points to website
     * repository, in order to swith between user and website repositories, use method (changeContext) on this object.
     * @param req HttpServletRequest as received in JSP or servlet
     * @return hierarchy manager, for the website repository
     * @deprecated as on magnolia 2.0, use SessionAccessControl instead
     * @see info.magnolia.cms.security.SessionAccessControl#getHierarchyManager(javax.servlet.http.HttpServletRequest)
     */
    public static HierarchyManager getHierarchyManager(HttpServletRequest req) {
        return (HierarchyManager) req.getAttribute(Aggregator.HIERARCHY_MANAGER);
    }

    /**
     * <p>
     * this only works for forms which uses enctype=multipart/form-data
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @return initialised multipart form object with the posted data
     */
    public static MultipartForm getPostedForm(HttpServletRequest req) {
        return (MultipartForm) req.getAttribute("multipartform"); //$NON-NLS-1$
    }

    /**
     * <p>
     * get ContentNode object as passed to the include tag
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @return ContentNode , local container specific to the current JSP/Servlet paragraph
     */
    public static Content getLocalContentNode(HttpServletRequest req) {
        try {
            return (Content) req.getAttribute(Resource.LOCAL_CONTENT_NODE);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * <p>
     * set ContentNode object in resources , scope:TAG
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @param node to be set
     */
    public static void setLocalContentNode(HttpServletRequest req, Content node) {
        req.setAttribute(Resource.LOCAL_CONTENT_NODE, node);
    }

    /**
     * <p>
     * removes ContentNode object in resources , scope:TAG
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     */
    public static void removeLocalContentNode(HttpServletRequest req) {
        req.removeAttribute(Resource.LOCAL_CONTENT_NODE);
    }

    /**
     * <p>
     * get ContentNode object as set by the "set" tag
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @return ContentNode , global container specific to the current JSP/Servlet page
     */
    public static Content getGlobalContentNode(HttpServletRequest req) {
        try {
            return (Content) req.getAttribute(Resource.GLOBAL_CONTENT_NODE);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * <p>
     * set ContentNode object in resources, scope:page
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     * @param node to be set
     */
    public static void setGlobalContentNode(HttpServletRequest req, Content node) {
        req.setAttribute(Resource.GLOBAL_CONTENT_NODE, node);
    }

    /**
     * <p>
     * removes ContentNode object in resources , scope:page
     * </p>
     * @param req HttpServletRequest as received in JSP or servlet
     */
    public static void removeGlobalContentNode(HttpServletRequest req) {
        req.removeAttribute(Resource.GLOBAL_CONTENT_NODE);
    }

    /**
     *
     */
    public static void setLocalContentNodeCollectionName(HttpServletRequest req, String name) {
        req.setAttribute(Resource.LOCAL_CONTENT_NODE_COLLECTION_NAME, name);
    }

    /**
     *
     */
    public static String getLocalContentNodeCollectionName(HttpServletRequest req) {
        try {
            return (String) req.getAttribute(Resource.LOCAL_CONTENT_NODE_COLLECTION_NAME);
        }
        catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     *
     */
    public static void removeLocalContentNodeCollectionName(HttpServletRequest req) {
        req.removeAttribute(Resource.LOCAL_CONTENT_NODE_COLLECTION_NAME);
    }

    /**
     * Check for preview mode.
     * @param req HttpServletRequest as received in JSP or servlet
     * @return boolean , true if preview is enabled
     */
    public static boolean showPreview(HttpServletRequest req) {
        return BooleanUtils.toBoolean((Boolean) req.getSession().getAttribute(MGNL_PREVIEW_ATTRIBUTE));
    }
}
