/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.cms.filters;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * A basic implementation which uses a request attribute to mark a request to a web container
 * resource.
 * @version $Id$
 *
 */
public class WebContainerResourcesImpl implements WebContainerResources {

    /**
     * A request can be marked as requesting a web container resource by adding this attribute to the request.
     */
    public static final String WEB_CONTAINER_RESOURCE_MARKER_ATTRIBUTE = WebContainerResources.class.getName();

    public boolean isWebContainerResource(ServletRequest request) {
        return request.getAttribute(WebContainerResourcesImpl.WEB_CONTAINER_RESOURCE_MARKER_ATTRIBUTE) != null;
    }

    public void forward(String pathToWebContainerResource, ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(WEB_CONTAINER_RESOURCE_MARKER_ATTRIBUTE, Boolean.TRUE);
        try {
            request.getRequestDispatcher(pathToWebContainerResource).forward(request, response);
        }
        finally {
            // remove the attribute just set, because a custom magnolia managed error-page may have
            // been configured in
            // web.xml
            request.removeAttribute(WEB_CONTAINER_RESOURCE_MARKER_ATTRIBUTE);
        }
    }

}
