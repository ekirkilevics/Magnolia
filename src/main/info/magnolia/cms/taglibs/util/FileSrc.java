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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.taglibs.ContentNodeIterator;
import info.magnolia.cms.util.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;


/**
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 * @deprecated
 */
public class FileSrc extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static Logger log = Logger.getLogger(FileSrc.class);

    private NodeData nodeData;

    private String nodeDataName = "";

    private ContentNode contentNode;

    private String contentNodeName = "";

    private String fileNameOnly = "";

    private Content actpage;

    private HttpServletRequest request;

    private String fileExtension;

    private String fileName;

    private String fileExtendedName;

    private String slash = "";

    /**
     * @deprecated
     */
    public void setAtomName(String name) {
        this.setNodeDataName(name);
    }

    /**
     * @deprecated
     * @param nodeDataName
     */
    public void setNodeDataName(String nodeDataName) {
        this.nodeDataName = nodeDataName;
    }

    /**
     * @deprecated
     */
    public void setContainerName(String name) {
        this.setContentNodeName(name);
    }

    /**
     * @deprecated
     * @param contentNodeName
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
    }

    /**
     * @deprecated
     * @param value
     */
    public void setFileNameOnly(String value) {
        this.fileNameOnly = "true";
    }

    public int doStartTag() {
        this.request = (HttpServletRequest) pageContext.getRequest();
        this.actpage = Resource.getCurrentActivePage(request);
        if (!this.contentNodeName.equals("")) {
            try {
                this.contentNode = this.actpage.getContentNode(this.contentNodeName);
            }
            catch (RepositoryException re) {
                writeSrc("");
            }
            if (this.contentNode == null) {
                writeSrc("");
                return SKIP_BODY;
            }
        }
        else {
            this.contentNode = Resource.getLocalContentNode(request);
            if (this.contentNode == null) {
                this.contentNode = Resource.getGlobalContentNode(request);
            }
            if (this.contentNode != null) {
                this.contentNodeName = this.contentNode.getName();
            }
            else {
                writeSrc("");
                return SKIP_BODY;
            }
        }
        if (this.nodeDataName.equals("")) {
            writeSrc("");
            return SKIP_BODY;
        }
        try {
            this.nodeData = this.contentNode.getNodeData(this.contentNodeName);
        }
        catch (Exception e) {
            writeSrc("");
            return SKIP_BODY;
        }
        if (this.nodeData == null) {
            writeSrc("");
            return SKIP_BODY;
        }
        setFileProperties();

        String contentNodeCollectionName = (String) pageContext.getAttribute(
            ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME,
            PageContext.REQUEST_SCOPE);
        if (this.fileNameOnly.equals("true")) {
            try {
                writeSrc(this.fileExtendedName);
            }
            catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
        else {
            if (contentNodeCollectionName == null) {
                // we are not in a loop
                try {
                    writeSrc(this.contentNode.getHandle()
                        + "/"
                        + this.nodeDataName
                        + this.slash
                        + this.fileExtendedName);
                }
                catch (Exception e) {
                    log.debug(e.getMessage());
                }
            }
            else {
                try {
                    writeSrc(Resource.getLocalContentNode(request).getHandle()
                        + "/"
                        + this.nodeDataName
                        + this.slash
                        + this.fileExtendedName);
                }
                catch (Exception e) {
                    log.debug(e.getMessage());
                }
            }
        }
        return EVAL_PAGE;
    }

    private void writeSrc(String src) {
        JspWriter out = pageContext.getOut();
        try {
            out.print(src);
        }
        catch (Exception e) {
            log.info("Exception caught: " + e.getMessage(), e);
        }
    }

    /**
     * @deprecated
     */
    private void setFileProperties() {
        this.fileExtension = Server.getDefaultExtension();
        ContentNode properties = null;
        String contentNodeCollectionName = (String) pageContext.getAttribute(
            ContentNodeIterator.CONTENT_NODE_COLLECTION_NAME,
            PageContext.REQUEST_SCOPE);
        if (contentNodeCollectionName == null) {
            // we are not in a loop
            try {
                properties = Resource.getGlobalContentNode(this.request).getContentNode(
                    this.nodeDataName + "_properties");
            }
            catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
        else {
            try {
                properties = Resource.getLocalContentNode(this.request).getContentNode(
                    this.nodeDataName + "_properties");
            }
            catch (Exception e) {
                log.info("Exception caught: " + e.getMessage(), e);
            }
        }
        if (properties != null) {
            this.fileName = properties.getNodeData("fileName").getString();
            this.fileExtension = properties.getNodeData("extension").getString();
            if (this.fileName.equals("")) {
                this.fileExtendedName = "." + this.fileExtension;
            }
            else {
                this.slash = "/";
                this.fileExtendedName = this.fileName;
                int posLastDot = this.fileName.lastIndexOf(".");
                int posExt = this.fileName.lastIndexOf("." + this.fileExtension);
                if (posExt == -1 || (posExt != -1 && posExt != posLastDot)) {
                    this.fileExtendedName += "." + this.fileExtension; // magnolia v 1.0: fileName saved with extension
                }
            }
        }
    }
}
