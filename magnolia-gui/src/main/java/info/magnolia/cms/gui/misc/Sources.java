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
package info.magnolia.cms.gui.misc;

/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Sources {

    /**
     * Attribute set in request when links are drawn by custom tags.
     */
    public static final String REQUEST_LINKS_DRAWN = "mgnl_links_drawn"; //$NON-NLS-1$

    /**
     * Context path for the current request.
     */
    private String contextPath;

    /**
     * Instantiate a new Source for a given context path.
     * @param contextPath context path for the current request (request.getContextPath)
     */
    public Sources(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getHtmlJs() {
        StringBuffer html = new StringBuffer();

        html.append("<script type=\"text/javascript\" src=\""); //$NON-NLS-1$
        html.append(contextPath);
        html.append("/admintemplates/js/admincentral.jsp\"></script>"); //$NON-NLS-1$

        html.append("<script type=\"text/javascript\" src=\""); //$NON-NLS-1$
        html.append(contextPath);
        html.append("/.resources/admin-js/dialogs/dialogs.js\"></script>"); //$NON-NLS-1$

        html.append("<script type=\"text/javascript\" src=\""); //$NON-NLS-1$
        html.append(contextPath);
        html.append("/.resources/admin-js/dialogs/calendar.js\"></script>"); //$NON-NLS-1$

        return html.toString();
    }

    public String getHtmlCss() {
        StringBuffer html = new StringBuffer();
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""); //$NON-NLS-1$
        html.append(contextPath);
        html.append("/.resources/admin-css/admin-all.css\" />"); //$NON-NLS-1$
        return html.toString();
    }

}
