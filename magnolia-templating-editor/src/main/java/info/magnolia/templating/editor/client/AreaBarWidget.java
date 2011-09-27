/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating.editor.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

/**
 * Area bar.
 */
public class AreaBarWidget extends AbstractBarWidget {

    private PageEditor pageEditor;

    private String workspace;
    private String path;

    private String label;
    private String name;
    private String availableComponents;
    private String type;
    private String dialog;
    private boolean showAddButton = true;

    public AreaBarWidget(AbstractBarWidget parentBar, final PageEditor pageEditor, Element element) {
        super(parentBar);
        this.pageEditor = pageEditor;

        String content = element.getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);

        this.name = element.getAttribute("name");
        this.availableComponents = element.getAttribute("availableComponents");
        this.type = element.getAttribute("type");
        this.dialog = element.getAttribute("dialog");
        this.label = element.getAttribute("label");
        if (element.hasAttribute("showAddButton")) {
            this.showAddButton = Boolean.parseBoolean(element.getAttribute("showAddButton"));
        }

        setClassName("mgnlAreaControlBar");

        Label areaName = new Label(this.label);
        //set primary style overrides gwt default css class, in this case gwt-Label
        areaName.setStylePrimaryName("mgnlAreaLabel");
        add(areaName);

        if (element.hasAttribute("dialog") && type.equals(PageEditor.AREA_TYPE_LIST)) {
            Button button = new Button(getDictionary().get("buttons.editarea.js"));
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    pageEditor.openDialog(dialog, workspace, path, null, name);
                }
            });
            addButton(button, Float.RIGHT);
        }

        if (showAddButton) {
            Button addButton = new Button(getDictionary().get("buttons.addcomponent.js"));
            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (type.equals(PageEditor.AREA_TYPE_LIST)) {
                        pageEditor.addComponent(workspace, path, name, null, availableComponents);
                    } else if (type.equals(PageEditor.AREA_TYPE_SINGLE)) {
                        pageEditor.addComponent(workspace, path, null, name, availableComponents);
                    }
                }
            });
            addButton(addButton, Float.RIGHT);
        }
    }

    @Override
    protected void onSelect() {
        super.onSelect();
        if (type.equals(PageEditor.AREA_TYPE_LIST)) {
            pageEditor.updateSelection(this, PageEditor.SELECTION_TYPE_AREA_LIST, workspace, path, name, null, availableComponents, dialog);
        } else if (type.equals(PageEditor.AREA_TYPE_SINGLE)) {
            if (showAddButton) {
                pageEditor.updateSelection(this, PageEditor.SELECTION_TYPE_AREA_SINGLE, workspace, path, null, name, availableComponents, dialog);
            } else {
                pageEditor.updateSelection(this, PageEditor.SELECTION_TYPE_COMPONENT_IN_SINGLE, workspace, path, null, null, availableComponents, dialog);
            }
        }
    }

    public void mutateIntoSingleBar(Element element) {

        String content = element.getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);

        this.label = element.getAttribute("label");
        this.dialog = element.getAttribute("dialog");
        this.availableComponents = "";

        Button button = new Button(getDictionary().get("buttons.editcomponent.js"));
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (showAddButton) {
                    pageEditor.addComponent(workspace, path, null, name, availableComponents);
                } else {
                    pageEditor.openDialog(dialog, workspace, path, null, null);
                }
            }
        });
        addButton(button, Float.RIGHT);
    }

    @Override
    public void attach(Element element) {
        element.appendChild(getElement());
        onAttach();
    }

    public String getAvailableComponents() {
        return availableComponents;
    }

    public String getType() {
        return type;
    }
}
