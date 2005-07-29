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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import java.io.InputStream;

import javax.jcr.RepositoryException;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public class File {

    private NodeData data;

    private Content properties;

    private String extension;

    private String fileName;

    private String contentType;

    private String nodeDataTemplate;

    private int size;

    public Content getProperties() {
        return properties;
    }

    public void setProperties(Content properties) {
        this.properties = properties;

        this.setNodeDataTemplate(this.properties.getNodeData("nodeDataTemplate").getString()); //$NON-NLS-1$
        this.setExtension(this.properties.getNodeData("extension").getString()); //$NON-NLS-1$
        this.setFileName(this.properties.getNodeData("fileName").getString()); //$NON-NLS-1$
        this.setContentType(this.properties.getNodeData("contentType").getString()); //$NON-NLS-1$
        Integer size = new Integer(this.properties.getNodeData("size").getString()); //$NON-NLS-1$
        this.setSize(size.intValue());

    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getNodeDataTemplate() {
        return nodeDataTemplate;
    }

    public void setNodeDataTemplate(String nodeDataTemplate) {
        this.nodeDataTemplate = nodeDataTemplate;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public NodeData getNodeData() {
        return this.data;
    }

    public void setNodeData(NodeData data) {
        this.data = data;
    }

    public InputStream getStream() {
        try {
            return this.data.getValue().getStream();
        }
        catch (RepositoryException re) {
            return null;
        }
    }
}
