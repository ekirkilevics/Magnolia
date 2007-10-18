/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
