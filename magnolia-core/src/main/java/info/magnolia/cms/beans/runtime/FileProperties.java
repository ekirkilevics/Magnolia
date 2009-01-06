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
package info.magnolia.cms.beans.runtime;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import org.apache.commons.lang.StringUtils;


/**
 * Similar to info.magnolia.cms.beans.runtime.File but exposes the binary's properties using constants.
 *
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class FileProperties {

    public static final String PROPERTIES_CONTENTNODE = "properties"; //$NON-NLS-1$

    public static final String PROPERTY_CONTENTTYPE = "jcr:mimeType"; //$NON-NLS-1$

    public static final String PROPERTY_ENCODING = "jcr:encoding"; //$NON-NLS-1$

    public static final String PROPERTY_LASTMODIFIED = "jcr:lastModified"; //$NON-NLS-1$

    public static final String PROPERTY_SIZE = "size"; //$NON-NLS-1$

    public static final String PROPERTY_TEMPLATE = "nodeDataTemplate"; //$NON-NLS-1$

    public static final String PROPERTY_EXTENSION = "extension"; //$NON-NLS-1$

    public static final String PROPERTY_FILENAME = "fileName"; //$NON-NLS-1$

    public static final String PROPERTY_ICON = "icon"; //$NON-NLS-1$

    public static final String PROPERTY_WIDTH = "width"; //$NON-NLS-1$

    public static final String PROPERTY_HEIGHT = "height"; //$NON-NLS-1$

    public static final String EXTENSION = "extension"; // Pdf //$NON-NLS-1$

    public static final String EXTENSION_LOWER_CASE = "extensionLowerCase"; // pdf //$NON-NLS-1$

    public static final String EXTENSION_UPPER_CASE = "extensionUpperCase"; // PDF //$NON-NLS-1$

    public static final String NAME = "name"; // report2004.Pdf //$NON-NLS-1$

    public static final String NAME_WITHOUT_EXTENSION = "nameWithoutExtension"; // report2004 //$NON-NLS-1$

    public static final String CONTENT_TYPE = "jcr:mimeType"; // application/pdf //$NON-NLS-1$

    public static final String ICON = "icon"; // the icon for this type

    public static final String TEMPLATE = "template"; // ((according to dialog)) //$NON-NLS-1$

    public static final String HANDLE = "handle"; // /en/mainColumnParagraph/04/file //$NON-NLS-1$

    /**
     * /en/mainColumnParagraph/04/file.Pdf
     */
    public static final String PATH_WITHOUT_NAME = "pathWithoutName"; //$NON-NLS-1$

    /**
     * path including fileName: <code>/en/mainColumnParagraph/04/file/report2004.Pdf</code>
     */
    public static final String PATH = "path"; //$NON-NLS-1$

    /**
     * size in bytes: <code>263492</code>
     */
    public static final String SIZE_BYTES = "sizeBytes"; //$NON-NLS-1$

    /**
     * size in KB: <code>257.3</code>
     */
    public static final String SIZE_KB = "sizeKB"; //$NON-NLS-1$

    /**
     * size in MB: <code>0.2</code>
     */
    public static final String SIZE_MB = "sizeMB"; //$NON-NLS-1$

    /**
     * size and unit depending of size in bytes, KB, or MB: <code>257.3</code>
     */
    public static final String SIZE = "size"; //$NON-NLS-1$

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
        NodeData props = this.getContent().getNodeData(this.nodeDataName);
        String filename = props.getAttribute(PROPERTY_FILENAME);
        String ext = props.getAttribute(PROPERTY_EXTENSION);
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
            value = props.getAttribute(PROPERTY_CONTENTTYPE);
        }
        else if (property.equals(TEMPLATE)) {
            value = props.getAttribute(PROPERTY_TEMPLATE);
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
        else if (property.equals(ICON)) {
            value = MIMEMapping.getMIMETypeIcon(ext);
        }
        else if (property.equals(SIZE_BYTES)) {
            value = props.getAttribute(PROPERTY_SIZE);
        }
        else if (property.equals(SIZE_KB)) {
            double size = Long.parseLong(props.getAttribute(PROPERTY_SIZE));
            String sizeStr;
            size = size / 1024;
            sizeStr = Double.toString(size);
            sizeStr = sizeStr.substring(0, sizeStr.indexOf(".") + 2); //$NON-NLS-1$
            value = sizeStr;
        }
        else if (property.equals(SIZE_MB)) {
            double size = Long.parseLong(props.getAttribute(PROPERTY_SIZE));
            String sizeStr;
            size = size / (1024 * 1024);
            sizeStr = Double.toString(size);
            sizeStr = sizeStr.substring(0, sizeStr.indexOf(".") + 2); //$NON-NLS-1$
            value = sizeStr;
        }
        else if (property.equals(SIZE)) {
            double size = Long.parseLong(props.getAttribute(PROPERTY_SIZE));
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
        else if (property.equals(PROPERTY_WIDTH)) {
            value = props.getAttribute(PROPERTY_WIDTH);
        }
        else if (property.equals(PROPERTY_HEIGHT)) {
            value = props.getAttribute(PROPERTY_HEIGHT);
        }
        else { // property.equals(PATH|null|""|any other value)
            value = this.getContent().getHandle() + "/" + this.getNodeDataName() + "/" + fullName; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return value;
    }
}
