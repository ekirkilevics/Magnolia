/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.context;

import info.magnolia.objectfactory.Components;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default context provider.
 * @author had
 * @version $Id$
 */
@Singleton
public class ContextFactory {

    /**
     * @return new instance of the web context on every call.
     */
    public WebContext createWebContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        final WebContextFactory ctxFactory = Components.getComponentProvider().getComponent(WebContextFactory.class);
        return ctxFactory.createWebContext(request, response, servletContext);
    }

    /**
     * @return singleton instance of the <code>SystemContext</code>.
     * @deprecated since 4.5, use IoC, i.e., declare a dependency on SystemContext in your component.
     */
    @Deprecated
    public SystemContext getSystemContext() {
        return Components.getComponent(SystemContext.class);
    }

    /**
     * @return singleton instance of itself.
     * @deprecated since 4.5, use IoC, i.e., declare a dependency on ContextFactory in your component.
     */
    @Deprecated
    public static ContextFactory getInstance() {
        return Components.getComponent(ContextFactory.class);
    }
}
