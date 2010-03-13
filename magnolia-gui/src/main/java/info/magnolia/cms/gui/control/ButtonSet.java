/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * @author Vinzenz Wyser
 * @version 2.0
 */
public class ButtonSet extends ControlImpl {

    // default values for divided button (checkbox, radio)
    private static final String HTML_PRE_DIVIDED = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"; //$NON-NLS-1$

    private static final String HTML_POST_DIVIDED = "</table>"; //$NON-NLS-1$

    private static final String BUTTONHTML_PRE_DIVIDED = "<tr><td>"; //$NON-NLS-1$

    private static final String BUTTONHTML_INTER_DIVIDED = "</td><td>"; //$NON-NLS-1$

    private static final String BUTTONHTML_POST_DIVIDED = "</td></tr>"; //$NON-NLS-1$

    // default values for push button

    private static final String HTML_INTER_PUSH = " "; //$NON-NLS-1$

    private List buttons = new ArrayList();

    private int buttonType = BUTTONTYPE_RADIO;

    private String buttonHtmlPre; // html before each button

    private String buttonHtmlInter; // html between each button and label (not available for push button)

    private String buttonHtmlPost; // html after each label

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
                return StringUtils.EMPTY;
            }
            return BUTTONHTML_PRE_DIVIDED;
        }
        return this.buttonHtmlPre;
    }

    public void setButtonHtmlInter(String s) {
        this.buttonHtmlInter = s;
    }

    public String getButtonHtmlInter() {
        if (this.buttonHtmlInter == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return StringUtils.EMPTY;
            }
            return BUTTONHTML_INTER_DIVIDED;
        }
        return this.buttonHtmlInter;
    }

    public void setButtonHtmlPost(String s) {
        this.buttonHtmlPost = s;
    }

    public String getButtonHtmlPost() {
        if (this.buttonHtmlPost == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return StringUtils.EMPTY;
            }
            return BUTTONHTML_POST_DIVIDED;
        }
        return buttonHtmlPost;

    }

    public String getHtmlPre() {
        if (super.getHtmlPre(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return StringUtils.EMPTY;
            }
            return HTML_PRE_DIVIDED;
        }
        return super.getHtmlPre();
    }

    public String getHtmlInter() {
        if (super.getHtmlInter(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return HTML_INTER_PUSH;
            }
            return StringUtils.EMPTY;
        }
        return super.getHtmlInter();
    }

    public String getHtmlPost() {
        if (super.getHtmlPost(null) == null) {
            if (this.getButtonType() == BUTTONTYPE_PUSHBUTTON) {
                return StringUtils.EMPTY;
            }
            return HTML_POST_DIVIDED;
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
            if (StringUtils.isEmpty(b.getCssClass())) {
                b.setCssClass(this.getCssClass());
            }
            b.setId(this.getName() + "_SETBUTTON_" + i); //$NON-NLS-1$
            if (this.getValueType() == ControlImpl.VALUETYPE_MULTIPLE) {
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
                if (StringUtils.isNotEmpty(this.getValue())) {
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
