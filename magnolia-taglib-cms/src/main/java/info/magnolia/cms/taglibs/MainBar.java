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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.inline.BarMain;
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
 * @author Marcel Salathe
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class MainBar extends TagSupport implements BarTag {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MainBar.class);

    private String paragraph;

    private boolean adminButtonVisible = true;

    /**
     * Label for the properties button.
     */
    private String label;

    /**
     * Addition buttons (left).
     */
    private List buttonLeft;

    /**
     * Addition buttons (right).
     */
    private List buttonRight;

    /**
     * Setter for <code>label</code>.
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Set current content type, could be any developer defined name.
     * @deprecated
     * @param type paragraph type
     */
    public void setParFile(String type) {
        this.setParagraph(type);
    }

    /**
     * Set paragarph type.
     * @param s paragraph type
     */
    public void setParagraph(String s) {
        this.paragraph = s;
    }

    public void setAdminButtonVisible(boolean adminButtonVisible) {
        this.adminButtonVisible = adminButtonVisible;
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
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {

        if (Server.isAdmin()) {
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {

        HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
        Content activePage = Resource.getActivePage();
        if (Server.isAdmin() && activePage != null && activePage.isGranted(Permission.SET)) {
            try {
                BarMain bar = new BarMain(request);
                bar.setPath(this.getPath());
                bar.setParagraph(this.paragraph);
                bar.setAdminButtonVisible(this.adminButtonVisible);
                bar.setDefaultButtons();

                if (label != null) {
                    bar.getButtonProperties().setLabel(label);
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
                log.warn("Exception caught during display: " + e.getMessage(), e); //$NON-NLS-1$
            }
        }

        this.buttonLeft = null;
        this.buttonRight = null;

        return EVAL_PAGE;
    }

    /**
     * Get the content path (Page or Node)
     * @return String path
     */
    private String getPath() {
        try {
            return Resource.getCurrentActivePage().getHandle();
        }
        catch (Exception re) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        this.paragraph = null;
        this.adminButtonVisible = true;
        this.label = null;
    }

}
