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

import info.magnolia.cms.core.Content;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class Button extends ControlSuper {

    private String label;

    private String iconSrc;

    private String onclick;

    private final String htmlPreDivided = "<table cellpadding=0 cellspacing=0 border=0><tr><td>"; // html before

    // buttton (radio,
    // checkbox)
    private final String htmlInterDivided = "</td><td>"; // html between button and label

    private final String htmlPostDivided = "</td></tr></table>"; // html after label (radio, checkbox)

    private final String htmlPrePush = ""; // html before buttton (push button)

    private final String htmlInterPush = "";

    private final String htmlPostPush = ""; // html after button

    private int state = BUTTONSTATE_NORMAL;

    private int buttonType = BUTTONTYPE_PUSHBUTTON;

    private int labelNbspPadding = 3;

    private String pushButtonTag = "span";

    private boolean small;

    public Button() {
    }

    public Button(String name, String value) {
        super(name, value);
    }

    public Button(String name, Content websiteNode) {
        super(name, websiteNode);
    }

    // why padding with &nbsp;s ?
    // css padding causes problems in td of width=1 (td width equals to text width, not entire button)
    public void setLabelNbspPadding(int i) {
        this.labelNbspPadding = i;
    }

    public int getLabelNbspPadding() {
        return this.labelNbspPadding;
    }

    public String getHtmlLabelNbspPadding() {
        StringBuffer html = new StringBuffer();
        for (int i = 0; i < this.getLabelNbspPadding(); i++) {
            html.append("&nbsp;");
        }
        return html.toString();
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

    public void setIconSrc(String s) {
        this.iconSrc = s;
    }

    public String getIconSrc() {
        if (iconSrc == null) {
            return "";
        }

        return "<img src=\"" + this.iconSrc + "\">";
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
                this.setHtmlPre(this.htmlPrePush);
            }
            else {
                this.setHtmlPre(this.htmlPreDivided);
            }
        }
    }

    public void setHtmlInter() {
        if (super.getHtmlInter(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                this.setHtmlInter(this.htmlInterPush);
            }
            else {
                this.setHtmlInter(this.htmlInterDivided);
            }
        }
    }

    public void setHtmlPost() {
        if (super.getHtmlPost(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                this.setHtmlPost(this.htmlPostPush);
            }
            else {
                this.setHtmlPost(this.htmlPostDivided);
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
        html.append(">");
        if (this.getSaveInfo()) {
            html.append(this.getHtmlSaveInfo());
        }
        html.append(this.getHtmlInter());
        html.append("<a href=\"javascript:mgnlShiftDividedButton('" + this.getId() + "');");
        if (StringUtils.isNotEmpty(this.getOnclick())) {
            html.append(this.getOnclick());
        }
        html.append("\" " + this.getHtmlCssClass() + ">");

        // html.append("["+this.getLabel()+"]["+this.getValue()+"]");
        html.append(this.getIconSrc());
        html.append(this.getLabel());
        html.append("</a>");
        return html.toString();
    }

    public String getHtmlPushbutton() {
        StringBuffer html = new StringBuffer();
        html.append("<" + this.getPushButtonTag());
        if (this.getCssClass().equals("")) {
            if (this.getSmall()) {
                this.setCssClass(CSSCLASS_CONTROLBUTTONSMALL);
                this.setLabelNbspPadding(1);
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
        html.append("<nobr>");
        html.append(this.getHtmlLabelNbspPadding());
        html.append(this.getIconSrc());
        html.append(this.getLabel());
        html.append(this.getHtmlLabelNbspPadding());
        html.append("</nobr>");
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
}
