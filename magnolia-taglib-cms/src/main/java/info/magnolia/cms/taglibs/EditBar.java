/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.inline.BarEdit;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;


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

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private String nodeName;

    private String nodeCollectionName;

    private String paragraph;

    private String editLabel;

    private String deleteLabel;

    private String moveLabel;

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

        if ((!adminOnly || ServerConfiguration.getInstance().isAdmin()) && Resource.getActivePage().isGranted(Permission.SET)) {
            try {
                BarEdit bar = new BarEdit((HttpServletRequest) this.pageContext.getRequest());

                bar.setShowTitle(isShowParagraphName());

                Content localContentNode = Resource.getLocalContentNode();

                if (this.paragraph == null) {
                    Content contentParagraph = localContentNode;
                    if (contentParagraph != null) {
                        this.paragraph = contentParagraph.getMetaData().getTemplate();
                    }
                }
                bar.setParagraph(this.paragraph);

                if (this.nodeCollectionName == null) {
                    this.nodeCollectionName = StringUtils.defaultString(Resource
                        .getLocalContentNodeCollectionName());
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
                        path = localContentNode.getHandle();
                        if (path.endsWith(this.nodeCollectionName + "/" + this.nodeName)) {
                            path = StringUtils.removeEnd(path, "/" + this.nodeCollectionName + "/" + this.nodeName);
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
                        bar.setButtonDelete(null);
                    }
                    else {
                        bar.getButtonDelete().setLabel(this.deleteLabel);
                    }
                }
                bar.placeDefaultButtons();
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
        this.editLabel = null;
        this.deleteLabel = null;
        this.moveLabel = null;
        this.adminOnly = true;
        this.showParagraphName = false;
    }

    public boolean isShowParagraphName() {
        return showParagraphName;
    }

    public void setShowParagraphName(boolean showParagraphName) {
        this.showParagraphName = showParagraphName;
    }

}
