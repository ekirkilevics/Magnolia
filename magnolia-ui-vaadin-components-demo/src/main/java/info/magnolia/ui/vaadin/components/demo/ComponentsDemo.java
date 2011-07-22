/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package info.magnolia.ui.vaadin.components.demo;

import info.magnolia.ui.vaadin.components.Melodion;
import info.magnolia.ui.vaadin.components.Melodion.Tab;

import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Window;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class ComponentsDemo extends Application
{
    private Window window;

    @Override
    public void init()
    {
        setTheme("demo");

        window = new Window("My Vaadin Application");
        setMainWindow(window);

        Melodion melodion = new Melodion();
        melodion.setWidth(300, Melodion.UNITS_PIXELS);

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

        melodion.addTab(new Label("Moderation"));
        melodion.addTab(new Label("Internal messages"));

        melodion.addSpacer();

        melodion.addTab(new Label("Welcome board"));

        Label settings = new Label("Settings");
        settings.setIcon(new ThemeResource("../runo/icons/16/settings.png"));
        melodion.addTab(settings);

        Label profile = new Label("Profile");
        profile.setIcon(new ThemeResource("../runo/icons/16/user.png"));
        melodion.addTab(profile);

        window.addComponent(melodion);
    }
}
