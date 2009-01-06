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
package info.magnolia.cms.servlets;

import java.io.IOException;


/**
 * This Handler is used in the MVCServlet
 * @author Philipp Bracher
 * @version $Id$
 */
public interface MVCServletHandler {

    String VIEW_NOTHING = "nothing"; //$NON-NLS-1$

    /**
     * Depending on the request it is generating a logical command name
     * @return name of the command
     */
    String getCommand();

    /**
     * Call the method through reflection
     * @param command
     * @return the name of the view to show (used in renderHtml)
     */
    String execute(String command);

    /**
     * Render the tree depending on the view name.
     * @param view
     * @return
     * @throws IOException
     */
    void renderHtml(String view) throws IOException;

    /**
     * The name of the handler
     */
    String getName();

    /**
     * Called after instantiating
     */
    void init() throws Exception;
}
