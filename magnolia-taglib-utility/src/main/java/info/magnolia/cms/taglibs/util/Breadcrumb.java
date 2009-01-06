/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.taglibs.Resource;

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
 * Outputs "breadcrumbs" with links to parents of the current page.
 * @jsp.tag name="breadcrumb" body-content="empty"
 *
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

    private int startLevel = 1;

    /**
     * Exclude current page from breadcrumb.
     */
    private boolean excludeCurrent;

    /**
     * Output as link. (default: true)
     */
    private boolean link = true;

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
     * displayed between the page names, e.g. ">"
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * At which level to start.
     * Often you will want to omit top levels, e.g. if you split your site into multiple languages.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setStartLevel(String startLevel) {
        this.startLevel = (new Integer(startLevel)).intValue();
        if (this.startLevel < 1) {
            this.startLevel = 1;
        }
    }

    /**
     * Name for a page property which, if set, will make the page hidden in the breadcrumb.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setHideProperty(String hideProperty) {
        this.hideProperty = hideProperty;
    }

    /**
     * Exclude the current (active) page from the breadcrumb. Defaults to false.
     * If true, the current (active) page is not included in breadcrumb.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setExcludeCurrent(boolean excludeCurrent) {
        this.excludeCurrent = excludeCurrent;
    }

    /**
     * Create links to pages. Defaults to true.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setLink(boolean link) {
        this.link = link;
    }

    /**
     * Name for a page property which holds the title to display in breadcrumbs.
     * If empty, the standard title is used.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTitleProperty(String titleProperty) {
        this.titleProperty = titleProperty;
    }

    /**
     * Css class added to the current page link.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setActiveCss(String activeCss) {
        this.activeCss = activeCss;
    }

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
