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

import java.io.IOException;

import javax.servlet.jsp.JspWriter;


/**
 * Interface for dialogs. A Magnolia Dialog should at least implements the <code>drawHtml</code> method to add html
 * code to a page.
 * @author Vinzenz Wyser
 * @version 2.0
 */
public interface DialogInterface {

    void drawHtml(JspWriter out) throws IOException;

    void drawSubs(JspWriter out) throws IOException;

    void drawHtmlPreSubs(JspWriter out) throws IOException;

    void drawHtmlPostSubs(JspWriter out) throws IOException;
}
