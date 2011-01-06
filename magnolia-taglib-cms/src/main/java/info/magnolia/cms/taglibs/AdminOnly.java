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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;

import javax.servlet.jsp.jstl.core.ConditionalTagSupport;


/**
 * Everything between adminOnly tags will only be shown on an authoring instance, not on a public instance. This
 * allows you to provide functionality that is only available to page authors but not to the general public.
 * @jsp.tag name="adminOnly" body-content="JSP"
 * @jsp.tag-example
 * <pre>
 * &lt;cms:adminOnly&gt;
 *    &lt;cms:editBar/&gt;
 * &lt;/cms:adminOnly&gt;
 *</pre>
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class AdminOnly extends ConditionalTagSupport {
    /**
     * Determines if the content should be displayed in preview mode.
     */
    private boolean showInPreview;

    /**
     * Also show content in preview mode. Default is false.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setShowInPreview(boolean showInPreview) {
        this.showInPreview = showInPreview;
    }

    protected boolean condition() {
        if (ServerConfiguration.getInstance().isAdmin() && (!MgnlContext.getAggregationState().isPreviewMode() || showInPreview)) {
            return true;
        }
        return false;
    }

    public void release() {
        this.showInPreview = false;
        super.release();
    }
}
