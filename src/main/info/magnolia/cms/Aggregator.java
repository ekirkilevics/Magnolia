/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.SessionAccessControl;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Class Aggregator is responsible to identify the request and gather content for the requested <code>Content </code>
 * object. its also a responsibilty of this class to place the aggregated object to the proper place in this context its
 * a HttpServletRequest which will hold this Content object for further processing.
 * @author Sameer Charles
 * @version 2.0
 */
public class Aggregator {

    public static final String ACTPAGE = "static_actpage";

    public static final String CURRENT_ACTPAGE = "actpage";

    public static final String FILE = "file";

    public static final String HANDLE = "handle";

    public static final String EXTENSION = "extension";

    public static final String HIERARCHY_MANAGER = "hierarchyManager";

    public static final String REQUEST_RECEIVER = "requestReceiver";

    public static final String DIRECT_REQUEST_RECEIVER = "/ResourceDispatcher";

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Aggregator.class);

    private static final int ATOM = 2;

    private HttpServletRequest request;

    private Content requestedPage;

    private Content subContentNode;

    private NodeData requestedData;

    private HierarchyManager hierarchyManager;

    private String uri;

    private String extension;

    private String requestReceiver;

    /**
     * constructor
     * @param req HttpServletRequest as received by the servlet engine
     */
    public Aggregator(HttpServletRequest req, HttpServletResponse response) {
        this.request = req;
    }

    /**
     * Update requested page of the current request.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    private void getRequestedContent() throws PathNotFoundException, RepositoryException {
        this.requestedPage = this.hierarchyManager.getContent(this.uri);
    }

    /**
     * Update requested content of the current request.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    private void getRequestedContent(int type) throws PathNotFoundException, RepositoryException {
        this.requestedData = this.hierarchyManager.getNodeData(this.uri);
        try {
            this.subContentNode = this.hierarchyManager.getContent(this.uri + "_properties");
        }
        catch (PathNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Path not found: " + e.getMessage());
            }
        }
    }

    /**
     * Parse uri to get exact Content path.
     */
    private void parseURI() {
        try {
            int lastIndexOfDot = Path.getURI(this.request).lastIndexOf(".");
            this.uri = Path.getURI(this.request).substring(0, lastIndexOfDot);
            this.extension = Path.getURI(this.request).substring(lastIndexOfDot + 1);
        }
        catch (Exception e) {
            this.uri = Path.getURI(this.request);
            this.extension = Server.getDefaultExtension();
        }
    }

    /**
     * Collect content from the pre configured repository and attach it to the HttpServletRequest.
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public boolean collect() throws PathNotFoundException, RepositoryException {
        boolean success = true;
        this.hierarchyManager = SessionAccessControl.getHierarchyManager(this.request);
        this.parseURI();
        if (this.hierarchyManager.isNodeData(this.uri)) {
            this.getRequestedContent(Aggregator.ATOM);
            this.setRequestReceiver(Aggregator.ATOM);
        }
        else if (this.hierarchyManager.isPage(this.uri)) {
            this.getRequestedContent();
            this.setRequestReceiver();
        }
        else {
            /* check again, resource might have different name */
            int lastIndexOfSlash = this.uri.lastIndexOf("/");
            if (lastIndexOfSlash > -1) {
                this.uri = this.uri.substring(0, lastIndexOfSlash);
                try {
                    this.getRequestedContent(Aggregator.ATOM);
                    this.setRequestReceiver(Aggregator.ATOM);
                }
                catch (Exception e) {
                    this.setRequestReceiver(true);
                    success = false;
                }
            }
            else {
                this.setRequestReceiver(true);
                success = false;
            }
        }
        this.updateRequest();
        return success;
    }

    /**
     * Set the template responsible to handle this request.
     */
    private void setRequestReceiver() {
        try {
            String templateName = this.requestedPage.getMetaData().getTemplate();
            this.requestReceiver = Template.getInfo(templateName).getPath(this.extension);
        }
        catch (Exception e) {
            log.error("Failed to set request receiver");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Set the template responsible to handle this request.
     * @param type
     */
    private void setRequestReceiver(int type) {
        try {
            String templateName = this.subContentNode.getNodeData("nodeDataTemplate").getString();
            if (StringUtils.isEmpty(templateName)) {
                this.setRequestReceiver(true);
                return;
            }
            this.requestReceiver = Template.getInfo(templateName).getPath(this.extension);
        }
        catch (Exception e) {
            this.setRequestReceiver(true);
            return;
        }
    }

    /**
     * Set the servlet responsible to handle direct resource request.
     */
    private void setRequestReceiver(boolean direct) {
        this.requestReceiver = Aggregator.DIRECT_REQUEST_RECEIVER;
    }

    /**
     * Attach all collected information to the HttpServletRequest.
     */
    private void updateRequest() {
        if (this.requestedPage != null) {
            this.request.setAttribute(Aggregator.ACTPAGE, this.requestedPage);
            this.request.setAttribute(Aggregator.CURRENT_ACTPAGE, this.requestedPage);
        }
        if ((this.requestedData != null) && (this.subContentNode != null)) {
            File file = new File();
            file.setProperties(this.subContentNode);
            file.setNodeData(this.requestedData);
            this.request.setAttribute(Aggregator.FILE, file);
        }
        this.request.setAttribute(Aggregator.HANDLE, this.uri);
        this.request.setAttribute(Aggregator.EXTENSION, this.extension);
        this.request.setAttribute(Aggregator.HIERARCHY_MANAGER, this.hierarchyManager);
        this.request.setAttribute(Aggregator.REQUEST_RECEIVER, this.requestReceiver);
    }
}
