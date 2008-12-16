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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;

import javax.servlet.jsp.jstl.core.ConditionalTagSupport;


/**
 * Everything between publicOnly tags will only be shown on the public instance.
 * @jsp.tag name="publicOnly" body-content="JSP"
 * @jsp.tag-example
 * <cms:publicOnly>
 *    Public now!
 * </cms:publicOnly>
 *
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class PublicOnly extends ConditionalTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Show in preview mode?
     */
    private boolean showInPreview;

    /**
     * Also show content in preview mode. Default is false.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setShowInPreview(boolean showInPreview) {
        this.showInPreview = showInPreview;
    }

    /**
     * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#condition()
     */
    protected boolean condition() {
        if (!ServerConfiguration.getInstance().isAdmin() || (showInPreview && MgnlContext.getAggregationState().isPreviewMode())) {
            return true;
        }
        return false;
    }

    /**
     * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#release()
     */
    public void release() {
        super.release();
        this.showInPreview = false;
    }

}
