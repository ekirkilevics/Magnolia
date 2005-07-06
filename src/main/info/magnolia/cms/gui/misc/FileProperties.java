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
package info.magnolia.cms.gui.misc;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class FileProperties {

    public static final String PROPERTIES_CONTENTNODE = "properties"; //$NON-NLS-1$

    public static final String PROPERTY_CONTENTTYPE = "contentType"; //$NON-NLS-1$

    public static final String PROPERTY_SIZE = "size"; //$NON-NLS-1$

    public static final String PROPERTY_TEMPLATE = "nodeDataTemplate"; //$NON-NLS-1$

    public static final String PROPERTY_EXTENSION = "extension"; //$NON-NLS-1$

    public static final String PROPERTY_FILENAME = "fileName"; //$NON-NLS-1$

    public static final String EXTENSION = "extension"; // Pdf //$NON-NLS-1$

    public static final String EXTENSION_LOWER_CASE = "extensionLowerCase"; // pdf //$NON-NLS-1$

    public static final String EXTENSION_UPPER_CASE = "extensionUpperCase"; // PDF //$NON-NLS-1$

    public static final String NAME = "name"; // report2004.Pdf //$NON-NLS-1$

    public static final String NAME_WITHOUT_EXTENSION = "nameWithoutExtension"; // report2004 //$NON-NLS-1$

    public static final String CONTENT_TYPE = "contentType"; // application/pdf //$NON-NLS-1$

    public static final String TEMPLATE = "template"; // ((according to dialog)) //$NON-NLS-1$

    public static final String HANDLE = "handle"; // /en/mainColumnParagraph/04/file //$NON-NLS-1$

    public static final String PATH_WITHOUT_NAME = "pathWithoutName"; // /en/mainColumnParagraph/04/file.Pdf
                                                                        // //$NON-NLS-1$

    public static final String PATH = "path"; // path including fileName: //$NON-NLS-1$

    // /en/mainColumnParagraph/04/file/report2004.Pdf
    public static final String SIZE_BYTES = "sizeBytes"; // size in bytes: 263492 //$NON-NLS-1$

    public static final String SIZE_KB = "sizeKB"; // size in KB: 257.3 //$NON-NLS-1$

    public static final String SIZE_MB = "sizeMB"; // size in MB: 0.2 //$NON-NLS-1$

    public static final String SIZE = "size"; // size and unit depending of size in bytes, KB, or MB: 257.3
                                                // //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(FileProperties.class);

    private Content content;

    private String nodeDataName;

    public FileProperties(Content content, String nodeDataName) {
        this.setContent(content);
        this.setNodeDataName(nodeDataName);
    }

    public void setContent(Content c) {
        this.content = c;
    }

    public Content getContent() {
        return this.content;
    }

    public void setNodeDataName(String s) {
        this.nodeDataName = s;
    }

    public String getNodeDataName() {
        return this.nodeDataName;
    }

    public String getProperty(String property) {
        String value = StringUtils.EMPTY;
        try {
            Content props = this.getContent().getContent(this.nodeDataName + "_" + PROPERTIES_CONTENTNODE); //$NON-NLS-1$
            String filename = props.getNodeData(PROPERTY_FILENAME).getString();
            String ext = props.getNodeData(PROPERTY_EXTENSION).getString();
            String fullName = filename;
            String fullExt = StringUtils.EMPTY;
            if (StringUtils.isNotEmpty(ext)) {
                fullExt = "." + ext; //$NON-NLS-1$
                fullName += fullExt;
            }
            if (property.equals(EXTENSION)) {
                value = ext;
            }
            else if (property.equals(EXTENSION_LOWER_CASE)) {
                value = ext.toLowerCase();
            }
            else if (property.equals(EXTENSION_UPPER_CASE)) {
                value = ext.toUpperCase();
            }
            else if (property.equals(NAME_WITHOUT_EXTENSION)) {
                value = filename;
            }
            else if (property.equals(CONTENT_TYPE)) {
                value = props.getNodeData(PROPERTY_CONTENTTYPE).getString();
            }
            else if (property.equals(TEMPLATE)) {
                value = props.getNodeData(PROPERTY_TEMPLATE).getString();
            }
            else if (property.equals(HANDLE)) {
                value = this.getContent().getHandle() + "/" + this.getNodeDataName(); //$NON-NLS-1$
            }
            else if (property.equals(NAME)) {
                value = fullName;
            }
            else if (property.equals(PATH_WITHOUT_NAME)) {
                value = this.getContent().getHandle() + "/" + this.getNodeDataName() + fullExt; //$NON-NLS-1$
            }
            else if (property.equals(SIZE_BYTES)) {
                value = props.getNodeData(PROPERTY_SIZE).getString();
            }
            else if (property.equals(SIZE_KB)) {
                double size = props.getNodeData(PROPERTY_SIZE).getLong();
                String sizeStr;
                size = size / 1024;
                sizeStr = Double.toString(size);
                sizeStr = sizeStr.substring(0, sizeStr.indexOf(".") + 2); //$NON-NLS-1$
                value = sizeStr;
            }
            else if (property.equals(SIZE_MB)) {
                double size = props.getNodeData(PROPERTY_SIZE).getLong();
                String sizeStr;
                size = size / (1024 * 1024);
                sizeStr = Double.toString(size);
                sizeStr = sizeStr.substring(0, sizeStr.indexOf(".") + 2); //$NON-NLS-1$
                value = sizeStr;
            }
            else if (property.equals(SIZE)) {
                double size = props.getNodeData(PROPERTY_SIZE).getLong();
                String unit = "bytes";
                String sizeStr;
                if (size >= 1000) {
                    size = size / 1024;
                    unit = "KB";
                    if (size >= 1000) {
                        size = size / 1024;
                        unit = "MB";
                    }
                    sizeStr = Double.toString(size);
                    sizeStr = sizeStr.substring(0, sizeStr.indexOf(".") + 2); //$NON-NLS-1$
                }
                else {
                    sizeStr = Double.toString(size);
                    sizeStr = sizeStr.substring(0, sizeStr.indexOf(".")); //$NON-NLS-1$
                }
                value = sizeStr + " " + unit; //$NON-NLS-1$
            }
            else { // property.equals(PATH|null|""|any other value)
                value = this.getContent().getHandle() + "/" + this.getNodeDataName() + "/" + fullName; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException e) {
            log.info("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        return value;
    }
}
