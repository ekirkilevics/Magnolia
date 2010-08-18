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
package info.magnolia.cms.filters;

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
 * Reads the accessed content from the repository and puts it into the {@link AggregationState}.
 * @author philipp
 * @version $Id$
 *
 */
public class AggregatorFilter extends AbstractMgnlFilter{
    private static final Logger log = LoggerFactory.getLogger(AggregatorFilter.class);

    private final String VERSION_NUMBER = "mgnlVersion"; //$NON-NLS-1$


    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{

        boolean success;
        try {
            success = collect();
        }
        catch (AccessDeniedException e) {
            // don't throw further, simply return error and break filter chain
            log.debug(e.getMessage(), e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            // stop the chain
            return;
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }

        if (!success) {
            log.debug("Resource not found, redirecting request for [{}] to 404 URI", request.getRequestURI());

            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            else {
                log.info("Unable to redirect to 404 page, response is already committed. URI was {}", request.getRequestURI());
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
        final String handle = aggregationState.getHandle();
        final String repository = aggregationState.getRepository();

        final HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(repository);

        Content requestedPage = null;
        NodeData requestedData = null;
        final String templateName;

        if (!isJcrPathValid(handle)) {
            // avoid calling isExist if the path can't be valid
            return false;
        }
        if (hierarchyManager.isExist(handle) && !hierarchyManager.isNodeData(handle)) {
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

            templateName = requestedPage.getMetaData().getTemplate();

            if (StringUtils.isBlank(templateName)) {
                log.error("No template configured for page [{}].", requestedPage.getHandle()); //$NON-NLS-1$
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

                    final String handleToUse = StringUtils.substringBeforeLast(handle, "/"); //$NON-NLS-1$

                    try {
                        requestedData = hierarchyManager.getNodeData(handleToUse);
                        aggregationState.setHandle(handleToUse);

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
                templateName = requestedData.getAttribute(FileProperties.PROPERTY_TEMPLATE);
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

        aggregationState.setTemplateName(templateName);

        return true;
    }

    /**
     * Check if the path *may be* a valid path before calling getItem, in order to avoid annoying logs.
     * @param handle node handle
     * @return true if the path is invalid
     */
    private boolean isJcrPathValid(String handle) {
        if (StringUtils.isBlank(handle) || StringUtils.equals(handle, "/")) {
            // empty path not allowed
            return false;
        }
        if (StringUtils.containsAny(handle, new char[]{':', '*', '\n'})) {
            // not allowed chars
            return false;
        }
        if (StringUtils.contains(handle, " /")) {
            // trailing slash not allowed
            return false;
        }
        return true;
    }

}
