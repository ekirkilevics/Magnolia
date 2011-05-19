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
import info.magnolia.cms.gui.misc.Sources;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.exception.NestableRuntimeException;


/**
 * Adds the needed css and js links for magnolia edit controls. This tag should always bee added to html head.

 * @jsp.tag name="links" body-content="empty"
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 * @since 2.1
 */
public class LinksTag extends TagSupport {

    /**
     * Show links only in admin instance, default to <code>true</code>.
     */
    private boolean adminOnly = true;

    @Override
    public int doStartTag() throws JspException {
        if (!adminOnly || ServerConfiguration.getInstance().isAdmin()) {

            HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();

            // check if links have already been added.
            if (request.getAttribute(Sources.REQUEST_LINKS_DRAWN) == null) {

                Sources src = new Sources(request.getContextPath());
                JspWriter out = this.pageContext.getOut();
                try {
                    out.write(src.getHtmlCss());
                    out.write(src.getHtmlJs());
                }
                catch (IOException e) {
                    // should never happen
                    throw new NestableRuntimeException(e);
                }

                request.setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);
            }
        }

        return EVAL_PAGE;
    }

    /**
     * Show links only in admin instance, defaults to true. You can set it to false if you want magnolia css and js
     * files added also for a public instance.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    @Override
    public void release() {
        this.adminOnly = true;
        super.release();
    }

}
