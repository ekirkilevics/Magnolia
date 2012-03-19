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


import static info.magnolia.templating.editor.client.jsni.JavascriptUtils.getI18nMessage;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.editor.client.PageEditor;
import info.magnolia.templating.editor.client.dom.MgnlElement;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.PushButton;


/**
 * Area bar.
 */
public class AreaBar extends AbstractBar {

    private String workspace;
    private String path;

    private String name;
    private String type;
    private String dialog;
    private boolean optional;
    private boolean created;
    private boolean editable = true;

    public AreaBar(MgnlElement mgnlElement) throws IllegalArgumentException {
        super(mgnlElement);

        checkMandatories(mgnlElement.getAttributes());

        GWT.log("Area ["+this.name+"] is of type " + this.type);

        this.addStyleName("area");

        setVisible(false);
        createButtons();

        attach();

    }

    private void createButtons() {

        if (this.dialog != null) {
            // do not show edit-icon if the area has not been created
            if (!this.optional || (this.optional && this.created)) {
                PushButton editButton = new PushButton();
                editButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        PageEditor.openDialog(dialog, workspace, path);
                    }
                });
                editButton.setTitle(getI18nMessage("buttons.area.edit.js"));
                editButton.setStylePrimaryName("mgnlEditorPushButton");
                editButton.addStyleName("edit");
                addPrimaryButton(editButton);
            }
        }
        if(this.optional) {
            if (this.created) {
                PushButton removeButton = new PushButton();
                removeButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        PageEditor.deleteComponent(path);
                    }
                });
                removeButton.setTitle(getI18nMessage("buttons.area.delete.js"));
                removeButton.setStylePrimaryName("mgnlEditorPushButton");
                removeButton.addStyleName("remove");
                addSecondaryButton(removeButton);
            }
            else {
                PushButton createbutton = new PushButton();
                createbutton.setStylePrimaryName("mgnlEditorButton");

                createbutton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        PageEditor.createComponent(workspace, path, "mgnl:area");
                    }
                });
                createbutton.setTitle(getI18nMessage("buttons.area.add.js"));
                createbutton.setStylePrimaryName("mgnlEditorPushButton");
                createbutton.addStyleName("add");
                addSecondaryButton(createbutton);
            }
        }

    }

    private void checkMandatories(Map<String, String> attributes) throws IllegalArgumentException {

        String content = attributes.get("content");
        if (content != null) {
            int i = content.indexOf(':');

            this.workspace = content.substring(0, i);
            this.path = content.substring(i + 1);
        }

        this.name = attributes.get("name");
        this.type = attributes.get("type");

        this.dialog = attributes.get("dialog");

        if (attributes.containsKey("editable")) {
            this.editable = Boolean.parseBoolean(attributes.get("editable"));
        }

        String availableComponents = "";
        if(!AreaDefinition.TYPE_NO_COMPONENT.equals(this.type)) {
            availableComponents = attributes.get("availableComponents");
        }

        boolean showAddButton = Boolean.parseBoolean(attributes.get("showAddButton"));
        this.optional = Boolean.parseBoolean(attributes.get("optional"));
        this.created = Boolean.parseBoolean(attributes.get("created"));

        // break no matter what follows
        if (!this.editable) {
            throw new IllegalArgumentException("Not injecting any Areabar");
        }

        // area can be deleted or created
       if (this.optional) {
            return;
        }

        else if (this.type.equals(AreaDefinition.TYPE_SINGLE)) {
            return;
        }

        // can add components to area
        else if (showAddButton && !availableComponents.isEmpty()) {
            return;
        }

        // area can be edited
        else if (dialog != null && !dialog.isEmpty()) {
            return;
        }

        else {
            throw new IllegalArgumentException("Not injecting any Areabar");
        }
    }

}
