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


import com.google.gwt.core.client.EntryPoint;

import info.magnolia.module.genuinecentral.gwt.client.tree.DefaultTreeConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Vivian Steller Created 23.08.2009 09:12:43
 */
public class AdminCentral implements EntryPoint {

    public static boolean isExplorer() {
        String test = Window.Location.getPath();
        if (test.indexOf("pages") != -1) {
            return false;
        }
        return true;
    }

    public static final String ADMIN_CENTRAL = "adminCentral";
    public static final String MODEL = "model";
    private static final String DEFAULT_TREE_CONFIG = "defaultTreeConfig";

    private Viewport viewport;
    private TabPanel menuPanel;

    public AdminCentral() {
/*        dialogRegistry = new DialogRegistryClient();
        // dataSourceProvider = new DataSourceProvider(dialogRegistry);
        // dialogBuilder = new DialogBuilder(dataSourceProvider,
        // dialogRegistry);
        dialogBuilder = new DialogBuilder(dialogRegistry);

        contentStore = new MgnlContentStore();
*/    }

/*    public void onModuleLoad() {
        // SC.showConsole();

        // button
        IButton buttonShowWindow = new IButton("Show Window");
        buttonShowWindow.setShowRollOver(true);
        buttonShowWindow.setShowDown(true);
        buttonShowWindow.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                SC.askforValue("UUID: 1 or 2", new ValueCallback() {
                    public void execute(String uuid) {
                        MgnlContent content = contentStore.getContent(uuid);

                        showDialog(content);
                    }

                });

            }
        });

        IButton logJSOButton = new IButton("Log JSO") {
            {
                addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        SC.askforValue("UUID: 1 or 2", new ValueCallback() {
                            public void execute(String value) {
                                JavaScriptObject content = contentStore.getContent(value);
                                SC.say(jsoAsString(content).toString().replace("\n", "<br/>"));
                            }
                        });
                    }
                });
            }
        };

        final TreeGrid tree = createTree();
        IButton buttonShowTree = new IButton("Show Tree");
        buttonShowTree.setShowRollOver(true);
        buttonShowTree.setShowDown(true);
        buttonShowTree.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                tree.show();
            }
        });
        IButton buttonHideTree = new IButton("Hide Tree");
        buttonHideTree.setShowRollOver(true);
        buttonHideTree.setShowDown(true);
        buttonHideTree.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                tree.hide();
            }
        });

        HStack layoutTopRow = new HStack(10);
        layoutTopRow.setHeight(50);

        final VLayout layoutMain = new VLayout();
        layoutMain.setMembersMargin(5);
        layoutMain.addMember(layoutTopRow);
        layoutMain.addMember(buttonShowWindow);
        layoutMain.addMember(logJSOButton);

        layoutMain.addMember(buttonShowTree);
        layoutMain.addMember(buttonHideTree);

        layoutMain.addMember(tree);

        layoutMain.draw();
    }

    public void showDialog(MgnlContent content) {
        dialogBuilder.createDialog(content, new AsyncCallback<Window>() {
            public void onSuccess(Window window) {
                window.show();
            }

            public void onFailure(Throwable caught) {
                throw new RuntimeException(caught);
            }
        });
    }

    private StringBuffer jsoAsString(JavaScriptObject content) {
        StringBuffer str = new StringBuffer();
        String[] propertyNames = JSOHelper.getProperties(content);
        for (String propertyName : propertyNames) {
            str.append(propertyName).append(": ").append(JSOHelper.getAttribute(content, propertyName));
            str.append("\n");
        }
        return str;
    };

    public TreeGrid createTree() {

        final TreeGrid treeGrid = new TreeGrid();
        treeGrid.setWidth(300);
        treeGrid.setHeight(200);
        treeGrid.setLayoutAlign(Alignment.CENTER);
        treeGrid.setLeft(250);
        treeGrid.setCanEdit(true);
        treeGrid.setCanAcceptDroppedRecords(true);
        treeGrid.setCanReorderRecords(true);

        TreeGridField field = new TreeGridField("name", "Name");
        field.setCanEdit(true);
        final TreeGridField field2 = new TreeGridField("password", "Password");
        field.setCanSort(false);

        treeGrid.addCellSavedHandler(new CellSavedHandler() {

            public void onCellSaved(CellSavedEvent event) {
                SC.say("Modified " + treeGrid.getSelectedRecord().getAttribute("name"));
            }
        });

        treeGrid.setEditByCell(true);
        treeGrid.setFields(field, field2);

        // context menu
        Menu contextMenu = new Menu();
        contextMenu.addItem(new MenuItem("Remove node"));
        contextMenu.addCellClickHandler(new CellClickHandler() {
            public void onCellClick(CellClickEvent event) {

                SC.confirm("Remove this node?", new BooleanCallback() {
                    public void execute(Boolean value) {
                        if (value) {
                            treeGrid.removeSelectedData();
                        }
                    }
                });
            }
        });

        treeGrid.setContextMenu(contextMenu);
        treeGrid.addCellClickHandler(new CellClickHandler() {
            public void onCellClick(CellClickEvent event) {
                if (event.isRightButtonDown() && event.getColNum() == 0) {
                    treeGrid.getContextMenu().showContextMenu();
                }
            }
        });

        final Tree tree = new Tree();

        tree.setModelType(TreeModelType.PARENT);
        tree.setParentIdField("super");
        tree.setNameProperty("Name");
        tree.setIdField("name");
        tree.setShowRoot(true);

        // add data to tree
        TreeNode content = new TreeNode();
        content.setAttribute("name", "Tim");
        content.setAttribute("password", "123");
        content.setAttribute("super", "users");
        TreeNode content2 = new TreeNode();
        content2.setAttribute("name", "James");
        content2.setAttribute("password", "456");
        content2.setAttribute("super", "users");
        TreeNode content3 = new TreeNode();
        content3.setAttribute("name", "users");

        tree.setData(new TreeNode[] { content3, content, content2 });
        */
        /*
         * //++sample with type children TreeNode content = new TreeNode();
         * content.setAttribute("name", "Tim"); content.setAttribute("password",
         * "123"); TreeNode content2 = new TreeNode();
         * content2.setAttribute("name", "James");
         * content2.setAttribute("password", "456"); TreeNode content3 = new
         * TreeNode(); content3.setAttribute("name", "users");
         * content3.setChildren(new TreeNode[]{content,content2});
         * tree.setModelType(TreeModelType.CHILDREN);
         * tree.setNameProperty("Name"); tree.setChildrenProperty("children");
         * tree.setRoot(content3); treeGrid.setShowRoot(true); //--sample tree
         * type children
         */
 /*       treeGrid.addDrawHandler(new DrawHandler() {
            public void onDraw(DrawEvent event) {
                tree.openAll();
            }
        });

        treeGrid.setData(tree);
        treeGrid.hide();
        return treeGrid;
    }*/

    public void onModuleLoad() {

        System.out.println("Loading Admin Central");

        String id = Window.Location.getParameter("id");
        if (id == null) {
            id = XDOM.getBody().getId();
        }

        // expose AC to all now
        Registry.register(ADMIN_CENTRAL, this);

        for (Map.Entry<String, Object> module : Registry.getAll().entrySet()) {
            System.out.println("(In AC) Found registered module: " + module.getKey() + " :: " + module.getValue());
        }
        // TODO: load the list of dependent modules from the admin central
        if (Registry.get("wcm") != null) {
            init();
        }
    }

    public void init() {

        System.out.println("Initializing Admin central");

        createDefaultTreeConfig();

        createDefaultMenu();

        viewport = new Viewport();
        viewport.setLayout(new BorderLayout());
        viewport.add(this.menuPanel, new BorderLayoutData(LayoutRegion.CENTER));


        createNorth();

        viewport.show();
        RootPanel.get().add(viewport);
    }

    private void createDefaultTreeConfig() {
        Registry.register(DEFAULT_TREE_CONFIG, new DefaultTreeConfig());
    }

    private void createDefaultMenu() {
        List<Tab> entries = new ArrayList<Tab>();
        // TODO: read the configuration from the config workspace
        entries.add(createTab("Website", "website", "/", "wcmTreeConfig"));
        // TODO: connect to config tree instead of website ... right now just showing different config for same workspace
        entries.add(createTab("Config", "webiste", "/", DEFAULT_TREE_CONFIG));

        this.menuPanel = new TabPanel();

        menuPanel.setResizeTabs(true);

        for (Tab entry: entries) {
            for (ModelData child : entry.getChildren()) {
                addMenuEntry(entry.getName(), child);
            }
        }
    }

    private Tab createTab(String name, String treeName, String treePath, String config) {
        Tab treeGrids = new Tab(name);
        MgnlTreeGrid tree = new MgnlTreeGrid(config);
        tree.setTree(treeName);
        tree.setPath(treePath);
        tree.setHeading(name);
        treeGrids.add(name, tree, null);
        return treeGrids;
    }

    private void addMenuEntry(String title, Object menuItem) {
        // TODO: right now the menu items are tab entries, but that will change later
        TabItem treeTab = new TabItem(title);
        treeTab.setScrollMode(Scroll.AUTO);
        TabEntry tab = (TabEntry) menuItem;
        if (tab.isFill()) {
            treeTab.setLayout(new FitLayout());
            treeTab.setScrollMode(Scroll.NONE);
        }

        treeTab.add(tab.getItem());
        getMenu().add(treeTab);
    }

    private TabPanel getMenu() {
        return this.menuPanel;
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
        viewport.add(northPanel, data);
    }

    public void openDialog(Object object) {
        PopupPanel dialog = new PopupPanel(false, true);
        dialog.add(new Label("welcome to the dialog hell"));
        dialog.setTitle("booo");
        dialog.setStyleName(Slate.SLATE.getName());
        dialog.setWidth("" + 200);
        dialog.setHeight("" + 200);
        dialog.setPopupPosition(10, 10);
        dialog.setVisible(true);
        dialog.show();
        viewport.add(dialog);
    }

}
