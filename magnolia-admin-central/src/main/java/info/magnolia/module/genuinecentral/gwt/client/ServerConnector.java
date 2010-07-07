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
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.DataField;
import com.extjs.gxt.ui.client.data.JsonLoadResultReader;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.data.BaseLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class ServerConnector {

    private final static ModelType FILE_MODEL_TYPE = new ModelType();
    private static final ModelType LIST_OF_FILES_MODEL_TYPE = new ModelType();
    private static final ModelType DIALOG_MODEL_TYPE = new ModelType();

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

        DIALOG_MODEL_TYPE.setRoot("subs");
        DIALOG_MODEL_TYPE.addField("name");
        DIALOG_MODEL_TYPE.addField("value");
        DIALOG_MODEL_TYPE.addField("type");
        DIALOG_MODEL_TYPE.addField("label");
        DIALOG_MODEL_TYPE.addField("subs");

    }

    private static final JsonLoadResultReader<List<FileModel>> jsonTreeReader = createConfiguredReader(LIST_OF_FILES_MODEL_TYPE);
    private static final JsonLoadResultReader<List<FileModel>> jsonDialogReader = createConfiguredReader(DIALOG_MODEL_TYPE);

    public static TreeLoader<FileModel> getTreeLoader(String treeName, String rootPath, Map<String, String> additionalParams) {
        final RequestBuilder requestBuilder = new RequestBuilder(GET, "/.magnolia/rest/" + treeName + rootPath);
        requestBuilder.setHeader("Accept", "application/json");

        // data proxy
        RestfullHttpProxy<List<FileModel>> proxy = new RestfullHttpProxy<List<FileModel>>(requestBuilder);
        return getConfiguredTreeLoader(proxy, jsonTreeReader, additionalParams);
    }


    public static Loader<List<FileModel>> getDialogLoader(final String dialogName, final Map<String, String> extraParams){
        final RequestBuilder requestBuilder = new RequestBuilder(GET, "/.magnolia/rest/dialogs/" + dialogName);
        requestBuilder.setHeader("Accept", "application/json");
        // data proxy
        RestfullHttpProxy<List<FileModel>> proxy = new RestfullHttpProxy<List<FileModel>>(requestBuilder);

        return new BaseLoader<List<FileModel>>(proxy, jsonDialogReader) {
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
        };
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


    private static <T extends BaseModelData> JsonLoadResultReader<List<T>> createConfiguredReader(final ModelType modelType) {
        // TODO: generate model types or use different kind of deserialization !!!
        JsonLoadResultReader<List<T>> jsonReader = new JsonLoadResultReader<List<T>>(modelType) {
            private void setValue(ModelData model, JSONValue value, DataField field){
                String name = field.getName();
                Class type = field.getType();
                if (value.isBoolean() != null) {
                    model.set(name, value.isBoolean().booleanValue());
                  } else if (value.isNumber() != null) {
                    if (type != null) {
                      Double d = value.isNumber().doubleValue();
                      if (type.equals(Integer.class)) {
                        model.set(name, d.intValue());
                      } else if (type.equals(Long.class)) {
                        model.set(name, d.longValue());
                      } else if (type.equals(Float.class)) {
                        model.set(name, d.floatValue());
                      } else {
                        model.set(name, d);
                      }
                    } else {
                      model.set(name, value.isNumber().doubleValue());
                    }
                  } else if (value.isObject() != null) {
                    // nothing
                  } else if (value.isString() != null) {
                    String s = value.isString().stringValue();
                    if (type != null) {
                      if (type.equals(Date.class)) {
                        if ("timestamp".equals(field.getFormat())) {
                          Date d = new Date(Long.parseLong(s) * 1000);
                          model.set(name, d);
                        } else {
                          DateTimeFormat format = DateTimeFormat.getFormat(field.getFormat());
                          Date d = format.parse(s);
                          model.set(name, d);
                        }
                      }
                    } else {
                      model.set(name, s);
                    }
                  } else if (value.isNull() != null) {
                    model.set(name, null);
                  }
            }
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
                public java.util.List<T> read(Object loadConfig, Object data) {
                    JSONObject jsonRoot = null;
                    if (data instanceof JavaScriptObject) {
                      jsonRoot = new JSONObject((JavaScriptObject) data);
                    } else {
                      jsonRoot = (JSONObject) JSONParser.parse((String) data);
                    }
                    JSONArray root = (JSONArray) jsonRoot.get(modelType.getRoot());
                    int size = root.size();
                    ArrayList<ModelData> models = new ArrayList<ModelData>();
                    for (int i = 0; i < size; i++) {
                      JSONObject obj = (JSONObject) root.get(i);
                      ModelData model = newModelInstance();
                      for (int j = 0; j < modelType.getFieldCount(); j++) {
                        DataField field = modelType.getField(j);
                        String map = field.getMap() != null ? field.getMap() : field.getName();
                        JSONValue value = obj.get(map);
                        if (value == null) continue;
                        if (value.isArray() != null) {
                            JSONArray nested = (JSONArray) value;
                            int nestedSize = nested.size();
                            List<ModelData> nestedModels = new ArrayList<ModelData>();
                            for(int n=0; n < nestedSize; n++){
                                JSONObject nestedObj = (JSONObject) nested.get(n);
                                ModelData nestedModel = newModelInstance();
                                for (int k = 0; k < modelType.getFieldCount(); k++) {
                                    field = modelType.getField(k);
                                    map = field.getMap() != null ? field.getMap() : field.getName();
                                    value = nestedObj.get(map);
                                    if (value == null) continue;
                                    setValue(nestedModel, value, field);
                                }
                                nestedModels.add(nestedModel);
                                model.set("subs", nestedModels);
                            }
                        } else {
                            setValue(model, value, field);
                        }
                      }
                      models.add(model);
                    }
                    int totalCount = models.size();
                    if (modelType.getTotalName() != null) {
                      totalCount = getTotalCount(jsonRoot);
                    }
                    return (List<T>) createReturnData(loadConfig, models, totalCount);
                };
            } ;

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
                FileModel child = jsonTreeReader.read(null, responseText).get(0);
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
