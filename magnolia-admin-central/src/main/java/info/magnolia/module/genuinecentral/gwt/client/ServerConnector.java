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
import static com.google.gwt.http.client.RequestBuilder.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.JsonLoadResultReader;
import com.extjs.gxt.ui.client.data.JsonReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class ServerConnector {

    private final static ModelType FILE_MODEL_TYPE = new ModelType();
    private static final ModelType LIST_OF_FILES_MODEL_TYPE = new ModelType();

    static {
        LIST_OF_FILES_MODEL_TYPE.setRoot("children");
        LIST_OF_FILES_MODEL_TYPE.setRecordName("content");
        LIST_OF_FILES_MODEL_TYPE.addField("uuid");
        LIST_OF_FILES_MODEL_TYPE.addField("name");
        LIST_OF_FILES_MODEL_TYPE.addField("path");
        LIST_OF_FILES_MODEL_TYPE.addField("status");
        LIST_OF_FILES_MODEL_TYPE.addField("hasChildren");
        LIST_OF_FILES_MODEL_TYPE.addField("template");

        FILE_MODEL_TYPE.setRoot("child");
        FILE_MODEL_TYPE.setRecordName("content");
        FILE_MODEL_TYPE.addField("uuid");
        FILE_MODEL_TYPE.addField("name");
        FILE_MODEL_TYPE.addField("path");
        FILE_MODEL_TYPE.addField("status");
        FILE_MODEL_TYPE.addField("hasChildren");
        FILE_MODEL_TYPE.addField("template");

    }

    private static final JsonLoadResultReader<List<FileModel>> jsonReader = createConfiguredReader();

    public static TreeLoader<FileModel> getTreeLoader(String treeName, String rootPath, Map<String, String> additionalParams) {
        final RequestBuilder requestBuilder = new RequestBuilder(GET, "/.magnolia/rest/" + treeName + rootPath);
        requestBuilder.setHeader("Accept", "application/json");

        // data proxy
        RestfullHttpProxy<List<FileModel>> proxy = new RestfullHttpProxy<List<FileModel>>(requestBuilder);


        return getConfiguredTreeLoader(proxy, jsonReader, additionalParams);
    }

    /**
     * Preconfigured tree loader. There should be no need to override this.
     * @param requestBuilder
     */
    private static TreeLoader<FileModel> getConfiguredTreeLoader(final RestfullHttpProxy<List<FileModel>> proxy, JsonLoadResultReader<List<FileModel>> jsonReader, final Map<String, String> extraParams) {
        // tree loader
        final TreeLoader<FileModel> loader = new BaseTreeLoader<FileModel>(proxy, jsonReader) {
            @Override
            public boolean hasChildren(FileModel parent) {
                return Boolean.parseBoolean("" + parent.get("hasChildren"));
            }

            @Override
            protected Object newLoadConfig() {
                Map<String, String> params = getRequestParameters(extraParams);
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
    private static Map<String, String> getRequestParameters(Map<String, String> extraParams) {
        // can be used to specify only certain properties to be retrieved or add extra params (such as username & pwd)
        Map<String, String> config = new java.util.HashMap<String, String>();
        config.put("mgnlUserId", "superuser");
        config.put("mgnlUserPSWD", "superuser");
        if (extraParams != null) {
            config.putAll(extraParams);
        }
        return config;
     }


    private static JsonLoadResultReader<List<FileModel>> createConfiguredReader() {
        // TODO: generate model types or use different kind of deserialization !!!

        JsonLoadResultReader<List<FileModel>> jsonReader = new JsonLoadResultReader<List<FileModel>>(LIST_OF_FILES_MODEL_TYPE) {
            protected ListLoadResult<ModelData> newLoadResult(Object loadConfig, List<ModelData> models) {
                throw new UnsupportedOperationException("Do not call me!");
            }

            protected Object createReturnData(Object loadConfig, List<ModelData> records, int totalCount) {
                ArrayList<FileModel> resultList = new ArrayList<FileModel>();
                for (ModelData record : records) {
                    FileModel model = new FileModel(record.getProperties());
                    resultList.add(model);
                }
                return resultList;
            }
        };
        return jsonReader;
    }

    public static void createContent(String treeName, final FileModel folder, final TreeStore<FileModel> store) {
        final String path = folder == null ? "" : folder.getPath();
        final RequestBuilder requestBuilder = new RequestBuilder(POST, "/.magnolia/rest/" + treeName + path + "/create");
        requestBuilder.setHeader("Accept", "application/json");
        RequestCallback callback = new RequestCallback() {

            public void onResponseReceived(Request request, Response response) {
                String responseText = response.getText();
                System.out.println("got response: " + responseText);
                System.out.println("Begin parsing response in an object");
                // TODO: remove this after service return created child on create
                responseText = "{children:[{\"name\":\"untitled"+store.getChildCount()+"\",\"path\":\""+path+"/untitled"+store.getChildCount()+"\",\"status\":\"modified\",\"template\":\"\",\"title\":\"\",\"availableTemplates\":[],\"uuid\":\"0c7fa58a-90fa-4392-86b4-7f9b95e0352b"+store.getChildCount()+"\",\"hasChildren\":false}]}";
                FileModel child = jsonReader.read(null, responseText).get(0);
                System.out.println("Child:" + child);
                if (folder != null) {
                    if (store.getChildCount(folder) == 0) {
                        //w/o child yet ... need to convert into a folder first
                        folder.setHasChildren(true);
                        store.update(folder);
                    }
                    store.insert(folder, child, store.getChildCount(folder), false);
                } else {
                    store.insert(child, store.getChildCount(), false);
                }
            }

            public void onError(Request request, Throwable exception) {
                exception.printStackTrace();
                System.out.println("got error: " + exception.getMessage());
            }

            @Override
            public String toString() {
                return super.toString();
            }
        };
        requestBuilder.setCallback(callback);

        try {
            requestBuilder.send();
        } catch (RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
