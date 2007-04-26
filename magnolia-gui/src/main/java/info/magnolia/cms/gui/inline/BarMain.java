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
import info.magnolia.cms.core.Aggregator;
import info.magnolia.cms.gui.control.Bar;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class BarMain extends Bar {

    private Button buttonEditView = new Button();

    private Button buttonPreview = new ButtonEdit();

    private Button buttonProperties = new Button();

    private Button buttonSiteAdmin = new Button();

    private int top;

    private int left;

    private String width = "100%"; //$NON-NLS-1$

    private boolean overlay = true;

    /**
     * true if the AdminCentral button is visible
     */
    private boolean adminButtonVisible = true;

    public BarMain(HttpServletRequest request) {
        this.setRequest(request);
    }

    public BarMain(HttpServletRequest request, String path, String nodeCollectionName, String nodeName, String paragraph) {
        this.setRequest(request);
        this.setPath(path);
        this.setNodeCollectionName(nodeCollectionName);
        this.setNodeName(nodeName);
        this.setParagraph(paragraph);
    }

    /**
     * <p>
     * sets the default buttons
     * </p>
     */
    public void setDefaultButtons() {
        this.setButtonEditView();
        this.setButtonPreview();
        this.setButtonSiteAdmin();
        this.setButtonProperties();
    }

    /**
     * <p>
     * places the default buttons to the very right/left position
     * </p>
     */
    public void placeDefaultButtons() {
        if (this.isAdminButtonVisible()) {
            this.getButtonsLeft().add(0, this.getButtonSiteAdmin());
        }
        this.getButtonsLeft().add(0, this.getButtonPreview());
        if (this.getParagraph() != null) {
            this.getButtonsRight().add(this.getButtonsRight().size(), this.getButtonProperties());
        }
    }

    public Button getButtonProperties() {
        return this.buttonProperties;
    }

    public void setButtonProperties(Button b) {
        this.buttonProperties = b;
    }

    public void setButtonProperties() {
        this.setButtonProperties(this.getPath(), this.getParagraph());
    }

    /**
     * <p>
     * sets the default page properties button
     * </p>
     * @param path , path of the current page
     * @param paragraph , paragraph type
     */
    public void setButtonProperties(String path, String paragraph) {
        ButtonEdit b = new ButtonEdit(this.getRequest());
        b.setLabel(MessagesManager.get("buttons.properties")); //$NON-NLS-1$
        b.setPath(path);
        b.setParagraph(paragraph);
        b.setDefaultOnclick(this.getRequest());
        this.setButtonProperties(b);
    }

    public Button getButtonPreview() {
        return this.buttonPreview;
    }

    public void setButtonPreview(Button b) {
        this.buttonPreview = b;
    }

    /**
     * <p>
     * sets the default preview button (to switch from edit to preview mode)
     * </p>
     */
    public void setButtonPreview() {
        Button b = new Button();
        String str = MessagesManager.get("buttons.preview"); //$NON-NLS-1$
        b.setLabel("&laquo; " + str); //$NON-NLS-1$
        b.setOnclick("mgnlPreview(true);"); //$NON-NLS-1$
        this.setButtonPreview(b);
    }

    public Button getButtonEditView() {
        return this.buttonEditView;
    }

    public void setButtonEditView(Button b) {
        this.buttonEditView = b;
    }

    /**
     * <p>
     * sets the default edit view button (to switch form preview to edit view mode)
     * </p>
     */
    public void setButtonEditView() {
        Button b = new Button();
        String str = MessagesManager.get("buttons.preview.hidden");
        b.setLabel("&raquo;" + str); //$NON-NLS-1$
        b.setOnclick("mgnlPreview(false);"); //$NON-NLS-1$
        this.setButtonEditView(b);
    }

    public Button getButtonSiteAdmin() {
        return this.buttonSiteAdmin;
    }

    public void setButtonSiteAdmin(Button b) {
        this.buttonSiteAdmin = b;
    }

    public void setButtonSiteAdmin() {
        this.setButtonSiteAdmin(this.getPath());
    }

    /**
     * <p>
     * sets the default site admin button
     * </p>
     * @param path , path of the current page (will show up in site admin)
     */
    public void setButtonSiteAdmin(String path) {
        Button b = new Button();
        b.setLabel(MessagesManager.get("buttons.admincentral")); //$NON-NLS-1$
        String repository = Aggregator.getRepository();
        b.setOnclick("MgnlAdminCentral.showTree('"+repository+"','" + path + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        this.setButtonSiteAdmin(b);
    }

    public void setTop(int i) {
        this.top = i;
    }

    public int getTop() {
        return this.top;
    }

    public void setLeft(int i) {
        this.left = i;
    }

    public int getLeft() {
        return this.left;
    }

    public void setWidth(String s) {
        this.width = s;
    }

    public String getWidth() {
        return this.width;
    }

    /**
     * <p>
     * sets if the main bar overlays the content (true, default) or if it is moving it downward (false)
     * </p>
     */
    public void setOverlay(boolean b) {
        this.overlay = b;
    }

    public boolean getOverlay() {
        return this.overlay;
    }

    /**
     * <p>
     * draws the main bar (incl. all magnolia specific js and css links)
     * </p>
     */
    public void drawHtml(JspWriter out) throws IOException {
        if (Server.isAdmin()) {

            boolean isGranted = Resource.getActivePage(this.getRequest()).isGranted(Permission.SET);
            if (isGranted) {

                // check if links have already been added.
                if (this.getRequest().getAttribute(Sources.REQUEST_LINKS_DRAWN) == null) {
                    this.drawHtmlLinks(out);
                    this.getRequest().setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);
                }

                int top = this.getTop();
                int left = this.getLeft();

                if (!Resource.showPreview(this.getRequest())) {
                    // is edit mode
                    this.setSmall(false);
                    if (this.getOverlay()) {
                        out.println("<div class=\"mgnlMainbar\" style=\"top:" //$NON-NLS-1$
                            + top
                            + "px;left:" //$NON-NLS-1$
                            + left
                            + "px;width:" //$NON-NLS-1$
                            + this.getWidth()
                            + ";\">"); //$NON-NLS-1$
                    }
                    out.println(this.getHtml());
                    if (this.getOverlay()) {
                        out.println("</div>"); //$NON-NLS-1$
                    }
                }
                else {
                    // is in preview mode
                    top += 4;
                    left += 4;
                    out.println("<div class=\"mgnlMainbarPreview\" style=\"top:" + top + "px;left:" + left + "px;\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    out.println(this.getButtonEditView().getHtml());
                    out.println("</div>"); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * <p>
     * draws the magnolia specific js and css links)
     * </p>
     */
    public void drawHtmlLinks(JspWriter out) throws IOException {
        out.println(new Sources(this.getRequest().getContextPath()).getHtmlCss());
        out.println(new Sources(this.getRequest().getContextPath()).getHtmlJs());
    }

    public boolean isAdminButtonVisible() {
        return this.adminButtonVisible;
    }

    public void setAdminButtonVisible(boolean adminButtonVisible) {
        this.adminButtonVisible = adminButtonVisible;
    }
}
