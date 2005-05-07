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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.util.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;


/**
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class AdminOnly extends ConditionalTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Show in preview mode?
     */
    private boolean showInPreview;

    /**
     * Show in preview mode?
     * @param showInPreview if <code>true</code> the content of the tag is shown in preview mode.
     */
    public void setShowInPreview(boolean showInPreview) {
        this.showInPreview = showInPreview;
    }

    /**
     * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#condition()
     */
    protected boolean condition() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        if (Server.isAdmin() && (!Resource.showPreview(request) || showInPreview)) {
            return true;
        }
        return false;
    }

    /**
     * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#release()
     */
    public void release() {
        this.showInPreview = false;
        super.release();
    }
}
