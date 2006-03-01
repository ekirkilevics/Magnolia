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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.Server;
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
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Content actpage = Resource.getCurrentActivePage(request);
        int endLevel = 0;

        try {
            endLevel = actpage.getLevel();

            if (this.excludeCurrent) {
                endLevel--;
            }

            JspWriter out = pageContext.getOut();
            for (int i = this.startLevel; i <= endLevel; i++) {
                if (i != this.startLevel) {
                    out.print(StringUtils.defaultString(this.delimiter, " > ")); //$NON-NLS-1$
                }
                if (this.link) {
                    out.print("<a href=\""); //$NON-NLS-1$
                    out.print(request.getContextPath());
                    out.print(actpage.getAncestor(i).getHandle() + "." + Server.getDefaultExtension());
                    out.print("\">"); //$NON-NLS-1$
                }
                out.print(actpage.getAncestor(i).getTitle());
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
        super.release();
    }

}
