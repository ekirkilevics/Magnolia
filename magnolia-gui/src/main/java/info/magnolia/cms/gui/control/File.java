/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.control;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.MessagesManager;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class File extends ControlImpl {

    public static final String REMOVE = "remove"; //$NON-NLS-1$

    private String cssClassFileName;

    private String nodeDataTemplate;

    /**
     * Package private constructor
     */
    File() {
    }

    /**
     * @param name
     * @param value
     */
    public File(String name, String value) {
        super(name, value);
    }

    /**
     * @param name
     * @param content
     */
    public File(String name, Content content) {
        super(name, content);
    }

    public void setCssClassFileName(String s) {
        this.cssClassFileName = s;
    }

    public String getCssClassFileName() {
        return this.cssClassFileName;
    }

    public String getHtmlCssClassFileName() {
        if (StringUtils.isNotEmpty(this.cssClassFileName)) {
            return " class=\"" + this.cssClassFileName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        }

        return StringUtils.EMPTY;
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append(this.getHtmlBrowse());
        html.append(this.getHtmlFileName());
        html.append(this.getHtmlNodeDataTemplate());
        html.append(this.getHtmlRemove());
        return html.toString();
    }

    public String getHtmlBrowse() {
        StringBuffer html = new StringBuffer();
        html.append("<input type=\"file\""); //$NON-NLS-1$
        html.append(" name=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" id=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" onchange=\"mgnlControlFileSetFileName('" + this.getName() + "')\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" onblur=\"mgnlControlFileSetFileName('" + this.getName() + "')\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(this.getHtmlCssClass());
        html.append(" />"); //$NON-NLS-1$
        Hidden control0 = new Hidden(this.getName() + "_" + REMOVE, StringUtils.EMPTY); //$NON-NLS-1$
        control0.setSaveInfo(false);
        html.append(control0.getHtml());
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        return html.toString();
    }

    public String getFileName() {
        return getPropertyString(FileProperties.PROPERTY_FILENAME);
    }

    public String getImageWidth() {
        return getPropertyString(FileProperties.PROPERTY_WIDTH);
    }

    public String getImageHeight() {
        return getPropertyString(FileProperties.PROPERTY_HEIGHT);
    }

    public void setNodeDataTemplate(String s) {
        this.nodeDataTemplate = s;
    }

    public String getNodeDataTemplate() {
        return this.nodeDataTemplate;
    }

    public String getExtension() {
        return getPropertyString(FileProperties.PROPERTY_EXTENSION);
    }

    public String getHtmlFileName() {
        Edit control = new Edit(this.getName() + "_" + FileProperties.PROPERTY_FILENAME, this.getFileName()); //$NON-NLS-1$
        control.setSaveInfo(false);
        if (StringUtils.isNotEmpty(this.getCssClassFileName())) {
            control.setCssClass(this.cssClassFileName);
        }

        control.setCssStyles("width", "45%"); //$NON-NLS-1$ //$NON-NLS-2$
        return control.getHtml();
    }

    public String getHtmlNodeDataTemplate() {
        Hidden control = new Hidden(this.getName() + "_" + FileProperties.PROPERTY_TEMPLATE, this.getNodeDataTemplate()); //$NON-NLS-1$
        control.setSaveInfo(false);
        return control.getHtml();
    }

    public String getHtmlRemove() {
        return getHtmlRemove(StringUtils.EMPTY);
    }

    public String getHtmlRemove(String additionalOnclick) {
        Button control1 = new Button();
        control1.setLabel(MessagesManager.get("dialog.file.remove")); //$NON-NLS-1$
        control1.setCssClass("mgnlControlButtonSmall"); //$NON-NLS-1$
        control1.setOnclick(additionalOnclick + "mgnlControlFileRemove('" + this.getName() + "')"); //$NON-NLS-1$ //$NON-NLS-2$
        return control1.getHtml();
    }

    public String getHandle() {
        return this.getWebsiteNode().getHandle() + "/" + this.getName(); //$NON-NLS-1$
    }

    public String getPath() {
        return getHandle() + "/" + this.getFileName() + "." + this.getExtension(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Read a property from content node, check nulls.
     * @param propertyName node data name
     * @return property string, "" if not found
     * @throws RepositoryException
     */
    protected String getPropertyString(String propertyName) {
        if (this.getWebsiteNode() != null) {
            NodeData nodeData = getPropertyNode();
            if (nodeData != null) {
                return nodeData.getAttribute(propertyName);
            }
        }

        return StringUtils.EMPTY;
    }

    protected NodeData getPropertyNode() {
        return this.getWebsiteNode().getNodeData(this.getName());
    }
}
