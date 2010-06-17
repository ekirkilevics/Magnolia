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
import static com.google.gwt.http.client.Response.SC_NO_CONTENT;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.HttpProxy;
import com.extjs.gxt.ui.client.data.JsonLoadResultReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MgnlTreeGrid extends LayoutContainer {

  @Override
  protected void onRender(Element parent, int index) {
    super.onRender(parent, index);

    setLayout(new FlowLayout(10));

    RequestBuilder requestBuilder = new RequestBuilder(GET, "/.magnolia/rest/website/news?mgnlUserId=superuser&mgnlUserPSWD=superuser");
    requestBuilder.setHeader("Accept", "application/json");

    // data proxy
    HttpProxy<List<FileModel>> proxy = new HttpProxy<List<FileModel>>(requestBuilder) {
      @Override
      public void load(final DataReader<List<FileModel>> reader, final Object loadConfig, final AsyncCallback<List<FileModel>> callback) {
        System.out.println("Attempting to load folders:" + loadConfig + " :: " + callback);
        super.load(reader, loadConfig, callback);
        System.out.println("After super:" + loadConfig + " :: " + callback);
        // set the callback
        this.builder.setCallback(new RequestCallback() {

            public void onResponseReceived(Request request, Response response) {
                System.out.println("got response from http:" + response);

                try {
                    if (response.getStatusCode() == SC_NO_CONTENT || response.getStatusCode() == SC_NOT_FOUND) {
                        throw new Exception("No such tree");
                    }

                    System.out.println("response text:" + response.getText());
                    List<FileModel> result = null;
                    result = reader.read(loadConfig, response.getText());
                    System.out.println("results:" + result);
                    if (result == null || result.isEmpty()) {
                        result = new ArrayList<FileModel>();
                        // populate the dialog with the data from the response
                        result.add(new FolderModel("/", "/"));
                    }
                    callback.onSuccess(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to load tree: " + e.getMessage(), e);
                }
            }

            public void onError(Request request, Throwable exception) {
                // dispatch the exception
                throw new RuntimeException("Failed to load tree: " + exception.getMessage(), exception);
            }
        });

        try {
            this.builder.send();
        } catch (com.google.gwt.http.client.RequestException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        //service.getFolderChildren((FileModel) loadConfig, callback);
      }
    };

    // json reader
    ModelType type = new ModelType();
    type.setRoot("children");
    type.setRecordName("content");
    type.addField("uuid");
    type.addField("name");
    type.addField("status");
    type.addField("hasChildren");
    type.addField("template");
    JsonLoadResultReader<List<FileModel>> jsonReader = new JsonLoadResultReader<List<FileModel>>(type) {
        protected ListLoadResult<ModelData> newLoadResult(Object loadConfig, List<ModelData> models) {
            throw new UnsupportedOperationException("Do not call me!");
          }
        protected Object createReturnData(Object loadConfig, List<ModelData> records, int totalCount) {
            ArrayList<FileModel> resultList = new ArrayList<FileModel>();
            for (ModelData record: records) {
                resultList.add(new FileModel(record.getProperties()));
            }
            return resultList;
          }

    };

    // tree loader
    final TreeLoader<FileModel> loader = new BaseTreeLoader<FileModel>(proxy, jsonReader) {
      @Override
      public boolean hasChildren(FileModel parent) {
        return Boolean.parseBoolean("" + parent.get("hasChildren"));
      }

      @Override
        protected Object newLoadConfig() {
            // return load config to be used in the proxy.load() Default is null
            return super.newLoadConfig();
        }


    };

    // trees store
    final TreeStore<FileModel> store = new TreeStore<FileModel>(loader);
    store.setStoreSorter(new StoreSorter<FileModel>() {

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
    });

    ColumnConfig name = new ColumnConfig("name", "Name", 100);
    name.setRenderer(new TreeGridCellRenderer<ModelData>());

    ColumnConfig date = new ColumnConfig("date", "Date", 100);
    date.setDateTimeFormat(DateTimeFormat.getMediumDateTimeFormat());

    ColumnConfig size = new ColumnConfig("size", "Size", 100);

    ColumnModel cm = new ColumnModel(Arrays.asList(name, date, size));

    ContentPanel cp = new ContentPanel();
    cp.setBodyBorder(false);
    cp.setHeading("MgnlTreeGrid");
    cp.setButtonAlign(HorizontalAlignment.CENTER);
    cp.setLayout(new FitLayout());
    cp.setFrame(true);
    cp.setSize(600, 300);

    TreeGrid<ModelData> tree = new TreeGrid<ModelData>(store, cm);
    tree.setStateful(true);
    // stateful components need a defined id
    tree.setId("statefullmgnltreegrid");
    store.setKeyProvider(new ModelKeyProvider<FileModel>() {

      public String getKey(FileModel model) {
        return model.<String> get("id");
      }

    });
    tree.setBorders(true);
    tree.getStyle().setLeafIcon(IconHelper.createStyle("icon-page"));
    tree.setSize(400, 400);
    tree.setAutoExpandColumn("name");
    tree.setTrackMouseOver(false);
    cp.add(tree);

    ToolTipConfig config = new ToolTipConfig();
    config.setTitle("Bla bla bla Title");
    config.setShowDelay(1);
    config.setText("This is a stateful grid so it is opened always at the last opened place even after the refresh.");

    ToolButton btn = new ToolButton("x-tool-help");
    btn.setToolTip(config);

    cp.getHeader().addTool(btn);

    add(cp);
  }

}