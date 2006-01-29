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
package info.magnolia.cms.gui.inline;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.Bar;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.i18n.MessagesManager;
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
        b.setLabel(MessagesManager.getMessages(getRequest()).get("buttons.new")); //$NON-NLS-1$
        // todo: dynamic repository
        String repository = ContentRepository.WEBSITE;
        // if there are multiple paragraphs show the selectParagraph dialog
        if (StringUtils.contains(paragraph, ',')) {
            b.setOnclick("mgnlOpenDialog('" // //$NON-NLS-1$
                + path
                + "','" //$NON-NLS-1$
                + nodeCollectionName
                + "','" // //$NON-NLS-1$
                + nodeName
                + "','" // //$NON-NLS-1$
                + paragraph // this is a list
                + "','" // //$NON-NLS-1$
                + repository
                + "','.magnolia/dialogs/selectParagraph.html');"); //$NON-NLS-1$
        }
        // there is only one paragraph
        else {
            b.setOnclick("mgnlOpenDialog('" //$NON-NLS-1$
                + path
                + "','" //$NON-NLS-1$
                + nodeCollectionName
                + "','" //$NON-NLS-1$
                + nodeName
                + "','" //$NON-NLS-1$
                + paragraph
                + "','" //$NON-NLS-1$
                + repository
                + "');"); //$NON-NLS-1$
        }
        this.setButtonNew(b);
    }

    /**
     * <p>
     * draws the main bar (incl. all magnolia specific js and css sources)
     * </p>
     */
    public void drawHtml(JspWriter out) throws IOException {
        boolean isGranted = Resource.getActivePage(this.getRequest()).isGranted(Permission.SET);
        if (!Resource.showPreview(this.getRequest()) && isGranted) {
            this.setEvent("onmousedown", "mgnlMoveNodeEnd(this,'" + this.getPath() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.setEvent("onmouseover", "mgnlMoveNodeHigh(this);"); //$NON-NLS-1$ //$NON-NLS-2$
            this.setEvent("onmouseout", "mgnlMoveNodeReset(this);"); //$NON-NLS-1$ //$NON-NLS-2$
            this.setId(this.getNodeCollectionName() + "__" + this.getNodeName()); //$NON-NLS-1$
            out.println(this.getHtml());
        }
    }
}
