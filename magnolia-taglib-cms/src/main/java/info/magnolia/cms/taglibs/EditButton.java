/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import java.io.Writer;

import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.inline.ButtonEdit;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Displays an edit button.
 *
 * @jsp.tag name="editButton" body-content="empty"
 *
 * @author Marcel Salathe
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public class EditButton extends TagSupport {
    private static final Logger log = LoggerFactory.getLogger(EditButton.class);

    private String nodeName;

    private String nodeCollectionName;

    private String dialog;

    private String label;

    private String displayHandler;

    private boolean small = true;

    private boolean adminOnly = true;

    public int doStartTag() {
        this.displayHandler = StringUtils.EMPTY;
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() {
        if ((!adminOnly || ServerConfiguration.getInstance().isAdmin()) && MgnlContext.getAggregationState().getMainContent().isGranted(Permission.SET)) {

            try {
                if (this.getNodeCollectionName() != null && this.getNodeName() == null) {
                    // cannot draw edit button with nodeCllection and without node
                    return EVAL_PAGE;
                }
                JspWriter out = pageContext.getOut();
                ButtonEdit button = new ButtonEdit();
                button.setPath(this.getPath());
                button.setDialog(this.getDialog());
                button.setNodeCollectionName(this.getNodeCollectionName());
                button.setNodeName(this.getNodeName());
                button.setDefaultOnclick();
                if (this.getLabel() != null) {
                    button.setLabel(this.getLabel());
                }
                if (this.small) {
                    button.setSmall(true);
                }
                button.drawHtml((Writer) out);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return EVAL_PAGE;
    }

    /**
     * If attribute is not used, a content node with the name of the page will be added. If contentNodeName attribute is set but with no value
     * between the quotes, the properties are stored directly under the Web page. If a contentNodeName is set then a content node with the
     * according name will be added.
     *
     * @param name Container name which will be used to access/write content.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNodeName(String name) {
        this.nodeName = name;
    }

    private String getNodeName() {
        if (this.nodeName == null) {
            Content current = MgnlContext.getWebContext().getAggregationState().getCurrentContent();
            if (current == null) {
                return null;
            }
            return current.getName();
        }
        return this.nodeName;
    }

    /**
     * @param name , container name which will be used to access/write content.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContentNodeCollectionName(String name) {
        this.nodeCollectionName = name;
    }

    /**
     * @return content node collection name
     */
    private String getNodeCollectionName() {
        if (this.nodeCollectionName == null) {
            return Resource.getLocalContentNodeCollectionName();
        }
        return this.nodeCollectionName;
    }

    /**
    *  The paragraph type.
    * @deprecated since 4.1, use the setDialog() instead.
    * @param type , content type
    * @jsp.attribute required="false" rtexprvalue="true"
    */
   public void setParagraph(String type) {
       setDialog(type);
   }

   /**
    * @return String paragraph (type of par)
    * @deprecated since 4.1, use the getDialog() instead.
    */
   private String getParagraph() {
       return getDialog();
   }

   /**
    * The dialog type.
    * @param type , content type
    * @jsp.attribute required="false" rtexprvalue="true"
    */
   public void setDialog (String dialog) {
       this.dialog = dialog;
   }

   public String getDialog () {
       return this.dialog;
   }

   /**
     * Set display handler (JSP / Servlet), needs to know the relative path from WEB-INF.
     * @TODO: deprecate this ???
     * @param path relative to WEB-INF.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setTemplate(String path) {
        this.displayHandler = path;
    }

    /**
     * Show only in admin instance, defaults to true.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    /**
     * @return template path
     */
    public String getTemplate() {
        if (this.displayHandler == null) {
            Content localContainer = MgnlContext.getWebContext().getAggregationState().getCurrentContent();
            String templateName = localContainer.getNodeData("dialog").getString(); //$NON-NLS-1$
            return ParagraphManager.getInstance().getParagraphDefinition(templateName).getTemplatePath();
        }
        return this.displayHandler;
    }

    /**
     * get the content path (Page or Node)
     * @return String path
     */
    private String getPath() {
        try {
            return MgnlContext.getWebContext().getAggregationState().getCurrentContent().getHandle();
        }
        catch (Exception re) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * @deprecated use the label attribute instead.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setEditLabel(String label) {
        this.setLabel(label);
    }

    /**
     * The button's label. (defaults to "Edit")
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return String , label for the edit bar
     */
    private String getLabel() {
        return this.label;
    }

    /**
     * Sets the size of the button, true for small button, false for a large one (defaults to true).
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setSmall(boolean small) {
        this.small = small;
    }

    public void release() {
        this.nodeName = null;
        this.nodeCollectionName = null;
        this.dialog = null;
        this.label = null;
        this.displayHandler = null;
        this.small = true;
        this.adminOnly=true;
        super.release();
    }
}
