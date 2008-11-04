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
package info.magnolia.cms.gui.control;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Button extends ControlImpl {

    /**
     * html before.
     */
    private static final String HTML_PRE_DIVIDED = "<table cellpadding=0 cellspacing=0 border=0><tr><td>"; //$NON-NLS-1$

    private String label;

    private String iconSrc;

    private String onclick;

    private int state = BUTTONSTATE_NORMAL;

    private int buttonType = BUTTONTYPE_PUSHBUTTON;

    private String pushButtonTag = "span"; //$NON-NLS-1$

    private boolean small;

    public Button() {
    }

    public Button(String name, String value) {
        super(name, value);
    }

    public void setLabel(String s) {
        this.label = s;
    }

    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }

        return this.getValue();
    }

    /**
     * Sets the source path for the image. URI must contain the context path.
     * @param s image source, with context path
     */
    public void setIconSrc(String s) {
        this.iconSrc = s;
    }

    public String getIconSrc() {
        if (iconSrc == null) {
            return StringUtils.EMPTY;
        }

        // iconSrc already has context path
        return "<img src=\"" + this.iconSrc + "\" />"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void setOnclick(String s) {
        this.onclick = s;
    }

    public String getOnclick() {
        return this.onclick;
    }

    public void setHtmlPre() {
        if (super.getHtmlPre(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                this.setHtmlPre(StringUtils.EMPTY);
            }
            else {
                this.setHtmlPre(HTML_PRE_DIVIDED);
            }
        }
    }

    public void setHtmlInter() {
        if (super.getHtmlInter(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                this.setHtmlInter(StringUtils.EMPTY);
            }
            else {
                this.setHtmlInter("</td><td>"); //$NON-NLS-1$
            }
        }
    }

    public void setHtmlPost() {
        if (super.getHtmlPost(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                this.setHtmlPost(StringUtils.EMPTY);
            }
            else {
                this.setHtmlPost("</td></tr></table>"); //$NON-NLS-1$
            }
        }
    }

    public void setSmall(boolean b) {
        this.small = b;
    }

    public boolean getSmall() {
        return this.small;
    }

    public void setPushButtonTag(String s) {
        this.pushButtonTag = s;
    }

    public String getPushButtonTag() {
        return this.pushButtonTag;
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        this.setHtmlPre();
        this.setHtmlInter();
        this.setHtmlPost();
        html.append(this.getHtmlPre());
        if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
            html.append(this.getHtmlPushbutton());
        }
        else {
            html.append(this.getHtmlDividedbutton());
        }
        html.append(this.getHtmlPost());
        return html.toString();
    }

    public String getHtmlDividedbutton() {
        StringBuffer html = new StringBuffer();
        String buttonType;
        if (this.getButtonType() == BUTTONTYPE_RADIO) {
            buttonType = "radio"; //$NON-NLS-1$
        }
        else {
            buttonType = "checkbox"; //$NON-NLS-1$
        }
        html.append("<input type=\"" + buttonType + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" name=\"" + this.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" value=\"" + ControlImpl.escapeHTML(this.getValue()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        html.append(" id=\"" + this.getId() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        if (StringUtils.isNotEmpty(this.getOnclick())) {
            html.append(" onclick=\"" + this.getOnclick() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (this.getState() == BUTTONSTATE_PUSHED) {
            html.append(" checked=\"checked\""); //$NON-NLS-1$
        }
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        html.append(" />"); //$NON-NLS-1$
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        html.append(this.getHtmlInter());
        html.append("<a href=\"javascript:mgnlShiftDividedButton('" + this.getId() + "');"); //$NON-NLS-1$ //$NON-NLS-2$
        if (StringUtils.isNotEmpty(this.getOnclick())) {
            html.append(this.getOnclick());
        }
        html.append("\" " + this.getHtmlCssClass() + ">"); //$NON-NLS-1$ //$NON-NLS-2$

        html.append(this.getLabel());
        html.append(this.getIconSrc());

        html.append("</a>"); //$NON-NLS-1$
        return html.toString();
    }

    public String getHtmlPushbutton() {
        StringBuffer html = new StringBuffer();
        html.append("<" + this.getPushButtonTag()); //$NON-NLS-1$
        if (StringUtils.isEmpty(this.getCssClass())) {
            if (this.getSmall()) {
                this.setCssClass(CSSCLASS_CONTROLBUTTONSMALL);
            }
            else {
                this.setCssClass(CSSCLASS_CONTROLBUTTON);
            }
        }
        if (this.getState() == BUTTONSTATE_PUSHED) {
            this.setCssClass(this.getCssClass() + "_PUSHED"); //$NON-NLS-1$
        }

        this.setEvent("onclick", "mgnlShiftPushButtonClick(this);"); //$NON-NLS-1$ //$NON-NLS-2$
        if (StringUtils.isNotEmpty(this.getOnclick())) {
            this.setEvent("onclick", this.getOnclick()); //$NON-NLS-1$
        }
        this.setEvent("onmousedown", "mgnlShiftPushButtonDown(this);"); //$NON-NLS-1$ //$NON-NLS-2$
        this.setEvent("onmouseout", "mgnlShiftPushButtonOut(this);"); //$NON-NLS-1$ //$NON-NLS-2$

        html.append(this.getHtmlEvents());
        html.append(this.getHtmlId());
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        html.append(">"); //$NON-NLS-1$

        html.append(this.getIconSrc());
        html.append(this.getLabel());
        html.append("</" + this.getPushButtonTag() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
        return html.toString();
    }

    public void setState(int i) {
        this.state = i;
    }

    public int getState() {
        return this.state;
    }

    public void setButtonType(int i) {
        this.buttonType = i;
    }

    public int getButtonType() {
        return this.buttonType;
    }

    /**
     * @deprecated do nothing
     * @param i ignored
     */
    public void setLabelNbspPadding(int i) {
        // do nothing
    }
}
