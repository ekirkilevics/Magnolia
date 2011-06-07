/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.gui.control.Bar;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.core.Access;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class BarNew extends Bar {

    private String paragraph;

    private Button buttonNew = new Button();

    private I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();

    /**
     * @deprecated since 4.0 - use the empty constructor.
     */
    public BarNew(HttpServletRequest request) {
    }

    public BarNew() {

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
        final String labelKey = StringUtils.isBlank(paragraph) ? "buttons.noparagraph" : "buttons.new";
        b.setLabel(MessagesManager.getMessages().get(labelKey)); //$NON-NLS-1$
        String repository = MgnlContext.getAggregationState().getRepository();
        // if there are multiple paragraphs show the selectParagraph dialog
        if (StringUtils.contains(paragraph, ',')) {
            b.setOnclick(onClickForNewButton(path, nodeCollectionName, nodeName, paragraph, repository, "selectParagraph"));
        } else if (StringUtils.isNotBlank(paragraph)) {
            // there is only one paragraph
            b.setOnclick(onClickForNewButton(path, nodeCollectionName, nodeName, paragraph, repository, "editParagraph"));
        }
        this.setButtonNew(b);
    }

    protected String onClickForNewButton(String path, String nodeCollectionName, String nodeName, String paragraph, String repository, String dialogName) {
        return "mgnlOpenDialog('"
                + path
                + "','"
                + nodeCollectionName
                + "','"
                + nodeName
                + "','"
                + paragraph
                + "','"
                + repository
                + "','.magnolia/dialogs/" + dialogName + ".html'"
                + ", null" //width
                + ", null" //height
                + (i18nSupport.isEnabled()? ", '" + i18nSupport.getLocale().toString() + "'":"")
                +");";
    }

    /**
     * @deprecated use drawHtml(Writer out) instead.
     */
    public void drawHtml(JspWriter out) throws IOException {
        drawHtml((Writer) out);
    }

    /**
     * Draws the main bar (incl. all magnolia specific js and css sources).
     */
    public void drawHtml(Writer out) throws IOException {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final String permission = Access.convertPermissions(Permission.SET);
        final Node mainContent = aggregationState.getMainContent();
        boolean isGranted;
        try {
            isGranted = Access.isGranted(mainContent.getSession(), mainContent.getPath(), permission);
        } catch (RepositoryException e) {
            // TODO dlipp - apply consistent ExceptionHandling
            throw new RuntimeException(e);
        }
        if (!aggregationState.isPreviewMode() && isGranted) {
            this.setEvent("onmousedown", "mgnlMoveNodeEnd(this,'" + this.getPath() + "');"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.setEvent("onmouseover", "mgnlMoveNodeHigh(this);"); //$NON-NLS-1$ //$NON-NLS-2$
            this.setEvent("onmouseout", "mgnlMoveNodeReset(this);"); //$NON-NLS-1$ //$NON-NLS-2$
            this.setId(this.getNodeCollectionName() + "__" + this.getNodeName()); //$NON-NLS-1$
            println(out, this.getHtml());
        }
    }

    public String getParagraph() {
        return this.paragraph;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}
