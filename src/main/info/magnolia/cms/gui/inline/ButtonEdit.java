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
package info.magnolia.cms.gui.inline;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.i18n.ContextMessages;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class ButtonEdit extends Button {

    String label = ContextMessages.getInstance(getRequest()).get("buttons.edit");

    public ButtonEdit() {
    }

    public ButtonEdit(HttpServletRequest request) {
        this.setRequest(request);
    }

    public ButtonEdit(
        HttpServletRequest request,
        String path,
        String nodeCollectionName,
        String nodeName,
        String paragraph) {
        this.setRequest(request);
        this.setPath(path);
        this.setNodeCollectionName(nodeCollectionName);
        this.setNodeName(nodeName);
        this.setParagraph(paragraph);
    }

    public void setDefaultOnclick(HttpServletRequest request) {
        String nodeCollectionName = this.getNodeCollectionName();
        if (nodeCollectionName == null) {
            nodeCollectionName = StringUtils.EMPTY;
        }
        String nodeName = this.getNodeName();
        if (nodeName == null) {
            nodeName = StringUtils.EMPTY;
        }
        // todo: dynamic repository
        String repository = ContentRepository.WEBSITE;
        this.setOnclick("mgnlOpenDialog('"
            + this.getPath()
            + "','"
            + nodeCollectionName
            + "','"
            + nodeName
            + "','"
            + this.getParagraph()
            + "','"
            + repository
            + "');");
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String s) {
        this.label = s;
    }

    /**
     * <p>
     * draws the edit button
     * </p>
     * <p>
     * request has to be set!
     * </p>
     */
    public void drawHtml(JspWriter out) throws IOException {
        // todo: attribute for preview name not static!
        // todo: a method to get preview?
        if (this.getRequest() != null) {
            String prev = (String) this.getRequest().getSession().getAttribute("mgnlPreview");
            boolean isGranted = Resource.getActivePage(this.getRequest()).isGranted(Permission.SET);
            if (prev == null && isGranted) {
                out.println(this.getHtml());
            }
        }
    }
}
