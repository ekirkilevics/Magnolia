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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class DialogInclude extends DialogBox {

    private static Logger log = Logger.getLogger(DialogInclude.class);

    public DialogInclude() {
    }

    public DialogInclude(ContentNode configNode, Content websiteNode) throws RepositoryException {
        super(configNode, websiteNode);
    }

    public void drawHtml(JspWriter out) throws IOException {
        this.drawHtmlPre(out);
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            request = this.getTopParent().getRequest();
        }
        PageContext pageContext = this.getPageContext();
        if (pageContext == null) {
            pageContext = this.getTopParent().getPageContext();
        }
        try {
            pageContext.setAttribute("dialogObject", this, PageContext.REQUEST_SCOPE);
            pageContext.include(this.getConfigValue("file"));
            pageContext.removeAttribute("dialogObject", PageContext.REQUEST_SCOPE);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        this.drawHtmlPost(out);
    }
}
