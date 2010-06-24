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

import static com.google.gwt.http.client.RequestBuilder.GET;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.JsonLoadResultReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;

public class MgnlTreeGrid extends LayoutContainer {

    private String treeName = "";
    private String path = "";
    private TreeGrid<ModelData> tree;
    private ContentPanel cp;
    private String heading = "";

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);

        setLayout(new FlowLayout(10));

        final RequestBuilder requestBuilder = new RequestBuilder(GET, "/.magnolia/rest/" + treeName + path);
        requestBuilder.setHeader("Accept", "application/json");

        // data proxy
        RestfullHttpProxy<List<FileModel>> proxy = new RestfullHttpProxy<List<FileModel>>(requestBuilder);

        JsonLoadResultReader<List<FileModel>> jsonReader = getConfiguredReader();

        final TreeLoader<FileModel> loader = getConfiguredTreeLoader(proxy, jsonReader);

        // trees store
        final TreeStore<FileModel> store = getConfiguredTreeStore(loader);

        ColumnModel cm = getColumnConfiguration();

        cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeading(heading);
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        cp.setLayout(new BorderLayout());
        cp.setFrame(true);
        cp.setSize(600, 300);

        tree = new TreeGrid<ModelData>(store, cm);
        tree.setStateful(true);
        // stateful components need a defined id
        // TODO: each tree instance will need different id
        tree.setId("statefull" + treeName + "mgnltreegrid");
        tree.setBorders(true);
        // TODO: use one of our icons ...
        tree.getStyle().setLeafIcon(IconHelper.createStyle("icon-page"));
        tree.setSize(400, 400);
        tree.setAutoExpandColumn("name");
        tree.setTrackMouseOver(false);
        //tree.setAutoHeight(true);

        //cp.add(getToolBar(tree), new BorderLayoutData(LayoutRegion.NORTH));
        ContentPanel south = new ContentPanel(new FitLayout());
        south.setHeaderVisible(false);
        south.add(getToolBar(tree));
        BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 30);
        southData.setSplit(true);
        southData.setCollapsible(true);
        southData.setFloatable(false);
        cp.add(south, southData);
        ContentPanel center = new ContentPanel(new FitLayout());
        center.add(tree);
        center.setHeaderVisible(false);
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setCollapsible(false);
        centerData.setHideCollapseTool(true);
        // 0..1 are %
        centerData.setSize(1);
        cp.add(center, centerData);

        // tool tip
        ToolTipConfig config = new ToolTipConfig();
        config.setTitle("Bla bla bla Title");
        config.setShowDelay(1);
        config.setText("This is a stateful grid so it is opened always at the last opened place even after the refresh.");

        ToolButton btn = new ToolButton("x-tool-help");
        btn.setToolTip(config);

        cp.getHeader().addTool(btn);

        // context menu
        Menu contextMenu = new Menu();
        contextMenu.setWidth(140);
        contextMenu.setHeight(60);
        contextMenu.setFocusOnShow(true);
        contextMenu.setTitle("Tree Context Menu");

        MenuItem insert = new MenuItem();
        insert.setText("Insert Item");
        insert.setBorders(true);
        //insert.setIcon(ICONS.add());
        contextMenu.add(insert);

        MenuItem remove = new MenuItem();
        remove.setText("Remove Selected");
        //remove.setIcon(ICONS.delete());
        contextMenu.add(remove);

        insert.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                ModelData folder = tree.getSelectionModel().getSelectedItem();
                if (folder != null) {
                    // ...
                }
            }
        });

        remove.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                List<ModelData> selected = tree.getSelectionModel().getSelectedItems();
                for (ModelData sel : selected) {
                    store.remove((FileModel) sel);
                }
            }
        });

        tree.setContextMenu(contextMenu);

        add(cp);
    }

    /**
     * Prepares pre-configured tree store with the column sorter and key provider.
     */
    protected TreeStore<FileModel> getConfiguredTreeStore(TreeLoader<FileModel> loader) {
        final TreeStore<FileModel> store = new TreeStore<FileModel>(loader);
        store.setStoreSorter(getColumnDataSorter());
        store.setKeyProvider(new ModelKeyProvider<FileModel>() {
            public String getKey(FileModel model) {
                // TODO: Same name siblings will have same path!!!!
                return model.getPath();
            }

        });
        return store;
    }

    /**
     * Override this method to change columns displayed by the tree
     */
    protected ColumnModel getColumnConfiguration() {
        ColumnConfig name = new ColumnConfig("name", "Name", 100);
        name.setRenderer(new TreeGridCellRenderer<ModelData>());

        ColumnConfig date = new ColumnConfig("date", "Date", 100);
        date.setDateTimeFormat(DateTimeFormat.getMediumDateTimeFormat());

        ColumnConfig size = new ColumnConfig("size", "Size", 100);

        ColumnModel cm = new ColumnModel(Arrays.asList(name, date, size));
        return cm;
    }

    /**
     * Override this method to provide special sorting rules for columns
     */
    protected StoreSorter<FileModel> getColumnDataSorter() {
        return new StoreSorter<FileModel>() {

            @Override
            public int compare(Store<FileModel> store, FileModel m1, FileModel m2, String property) {
                boolean m1Folder = m1 instanceof FolderModel;
                boolean m2Folder = m2 instanceof FolderModel;

                if (m1Folder && !m2Folder) {
                    return -1;
                } else if (!m1Folder && m2Folder) {
                    return 1;
                }

                return super.compare(store, m1, m2, property);
            }
        };
    }

    /**
     * Preconfigured tree loader. There should be no need to override this.
     * @param requestBuilder
     */
    protected TreeLoader<FileModel> getConfiguredTreeLoader(final RestfullHttpProxy<List<FileModel>> proxy, JsonLoadResultReader<List<FileModel>> jsonReader) {
        // tree loader
        final TreeLoader<FileModel> loader = new BaseTreeLoader<FileModel>(proxy, jsonReader) {
            @Override
            public boolean hasChildren(FileModel parent) {
                return Boolean.parseBoolean("" + parent.get("hasChildren"));
            }

            @Override
            protected Object newLoadConfig() {
                Map<String, String> params = getRequestParameters();
                if (params == null) {
                    return null;
                }
                ModelData config = new BaseModelData();
                for (Map.Entry<String, String> param : params.entrySet()) {
                    config.set(param.getKey(), param.getValue());
                }
                return config;
            }

            @Override
            public boolean loadChildren(FileModel parent) {
                System.out.println("load children for parent:" + parent);
                // proxy is not really RESTfull, we need to change the init uri on load children call
                RestfullHttpProxy<List<FileModel>> restfullProxy = (RestfullHttpProxy<List<FileModel>>) proxy;
                String url = restfullProxy.getOriginalURL();
                // Using StringUtils would require inclusion of its sources for compilation ... this could bloat the build quite quickly
                //url = StringUtils.removeEnd(url, "/") + StringUtils.removeStart(parent.getPath(), "/");
                if (url != null && url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                final String parentPath = parent.getPath();
                if (parentPath == null) {
                    url += "/";
                } else if (parentPath.startsWith("/")) {
                    url += parentPath;
                } else {
                    url += "/" + parentPath;
                }
                System.out.println("Setting url to:" + url);
                restfullProxy.setURL(url);
                return super.loadChildren(parent);
            }
        };
        return loader;
    }

    /**
     * Override this to include extra parameters
     * @return
     */
    protected Map<String, String> getRequestParameters() {
        // can be used to specify only certain properties to be retrieved or add extra params (such as username & pwd)
        Map<String, String> config = new java.util.HashMap<String, String>();
        config.put("mgnlUserId", "superuser");
        config.put("mgnlUserPSWD", "superuser");
        return config;
     }

    private JsonLoadResultReader<List<FileModel>> getConfiguredReader() {
        // TODO: generate model types or use different kind of deserialization !!!
        ModelType type = new ModelType();
        type.setRoot("children");
        type.setRecordName("content");
        type.addField("uuid");
        type.addField("name");
        type.addField("path");
        type.addField("status");
        type.addField("hasChildren");
        type.addField("template");

        JsonLoadResultReader<List<FileModel>> jsonReader = new JsonLoadResultReader<List<FileModel>>(type) {
            protected ListLoadResult<ModelData> newLoadResult(Object loadConfig, List<ModelData> models) {
                throw new UnsupportedOperationException("Do not call me!");
            }

            protected Object createReturnData(Object loadConfig, List<ModelData> records, int totalCount) {
                ArrayList<FileModel> resultList = new ArrayList<FileModel>();
                for (ModelData record : records) {
                    FileModel model;
                    Object hasChildren = record.get("hasChildren");
                    if (hasChildren != null && Boolean.parseBoolean(hasChildren.toString())) {
                        model = new FolderModel(record.getProperties());
                    } else {
                        model = new FileModel(record.getProperties());
                    }
                    resultList.add(model);
                }
                return resultList;
            }
        };
        return jsonReader;
    }

    private ButtonBar getToolBar(final TreeGrid<ModelData> tree) {
        ButtonBar buttonBar = new ButtonBar();

        Button add = new Button("New Page");
        add.setIcon(IconHelper.createPath("/.resources/icons/16/folder.png"));
        buttonBar.add(add);
        Button del = new Button("Delete");
        del.setIcon(IconHelper.createPath("/.resources/icons/16/delete2.gif"));
        buttonBar.add(del);
        Button act = new Button("Activate");
        act.setIcon(IconHelper.createPath("/.resources/icons/16/arrow_right_green.gif"));
        buttonBar.add(act);
        Button deact = new Button("Deactivate");
        deact.setIcon(IconHelper.createPath("/.resources/icons/16/arrow_left_red.gif"));
        buttonBar.add(deact);

        Button expand = new Button("Expand All");
        Button collapse = new Button("Collapse All");
        expand.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                tree.expandAll();
            }
        });

        collapse.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                tree.collapseAll();
            }
        });
        buttonBar.add(expand);
        buttonBar.add(collapse);

        buttonBar.setAutoHeight(false);
        buttonBar.setHeight(40);
        buttonBar.setBorders(true);

        return buttonBar;
    }


    public void setHeading(String heading) {
        if (cp != null) {
            cp.setHeading(heading);
        }
        this.heading = heading;
    }

    public String getTree() {
        return treeName;
    }

    public void setTree(String tree) {
        this.treeName = tree;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TreeGrid<ModelData> getTreeImpl() {
        return this.tree;
    }

}