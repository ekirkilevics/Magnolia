/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.templating.editor.client.widget.controlbar;


import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.jsni.JavascriptUtils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Float;


import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for horizontal bars with buttons.
 */
public abstract class AbstractBar extends FlowPanel {

    private String label = "";
    private FlowPanel buttonWrapper;
    private MgnlElement mgnlElement;

    private FlowPanel primaryButtons;
    private FlowPanel secondaryButtons;

    public AbstractBar(MgnlElement mgnlElement) {

        this.setMgnlElement(mgnlElement);

        buttonWrapper = new FlowPanel();
        buttonWrapper.setStylePrimaryName("mgnlEditorBarButtons");

        add(buttonWrapper);

        if (mgnlElement != null) {
            this.label = mgnlElement.getComment().getAttribute("label");
            if (label != null && !label.isEmpty()) {
                Label areaName = new Label(this.label);
                //tooltip. Nice to have when area label is truncated because too long.
                areaName.setTitle(this.label);
                areaName.setStylePrimaryName("mgnlEditorBarLabel");

                //setStylePrimaryName(..) replaces gwt default css class, in this case gwt-Label
                add(areaName);
            }
        }



        setClassName("mgnlEditorBar");

    }

    protected void setId(String id){
        getElement().setId(id);
    }

    /**
     * Adds this widget to this bar as a button. The default (primary) style applied is <code>mgnlEditorButton</code>. See also <code>editor.css</code>.
     */
    protected void addButton(final Widget button, final Float cssFloat) {
        button.setStylePrimaryName("mgnlEditorButton");
        button.getElement().getStyle().setFloat(cssFloat);

        buttonWrapper.add(button);
    }

    /**
     * Adds this widget to this bar as a button. The default (primary) style applied is <code>mgnlEditorButton</code>. See also <code>editor.css</code>.
     */
    protected void addSecondaryButton(final Widget button, final Float cssFloat) {
        if (secondaryButtons == null) {
            secondaryButtons = new FlowPanel();
            secondaryButtons.setStylePrimaryName("mgnlEditorBarSecondaryButtons");
            buttonWrapper.add(secondaryButtons);
        }
        button.setStylePrimaryName("mgnlEditorButton");
        button.getElement().getStyle().setFloat(cssFloat);

        secondaryButtons.add(button);
    }

    /**
     * Adds this widget to this bar as a button. The default (primary) style applied is <code>mgnlEditorButton</code>. See also <code>editor.css</code>.
     */
    protected void addPrimaryButton(final Widget button, final Float cssFloat) {
        if (primaryButtons == null) {
            primaryButtons = new FlowPanel();
            primaryButtons.setStylePrimaryName("mgnlEditorBarPrimaryButtons");
            buttonWrapper.add(primaryButtons);
        }
        button.setStylePrimaryName("mgnlEditorButton");
        button.getElement().getStyle().setFloat(cssFloat);

        primaryButtons.add(button);
    }

    /**
     * Adds this widget to this bar as a button. It allows overriding the default (primary) style applied <code>mgnlEditorButton</code>. See also <code>editor.css</code>.
     */
    protected void addButton(final Widget button, final Float cssFloat, final String primaryStyleName) {
        if(JavascriptUtils.isEmpty(primaryStyleName)) {
             addButton(button, cssFloat);
             return;
        }
        button.setStylePrimaryName(primaryStyleName);
        button.getElement().getStyle().setFloat(cssFloat);

        buttonWrapper.add(button);
    }

    protected void setClassName(String className) {
        getElement().setClassName(className);
    }

    /**
     * @return the element's underlying {@link Style}. You can use this object to manipulate the css style attribute of this bar widget.
     */
    protected Style getStyle() {
        return getElement().getStyle();
    }

    /**
     *  TODO: we should not have to call onAttach ourself?
     */

    public void attach() {
        if (getMgnlElement().getEditElement() != null) {
            Element parent = getMgnlElement().getEditElement();
            parent.insertFirst(getElement());
        }
        else if (getMgnlElement().getFirstElement() != null && getMgnlElement().getFirstElement() == getMgnlElement().getLastElement()) {
            attach(getMgnlElement());
        }
        else {
            attach(getMgnlElement().getComment().getElement());
        }
    }

    public void attach(MgnlElement mgnlElement) {
        Element element = mgnlElement.getFirstElement();
        if (element != null) {
            element.insertFirst(getElement());
        }
        onAttach();
    }

    public void attach(Element element) {
        final Node parentNode = element.getParentNode();
        parentNode.insertAfter(getElement(), element);
        onAttach();
    }

    public void toggleVisible() {
        setVisible(!isVisible());
    }

    @Override
    protected void onAttach() {
        PageEditor.model.addElements(this.getMgnlElement(), getElement());
        super.onAttach();
    }

    public void setMgnlElement(MgnlElement mgnlElement) {
        this.mgnlElement = mgnlElement;
    }

    public MgnlElement getMgnlElement() {
        return mgnlElement;
    }

}
