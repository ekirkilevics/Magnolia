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
package info.magnolia.module.genuinecentral.gwt.client.presenter;

import info.magnolia.module.genuinecentral.gwt.client.MagnoliaService;
import info.magnolia.module.genuinecentral.gwt.client.MenuConfiguration;
import info.magnolia.module.genuinecentral.gwt.client.TreeConfiguration;

import java.util.Map;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class AdminCentralPresenter implements Presenter {

    private final MagnoliaService service;
    private final HandlerManager eventBus;
    private final Display display;

    private Map<String, MenuConfiguration> config;

    public interface Display {
        Widget asWidget();

        void addMenuHandler(MenuHandler menuHandler);

        void showTree(String treeName, String repository, String initialPath, String treeConfigurationName);

        void setMenuConfiguration(Map<String, MenuConfiguration> config);

        void hideTree();
    }

    public AdminCentralPresenter(MagnoliaService aService, HandlerManager eventBus, Display view) {
        this.service = aService;
        this.eventBus = eventBus;
        this.display = view;
        init();
    }

    private void init() {
        config = service.getMenuConfiguration();
        // TODO: should we feed complete config to the view or give it items one by one? ... giving all let view control how and if re-rendering will be done between creation of items and in general let the view to optimize for performance at the expense of making it more smart
        display.setMenuConfiguration(config);
    }

    public void go(final HasWidgets container) {
        bind();
        container.clear();
        container.add(display.asWidget());
    }

    public void bind() {
        this.display.addMenuHandler(new MenuHandler() {
            public void onMenuItemExpanded(String heading) {
                display.hideTree();
                //TODO: potentially publish this event on the event bus
                MenuConfiguration menu = config.get(heading);
                System.out.println("heading:" + heading);
                if (menu.hasShowTreeOnClick()) {
                    TreeConfiguration treeConfig = menu.getTreeConfiguration();
                    System.out.println("tree:" + treeConfig.getTitle());
                    //display.showTree(heading, "website", "/", "wcmTreeConfig");
                    // and once we get menus and their tree config from the server even the line below will work
                    display.showTree(treeConfig.getTitle(), treeConfig.getRepository(), treeConfig.getRootPath(), treeConfig.getViewLayoutName());
                }

            }
        });
    }
}
