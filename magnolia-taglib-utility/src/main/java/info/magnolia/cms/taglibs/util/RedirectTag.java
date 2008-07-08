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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Redirects to the first child page. If the server is an authoring instance or magnolia and the preview mode is not
 * active the tag will simply add to the pagecontext a variable named from the <code>var</code> attribute containing
 * the path of the child page.
 * </p>
 * <p>
 * A typical requirement is to have pages with no content which will simply redirect to a child page: using this tag you
 * can easily build a "redirect" template and use it for empty pages:
 * </p>
 *
 * <pre>
 *                         Title                    Template               Mod. Date
 * -----------------------^----------------^-------^----------------------^---------------
 * - siteroot             -                o       redirect                05-01-01
 *   - it                 -                o       redirect                05-01-01
 *     + home             Home page        o       home                    05-01-01
 *
 * </pre>
 *
 * <p>
 * This tag should be put <strong>before</strong> any other tag or include in the page, since response should not be
 * committed yet for it to work.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;cmsu:redirect var="destpage" />
 *
 * This page has no content and it will redirect to
 * &lt;a href="${pageContext.request.contextPath}${destpage}">${destpage}&lt;/a> in a public instance.
 * </pre>
 *
 * @author Fabrizio Giustina
 * @version $Id$
 * @since 2.2
 */
public class RedirectTag extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(RedirectTag.class);

    /**
     * Name for the variable which will contain the URL of the page this tag will redirect to.
     */
    private String var;

    /**
     * Setter for the <code>var</code> tag parameter.
     * @param var Name for the variable which will contain the URL of the page this tag will redirect to
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String location = getRedirectLocation(request);

        Content activePage = Resource.getActivePage();

        // on public servers, during preview or when the user can't edit the page, just send the redirect
        if (!ServerConfiguration.getInstance().isAdmin() || Resource.showPreview() || !activePage.isGranted(Permission.SET)) {
            if (location != null) {
                try {
                    ((HttpServletResponse) pageContext.getResponse()).sendRedirect(request.getContextPath() + location);
                }
                catch (IOException e) {
                    log.error("Could not redirect to first child HTML page: " + e.getMessage()); //$NON-NLS-1$
                }
                return Tag.SKIP_PAGE;
            }
        }
        else if (StringUtils.isNotBlank(var)) {
            request.setAttribute(var, location);
        }
        return super.doStartTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        this.var = null;
        super.release();
    }

    /**
     * Returns the locationto which we intend to redirect.
     * @param request The HTTP request.
     * @return A URI if a child page is available, or null.
     */
    private String getRedirectLocation(HttpServletRequest request) {
        Content page = Resource.getActivePage();
        Iterator it = page.getChildren().iterator();
        if (it.hasNext()) {
            Content c = (Content) it.next();
            return c.getHandle() + '.' + ServerConfiguration.getInstance().getDefaultExtension();
        }

        return null;
    }

}
