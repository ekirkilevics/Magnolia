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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.gui.inline.BarEdit;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @author Marcel Salathe
 * @version $Revision: $ ($Author: $)
 */
public class EditBar extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(EditBar.class);

    // private static final String EDIT_DIALOG_HANDLE = "/.CMSadmin/dialogs/standard.html";
    // private static final String DEFAULT_EDIT_LABEL = "Edit";
    // private static final String DEFAULT_MOVE_LABEL = "Move";
    // private static final String DEFAULT_DELETE_LABEL = "Delete";
    private String nodeName;

    private String nodeCollectionName;

    private String paragraph;

    private String editLabel;

    private String deleteLabel;

    private String moveLabel;

    private String displayHandler;

    private HttpServletRequest request;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        this.displayHandler = "";
        this.request = (HttpServletRequest) pageContext.getRequest();
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        /*
         * if (!ServerInfo.isAdmin() || Resource.showPreview(this.request)) return EVAL_PAGE; if
         * (!Resource.getActivePage(this.request).isGranted(Permission.WRITE_PROPERTY)) return EVAL_PAGE;
         */
        try {
            this.display();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return EVAL_PAGE;
    }

    /**
     * <p>
     * set working contentNode
     * </p>
     * @param name , comtainer name which will be used to access/write content
     */
    public void setContentNodeName(String name) {
        this.nodeName = name;
    }

    /**
     *
     */
    private String getNodeName() {
        if (this.nodeName == null) {
            return Resource.getLocalContentNode(this.request).getName();
        }
        return this.nodeName;
    }

    /**
     * <p>
     * set working contentNode
     * </p>
     * @param name , comtainer name which will be used to access/write content
     */
    public void setContentNodeCollectionName(String name) {
        this.nodeCollectionName = name;
    }

    /**
     * @return content node collection name
     */
    private String getNodeCollectionName() {
        if (this.nodeCollectionName == null) {
            return StringUtils.defaultString(Resource.getLocalContentNodeCollectionName(this.request));
        }
        return this.nodeCollectionName;
    }

    /**
     * @deprecated
     * <p>
     * set current content type, could be any developer defined name
     * </p>
     * @param type , content type
     */
    public void setParFile(String type) {
        this.setParagraph(type);
    }

    /**
     * <p>
     * set current content type, could be any developer defined name
     * </p>
     * @param type , content type
     */
    public void setParagraph(String type) {
        this.paragraph = type;
    }

    /**
     * @return String paragraph (type of par)
     */
    private String getParagraph() {
        if (this.paragraph == null) {

            return Resource.getLocalContentNode(this.request).getNodeData("paragraph").getString();

        }
        return this.paragraph;
    }

    /**
     * <p>
     * set display handler (JSP / Servlet), needs to know the relative path from WEB-INF
     * </p>
     * @param path , relative to WEB-INF
     */
    public void setTemplate(String path) {
        this.displayHandler = path;
    }

    /**
     * <p>
     * get the content path (Page or Node)
     * </p>
     * @return String path
     */
    private String getPath() {
        try {
            return Resource.getCurrentActivePage(this.request).getHandle();
        }
        catch (Exception re) {
            return "";
        }
    }

    /**
     * <p>
     * set the edit label (else "Edit")
     * </p>
     * @param label , under which content must be stored
     */
    public void setEditLabel(String label) {
        this.editLabel = label;
    }

    /**
     * @return String , label for the edit bar
     */
    private String getEditLabel() {
        return this.editLabel;
    }

    /**
     * <p>
     * set the delete label (else "Delete")
     * </p>
     * @param label , under which content must be stored
     */
    public void setDeleteLabel(String label) {
        this.deleteLabel = label;
    }

    /**
     * @return String , label for the edit bar
     */
    private String getDeleteLabel() {
        return this.deleteLabel;
    }

    public void setMoveLabel(String label) {
        this.moveLabel = label;
    }

    /**
     * @return String , label for the edit bar
     */
    private String getMoveLabel() {
        return this.moveLabel;
    }

    /**
     * <p>
     * displays edit bar
     * </p>
     * @throws IOException
     */
    private void display() throws IOException {
        BarEdit bar = new BarEdit(this.request);
        bar.setPath(this.getPath());
        bar.setParagraph(this.getParagraph());
        bar.setNodeCollectionName(this.getNodeCollectionName());
        bar.setNodeName(this.getNodeName());
        bar.setDefaultButtons();

        if (this.getEditLabel() != null) {
            if (StringUtils.isEmpty(this.getEditLabel())) {
                bar.setButtonEdit(null);
            }
            else {
                bar.getButtonEdit().setLabel(this.getEditLabel());
            }
        }

        if (this.getMoveLabel() != null) {
            if (StringUtils.isEmpty(this.getMoveLabel())) {
                bar.setButtonMove(null);
            }
            else {
                bar.getButtonMove().setLabel(this.getMoveLabel());
            }
        }

        if (this.getDeleteLabel() != null) {
            if (StringUtils.isEmpty(this.getDeleteLabel())) {
                bar.setButtonDelete(null);
            }
            else {
                bar.getButtonDelete().setLabel(this.getDeleteLabel());
            }
        }
        bar.placeDefaultButtons();
        bar.drawHtml(pageContext.getOut());
    }
}
