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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class ButtonSet extends ControlSuper {

    private List buttons = new ArrayList();

    private int buttonType = BUTTONTYPE_RADIO;

    private String buttonHtmlPre; // html before each button

    private String buttonHtmlInter; // html between each button and label (not available for push button)

    private String buttonHtmlPost; // html after each label

    // default values for divided button (checkbox, radio)
    private final String htmlPreDivided = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">";

    private final String htmlInterDivided = "";

    private final String htmlPostDivided = "</table>";

    private final String buttonHtmlPreDivided = "<tr><td>";

    private final String buttonHtmlInterDivided = "</td><td>";

    private final String buttonHtmlPostDivided = "</td></tr>";

    // default values for push button
    private final String htmlPrePush = "";

    private final String htmlInterPush = " ";

    private final String htmlPostPush = "";

    private final String buttonHtmlPrePush = "";

    private final String buttonHtmlInterPush = "";

    private final String buttonHtmlPostPush = "";

    public ButtonSet() {
    }

    public ButtonSet(String name, String value) {
        super(name, value);
    }

    public ButtonSet(String name, List values) {
        super(name, values);
    }

    public ButtonSet(String name, Content websiteNode) {
        super(name, websiteNode);
    }

    public void setButtons(List buttons) {
        this.buttons = buttons;
    }

    public void setButtons(Button button) {
        this.getButtons().add(button);
    }

    public List getButtons() {
        return this.buttons;
    }

    public void setButtonHtmlPre(String s) {
        this.buttonHtmlPre = s;
    }

    public String getButtonHtmlPre() {
        if (this.buttonHtmlPre == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return this.buttonHtmlPrePush;
            }
            else {
                return this.buttonHtmlPreDivided;
            }
        }
        else {
            return this.buttonHtmlPre;
        }
    }

    public void setButtonHtmlInter(String s) {
        this.buttonHtmlInter = s;
    }

    public String getButtonHtmlInter() {
        if (this.buttonHtmlInter == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return this.buttonHtmlInterPush;
            }
            else {
                return this.buttonHtmlInterDivided;
            }
        }
        else {
            return this.buttonHtmlInter;
        }
    }

    public void setButtonHtmlPost(String s) {
        this.buttonHtmlPost = s;
    }

    public String getButtonHtmlPost() {
        if (this.buttonHtmlPost == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return this.buttonHtmlPostPush;
            }
            else {
                return this.buttonHtmlPostDivided;
            }
        }
        else {
            return this.buttonHtmlPost;
        }
    }

    public String getHtmlPre() {
        if (super.getHtmlPre(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return this.htmlPrePush;
            }
            else {
                return this.htmlPreDivided;
            }
        }
        else {
            return super.getHtmlPre();
        }
    }

    public String getHtmlInter() {
        if (super.getHtmlInter(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return this.htmlInterPush;
            }
            return this.htmlInterDivided;
        }
        return super.getHtmlInter();
    }

    public String getHtmlPost() {
        if (super.getHtmlPost(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return this.htmlPostPush;
            }
            return this.htmlPostDivided;
        }
        return super.getHtmlPost();

    }

    public void setButtonType(int i) {
        this.buttonType = i;
    }

    public int getButtonType() {
        return this.buttonType;
    }

    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append(this.getHtmlPre());
        Iterator it = this.getButtons().iterator();
        int i = 0;
        while (it.hasNext()) {
            Button b = (Button) it.next();
            if (b.getName() == null) {
                b.setName(this.getName());
            }
            b.setButtonType(this.getButtonType());
            b.setSaveInfo(false);
            if (b.getHtmlPre(null) == null) {
                b.setHtmlPre(this.getButtonHtmlPre());
            }
            if (b.getHtmlInter(null) == null) {
                b.setHtmlInter(this.getButtonHtmlInter());
            }
            if (b.getHtmlPost(null) == null) {
                b.setHtmlPost(this.getButtonHtmlPost());
            }
            if (b.getCssClass().equals("")) {
                b.setCssClass(this.getCssClass());
            }
            b.setId(this.getName() + "_SETBUTTON_" + i);
            if (this.getValueType() == ControlSuper.VALUETYPE_MULTIPLE) {
                if (this.getValues().size() != 0) {
                    if (this.getValues().contains(b.getValue())) {
                        b.setState(BUTTONSTATE_PUSHED);
                    }
                    else {
                        b.setState(BUTTONSTATE_NORMAL);
                    }
                }
            }
            else {
                if (!this.getValue().equals("")) {
                    if (this.getValue().equals(b.getValue())) {
                        b.setState(BUTTONSTATE_PUSHED);
                    }
                    else {
                        b.setState(BUTTONSTATE_NORMAL);
                    }
                }
            }
            html.append(b.getHtml());
            if (it.hasNext()) {
                html.append(this.getHtmlInter());
            }
            i++;
        }
        html.append(this.getHtmlPost());
        html.append(this.getHtmlSaveInfo());
        return html.toString();
    }
}
