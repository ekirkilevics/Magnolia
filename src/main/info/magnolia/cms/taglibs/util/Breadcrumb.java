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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;


/**
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class Breadcrumb extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Breadcrumb.class);

    private String delimiter = " > ";

    private int startLevel = 1;

    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        Content actpage = Resource.getCurrentActivePage(request);
        int currentLevel = 0;
        try {
            currentLevel = actpage.getLevel();
            JspWriter out = pageContext.getOut();
            for (int i = this.startLevel; i <= currentLevel; i++) {
                if (i != this.startLevel) {
                    out.print(this.delimiter);
                }
                out.print("<a href=\"");
                out.print(request.getContextPath());
                out.print(actpage.getAncestor(i).getHandleWithDefaultExtension());
                out.print("\">");
                out.print(actpage.getAncestor(i).getTitle());
                out.print("</a>");
            }
        }
        catch (Exception e) {
            log.debug("Exception caught: " + e.getMessage(), e);
        }

        return super.doStartTag();
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setStartLevel(String startLevel) {
        this.startLevel = (new Integer(startLevel)).intValue();
        if (this.startLevel < 1) {
            this.startLevel = 1;
        }
    }
}
