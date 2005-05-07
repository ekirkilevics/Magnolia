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
package info.magnolia.cms.gui.control;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Button extends ControlSuper {

    /**
     * html before.
     */
    private static final String HTML_PRE_DIVIDED = "<table cellpadding=0 cellspacing=0 border=0><tr><td>";

    private String label;

    private String iconSrc;

    private String onclick;

    private int state = BUTTONSTATE_NORMAL;

    private int buttonType = BUTTONTYPE_PUSHBUTTON;

    private String pushButtonTag = "span";

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
        return "<img src=\"" + this.iconSrc + "\" />";
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
                this.setHtmlInter("</td><td>");
            }
        }
    }

    public void setHtmlPost() {
        if (super.getHtmlPost(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                this.setHtmlPost(StringUtils.EMPTY);
            }
            else {
                this.setHtmlPost("</td></tr></table>");
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
            buttonType = "radio";
        }
        else {
            buttonType = "checkbox";
        }
        html.append("<input type=\"" + buttonType + "\"");
        html.append(" name=\"" + this.getName() + "\"");
        html.append(" value=\"" + this.getValue() + "\"");
        html.append(" id=\"" + this.getId() + "\"");
        if (StringUtils.isNotEmpty(this.getOnclick())) {
            html.append(" onclick=\"" + this.getOnclick() + "\"");
        }
        if (this.getState() == BUTTONSTATE_PUSHED) {
            html.append(" checked");
        }
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        html.append(" />");
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        html.append(this.getHtmlInter());
        html.append("<a href=\"javascript:mgnlShiftDividedButton('" + this.getId() + "');");
        if (StringUtils.isNotEmpty(this.getOnclick())) {
            html.append(this.getOnclick());
        }
        html.append("\" " + this.getHtmlCssClass() + ">");

        html.append(this.getIconSrc());
        html.append(this.getLabel());
        html.append("</a>");
        return html.toString();
    }

    public String getHtmlPushbutton() {
        StringBuffer html = new StringBuffer();
        html.append("<" + this.getPushButtonTag());
        if (StringUtils.isEmpty(this.getCssClass())) {
            if (this.getSmall()) {
                this.setCssClass(CSSCLASS_CONTROLBUTTONSMALL);
            }
            else {
                this.setCssClass(CSSCLASS_CONTROLBUTTON);
            }
        }
        if (this.getState() == BUTTONSTATE_PUSHED) {
            this.setCssClass(this.getCssClass() + "_PUSHED");
        }
        html.append(" onclick=\"mgnlShiftPushButtonClick(this);");

        if (StringUtils.isNotEmpty(this.getOnclick())) {
            html.append(this.getOnclick());
        }
        html.append("\"");

        html.append(" onmousedown=\"mgnlShiftPushButtonDown(this);\"");
        html.append(" onmouseout=\"mgnlShiftPushButtonOut(this);\"");
        html.append(this.getHtmlId());
        html.append(this.getHtmlCssClass());
        html.append(this.getHtmlCssStyles());
        html.append(">");

        html.append(this.getIconSrc());
        html.append(this.getLabel());
        html.append("</" + this.getPushButtonTag() + ">");
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
