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
import info.magnolia.cms.gui.control.Bar;
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
public class BarNew extends Bar {

    private Button buttonNew = new Button();

    public BarNew(HttpServletRequest request) {
        this.setRequest(request);
    }

    /**
     * Sets the default buttons.
     */
    public void setDefaultButtons() {
        this.setButtonNew();
    }

    /**
     * Places the default buttons to the very right/left position.
     */
    public void placeDefaultButtons() {
        if (this.getButtonNew() != null) {
            this.getButtonsLeft().add(0, this.getButtonNew());
        }
    }

    public Button getButtonNew() {
        return this.buttonNew;
    }

    public void setButtonNew(Button b) {
        this.buttonNew = b;
    }

    public void setButtonNew() {
        this.setButtonNew(this.getPath(), this.getNodeCollectionName(StringUtils.EMPTY), this
            .getNodeName(StringUtils.EMPTY), this.getParagraph());
    }

    /**
     * Sets the default edit button.
     * @param path , path of the current page
     * @param nodeCollectionName , i.e. 'MainParagarphs'
     * @param nodeName , i.e. '01'
     * @param paragraph , paragraph type
     */
    public void setButtonNew(String path, String nodeCollectionName, String nodeName, String paragraph) {
        Button b = new Button();
        b.setLabel(ContextMessages.getInstanceSafely(getRequest()).get("buttons.new"));
        // todo: dynamic repository
        String repository = ContentRepository.WEBSITE;
        b.setOnclick("mgnlOpenDialog('"
            + path
            + "','"
            + nodeCollectionName
            + "','"
            + nodeName
            + "','"
            + paragraph
            + "','"
            + repository
            + "');");
        this.setButtonNew(b);
    }

    /**
     * <p>
     * draws the main bar (incl. all magnolia specific js and css sources)
     * </p>
     */
    public void drawHtml(JspWriter out) throws IOException {
        // todo: attribute for preview name not static!
        // todo: a method to get preview?
        String prev = (String) this.getRequest().getSession().getAttribute("mgnlPreview");
        boolean isGranted = Resource.getActivePage(this.getRequest()).isGranted(Permission.SET);
        if (prev == null && isGranted) {
            this.setEvent("onmousedown", "mgnlMoveNodeEnd(this,'" + this.getPath() + "');");
            this.setEvent("onmouseover", "mgnlMoveNodeHigh(this);");
            this.setEvent("onmouseout", "mgnlMoveNodeReset(this);");
            this.setId(this.getNodeCollectionName() + "__" + this.getNodeName());
            out.println(this.getHtml());
        }
    }
}
