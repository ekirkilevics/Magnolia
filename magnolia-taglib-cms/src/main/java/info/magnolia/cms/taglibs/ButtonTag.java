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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.context.MgnlContext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * Adds a button to a parent bar. Must be nested into a mainBar or newBar to work.
 * @jsp.tag name="button" body-content="empty"
 *
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
     * Name of the dialog to open.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    /**
     * Label for this button.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Where to add this button. Can be "left" or "right". Default is "left".
     * @jsp.attribute required="true" rtexprvalue="true"
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
            + MgnlContext.getAggregationState().getMainContent().getHandle()
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
