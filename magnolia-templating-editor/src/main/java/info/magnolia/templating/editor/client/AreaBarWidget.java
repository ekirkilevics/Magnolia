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
package info.magnolia.templating.editor.client;

import static info.magnolia.templating.editor.client.PageEditor.getDictionary;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.editor.client.dom.CMSBoundary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;


/**
 * Area bar.
 */
public class AreaBarWidget extends AbstractBarWidget {

    private String workspace;
    private String path;
    private String name;
    private String availableComponents;
    private String type;
    private String dialog;
    private boolean showAddButton = true;
    private boolean optional = false;
    private boolean created = true;

    public AreaBarWidget(final AbstractBarWidget parentBar, CMSBoundary boundary, final PageEditor pageEditor, final Element element) {
        super(parentBar, boundary, element.getAttribute("label"));

        String content = element.getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);

        this.name = element.getAttribute("name");
        this.type = element.getAttribute("type");

        GWT.log("Area ["+this.name+"] is of type " + this.type);

        if(AreaDefinition.TYPE_NO_COMPONENT.equals(this.type)) {
            this.availableComponents = "";
        } else {
            this.availableComponents = element.getAttribute("availableComponents");
        }

        this.dialog = element.getAttribute("dialog");
        if (element.hasAttribute("showAddButton")) {
            this.showAddButton = Boolean.parseBoolean(element.getAttribute("showAddButton"));
        }
        if (element.hasAttribute("optional")) {
            this.optional = Boolean.parseBoolean(element.getAttribute("optional"));
            this.created = Boolean.parseBoolean(element.getAttribute("created"));
        }


        createButtons(pageEditor, element);
        if (hasControls) {
            setClassName("mgnlAreaEditBar");
        }
        else {
            setClassName("mgnlAreaBar");
        }
    }
    @Override
    protected void select() {

        if (getBoundary() != null) {

            for (CMSBoundary boundary : getBoundary().getDescendants()) {
                for (AbstractBarWidget widget : boundary.getAreaWidgets()) {
                    widget.addStyleName("selected");
                }
                for (AbstractBarWidget widget : boundary.getComponentWidgets()) {
                    widget.addStyleName("selected");
                }
            }
        }
       super.select();

    }

    @Override
    protected void deSelect() {
        if (this.getBoundary() != null) {

            for (CMSBoundary boundary : getBoundary().getDescendants()) {
                for (AbstractBarWidget widget : boundary.getAreaWidgets()) {
                    widget.removeStyleName("selected");
                }
                for (AbstractBarWidget widget : boundary.getComponentWidgets()) {
                    widget.removeStyleName("selected");
                }
            }
        }
        super.deSelect();
    }
    public String getAvailableComponents() {
        return availableComponents;
    }

    public String getType() {
        return type;
    }

    private void createButtons(final PageEditor pageEditor, final Element element) {
        if(this.optional) {
            if(!this.created) {
                Button createButton = new Button(getDictionary().get("buttons.createarea.js"));
                createButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        pageEditor.createComponent(workspace, path, name, "mgnl:area");
                    }
                });
                addButton(createButton, Float.RIGHT);

            } else {
                createEditAndAddComponentButtons(pageEditor, element);

                Button removeButton = new Button(getDictionary().get("buttons.removearea.js"));
                removeButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        pageEditor.deleteComponent(path + "/" + name);
                    }
                });
                removeButton.addStyleName("mgnlRemoveButton");
                addButton(removeButton, Float.RIGHT);
            }
        } else {
            createEditAndAddComponentButtons(pageEditor, element);
        }
    }

    private void createEditAndAddComponentButtons(final PageEditor pageEditor, final Element element) {
        if (!AreaDefinition.TYPE_NO_COMPONENT.equals(type) && element.hasAttribute("dialog")) {
            Button editButton = new Button(getDictionary().get("buttons.editarea.js"));
            editButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    pageEditor.openDialog(dialog, workspace, path, null, name);
                }
            });
            addButton(editButton, Float.RIGHT);
        }

        if (this.showAddButton) {
            Button addButton = new Button(getDictionary().get("buttons.new.js"));
            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!AreaDefinition.TYPE_NO_COMPONENT.equals(type)) {
                        pageEditor.addComponent(workspace, path, name, null, availableComponents);
                    }
                }
            });
            addButton(addButton, Float.RIGHT);
        }
    }
}
