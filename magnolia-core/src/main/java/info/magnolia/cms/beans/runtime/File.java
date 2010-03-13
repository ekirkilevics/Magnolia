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
