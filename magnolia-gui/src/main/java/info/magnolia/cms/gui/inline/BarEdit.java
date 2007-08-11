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

import info.magnolia.cms.beans.config.Server;
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
public class BarEdit extends Bar {

    private Button buttonEdit = new ButtonEdit();

    private Button buttonMove = new Button();

    private Button buttonDelete = new Button();

    public BarEdit(HttpServletRequest request) {
        this.setRequest(request);
    }

    /**
     * <p>
     * sets the default buttons
     * </p>
     */
    public void setDefaultButtons() {
        this.setButtonEdit();
        this.setButtonMove();
        this.setButtonDelete();
    }

    /**
     * <p>
     * places the default buttons to the very right/left position
     * </p>
     */
    public void placeDefaultButtons() {
        if (this.getButtonMove() != null) {
            this.getButtonsLeft().add(0, this.getButtonMove());
        }
        if (this.getButtonEdit() != null) {
            this.getButtonsLeft().add(0, this.getButtonEdit());
        }
        if (this.getButtonDelete() != null) {
            this.getButtonsRight().add(this.getButtonsRight().size(), this.getButtonDelete());
        }
    }

    public Button getButtonEdit() {
        return this.buttonEdit;
    }

    public void setButtonEdit(Button b) {
        this.buttonEdit = b;
    }

    public void setButtonEdit() {
        this.setButtonEdit(this.getPath(), this.getNodeCollectionName(StringUtils.EMPTY), this
            .getNodeName(StringUtils.EMPTY), this.getParagraph());
    }

    /**
     * <p>
     * sets the default edit button
     * </p>
     * @param path , path of the current page
     * @param nodeCollectionName , i.e. 'MainParagarphs'
     * @param nodeName , i.e. '01'
     * @param paragraph , paragraph type
     */
    public void setButtonEdit(String path, String nodeCollectionName, String nodeName, String paragraph) {
        ButtonEdit b = new ButtonEdit(this.getRequest(), path, nodeCollectionName, nodeName, paragraph);
        b.setDefaultOnclick(this.getRequest());
        this.setButtonEdit(b);
    }

    public Button getButtonMove() {
        return this.buttonMove;
    }

    public void setButtonMove(Button b) {
        this.buttonMove = b;
    }

    public void setButtonMove() {
        this.setButtonMove(this.getNodeCollectionName(StringUtils.EMPTY), this.getNodeName(StringUtils.EMPTY));
    }

    /**
     * <p>
     * sets the default move button
     * </p>
     * @param nodeCollectionName , i.e. 'MainParagarphs'
     * @param nodeName , i.e. '01'
     */
    public void setButtonMove(String nodeCollectionName, String nodeName) {
        Button b = new Button();
        b.setLabel(MessagesManager.get("buttons.move")); //$NON-NLS-1$
        // sets the id of the bar
        this.setId(nodeCollectionName + "__" + nodeName); //$NON-NLS-1$
        b.setOnclick("mgnlMoveNodeStart('" + nodeCollectionName + "','" + nodeName + "','" + this.getId() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        this.setButtonMove(b);
    }

    public Button getButtonDelete() {
        return this.buttonDelete;
    }

    public void setButtonDelete(Button b) {
        this.buttonDelete = b;
    }

    public void setButtonDelete() {
        this.setButtonDelete(this.getPath(), this.getNodeCollectionName(), this.getNodeName());
    }

    /**
     * <p>
     * sets the default delete button
     * </p>
     * @param path , path of the current page
     * @param nodeCollectionName , i.e. 'MainColumnParagraphs'
     * @param nodeName , i.e. '01'
     */
    public void setButtonDelete(String path, String nodeCollectionName, String nodeName) {
        Button b = new Button();
        b.setLabel(MessagesManager.get("buttons.delete")); //$NON-NLS-1$
        b.setOnclick("mgnlDeleteNode('" + path + "','" + nodeCollectionName + "','" + nodeName + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        this.setButtonDelete(b);
    }

    /**
     * <p>
     * draws the main bar (incl. all magnolia specific js and css sources)
     * </p>
     */
    public void drawHtml(JspWriter out) throws IOException {
        boolean isGranted = Resource.getActivePage().isGranted(Permission.SET);
        if (!Resource.showPreview() && isGranted && Server.isAdmin()) {
            this.setEvent("onmousedown", "mgnlMoveNodeEnd(this,'" + this.getPath() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.setEvent("onmouseover", "mgnlMoveNodeHigh(this);"); //$NON-NLS-1$ //$NON-NLS-2$
            this.setEvent("onmouseout", "mgnlMoveNodeReset(this);"); //$NON-NLS-1$ //$NON-NLS-2$
            out.println(this.getHtml());
        }
    }
}
