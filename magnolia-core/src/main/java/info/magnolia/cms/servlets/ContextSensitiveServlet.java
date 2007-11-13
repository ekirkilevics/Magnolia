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
package info.magnolia.cms.servlets;

import info.magnolia.context.MgnlContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet which sets the context properly.
 *
 * @deprecated the context is now set through a filter.
 *
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class ContextSensitiveServlet extends HttpServlet {
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextSensitiveServlet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        initializeContext(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        initializeContext(req, resp);
    }

    /**
     * Initialize Magnolia context. It creates a context and initialize the user only if these do not exist yet.
     * <strong>Note</strong>: the implementation may get changed
     *
     * @param request the current request
     */
    protected void initializeContext(HttpServletRequest request, HttpServletResponse response) {
        if (!MgnlContext.hasInstance()) {
            MgnlContext.initAsWebContext(request, response, null);
        } else {
            // this will happen if a virtual uri mapping is pointing again to a virtual uri
            if (log.isDebugEnabled()) {
                log.debug("context of thread was already set");
            }
        }
    }

}
