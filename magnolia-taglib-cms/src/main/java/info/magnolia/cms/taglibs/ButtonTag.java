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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.util.Resource;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class ButtonTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Dialog name.
     */
    private String dialogName = "xxx";

    /**
     * Button label.
     */
    private String label;

    /**
     * position (<code>left|right</code>)
     */
    private String position;

    /**
     * Setter for <code>dialogName</code>.
     * @param dialogName The dialogName to set.
     */
    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    /**
     * Setter for <code>label</code>.
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Setter for <code>position</code>.
     * @param position The position to set.
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {

        BarTag bartag = (BarTag) findAncestorWithClass(this, BarTag.class);
        if (bartag == null) {
            throw new JspException("button tag should be enclosed in a mainbar or newbar tag");
        }

        Button button = new Button();
        button.setLabel(label);
        button.setOnclick("mgnlOpenDialog('"
            + Resource.getActivePage().getHandle()
            + "','','','"
            + dialogName
            + "','"
            + ContentRepository.WEBSITE
            + "')");

        if ("right".equalsIgnoreCase(position)) {
            bartag.addButtonRight(button);
        }
        else {
            bartag.addButtonLeft(button);
        }

        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        this.dialogName = null;
        this.label = null;
        this.position = null;
    }

}
