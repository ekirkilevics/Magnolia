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
package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.Content;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Interface for dialogs. A Magnolia Dialog should at least implements the <code>drawHtml</code> method to add html
 * code to a page. Dialogs must have an empty constructor; the <code>init(Content, Content, PageContext)</code> is
 * assured to be called before any other operation.
 * @author Vinzenz Wyser
 * @version 2.0
 */
public interface DialogControl {

    /**
     * Initialize a Dialog. This method is guaranteed to be called just after the control instantiation.
     * @param request current HttpServletRequest
     * @param response current HttpServletResponse
     * @param websiteNode current website node
     * @param configNode configuration node for the dialog
     * @throws RepositoryException
     */
    void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException;

    /**
     * Actually draw the dialog content.
     * @param out Writer
     * @throws IOException exceptions thrown when writing to the Writer can be safely rethrown by the dialog
     */
    void drawHtml(Writer out) throws IOException;

}
