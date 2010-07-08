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
package info.magnolia.module.genuinecentral.gwt.client.data;

import info.magnolia.module.genuinecentral.gwt.client.FileModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.DataField;
import com.extjs.gxt.ui.client.data.JsonLoadResultReader;
import com.extjs.gxt.ui.client.data.JsonReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * A {@link JsonReader} implementation which handle nested arrays (only one level deep).
 * TODO This sucks, find a better solution.
 * @author fgrilli
 * @param <D> the <code>ListLoadResult</code> type being returned by the reader
 *
 */
public class MgnlJsonLoadResultReader<D> extends JsonLoadResultReader<D> {

    private ModelType modelType;

    public MgnlJsonLoadResultReader(ModelType modelType) {
        super(modelType);
        this.modelType = modelType;
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

    @SuppressWarnings("unchecked")
    public D read(Object loadConfig, Object data) {
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
                    String nestedArrayName = map;
                    JSONArray nested = (JSONArray) value;
                    int nestedSize = nested.size();
                    List<ModelData> nestedModels = new ArrayList<ModelData>();
                    for (int n = 0; n < nestedSize; n++) {
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
                        model.set(nestedArrayName, nestedModels);
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
        return (D) createReturnData(loadConfig, models, totalCount);
    }

    private void setValue(ModelData model, JSONValue value, DataField field) {
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
                        DateTimeFormat format = DateTimeFormat.getFormat(field
                                .getFormat());
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
}
