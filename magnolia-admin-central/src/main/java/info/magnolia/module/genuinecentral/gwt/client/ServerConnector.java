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
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public class ServerConnector {

    public static TreeLoader<FileModel> getTreeLoader(String treeName, String rootPath, Map<String, String> additionalParams) {
        final RequestBuilder requestBuilder = new RequestBuilder(GET, "/.magnolia/rest/" + treeName + rootPath);
        requestBuilder.setHeader("Accept", "application/json");

        // data proxy
        RestfullHttpProxy<List<FileModel>> proxy = new RestfullHttpProxy<List<FileModel>>(requestBuilder);

        JsonLoadResultReader<List<FileModel>> jsonReader = getConfiguredReader();

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


    private static JsonLoadResultReader<List<FileModel>> getConfiguredReader() {
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

    public static void createContent(String treeName, String path) {
        final RequestBuilder requestBuilder = new RequestBuilder(POST, "/.magnolia/rest/" + treeName + path + "/create");
        requestBuilder.setHeader("Accept", "application/json");
        requestBuilder.setCallback(new RequestCallback() {

            public void onResponseReceived(Request request, Response response) {
                System.out.println("got response: " + response.getText());
            }

            public void onError(Request request, Throwable exception) {
                exception.printStackTrace();
                System.out.println("got error: " + exception.getMessage());
            }
        });

        try {
            requestBuilder.send();
        } catch (RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
