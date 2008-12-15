/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.util;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Sameer Charles
 * @version 1.1
 *
 * @deprecated formally since 4.0, but really should have stopped using this class for a long time
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
     * @return currently active page, as requested from the URI
     */
    public static Content getActivePage() {
        return MgnlContext.getAggregationState().getMainContent();
    }

    /**
     * <p>
     * get Content object as requested from the URI
     * </p>
     * @return currently active page, as requested from the URI
     */
    public static Content getCurrentActivePage() {
        Content currentActpage = MgnlContext.getAggregationState().getCurrentContent();
        if (currentActpage == null) {
            currentActpage = MgnlContext.getAggregationState().getMainContent();
        }
        return currentActpage;
    }

    /**
     * <p>
     * get file object associated with the requested atom
     * </p>
     * @return currently atom
     */
    public static File getFile() {
        return MgnlContext.getAggregationState().getFile();
    }

    /**
     * <p>
     * get ContentNode object as set by the "set" tag
     * </p>
     * @return ContentNode , global container specific to the current JSP/Servlet page
     */
    public static Content getGlobalContentNode() {
        try {
            return (Content) MgnlContext.getAttribute(Resource.GLOBAL_CONTENT_NODE);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * <p>
     * get ContentNode object as passed to the include tag
     * </p>
     * @return ContentNode , local container specific to the current JSP/Servlet paragraph
     */
    public static Content getLocalContentNode() {
        return (Content) MgnlContext.getAttribute(Resource.LOCAL_CONTENT_NODE);
    }

    /**
     *
     */
    public static String getLocalContentNodeCollectionName() {
        try {
            return (String) MgnlContext.getAttribute(Resource.LOCAL_CONTENT_NODE_COLLECTION_NAME);
        }
        catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * <p>
     * this only works for forms which uses enctype=multipart/form-data
     * </p>
     * @return initialised multipart form object with the posted data
     */
    public static MultipartForm getPostedForm() {
        return (MultipartForm) MgnlContext.getAttribute("multipartform"); //$NON-NLS-1$
    }

    /**
     * <p>
     * get selector as requested from the URI. The selector is the part between the handle and the extension.
     * selector("http://server/a.x.1.f.4.html") = "x.1.f.4"
     * </p>
     * <strong>Warning - this might change in the future - see MAGNOLIA-2343 for details.</strong>
     * @return selector String as requested from the URI
     */
    public static String getSelector() {
        return MgnlContext.getAggregationState().getSelector();
    }

    /**
     * Get the selector by index
     * <strong>Warning - this might change in the future - see MAGNOLIA-2343 for details.</strong>
     * @param index
     * @return the selector value
     */
    public static String getSelector(int index) {
        String[] selectors = StringUtils.split(getSelector(), ".");
        if (selectors.length > index) {
            return selectors[index];
        }
        return null;
    }

    /**
     * <p>
     * removes ContentNode object in resources , scope:page
     * </p>
     */
    public static void removeGlobalContentNode() {
        MgnlContext.removeAttribute(Resource.GLOBAL_CONTENT_NODE);
    }

    /**
     * removes ContentNode object in resources , scope:TAG
     */
    public static void removeLocalContentNode() {
        MgnlContext.removeAttribute(Resource.LOCAL_CONTENT_NODE);
    }

    /**
     *
     */
    public static void removeLocalContentNodeCollectionName() {
        MgnlContext.removeAttribute(Resource.LOCAL_CONTENT_NODE_COLLECTION_NAME);
    }

    /**
     * Restores the request's original <code>actpage</code> attribute (i.e. the one specified by the request URI).
     */
    public static void restoreCurrentActivePage() {
        setCurrentActivePage(getActivePage());
    }

    /**
     * Set the request's <code>actpage</code> attribute to <code>page</code>
     */
    public static void setCurrentActivePage(Content page) {
        MgnlContext.getAggregationState().setCurrentContent(page);
    }

    /**
     * <p>
     * set ContentNode object in resources, scope:page
     * </p>
     * @param node to be set
     */
    public static void setGlobalContentNode(Content node) {
        MgnlContext.setAttribute(Resource.GLOBAL_CONTENT_NODE, node);
    }

    /**
     * <p>
     * set ContentNode object in resources , scope:TAG
     * </p>
     * @param node to be set
     */
    public static void setLocalContentNode(Content node) {
        MgnlContext.setAttribute(Resource.LOCAL_CONTENT_NODE, node);
    }

    public static void setLocalContentNodeCollectionName(HttpServletRequest req, String name) {
        setLocalContentNodeCollectionName(name);
    }

    /**
     *
     */
    public static void setLocalContentNodeCollectionName(String name) {
        MgnlContext.setAttribute(Resource.LOCAL_CONTENT_NODE_COLLECTION_NAME, name);
    }

    /**
     * Check for preview mode.
     * @return boolean , true if preview is enabled
     */
    public static boolean showPreview() {
        // first check if its set in request scope
        if (MgnlContext.getParameter(MGNL_PREVIEW_ATTRIBUTE) != null) {
            return BooleanUtils.toBoolean(MgnlContext.getParameter(MGNL_PREVIEW_ATTRIBUTE));
        }

        Boolean value = (Boolean) MgnlContext.getAttribute(MGNL_PREVIEW_ATTRIBUTE);
        return BooleanUtils.toBoolean(value);
    }

    public static void setShowPreview(boolean showPreview){
        MgnlContext.setAttribute(MGNL_PREVIEW_ATTRIBUTE, Boolean.valueOf(showPreview));
    }

}
