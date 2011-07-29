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
package info.magnolia.ui.vaadin.components.demo;

import info.magnolia.ui.vaadin.components.Melodion;
import info.magnolia.ui.vaadin.components.Melodion.Tab;
import info.magnolia.ui.vaadin.components.Rack;
import info.magnolia.ui.vaadin.components.Rack.Unit;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;


/**
 * The Application's "main" class.
 */
@SuppressWarnings("serial")
public class ComponentsDemo extends Application {

    private Window window;

    @Override
    public void init() {
        setTheme("demo");

        window = new Window("Components demo");
        setMainWindow(window);

        HorizontalLayout layout = new HorizontalLayout();
        window.addComponent(layout);

        Panel melodionPanel = new Panel("Melodion", melodion());
        melodionPanel.setWidth(300, Sizeable.UNITS_PIXELS);
        melodionPanel.setHeight(480, Sizeable.UNITS_PIXELS);
        layout.addComponent(melodionPanel);

        Panel rackPanel = new Panel("Rack", rack());
        rackPanel.setWidth(200, Sizeable.UNITS_PIXELS);
        rackPanel.setHeight(220, Sizeable.UNITS_PIXELS);
        layout.addComponent(rackPanel);
    }

    private ComponentContainer rack() {
        Rack rack = new Rack();

        toolbar(rack);
        actions(rack);

        Unit status = rack.addUnit(new Button("Status"));
        status.setContent(new Label("Hello World!"));

        Unit versions = rack.addUnit(new Button("Versions"));
        versions.setContent(new Label("Hello World!"));

        return rack;
    }

    private void toolbar(Rack rack) {
        CssLayout layout = new CssLayout();
        rack.setToolbar(layout);

        Button preview = button();
        preview.setIcon(new ThemeResource("../chameleon/img/magnifier.png"));
        layout.addComponent(preview);

        Label spacer = new Label("&nbsp;", Label.CONTENT_XHTML);
        spacer.setSizeUndefined();
        layout.addComponent(spacer);

        Button addSubpage = button();
        addSubpage.setIcon(new ThemeResource("../runo/icons/16/document-add.png"));
        layout.addComponent(addSubpage);

        Button edit = button();
        edit.setIcon(new ThemeResource("../runo/icons/16/document-txt.png"));
        layout.addComponent(edit);

        Button properties = button();
        properties.setIcon(new ThemeResource("../runo/icons/16/settings.png"));
        layout.addComponent(properties);

        Button delete = button();
        delete.setIcon(new ThemeResource("../runo/icons/16/document-delete.png"));
        layout.addComponent(delete);
    }

    private void actions(Rack rack) {
        Unit actions = rack.addUnit(new Button("Actions"));
        actions.setClosable(false);
        CssLayout layout = new CssLayout();
        actions.setContent(layout);

        Button preview = button("Preview");
        preview.setIcon(new ThemeResource("../chameleon/img/magnifier.png"));
        layout.addComponent(preview);

        layout.addComponent(new Label("&nbsp;", Label.CONTENT_XHTML));

        Button addSubpage = button("Add subpage");
        addSubpage.setIcon(new ThemeResource("../runo/icons/16/document-add.png"));
        layout.addComponent(addSubpage);

        Button edit = button("Edit");
        edit.setIcon(new ThemeResource("../runo/icons/16/document-txt.png"));
        layout.addComponent(edit);

        Button properties = button("Properties");
        properties.setIcon(new ThemeResource("../runo/icons/16/settings.png"));
        layout.addComponent(properties);

        Button delete = button("Delete");
        delete.setIcon(new ThemeResource("../runo/icons/16/document-delete.png"));
        layout.addComponent(delete);
    }

    private Button button() {
        return button(new Button());
    }

    private Button button(Button button) {
        button.setStyleName("action");
        button.addStyleName("borderless");
        return button;
    }

    private Button button(String caption) {
        return button(new Button(caption));
    }

    private ComponentContainer melodion() {
        Melodion melodion = new Melodion();

        Tab templates = melodion.addTab(new Label("Templates"));
        templates.addButton(new NativeButton("Site-wide templates"));
        templates.addButton(new NativeButton("Cross-site templates"));

        melodion.addTab(new Label("Security"));
        melodion.addTab(new Label("Configuration"));

        melodion.addSpacer();

        Tab tools = melodion.addTab(new Label("Tools"));
        tools.addButton(new NativeButton("Browser"));
        tools.addButton(new NativeButton("Export your design"));
        tools.addButton(new NativeButton("Import templates"));

        melodion.addTab(new Label("Packager"));
        melodion.addTab(new Label("Store"));

        melodion.addSpacer();

        Label workItems = new Label("Work items");
        workItems.setCaption("5 new");
        melodion.addTab(workItems);

        Label moderation = new Label("Moderation");
        moderation.setCaption("2 new");
        melodion.addTab(moderation);
        melodion.addTab(new Label("Internal messages"));

        melodion.addSpacer();

        Label welcomeBoard = new Label("Welcome board");
        welcomeBoard.setIcon(new ThemeResource("../runo/icons/16/note.png"));
        melodion.addTab(welcomeBoard);

        Label settings = new Label("Settings");
        settings.setIcon(new ThemeResource("../runo/icons/16/settings.png"));
        melodion.addTab(settings);

        Label profile = new Label("Profile");
        profile.setIcon(new ThemeResource("../runo/icons/16/user.png"));
        melodion.addTab(profile);

        melodion.addSpacer();

        Label systemMessagesLabel = new Label("System messages");
        systemMessagesLabel.setCaption("3 new");
        Tab systemMessagesTab = melodion.addTab(systemMessagesLabel);
        systemMessagesTab.addButton(new NativeButton("Warnings"));
        systemMessagesTab.addButton(new NativeButton("Errors"));

        melodion.addTab(new Label("Audit log"));

        return melodion;
    }
}
