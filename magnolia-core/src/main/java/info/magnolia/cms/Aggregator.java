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
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;

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
 * @version 2.0
 */
public class Aggregator {

    public static final String ACTPAGE = "static_actpage"; //$NON-NLS-1$

    public static final String CURRENT_ACTPAGE = "actpage"; //$NON-NLS-1$

    public static final String FILE = "file"; //$NON-NLS-1$

    public static final String HANDLE = "handle"; //$NON-NLS-1$

    public static final String EXTENSION = "extension"; //$NON-NLS-1$

    public static final String HIERARCHY_MANAGER = "hierarchyManager"; //$NON-NLS-1$

    public static final String REQUEST_RECEIVER = "requestReceiver"; //$NON-NLS-1$

    public static final String DIRECT_REQUEST_RECEIVER = "/ResourceDispatcher"; //$NON-NLS-1$

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
        boolean success = true;

        String uri = StringUtils.substringBeforeLast(Path.getURI(request), "."); //$NON-NLS-1$
        String extension = StringUtils.substringAfterLast(Path.getURI(request), "."); //$NON-NLS-1$

        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);

        Content requestedPage = null;
        NodeData requestedData = null;
        String requestReceiver = null;

        if (hierarchyManager.isNodeData(uri)) {
            requestedData = hierarchyManager.getNodeData(uri);
            requestReceiver = getRequestReceiver(requestedData, extension);
        }
        else if (hierarchyManager.isPage(uri)) {
            requestedPage = hierarchyManager.getContent(uri); // ATOM
            requestReceiver = getRequestReceiver(requestedPage, extension);
        }
        else {
            // check again, resource might have different name
            int lastIndexOfSlash = uri.lastIndexOf("/"); //$NON-NLS-1$

            if (lastIndexOfSlash > 0) {
                uri = StringUtils.substringBeforeLast(uri, "/"); //$NON-NLS-1$
                try {
                    requestedData = hierarchyManager.getNodeData(uri);
                    requestReceiver = getRequestReceiver(requestedData, extension);
                }
                catch (RepositoryException e) {
                    requestReceiver = Aggregator.DIRECT_REQUEST_RECEIVER;
                    success = false;
                }
            }
            else {
                requestReceiver = Aggregator.DIRECT_REQUEST_RECEIVER;
                success = false;
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
        request.setAttribute(Aggregator.REQUEST_RECEIVER, requestReceiver);

        return success;
    }

    /**
     * Set the template responsible to handle this request.
     * @param type
     */
    private static String getRequestReceiver(NodeData requestedData, String extension) {
        try {
            String templateName = requestedData.getAttribute("nodeDataTemplate"); //$NON-NLS-1$
            if (StringUtils.isEmpty(templateName)) {
                return Aggregator.DIRECT_REQUEST_RECEIVER;
            }
            return TemplateManager.getInstance().getInfo(templateName).getPath(extension);
        }
        catch (Exception e) {
            return Aggregator.DIRECT_REQUEST_RECEIVER;
        }
    }

    /**
     * Set the servlet responsible to handle direct resource request.
     */
    private static String getRequestReceiver(Content requestedPage, String extension) {

        try {
            String templateName = requestedPage.getMetaData().getTemplate();

            if (StringUtils.isBlank(templateName)) {
                log.error("No template configured for page [{}].", requestedPage.getHandle()); //$NON-NLS-1$
            }

            Template template = TemplateManager.getInstance().getInfo(templateName);

            if (template == null) {

                log.error("Template [{}] for page [{}] not found.", //$NON-NLS-1$
                    templateName,
                    requestedPage.getHandle());

                return null;
            }

            return template.getPath(extension);
        }
        catch (Exception e) {
            log.error("Failed to set request receiver: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return null;
    }

}
