/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.vaadin;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


import com.vaadin.terminal.gwt.server.ApplicationServlet;

/**
 * We can set the "Resources" parameter for the {@link ApplicationServlet} only by defining the context path. To make this dynamic we manipulate the {@link ServletConfig}.
 * @version $Id$
 */
public class MagnoliaResoucesAwareApplicationServlet extends ApplicationServlet {


    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        // read the parameters
        final Map<String,String> parameters = new HashMap<String,String>();

        Enumeration initParameterNames = servletConfig.getInitParameterNames();
        while(initParameterNames.hasMoreElements()){
            String name = (String) initParameterNames.nextElement();
            parameters.put(name, servletConfig.getInitParameter(name));
        }

        // add our resources parameter
        parameters.put("Resources", servletConfig.getServletContext().getContextPath() + "/.resources");

        // initialize
        super.init(new ServletConfig() {

            public String getServletName() {
                return servletConfig.getServletName();
            }

            public ServletContext getServletContext() {
                return servletConfig.getServletContext();
            }

            public Enumeration getInitParameterNames() {
                return Collections.enumeration(parameters.keySet());
            }

            public String getInitParameter(String name) {
                return parameters.get(name);
            }
        });
    }

}
