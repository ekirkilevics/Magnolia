/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.misc;

import info.magnolia.context.MgnlContext;

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
        html.append("/.magnolia/pages/javascript.js\"></script>\n"); //$NON-NLS-1$

        html.append("<script type=\"text/javascript\" src=\""); //$NON-NLS-1$
        html.append(contextPath);
        html.append("/.magnolia/pages/messages." + MgnlContext.getUser().getLanguage() + ".js\"></script>\n"); //$NON-NLS-1$

        html.append("<script type=\"text/javascript\" src=\""); //$NON-NLS-1$
        html.append(contextPath);
        html.append("/.resources/admin-js/dialogs/dialogs.js\"></script>\n"); //$NON-NLS-1$

        html.append("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"");
        html.append(contextPath);
        html.append("/.resources/calendar/skins/aqua/theme.css\" title=\"Aqua\" />\n");

        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/.resources/calendar/calendar.js\"></script>\n");

        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/.resources/calendar/lang/calendar-");
        html.append(MgnlContext.getLocale().getLanguage());
        html.append(".js\"></script>\n");

        html.append("<script type=\"text/javascript\" src=\"");
        html.append(contextPath);
        html.append("/.resources/calendar/calendar-setup.js\"></script>\n");
        
        return html.toString();
    }

    public String getHtmlCss() {
        StringBuffer html = new StringBuffer();
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\""); //$NON-NLS-1$
        html.append(contextPath);
        html.append("/.resources/admin-css/admin-all.css\" />\n"); //$NON-NLS-1$
        return html.toString();
    }

}
