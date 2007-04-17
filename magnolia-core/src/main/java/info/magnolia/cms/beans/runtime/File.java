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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.core.NodeData;
import org.apache.commons.lang.math.NumberUtils;

import javax.jcr.RepositoryException;
import java.io.InputStream;


/**
 * Wraps a NodeData and exposes it into a simple file-like bean.
 *
 * @author Sameer Charles
 * @version 1.1
 */
public class File {

    private final NodeData data;

    private String extension;

    private String fileName;

    private String contentType;

    private String nodeDataTemplate;

    private int size;

    public File(NodeData data) {
        this.data = data;

        this.setNodeDataTemplate(data.getAttribute("nodeDataTemplate")); //$NON-NLS-1$
        this.setExtension(data.getAttribute("extension")); //$NON-NLS-1$
        this.setFileName(data.getAttribute("fileName")); //$NON-NLS-1$
        this.setContentType(data.getAttribute("contentType")); //$NON-NLS-1$

        String sizeString = data.getAttribute("size"); //$NON-NLS-1$
        if (NumberUtils.isNumber(sizeString)) {
            this.setSize(Integer.parseInt(sizeString));
        }

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

    public InputStream getStream() {
        try {
            return this.data.getValue().getStream();
        }
        catch (RepositoryException re) {
            return null;
        }
    }
}
