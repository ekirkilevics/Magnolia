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
package info.magnolia.cms.gui.inline;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.gui.control.Bar;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class BarMain extends Bar {

    private Button buttonEditView = new Button();

    private Button buttonPreview = new ButtonEdit();

    private Button buttonProperties = new Button();

    private Button buttonSiteAdmin = new Button();
    
    private Select languageChooser;
    
    private String dialog;

    private int top;

    private int left;

    private String width = "100%"; //$NON-NLS-1$

    private boolean overlay = true;

    /**
     * True if the AdminCentral button is visible.
     */
    private boolean adminButtonVisible = true;

    /**
     * @deprecated since 4.0 - use the empty constructor.
     */
    public BarMain(HttpServletRequest request) {
    }

    /**
     * @deprecated since 4.0
     */
    public BarMain(HttpServletRequest request, String path, String nodeCollectionName, String nodeName, String paragraph) {
        this(path, nodeCollectionName, nodeName, paragraph);
    }

    public BarMain(String path, String nodeCollectionName, String nodeName, String dialog) {
        this.setPath(path);
        this.setNodeCollectionName(nodeCollectionName);
        this.setNodeName(nodeName);
        this.setDialog(dialog);
    }

    public BarMain() {
    }

    /**
     * Sets the default buttons.
     */
    public void setDefaultButtons() {
        this.setButtonEditView();
        this.setButtonPreview();
        this.setButtonSiteAdmin();
        this.setButtonProperties();
        this.setLanguageChooser();
    }

    /**
     * Places the default buttons to the very right/left position.
     */
    public void placeDefaultButtons() {
        if (this.isAdminButtonVisible()) {
            this.getButtonsLeft().add(0, this.getButtonSiteAdmin());
        }
        this.getButtonsLeft().add(0, this.getButtonPreview());
        
        if(this.languageChooser != null){
            this.getButtonsRight().add(this.languageChooser);
        }
        
        if (this.getDialog() != null) {
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
        this.setButtonProperties(this.getPath(), this.getDialog());
    }

    /**
     * Sets the default page properties button.
     *
     * @param path , path of the current page
     * @param dialog , paragraph type
     */
    public void setButtonProperties(String path, String dialog) {
        ButtonEdit b = new ButtonEdit();
        b.setLabel(MessagesManager.get("buttons.properties")); //$NON-NLS-1$
        b.setPath(path);
        b.setDialog(dialog);
        b.setDefaultOnclick();
        this.setButtonProperties(b);
    }

    public Button getButtonPreview() {
        return this.buttonPreview;
    }

    public void setButtonPreview(Button b) {
        this.buttonPreview = b;
    }

    /**
     * Sets the default preview button (to switch from edit to preview mode).
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
     * Sets the default edit view button (to switch form preview to edit view mode).
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
     * Sets the default site admin button.
     * @param path , path of the current page (will show up in site admin)
     */
    public void setButtonSiteAdmin(String path) {
        Button b = new Button();
        b.setLabel(MessagesManager.get("buttons.admincentral")); //$NON-NLS-1$
        String repository = MgnlContext.getAggregationState().getRepository();
        b.setOnclick("MgnlAdminCentral.showTree('"+repository+"','" + path + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        this.setButtonSiteAdmin(b);
    }

    protected void setLanguageChooser() {
        final I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
        if(i18nSupport.isEnabled()){
            languageChooser = new LanguageChooser();
        }
    }
    
    public Select getLanguageChooser() {
        return this.languageChooser;
    }

    public void setLanguageChooser(Select languageChooser) {
        this.languageChooser = languageChooser;
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
     * Sets if the main bar overlays the content (true, default) or if it is moving it downward (false).
     */
    public void setOverlay(boolean b) {
        this.overlay = b;
    }

    public boolean getOverlay() {
        return this.overlay;
    }

    public String getDialog() {
        return this.dialog;
    }
    
    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    /**
     * @deprecated use drawHtml(Writer out) instead.
     */
    public void drawHtml(JspWriter out) throws IOException {
        drawHtml((Writer) out);
    }

    /**
     * Draws the main bar (incl. all magnolia specific js and css links).
     */
    public void drawHtml(Writer out) throws IOException {
        if (ServerConfiguration.getInstance().isAdmin()) {

            final AggregationState aggregationState = MgnlContext.getAggregationState();
            boolean isGranted = aggregationState.getMainContent().isGranted(Permission.SET);
            if (isGranted) {

                // check if links have already been added.
                if (this.getRequest().getAttribute(Sources.REQUEST_LINKS_DRAWN) == null) {
                    this.drawHtmlLinks(out);
                    this.getRequest().setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);
                }

                int top = this.getTop();
                int left = this.getLeft();

                if (!aggregationState.isPreviewMode()) {
                    // is edit mode
                    this.setSmall(false);
                    if (this.getOverlay()) {
                        println(out, "<div class=\"mgnlMainbar\" style=\"top:" //$NON-NLS-1$
                            + top
                            + "px;left:" //$NON-NLS-1$
                            + left
                            + "px;width:" //$NON-NLS-1$
                            + this.getWidth()
                            + ";\">"); //$NON-NLS-1$
                    }
                    println(out, this.getHtml());
                    if (this.getOverlay()) {
                        println(out, "</div>"); //$NON-NLS-1$
                    }
                }
                else {
                    // is in preview mode
                    top += 4;
                    left += 4;
                    println(out, "<div class=\"mgnlMainbarPreview\" style=\"top:" + top + "px;left:" + left + "px;\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    println(out, this.getButtonEditView().getHtml());
                    println(out, "</div>"); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * @deprecated use drawHtmlLinks(Writer out) instead.
     */
    public void drawHtmlLinks(JspWriter out) throws IOException {
        drawHtmlLinks((Writer) out);
    }

    /**
     * Draws the magnolia specific js and css links).
     */
    public void drawHtmlLinks(Writer out) throws IOException {
        println(out, new Sources(this.getRequest().getContextPath()).getHtmlCss());
        println(out, new Sources(this.getRequest().getContextPath()).getHtmlJs());
    }

    public boolean isAdminButtonVisible() {
        return this.adminButtonVisible;
    }

    public void setAdminButtonVisible(boolean adminButtonVisible) {
        this.adminButtonVisible = adminButtonVisible;
    }
}
