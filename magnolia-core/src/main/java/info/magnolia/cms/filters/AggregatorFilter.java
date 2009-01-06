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
package info.magnolia.cms.filters;

import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;

import java.io.IOException;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class AggregatorFilter extends AbstractMgnlFilter{

    private final String VERSION_NUMBER = "mgnlVersion"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AggregatorFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{

        boolean success;
        try {
            success = collect();
        }
        catch (AccessDeniedException e) {
            // don't throw further, simply return error and break filter chain
            log.debug(e.getMessage(), e);
            if (!response.isCommitted())
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            // stop the chain
            return;
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }

        if (!success) {
            if (log.isDebugEnabled()) {
                log.debug(
                    "Resource not found, redirecting request for [{}] to 404 URI", request.getRequestURI()); //$NON-NLS-1$
            }

            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            else {
                log.info("Unable to redirect to 404 page, response is already committed. URI was {}", //$NON-NLS-1$
                    request.getRequestURI());
            }
            // stop the chain
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * Collect content from the pre configured repository and attach it to the HttpServletRequest.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    protected boolean collect() throws RepositoryException {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        String handle = aggregationState.getHandle();
        String extension = aggregationState.getExtension();
        String repository = aggregationState.getRepository();

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(repository);

        Content requestedPage = null;
        NodeData requestedData = null;
        Template template = null;

        if (hierarchyManager.isPage(handle)) {
            requestedPage = hierarchyManager.getContent(handle);

            // check if its a request for a versioned page
            if (MgnlContext.getAttribute(VERSION_NUMBER) != null) {
                // get versioned state
                try {
                    requestedPage = requestedPage.getVersionedContent((String)MgnlContext.getAttribute(VERSION_NUMBER));
                }
                catch (RepositoryException re) {
                    log.debug(re.getMessage(), re);
                    log.error("Unable to get versioned state, rendering current state of {}", handle);
                }
            }

            String templateName = requestedPage.getMetaData().getTemplate();

            if (StringUtils.isBlank(templateName)) {
                log.error("No template configured for page [{}].", requestedPage.getHandle()); //$NON-NLS-1$
            }

            template = TemplateManager.getInstance().getInfo(templateName, extension);

            if (template == null) {
                log.error("Template [{}] for page [{}] not found.", templateName, requestedPage.getHandle());
            }
        }
        else {
            if (hierarchyManager.isNodeData(handle)) {
                requestedData = hierarchyManager.getNodeData(handle);
            }
            else {
                // check again, resource might have different name
                int lastIndexOfSlash = handle.lastIndexOf("/"); //$NON-NLS-1$

                if (lastIndexOfSlash > 0) {

                    handle = StringUtils.substringBeforeLast(handle, "/"); //$NON-NLS-1$

                    try {
                        requestedData = hierarchyManager.getNodeData(handle);
                        aggregationState.setHandle(handle);

                        // this is needed for binary nodedata, e.g. images are found using the path:
                        // /features/integration/headerImage instead of /features/integration/headerImage/header30_2

                    }
                    catch (PathNotFoundException e) {
                        // no page available
                        return false;
                    }
                    catch (RepositoryException e) {
                        log.debug(e.getMessage(), e);
                        return false;
                    }
                }
            }

            if (requestedData != null) {
                String templateName = requestedData.getAttribute(FileProperties.PROPERTY_TEMPLATE);

                if (!StringUtils.isEmpty(templateName)) {
                    template = TemplateManager.getInstance().getInfo(templateName, extension);
                }
            }
            else {
                return false;
            }
        }

        // Attach all collected information to the HttpServletRequest.
        if (requestedPage != null) {
            aggregationState.setMainContent(requestedPage);
            aggregationState.setCurrentContent(requestedPage);
        }
        if ((requestedData != null) && (requestedData.getType() == PropertyType.BINARY)) {
            File file = new File(requestedData);
            aggregationState.setFile(file);
        }

        aggregationState.setTemplate(template);

        return true;
    }

}
