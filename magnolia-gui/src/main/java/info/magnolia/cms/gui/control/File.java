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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.misc.FileProperties;
import info.magnolia.cms.i18n.MessagesManager;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


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
