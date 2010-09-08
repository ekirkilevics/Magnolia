/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.dialog.editor;

import info.magnolia.module.admincentral.control.DateControl;
import info.magnolia.module.admincentral.control.EditControl;
import info.magnolia.module.admincentral.control.FileControl;
import info.magnolia.module.admincentral.control.SelectControl;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * The droppable editor ... PoC.
 * @author had
 * @version $Id: $
 */
public class Droppings extends VerticalLayout {

    public Droppings(String dialogName) {
        GridLayout topLayout = new GridLayout(2, 5);
        topLayout.setSpacing(true);

        topLayout.setColumnExpandRatio(0, 1);
        FormLayout fieldEditingTarget = new FormLayout();

        // we need some default component to make sure container is big enough to be able to drag something into it
        Label dropFieldsLabel = new Label("Drop fields here");
        // TODO: doesn't work with the FormLayout why?
        SortableLayout finalLayout = new SortableLayout(new VerticalLayout(), dropFieldsLabel, fieldEditingTarget);
        finalLayout.addStyleName("no-horizontal-drag-hints");
        finalLayout.addComponent(dropFieldsLabel);

        Panel editorPanel = new Panel("Dialog fields configured for dialog "+dialogName+"", finalLayout);
        topLayout.addComponent(editorPanel, 0, 0);


        SortableLayout fieldChooser = new SortableLayout(new VerticalLayout(), null);
        fieldChooser.addStyleName("no-horizontal-drag-hints");
        for (Component component : createComponents()) {
            fieldChooser.addComponent(component);
        }
        Panel fieldTemplates = new Panel("Existing dialog fields:", fieldChooser);

        topLayout.addComponent(fieldTemplates, 1, 0);

        topLayout.addComponent(new Label("<span style=\"font-size: 80%;\">To add field to a dialog drag the field and drop it at the desired location.</span>", Label.CONTENT_XHTML), 0, 2, 1, 2);
        topLayout.addComponent(new Label("<span style=\"font-size: 80%;\">To remove a field simply drag it away from dialog back to list of available fields.</span>", Label.CONTENT_XHTML), 0, 3, 1, 3);

        topLayout.addComponent(fieldEditingTarget, 0, 4, 1, 4);
        addComponent(topLayout);
    }

    private List<Component> createComponents() {
        List<Component> components = new ArrayList<Component>();

//        components.add(new LinkControl().getControl(null, getWindow()));
//        components.add(new DateControl().getControl(null, getWindow()));
//        components.add(new FckEditorControl().getControl(null, getWindow()));
//        components.add(new RadioControl().getControl(null, getWindow()));
//        components.add(new SelectControl().getControl(null, getWindow()));
//        components.add(new SliderControl().getControl(null, getWindow()));
        components.add(new TemplateControl("edit", new EditControl()));
        components.add(new TemplateControl("upload", new FileControl()));
        components.add(new TemplateControl("date", new DateControl()));
        components.add(new TemplateControl("select", new SelectControl()));

        // TODO: fix layout
        //components.add(new TemplateControl("checkbox", new CheckBoxControl()));

        return components;
    }



}