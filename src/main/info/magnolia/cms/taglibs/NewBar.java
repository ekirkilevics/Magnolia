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

import info.magnolia.cms.gui.inline.BarNew;
import info.magnolia.cms.util.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;


/**
 * @author Sameer Charles
 * @version $Revision: $ ($Author: $)
 */
public class NewBar extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_NEW_LABEL = "New";

    private String contentNodeCollectionName;

    private String paragraph;

    private String newLabel;

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
        catch (Exception e) {
        }
        return EVAL_PAGE;
    }

    /**
     * <p>
     * set working container list
     * </p>
     * @param name , comtainer list name
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    public String getContentNodeCollectionName() {
        return this.contentNodeCollectionName;
    }

    /**
     * <p>
     * get the content path (Page or Node)
     * </p>
     * @return String path
     */
    private String getPath() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        try {
            return Resource.getCurrentActivePage(request).getHandle();
        }
        catch (Exception re) {
            return "";
        }
    }

    /**
     * @deprecated
     * <p>
     * set the tapes which defines all paragraps available in this contentNode collection
     * </p>
     * @param list , comma seperated list of all allowed paragraphs
     */
    public void setParFiles(String list) {
        this.paragraph = list;
    }

    /**
     * <p>
     * comma separeted list of paragraphs
     * </p>
     * @param list , comma seperated list of all allowed paragraphs
     */
    public void setParagraph(String list) {
        this.paragraph = list;
    }

    /**
     * @return String paragraph (type of par)
     */
    private String getParagraph() {
        return this.paragraph;
    }

    /**
     * <p>
     * set the new label
     * </p>
     * @param label
     */
    public void setNewLabel(String label) {
        this.newLabel = label;
    }

    /**
     * @return String , label for the new bar
     */
    private String getNewLabel() {
        return StringUtils.defaultString(this.newLabel, DEFAULT_NEW_LABEL);
    }

    /**
     * <p>
     * displays new bar
     * </p>
     * @throws java.io.IOException
     */
    private void display() throws IOException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        BarNew bar = new BarNew(request);
        bar.setPath(this.getPath());
        bar.setParagraph(this.getParagraph());
        bar.setNodeCollectionName(this.getContentNodeCollectionName());
        bar.setNodeName("mgnlNew");
        bar.setDefaultButtons();
        if (this.getNewLabel() != null) {
            if (this.getNewLabel().equals("")) {
                bar.setButtonNew(null);
            }
            else {
                bar.getButtonNew().setLabel(this.getNewLabel());
            }
        }
        bar.placeDefaultButtons();
        bar.drawHtml(pageContext.getOut());
    }
}
