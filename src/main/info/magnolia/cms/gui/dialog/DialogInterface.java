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

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;


/**
 * Interface for dialogs. A Magnolia Dialog should at least implements the <code>drawHtml</code> method to add html
 * code to a page. Dialogs must have an empty constructor; the <code>init(Content, Content, PageContext)</code> is
 * assured to be called before any other operation.
 * @author Vinzenz Wyser
 * @version 2.0
 */
public interface DialogInterface {

    /**
     * Initialize a Dialog. This method is guaranteed to be called just after the control instantiation.
     * @param configNode configuration node for the dialog
     * @param websiteNode current website node
     * @param pageContext jsp page context
     * @throws RepositoryException
     */
    void init(Content configNode, Content websiteNode, PageContext pageContext) throws RepositoryException;

    /**
     * Actually draw the dialog content.
     * @param out JspWriter
     * @throws IOException exceptions thrown when writing to the JspWriter can be safely rethrown by the dialog
     */
    void drawHtml(JspWriter out) throws IOException;

    /**
     * ???????
     * @param out JspWriter
     * @throws IOException exceptions thrown when writing to the JspWriter can be safely rethrown by the dialog
     */
    void drawSubs(JspWriter out) throws IOException;

    /**
     * ???????
     * @param out JspWriter
     * @throws IOException exceptions thrown when writing to the JspWriter can be safely rethrown by the dialog
     */
    void drawHtmlPreSubs(JspWriter out) throws IOException;

    /**
     * ???????
     * @param out JspWriter
     * @throws IOException exceptions thrown when writing to the JspWriter can be safely rethrown by the dialog
     */
    void drawHtmlPostSubs(JspWriter out) throws IOException;
}
