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

import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.ui.Widget;

import info.magnolia.module.genuinecentral.gwt.client.models.DialogModel;
import info.magnolia.module.genuinecentral.gwt.client.models.FileModel;
import info.magnolia.module.genuinecentral.gwt.client.presenter.DialogPresenter.Display;

public class DialogView implements Display {

    private Dialog widget;

    public DialogView() {
        this.widget = createDialog();
        widget.show();
        widget.center();

    }

    private Dialog createDialog() {
        final Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setWidth(600);
        dialog.setHeight(500);

        dialog.setHeading("I am dialog");
        dialog.addButton(new Button("Cancel"));
        dialog.addButton(new Button("Submit"));
        return dialog;
      }

    public Widget asWidget() {
        return this.widget;
    }

    public void setData(Loader<List<DialogModel>> store) {
        store.load();
        store.addLoadListener(new LoadListener(){
            @Override
            public void loaderLoad(LoadEvent le) {
                List<FileModel> data = le.getData();
                FormData formData = new FormData("80%");

                TabPanel tabs = new TabPanel();
                tabs.setTabScroll(true);
                for(FileModel model: data){
                    String type = model.get("type");
                    if("tab".equals(type)){
                        TabItem tab = new TabItem();
                        tab.setStyleAttribute("padding", "10px");
                        tab.setText((String)model.get("label", "no label"));
                        tab.setLayout(new FormLayout());
                        tab.setAutoHeight(true);
                        List<BaseModelData> controls = model.get("subs");
                        if(controls == null) continue;
                        for(BaseModelData control: controls) {
                            type = control.get("type");
                            if("edit".equals(type)){
                                TextField<String> textField = new TextField<String>();
                                textField.setFieldLabel((String)control.get("label", "no label"));
                                textField.setValue((String)control.get("value", ""));
                                textField.setAllowBlank(!(Boolean)control.get("required"));
                                String width = control.get("width");
                                if(width != null && width.length() > 0){
                                    //FIXME seems to have no effect at all
                                    textField.setWidth(width);
                                }
                                String description = control.get("description");
                                if(description != null && description.length() > 0){
                                    textField.setToolTip(description);
                                }
                                tab.add(textField, formData);
                            } else if("date".equals(type)){
                                DateField date = new DateField();
                                date.setFieldLabel((String)control.get("label", "no label"));
                                long timestamp = control.get("value") != null ? Long.parseLong(""+control.get("value")) : new Date().getTime();
                                date.setValue(new Date(timestamp));
                                tab.add(date, formData);
                            }
                        }
                        tabs.add(tab);
                    }
                }
                widget.add(tabs,formData);
            }
        });
    }

}
