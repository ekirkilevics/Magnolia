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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.gui.control.Button;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.inline.BarEdit;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.dialogs.ParagraphSelectDialog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Displays Magnolia editBar which allows you to edit a paragraph. This tag is often used within
 * contentNodeIterator, which in turn will set all relevant parameters automatically.
 *
 * @jsp.tag name="editBar" body-content="JSP"
 *
 * @author Sameer Charles
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class EditBar extends TagSupport {

    private String nodeName;

    private String nodeCollectionName;

    private String paragraph;

    private String dialog;

    private String editLabel;

    private String deleteLabel;

    private String moveLabel;
    
    private static final Logger log = LoggerFactory.getLogger(EditBar.class);

    /**
     * Show links only in admin instance.
     */
    private boolean adminOnly = true;

    /**
     * Show paragraph name in the bar.
     */
    private boolean showParagraphName = false;

    /**
     * The contentNode (i.e. paragraph) you wish to edit.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNodeName(String name) {
        this.nodeName = name;
    }

    /**
     * The contentNode collection.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNodeCollectionName(String name) {
        this.nodeCollectionName = name;
    }

    /**
     * Name of paragraph (as defined in config). Does not
     * have to be set inside "contentNodeIterator".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }

    /**
     * Name of the dialog to open. If specified, overrides
     * the paragraph attribute, or the dialog name determined
     * by the current paragraph if any.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    /**
     * The text of the edit button, defaults to "Edit".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setEditLabel(String label) {
        this.editLabel = label;
    }

    /**
     * The text of the delete button, defaults to "Delete". Use "" to get no delete button.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDeleteLabel(String label) {
        this.deleteLabel = label;
    }

    /**
     * The text of the move button, defaults to "Move". Use "" to get no move button.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setMoveLabel(String label) {
        this.moveLabel = label;
    }

    /**
     * Show only in admin instance, default to true.
     * @param adminOnly The adminOnly to set.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public boolean isShowParagraphName() {
        return showParagraphName;
    }

    /**
     * Show the paragraph name, default to false.
     * @param showParagraphName Show the paragraph name.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setShowParagraphName(boolean showParagraphName) {
        this.showParagraphName = showParagraphName;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {

        final AggregationState aggregationState = MgnlContext.getAggregationState();
        if ((!adminOnly || ServerConfiguration.getInstance().isAdmin()) && aggregationState.getMainContent().isGranted(Permission.SET)) {
            try {
                BarEdit bar = new BarEdit();

                Content localContentNode = Resource.getLocalContentNode();

                if(StringUtils.isNotEmpty(this.nodeName)){
                    try {
                        if (localContentNode.hasContent(this.nodeName)) {
                            localContentNode = localContentNode.getContent(this.nodeName);
                        } else {
                            localContentNode = null;
                        }
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }

                final String paragraphToUse;
                if (this.dialog != null) {
                    paragraphToUse = this.dialog;
                } else if (this.paragraph == null && localContentNode != null) {
                    paragraphToUse = localContentNode.getMetaData().getTemplate();
                } else {
                    paragraphToUse = this.paragraph;
                }
                bar.setParagraph(paragraphToUse);

                if (this.nodeCollectionName == null) {
                    this.nodeCollectionName = StringUtils.defaultString(Resource.getLocalContentNodeCollectionName());
                }
                bar.setNodeCollectionName(this.nodeCollectionName);

                if (this.nodeName == null) {
                    if (localContentNode != null) {
                        this.nodeName = localContentNode.getName();
                    }
                }
                bar.setNodeName(this.nodeName);

                try {
                    String path;
                    if (localContentNode != null) {
                        path = localContentNode.getParent().getHandle();
                        if (StringUtils.isNotEmpty(this.nodeCollectionName) && path.endsWith("/" + this.nodeCollectionName)) {
                            path = StringUtils.removeEnd(path, "/" + this.nodeCollectionName);
                        }
                    }
                    else {
                        path = Resource.getCurrentActivePage().getHandle();
                    }
                    bar.setPath(path);
                } 
                catch (Exception re) {
                    bar.setPath(StringUtils.EMPTY);
                }

                bar.setDefaultButtons();
                // TODO - yes this is a bit ugly - 1) we should in fact open the correct dialog immediately instead of faking the paragraph parameter - 2) the gui elements should not have defaults nor know anything about urls and onclick functions
                if (this.dialog == null) {
                    bar.getButtonEdit().setDialogPath(ParagraphSelectDialog.EDITPARAGRAPH_DIALOG_URL);
                    bar.getButtonEdit().setDefaultOnclick(); // re-set the onclick after having set the dialog path.
                }

                if (this.editLabel != null) {
                    if (StringUtils.isEmpty(this.editLabel)) {
                        bar.setButtonEdit(null);
                    }
                    else {
                        bar.getButtonEdit().setLabel(this.editLabel);
                    }
                }

                if (this.moveLabel != null) {
                    if (StringUtils.isEmpty(this.moveLabel)) {
                        bar.setButtonMove(null);
                    }
                    else {
                        bar.getButtonMove().setLabel(this.moveLabel);
                    }
                }

                if (this.deleteLabel != null) {
                    if (StringUtils.isEmpty(this.deleteLabel)) {
                        bar.setButtonDelete((Button)null);
                    }
                    else {
                        bar.getButtonDelete().setLabel(this.deleteLabel);
                    }
                }
                bar.placeDefaultButtons();

                if (isShowParagraphName() && this.dialog == null) {
                    final Paragraph paragraphInfo = ParagraphManager.getInstance().getParagraphDefinition(paragraphToUse);
                    final Messages msgs = MessagesManager.getMessages(paragraphInfo.getI18nBasename());
                    final String label = msgs.getWithDefault(paragraphInfo.getTitle(), paragraphInfo.getTitle());
                    bar.setLabel(label);
                }

                bar.drawHtml(pageContext.getOut());
            }
            catch (IOException e) {
                throw new NestableRuntimeException(e);
            }
        }
        reset();

        return EVAL_PAGE;
    }

    protected void reset() {
        this.nodeName = null;
        this.nodeCollectionName = null;
        this.paragraph = null;
        this.dialog = null;
        this.editLabel = null;
        this.deleteLabel = null;
        this.moveLabel = null;
        this.adminOnly = true;
        this.showParagraphName = false;
    }

}
