/**
 * This file Copyright (c) 2003-2007 Magnolia International
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

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Draws a breadcrumbs with links to parents of the current page.
 * @author Marcel Salathe
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class Breadcrumb extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Breadcrumb.class);

    /**
     * Delimeter between links.
     */
    private String delimiter;

    /**
     * Breadcrumb start level.
     */
    private int startLevel = 1;

    /**
     * Exclude current page from breadcrumb.
     */
    private boolean excludeCurrent;

    /**
     * Output as link. (default: true)
     */
    private boolean link = true;

    /**
     * Name for a page property which, if set, will make the page hidden in the breadcrumb.
     */
    private String hideProperty;

    /**
     * Name for the property used as page title.
     */
    private String titleProperty;

    /**
     * Css class for active page.
     */
    private String activeCss = "active";

    /**
     * Setter for the <code>delimeter</code> tag attribute.
     * @param delimiter delimeter between links
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Setter for the <code>startLevel</code> tag attribute.
     * @param startLevel breadcrumb start level
     */
    public void setStartLevel(String startLevel) {
        this.startLevel = (new Integer(startLevel)).intValue();
        if (this.startLevel < 1) {
            this.startLevel = 1;
        }
    }

    public void setHideProperty(String hideProperty) {
        this.hideProperty = hideProperty;
    }

    /**
     * Setter for <code>excludeCurrent</code>.
     * @param excludeCurrent if <code>true</code> the current (active) page is not included in breadcrumb.
     */
    public void setExcludeCurrent(boolean excludeCurrent) {
        this.excludeCurrent = excludeCurrent;
    }

    /**
     * Setter for <code>link</code>.
     * @param link if <code>true</code> all pages are linked to.
     */
    public void setLink(boolean link) {
        this.link = link;
    }

    /**
     * Setter for <code>titleProperty</code>.
     * @param titleProperty name of nodeData for page title
     */
    public void setTitleProperty(String titleProperty) {
        this.titleProperty = titleProperty;
    }

    /**
     * Setter for <code>activeCss</code>.
     * @param activeCss The activeCss to set.
     */
    public void setActiveCss(String activeCss) {
        this.activeCss = activeCss;
    }

    /**
     * {@inheritDoc}
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Content actpage = Resource.getCurrentActivePage();
        int endLevel = 0;

        try {
            endLevel = actpage.getLevel();

            if (this.excludeCurrent) {
                endLevel--;
            }

            JspWriter out = pageContext.getOut();
            for (int j = this.startLevel; j <= endLevel; j++) {
                Content page = actpage.getAncestor(j);

                if (StringUtils.isNotEmpty(hideProperty) && page.getNodeData(hideProperty).getBoolean()) {
                    continue;
                }

                String title = null;
                if (StringUtils.isNotEmpty(titleProperty)) {
                    title = page.getNodeData(titleProperty).getString(StringUtils.EMPTY);
                }

                if (StringUtils.isEmpty(title)) {
                    title = page.getTitle();
                }

                if (j != this.startLevel) {
                    out.print(StringUtils.defaultString(this.delimiter, " &gt; ")); //$NON-NLS-1$
                }
                if (this.link) {
                    out.print("<a href=\""); //$NON-NLS-1$
                    out.print(request.getContextPath());
                    out.print(page.getHandle());
                    out.print("."); //$NON-NLS-1$
                    out.print(ServerConfiguration.getInstance().getDefaultExtension());
                    if (actpage.getHandle().equals(page.getHandle())) {
                        out.print("\" class=\""); //$NON-NLS-1$
                        out.print(activeCss);
                    }

                    out.print("\">"); //$NON-NLS-1$

                }
                out.print(title);
                if (this.link) {
                    out.print("</a>"); //$NON-NLS-1$
                }
            }
        }
        catch (RepositoryException e) {
            log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        catch (IOException e) {
            throw new NestableRuntimeException(e);
        }

        return super.doStartTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.startLevel = 1;
        this.delimiter = null;
        this.excludeCurrent = false;
        this.link = true;
        this.hideProperty = null;
        this.titleProperty = null;
        this.activeCss = "active";
        super.release();
    }

}
