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


import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.editor.client.dom.CMSComment;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import static info.magnolia.templating.editor.client.jsni.LegacyJavascript.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.dom.client.Element;


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

    public AreaBarWidget(MgnlElement mgnlElement, final PageEditor pageEditor) {
        super(mgnlElement);

        String content = mgnlElement.getComment().getAttribute("content");
        if (content != null) {
            int i = content.indexOf(':');

            this.workspace = content.substring(0, i);
            this.path = content.substring(i + 1);
           }

        setVisible(false);

        this.name = mgnlElement.getComment().getAttribute("name");
        this.type = mgnlElement.getComment().getAttribute("type");

        GWT.log("Area ["+this.name+"] is of type " + this.type);

        if(AreaDefinition.TYPE_NO_COMPONENT.equals(this.type)) {
            this.availableComponents = "";
        } else {
            this.availableComponents = mgnlElement.getComment().getAttribute("availableComponents");
        }

        this.dialog = mgnlElement.getComment().getAttribute("dialog");
        if (mgnlElement.getComment().hasAttribute("showAddButton")) {
            this.showAddButton = Boolean.parseBoolean(mgnlElement.getComment().getAttribute("showAddButton"));
        }
        if (mgnlElement.getComment().hasAttribute("optional")) {
            this.optional = Boolean.parseBoolean(mgnlElement.getComment().getAttribute("optional"));
            this.created = Boolean.parseBoolean(mgnlElement.getComment().getAttribute("created"));
        }


        createButtons(pageEditor, mgnlElement.getComment());
        this.addStyleName("area");

    }

    public void attach(MgnlElement mgnlElement) {
        Element element = mgnlElement.getFirstElement();
        if (element != null) {
            element.insertFirst(getElement());
        }
        onAttach();
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
                Button createButton = new Button(getI18nMessage("buttons.create.js"));
                createButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        pageEditor.createComponent(workspace, path, "mgnl:area");
                    }
                });
                addButton(createButton, Float.RIGHT);

            } else {
                createEditAndAddComponentButtons(pageEditor, comment);

                Button removeButton = new Button(getI18nMessage("buttons.remove.js"));
                removeButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        pageEditor.deleteComponent(path);
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
            Button editButton = new Button(getI18nMessage("buttons.edit.js"));
            editButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    pageEditor.openDialog(dialog, workspace, path, null, null);
                }
            });
            addButton(editButton, Float.RIGHT);
        }

        if (this.showAddButton) {
            Button addButton = new Button(getI18nMessage("buttons.add.js"));
            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!AreaDefinition.TYPE_NO_COMPONENT.equals(type)) {
                        pageEditor.addComponent(workspace, path, null, availableComponents);
                    }
                }
            });
            addButton(addButton, Float.RIGHT);
        }
    }


}
