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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.misc.FileProperties;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class File extends ControlSuper {

    public static final String REMOVE = "remove";

    public String cssClassFileName = "";

    private String nodeDataTemplate = null;

    public File() {
    }

    public File(String name, String value) {
        super(name, value);
    }

    public File(String name, Content websiteNode) {
        super(name, websiteNode);
    }

    public void setCssClassFileName(String s) {
        this.cssClassFileName = s;
    }

    public String getCssClassFileName() {
        return this.cssClassFileName;
    }

    public String getHtmlCssClassFileName() {
        if (!this.getCssClassFileName().equals(""))
            return " class=\"" + this.getCssClassFileName() + "\"";
        else
            return "";
    }

    public String getHtml() {
        String html = "";
        html += this.getHtmlBrowse();
        html += this.getHtmlFileName();
        html += this.getHtmlNodeDataTemplate();
        html += this.getHtmlRemove();
        return html;
    }

    public String getHtmlBrowse() {
        StringBuffer html = new StringBuffer();
        html.append("<input type=\"file\"");
        html.append(" name=\"" + this.getName() + "\"");
        html.append(" id=\"" + this.getName() + "\"");
        html.append(" onchange=\"mgnlControlFileSetFileName('" + this.getName() + "')\"");
        html.append(" onblur=\"mgnlControlFileSetFileName('" + this.getName() + "')\"");
        html.append(this.getHtmlCssClass());
        // html.append(this.getHtmlCssStyles());
        html.append(">");
        Hidden control0 = new Hidden(this.getName() + "_" + REMOVE, "");
        control0.setSaveInfo(false);
        html.append(control0.getHtml());
        if (this.getSaveInfo())
            html.append(this.getHtmlSaveInfo());
        return html.toString();
    }

    public String getFileName() {
        String fileName = "";
        try {
            fileName = this.getWebsiteNode().getContentNode(
                this.getName() + "_" + FileProperties.PROPERTIES_CONTENTNODE).getNodeData(
                FileProperties.PROPERTY_FILENAME).getString();
        }
        catch (Exception e) {
        }
        return fileName;
    }

    public void setNodeDataTemplate(String s) {
        this.nodeDataTemplate = s;
    }

    public String getNodeDataTemplate() {
        String template = this.nodeDataTemplate;
        if (template == null) {
            try {
                template = this.getWebsiteNode().getContentNode(
                    this.getName() + "_" + FileProperties.PROPERTIES_CONTENTNODE).getNodeData(
                    FileProperties.PROPERTY_TEMPLATE).getString();
            }
            catch (Exception e) {
            }
        }
        return template;
    }

    public String getExtension() {
        String ext = "";
        try {
            ext = this
                .getWebsiteNode()
                .getContentNode(this.getName() + "_" + FileProperties.PROPERTIES_CONTENTNODE)
                .getNodeData(FileProperties.PROPERTY_EXTENSION)
                .getString();
        }
        catch (Exception e) {
        }
        return ext;
    }

    public String getHtmlFileName() {
        Edit control = new Edit(this.getName() + "_" + FileProperties.PROPERTY_FILENAME, this.getFileName());
        control.setSaveInfo(false);
        control.setCssClass(this.getCssClassFileName());
        // control.setCssStyles(this.getCssStyles());
        control.setCssStyles("width", "45%");
        return control.getHtml();
    }

    public String getHtmlNodeDataTemplate() {
        Hidden control = new Hidden(this.getName() + "_" + FileProperties.PROPERTY_TEMPLATE, this.getNodeDataTemplate());
        control.setSaveInfo(false);
        return control.getHtml();
    }

    public String getHtmlRemove() {
        return getHtmlRemove("");
    }

    public String getHtmlRemove(String additionalOnclick) {
        Button control1 = new Button();
        control1.setLabel("Remove file");
        control1.setCssClass("mgnlControlButtonSmall");
        control1.setOnclick(additionalOnclick + "mgnlControlFileRemove('" + this.getName() + "')");
        return control1.getHtml();
    }

    public String getHandle() {
        String path = "";
        try {
            path = this.getWebsiteNode().getHandle() + "/" + this.getName();
        }
        catch (Exception e) {
        }
        return path;
    }

    public String getPath() {
        String path = "";
        try {
            path = this.getWebsiteNode().getHandle()
                + "/"
                + this.getName()
                + "/"
                + this.getFileName()
                + "."
                + this.getExtension();
        }
        catch (Exception e) {
        }
        return path;
    }
}
