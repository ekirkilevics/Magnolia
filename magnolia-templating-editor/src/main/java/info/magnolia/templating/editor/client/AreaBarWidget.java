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
import info.magnolia.templating.editor.client.dom.CMSComment;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.dom.client.Element;
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

    public AreaBarWidget(CMSBoundary boundary, final PageEditor pageEditor) {
        super(boundary);

        CMSBoundary parentArea = boundary.getParentArea();
        String content = boundary.getComment().getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);

        this.name = parentArea.getComment().getAttribute("name");
        this.type = parentArea.getComment().getAttribute("type");

        GWT.log("Area ["+this.name+"] is of type " + this.type);

        if(AreaDefinition.TYPE_NO_COMPONENT.equals(this.type)) {
            this.availableComponents = "";
        } else {
            this.availableComponents = parentArea.getComment().getAttribute("availableComponents");
        }

        this.dialog = boundary.getComment().getAttribute("dialog");
        if (parentArea.getComment().hasAttribute("showAddButton")) {
            this.showAddButton = Boolean.parseBoolean(parentArea.getComment().getAttribute("showAddButton"));
        }
        if (parentArea.getComment().hasAttribute("optional")) {
            this.optional = Boolean.parseBoolean(parentArea.getComment().getAttribute("optional"));
            this.created = Boolean.parseBoolean(parentArea.getComment().getAttribute("created"));
        }


        createButtons(pageEditor, boundary.getComment());
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

            CMSBoundary parentBoundary = getBoundary().getParentBoundary();
            if (parentBoundary != null) {
                for (CMSBoundary boundary : parentBoundary.getDescendants()) {
                    if (boundary.getWidget() != null) {
                        boundary.getWidget().addStyleName("selected");
                    }
                }
            }
            if (getBoundary().getParentArea() != null) {
                Element element = getBoundary().getParentArea().getFirstElement();
                if (element != null) {
                    element.addClassName("mgnlSelected");
                }
            }
        }
       super.select();

    }

    @Override
    protected void deSelect() {
        if (getBoundary() != null) {

            CMSBoundary parentBoundary = getBoundary().getParentBoundary();
            if (parentBoundary != null) {
                for (CMSBoundary boundary : parentBoundary.getDescendants()) {
                    if (boundary.getWidget() != null) {
                        boundary.getWidget().removeStyleName("selected");
                    }
                }
            }
            if (getBoundary().getParentArea() != null) {
                Element element = getBoundary().getParentArea().getFirstElement();
                if (element != null) {
                    element.removeClassName("mgnlSelected");
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

    private void createButtons(final PageEditor pageEditor, final CMSComment comment) {
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
                createEditAndAddComponentButtons(pageEditor, comment);

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
            createEditAndAddComponentButtons(pageEditor, comment);
        }
    }

    private void createEditAndAddComponentButtons(final PageEditor pageEditor, final CMSComment comment) {
        if (comment.hasAttribute("dialog")) {
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
