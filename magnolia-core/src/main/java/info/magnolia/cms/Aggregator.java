/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class Aggregator is responsible to identify the request and gather content for the requested <code>Content </code>
 * object. its also a responsibilty of this class to place the aggregated object to the proper place in this context its
 * a HttpServletRequest which will hold this Content object for further processing.
 * @author Sameer Charles
 * $Id$
 */
public class Aggregator {

    public static final String ACTPAGE = "static_actpage"; //$NON-NLS-1$

    public static final String CURRENT_ACTPAGE = "actpage"; //$NON-NLS-1$

    public static final String FILE = "file"; //$NON-NLS-1$

    public static final String HANDLE = "handle"; //$NON-NLS-1$

    public static final String EXTENSION = "extension"; //$NON-NLS-1$

    public static final String HIERARCHY_MANAGER = "hierarchyManager"; //$NON-NLS-1$

    public static final String TEMPLATE = "mgnl_Template"; //$NON-NLS-1$

    private static final String VERSION_NUMBER = "mgnlVersion"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Aggregator.class);

    /**
     * Don't instantiate.
     */
    private Aggregator() {
    }

    /**
     * Collect content from the pre configured repository and attach it to the HttpServletRequest.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public static boolean collect(HttpServletRequest request) throws PathNotFoundException, RepositoryException {

        String uri = StringUtils.substringBeforeLast(Path.getURI(request), "."); //$NON-NLS-1$
        String extension = StringUtils.substringAfterLast(Path.getURI(request), "."); //$NON-NLS-1$

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        Content requestedPage = null;
        NodeData requestedData = null;
        Template template = null;

        if (hierarchyManager.isPage(uri)) {
            requestedPage = hierarchyManager.getContent(uri); // ATOM

            // check if its a request for a versioned page
            if (request.getParameter(VERSION_NUMBER) != null) {
                // get versioned state
                try {
                    requestedPage = requestedPage.getVersionedContent(request.getParameter(VERSION_NUMBER));
                } catch (RepositoryException re) {
                    log.debug(re.getMessage(), re);
                    log.error("Unable to get versioned state, rendering current state of "+uri);
                }
            }

            String templateName = requestedPage.getMetaData().getTemplate();

            if (StringUtils.isBlank(templateName)) {
                log.error("No template configured for page [{}].", requestedPage.getHandle()); //$NON-NLS-1$
            }

            template = TemplateManager.getInstance().getInfo(templateName, extension);

            if (template == null) {
                log.error("Template [{}] for page [{}] not found.", //$NON-NLS-1$
                    templateName,
                    requestedPage.getHandle());
            }
        }
        else {
            if (hierarchyManager.isNodeData(uri)) {
                requestedData = hierarchyManager.getNodeData(uri);
            }
            else {
                // check again, resource might have different name
                int lastIndexOfSlash = uri.lastIndexOf("/"); //$NON-NLS-1$

                if (lastIndexOfSlash > 0) {
                    uri = StringUtils.substringBeforeLast(uri, "/"); //$NON-NLS-1$
                    try {
                        requestedData = hierarchyManager.getNodeData(uri);
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
                String templateName = requestedData.getAttribute("nodeDataTemplate"); //$NON-NLS-1$

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
            request.setAttribute(Aggregator.ACTPAGE, requestedPage);
            request.setAttribute(Aggregator.CURRENT_ACTPAGE, requestedPage);
        }
        if ((requestedData != null) && (requestedData.getType() == PropertyType.BINARY)) {
            File file = new File();
            file.setProperties(requestedData);
            file.setNodeData(requestedData);
            request.setAttribute(Aggregator.FILE, file);
        }

        request.setAttribute(Aggregator.HANDLE, uri);
        request.setAttribute(Aggregator.EXTENSION, extension);
        request.setAttribute(Aggregator.HIERARCHY_MANAGER, hierarchyManager);

        request.setAttribute(Aggregator.TEMPLATE, template);

        return true;
    }

}
