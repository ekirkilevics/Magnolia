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

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.inline.BarNew;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class NewBar extends TagSupport implements BarTag {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_NEW_LABEL = "buttons.new"; //$NON-NLS-1$

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(NewBar.class);

    private String contentNodeCollectionName;

    private String paragraph;

    private String newLabel;

    /**
     * Show only in admin instance.
     */
    private boolean adminOnly;

    /**
     * Addition buttons (left).
     */
    private List buttonLeft;

    /**
     * Addition buttons (right).
     */
    private List buttonRight;

    /**
     * Set the new label.
     * @param label
     */
    public void setNewLabel(String label) {
        this.newLabel = label;
    }

    /**
     * Setter for <code>adminOnly</code>.
     * @param adminOnly The adminOnly to set.
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    /**
     * Set working container list.
     * @param name comtainer list name
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * @param list , comma seperated list of all allowed paragraphs
     * @deprecated Set the tapes which defines all paragraps available in this contentNode collection
     */
    public void setParFiles(String list) {
        this.paragraph = list;
    }

    /**
     * Comma separeted list of paragraphs.
     * @param list , comma seperated list of all allowed paragraphs
     */
    public void setParagraph(String list) {
        this.paragraph = list;
    }

    /**
     * @see info.magnolia.cms.taglibs.BarTag#addButtonLeft(info.magnolia.cms.gui.control.Button)
     */
    public void addButtonLeft(Button button) {
        if (buttonLeft == null) {
            buttonLeft = new ArrayList();
        }
        buttonLeft.add(button);
    }

    /**
     * @see info.magnolia.cms.taglibs.BarTag#addButtonRight(info.magnolia.cms.gui.control.Button)
     */
    public void addButtonRight(Button button) {
        if (buttonRight == null) {
            buttonRight = new ArrayList();
        }
        buttonRight.add(button);
    }

    /**
     * @return String , label for the new bar
     */
    private String getNewLabel() {
        String defStr = MessagesManager.getMessages().get(DEFAULT_NEW_LABEL);
        return StringUtils.defaultString(this.newLabel, defStr);
    }

    /**
     * Get the content path (Page or Node).
     * @return String path
     */
    private String getPath() {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        try {
            return Resource.getCurrentActivePage(request).getHandle();
        }
        catch (Exception re) {
            return StringUtils.EMPTY;
        }
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

        if (!adminOnly || Server.isAdmin()) {
            HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();

            if (Server.isAdmin() && Resource.getActivePage(request).isGranted(Permission.SET)) {
                try {
                    BarNew bar = new BarNew(request);
                    bar.setPath(this.getPath());
                    bar.setParagraph(this.paragraph);
                    bar.setNodeCollectionName(this.contentNodeCollectionName);
                    bar.setNodeName("mgnlNew"); //$NON-NLS-1$
                    bar.setDefaultButtons();
                    if (this.getNewLabel() != null) {
                        if (StringUtils.isEmpty(this.getNewLabel())) {
                            bar.setButtonNew(null);
                        }
                        else {
                            bar.getButtonNew().setLabel(this.getNewLabel());
                        }
                    }

                    if (buttonRight != null) {
                        bar.getButtonsRight().addAll(buttonRight);
                    }
                    if (buttonLeft != null) {
                        bar.getButtonsLeft().addAll(buttonLeft);
                    }

                    bar.placeDefaultButtons();
                    bar.drawHtml(pageContext.getOut());
                }
                catch (Exception e) {
                    if (log.isDebugEnabled())
                        log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
                }
            }
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        this.contentNodeCollectionName = null;
        this.paragraph = null;
        this.newLabel = null;
        this.buttonLeft = null;
        this.buttonRight = null;
    }
}
