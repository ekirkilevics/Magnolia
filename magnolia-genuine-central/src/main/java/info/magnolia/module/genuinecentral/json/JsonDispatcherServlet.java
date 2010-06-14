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
package info.magnolia.module.genuinecentral.json;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import javax.servlet.*;
import java.io.IOException;
import java.util.*;

public class JsonDispatcherServlet extends HttpServletDispatcher {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(addInitParameters(servletConfig));
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        super.service(req, res);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private static ParameterAddingServletConfigWrapper addInitParameters(ServletConfig servletConfig) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("javax.ws.rs.Application", GenuineCentralJsonApplication.class.getName());
        parameters.put("resteasy.servlet.mapping.prefix", "/.magnolia/rest");
        return new ParameterAddingServletConfigWrapper(servletConfig, parameters);
    }

    private static class ServletConfigWrapper implements ServletConfig {

        protected ServletConfig target;

        public ServletConfigWrapper(ServletConfig target) {
            this.target = target;
        }

        public String getServletName() {
            return target.getServletName();
        }

        public ServletContext getServletContext() {
            return target.getServletContext();
        }

        public String getInitParameter(String name) {
            return target.getInitParameter(name);
        }

        public Enumeration getInitParameterNames() {
            return target.getInitParameterNames();
        }
    }

    private final static class ParameterAddingServletConfigWrapper extends ServletConfigWrapper {

        private LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();

        public ParameterAddingServletConfigWrapper(ServletConfig target, Map<String, String> parameters) {
            super(target);
            Enumeration parameterNames = target.getInitParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                this.parameters.put(parameterName, target.getInitParameter(parameterName));
            }
            this.parameters.putAll(parameters);
        }

        @Override
        public String getInitParameter(String name) {
            return parameters.get(name);
        }

        @Override
        public Enumeration getInitParameterNames() {
            return new Enumeration() {

                private Iterator iter = parameters.keySet().iterator();

                public boolean hasMoreElements() {
                    return iter.hasNext();
                }

                public Object nextElement() {
                    return iter.next();
                }
            };
        }
    }
}
