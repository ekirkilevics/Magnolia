/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * The servlet gets a {@link MVCServletHandler} with the method
 * {@link #getHandler(HttpServletRequest, HttpServletResponse)}.
 * {@link MVCServletHandler#getCommand()} is called and the returned command name is passed to the
 * {@link MVCServletHandler#execute(String)} method wich returns the view name. Finally it calls
 * {@link MVCServletHandler#renderHtml(String)} passing the view name.
 * <p>
 * Make a subclass to provide you own handler(s).
 * @version $Id$
 */
public abstract class MVCServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = LoggerFactory.getLogger(MVCServlet.class);

    /**
     * @see HttpServlet#doGet(HttpServletRequest,HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {

        // http://issues.apache.org/bugzilla/show_bug.cgi?id=22666
        //
        // 1. The Coyote HTTP/1.1 connector has a useBodyEncodingForURI attribute which
        // if set to true will use the request body encoding to decode the URI query
        // parameters.
        // - The default value is true for TC4 (breaks spec but gives consistent
        // behaviour across TC4 versions)
        // - The default value is false for TC5 (spec compliant but there may be
        // migration issues for some apps)
        // 2. The Coyote HTTP/1.1 connector has a URIEncoding attribute which defaults to
        // ISO-8859-1.
        // 3. The parameters class (o.a.t.u.http.Parameters) has a QueryStringEncoding
        // field which defaults to the URIEncoding. It must be set before the parameters
        // are parsed to have an effect.
        //
        // Things to note regarding the servlet API:
        // 1. HttpServletRequest.setCharacterEncoding() normally only applies to the
        // request body NOT the URI.
        // 2. HttpServletRequest.getPathInfo() is decoded by the web container.
        // 3. HttpServletRequest.getRequestURI() is not decoded by container.
        //
        // Other tips:
        // 1. Use POST with forms to return parameters as the parameters are then part of
        // the request body.

        // this can throw an exception in jetty
        try {
            request.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        }
        catch (java.lang.IllegalStateException e) {
            log.error("can't set character encoding for the request", e); //$NON-NLS-1$
        }

        MVCServletHandler handler = getHandler(request, response);

        if (handler == null) {
            log.warn("no handler found for url {}", request.getRequestURI()); //$NON-NLS-1$
            response.sendError(404);
            return;
        }

        // why do i have to change it if request was setted? But i have to!
        response.setCharacterEncoding("UTF-8"); //$NON-NLS-1$

        String command = handler.getCommand();
        String view = handler.execute(command);
        handler.renderHtml(view);
    }

    /**
     * @param request
     * @return
     */
    protected abstract MVCServletHandler getHandler(HttpServletRequest request, HttpServletResponse response);
}
