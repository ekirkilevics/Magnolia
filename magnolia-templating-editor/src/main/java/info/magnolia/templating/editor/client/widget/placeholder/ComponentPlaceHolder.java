/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.editor.client.widget.placeholder;


import java.util.Map;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;

/**
 * A Widget for adding components to area.
 *
 * @version $Id$
 */
public class ComponentPlaceHolder extends AbstractPlaceHolder {

    private boolean optional = false;
    private boolean created = false;
    private boolean showAddButton = false;
    private String availableComponents = "";
    private String type = "";
    private String areaWorkspace = "";
    private String areaPath = "";
    private String name = "";
    private FlowPanel buttonWrapper;

    public ComponentPlaceHolder(MgnlElement mgnlElement) throws IllegalArgumentException {

        super(mgnlElement);

        checkMandatories(mgnlElement.getComment().getAttributes());

        this.addStyleName("component");

        FlowPanel controlBar = new FlowPanel();
        controlBar.setStyleName("mgnlEditorBar");
        controlBar.addStyleName("placeholder");

        buttonWrapper = new FlowPanel();
        buttonWrapper.setStylePrimaryName("mgnlEditorBarButtons");

        controlBar.add(buttonWrapper);

        Label label = new Label("New Component");
        label.setStyleName("mgnlEditorBarLabel");
        controlBar.add(label);

        add(controlBar);

        FlowPanel elementWrapper = new FlowPanel();
        elementWrapper.setStyleName("mgnlEditorPlaceholderElements");

        setVisible(false);


        add(elementWrapper);

        createButtons();
        PageEditor.model.addComponentPlaceHolder(mgnlElement, this);
    }

    @SuppressWarnings("unused")
    private void createMouseEventsHandlers() {

        if (this.optional && !this.created) {
            if(!this.created) {
                addDomHandler(new MouseDownHandler() {

                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        PageEditor.createComponent(areaWorkspace, areaPath, "mgnl:area");

                    }
                }, MouseDownEvent.getType());
            }

        }
        else if (this.showAddButton){
            addDomHandler(new MouseDownHandler() {

                @Override
                public void onMouseDown(MouseDownEvent event) {
                    PageEditor.addComponent(areaWorkspace, areaPath, null, availableComponents);

                }
            }, MouseDownEvent.getType());
        }
    }

    private void createButtons() {

        if (this.showAddButton){
            PushButton button = new PushButton();
            button.setStylePrimaryName("mgnlEditorPushButton");
            button.addStyleName("add");

            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PageEditor.addComponent(areaWorkspace, areaPath, null, availableComponents);
                }
            });
            buttonWrapper.add(button);
        }
    }

    public void attach() {
        Element parent = getMgnlElement().getComponentElement();

        if (parent == null) {
            if (getMgnlElement().getLastElement() != null && getMgnlElement().getFirstElement() == getMgnlElement().getLastElement()) {
                attach(getMgnlElement());
            }
            else {
                attach(getMgnlElement().getEndComment().getElement());
            }
        }
        else {
            parent.insertFirst(getElement());
        }
        onAttach();
    }

    public void attach(MgnlElement mgnlElement) {
        Element element = mgnlElement.getFirstElement();
        if (element != null) {
            element.appendChild(getElement());
        }
    }

    public void attach(Element element) {
        final Node parentNode = element.getParentNode();
        parentNode.insertBefore(getElement(), element);
    }

    private void checkMandatories(Map<String, String> attributes) throws IllegalArgumentException {

        this.showAddButton = Boolean.parseBoolean(attributes.get("showAddButton"));
        this.optional = Boolean.parseBoolean(attributes.get("optional"));
        this.created = Boolean.parseBoolean(attributes.get("created"));
        this.type = attributes.get("type");
        this.name = attributes.get("name");


        String areaContent = attributes.get("content");
        int i = areaContent.indexOf(':');
        this.areaWorkspace = areaContent.substring(0, i);
        this.areaPath = areaContent.substring(i + 1);

        if(AreaDefinition.TYPE_NO_COMPONENT.equals(this.type)) {
            this.availableComponents = "";
        } else {
            this.availableComponents = attributes.get("availableComponents");
        }

        if (availableComponents.equals("")) {
            throw new IllegalArgumentException();
        }

        if (this.type.equals(AreaDefinition.TYPE_SINGLE) && (this.created || !getMgnlElement().getComponents().isEmpty())) {
            throw new IllegalArgumentException();
        }

    }

}
