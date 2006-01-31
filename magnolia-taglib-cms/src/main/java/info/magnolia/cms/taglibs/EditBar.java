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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.inline.BarEdit;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;


/**
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
     * set working contentNode
     * @param name , comtainer name which will be used to access/write content
     */
    public void setContentNodeName(String name) {
        this.nodeName = name;
    }

    /**
     * set working contentNode
     * @param name , comtainer name which will be used to access/write content
     */
    public void setContentNodeCollectionName(String name) {
        this.nodeCollectionName = name;
    }

    /**
     * set current content type, could be any developer defined name
     * @param type , content type
     */
    public void setParagraph(String type) {
        this.paragraph = type;
    }

    /**
     * set the edit label (else "Edit")
     * @param label , under which content must be stored
     */
    public void setEditLabel(String label) {
        this.editLabel = label;
    }

    /**
     * set the delete label (else "Delete")
     * @param label , under which content must be stored
     */
    public void setDeleteLabel(String label) {
        this.deleteLabel = label;
    }

    public void setMoveLabel(String label) {
        this.moveLabel = label;
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
        /*
         * if (!ServerInfo.isAdmin() || Resource.showPreview(this.request)) return EVAL_PAGE; if
         * (!Resource.getActivePage(this.request).isGranted(Permission.WRITE_PROPERTY)) return EVAL_PAGE;
         */
        try {
            this.display();
        }
        catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
        return EVAL_PAGE;
    }

    /**
     * Displays edit bar.
     * @throws IOException
     */
    private void display() throws IOException {
        BarEdit bar = new BarEdit((HttpServletRequest) this.pageContext.getRequest());

        try {
            bar.setPath(Resource.getCurrentActivePage((HttpServletRequest) this.pageContext.getRequest()).getHandle());
        }
        catch (Exception re) {
            bar.setPath(StringUtils.EMPTY);
        }

        if (this.paragraph == null) {
            Content contentParagraph = Resource.getLocalContentNode((HttpServletRequest) this.pageContext.getRequest());
            if (contentParagraph != null) {
                bar.setParagraph(contentParagraph.getMetaData().getTemplate());
            }
        }
        else {
            bar.setParagraph(this.paragraph);
        }

        if (this.nodeCollectionName == null) {
            bar.setNodeCollectionName(StringUtils.defaultString(Resource
                .getLocalContentNodeCollectionName((HttpServletRequest) this.pageContext.getRequest())));
        }
        else {
            bar.setNodeCollectionName(this.nodeCollectionName);
        }

        if (this.nodeName == null) {
            Content localContentNode = Resource.getLocalContentNode((HttpServletRequest) this.pageContext.getRequest());
            if (localContentNode != null) {
                bar.setNodeName(localContentNode.getName());
            }
        }
        else {
            bar.setNodeName(this.nodeName);
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

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.nodeName = null;
        this.nodeCollectionName = null;
        this.paragraph = null;
        this.editLabel = null;
        this.deleteLabel = null;
        this.moveLabel = null;
        super.release();
    }
}
