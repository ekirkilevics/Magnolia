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
package info.magnolia.module.genuinecentral.gwt.client;

import info.magnolia.module.genuinecentral.gwt.client.models.MenuModel;
import info.magnolia.module.genuinecentral.gwt.client.presenter.MenuHandler;
import info.magnolia.module.genuinecentral.gwt.client.presenter.AdminCentralPresenter.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.Widget;

public class AdminCentralView implements Display {

    Viewport widget;
    private List<MenuHandler> menuHandlers = new ArrayList<MenuHandler>();
    private Map<String, MenuConfiguration> menuConfig;
    private MgnlTreeGrid tree;

    public AdminCentralView() {
        this.widget = new Viewport();
        widget.setLayout(new BorderLayout());
        createNorth();
        createVerticalNavigation();
    }


    private void createNorth() {
        StringBuffer sb = new StringBuffer();
        sb.append("<div id='demo-header' class='x-small-editor'><div id='demo-theme'></div></div>");

        HtmlContainer northPanel = new HtmlContainer(sb.toString());
        northPanel.setStateful(false);

        // register some theme ... should be probably replaced with our own custom theme later.
        ThemeManager.register(Slate.SLATE);
        GXT.setDefaultTheme(Slate.SLATE, true);

        BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 33);
        data.setMargins(new Margins());
        widget.add(northPanel, data);
    }

    private MgnlTreeGrid createTree(final String name, final String treeName, final String treePath, final String config) {
        MgnlTreeGrid tree = new MgnlTreeGrid(config);
        tree.setTree(treeName);
        tree.setPath(treePath);
        tree.setHeading(name);
        return tree;
    }

    private void createVerticalNavigation(){
        ContentPanel west = new ContentPanel();
        west.setBodyBorder(false);
        west.setLayout(new AccordionLayout());

        Loader<List<MenuModel>> store = ServerConnector.getAdminCentralMenuLoader(null);
        store.load();

        //TODO: create menu item from info provided by the server via service
        final ContentPanel nav = new ContentPanel();
        nav.setHeading("Website");
        nav.setBorders(false);
        nav.setBodyStyle("fontSize: 12px; padding: 6px");
        nav.setExpanded(false);
        nav.addListener(Events.Expand , new Listener<BaseEvent>() {

            public void handleEvent(BaseEvent be) {
                for (MenuHandler handler : menuHandlers) {
                    handler.onMenuItemExpanded(nav.getHeading());
                }
            }});
        west.add(nav);

        final ContentPanel settings = new ContentPanel();
        settings.setHeading("Config");
        settings.setBorders(false);
        settings.addListener(Events.Expand, new Listener<BaseEvent>() {

            public void handleEvent(BaseEvent be) {
                for (MenuHandler handler : menuHandlers) {
                    handler.onMenuItemExpanded(settings.getHeading());
                }
            }});
        west.add(settings);

        BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200, 100, 300);
        westData.setMargins(new Margins(5, 0, 5, 5));
        westData.setCollapsible(true);
        widget.add(west, westData);
    }


    // AdminCentralPresenter.Display iface methods

    public Widget asWidget() {
        return this.widget;
    }

    public void addMenuHandler(MenuHandler menuHandler) {
        this.menuHandlers.add(menuHandler);
    }

    public void showTree(String treeTitle, String repository, String initialPath, String treeViewConfigName) {
        if (tree == null) {
            tree = this.createTree(treeTitle, repository, initialPath, treeViewConfigName);
        } else {
            tree.show();
        }
        widget.add(tree, new BorderLayoutData(LayoutRegion.CENTER));
        // force a layout since gxt doesn't do so by default
        widget.layout();
    }


    public void setMenuConfiguration(Map<String, MenuConfiguration> config) {
        this.menuConfig = config;
    }


    public void hideTree() {
        if (tree != null) {
            // TODO: might want to keep the comp in just hidden for performance reasons
            widget.remove(tree);
            widget.layout();
            tree = null;
        }

    }


}
