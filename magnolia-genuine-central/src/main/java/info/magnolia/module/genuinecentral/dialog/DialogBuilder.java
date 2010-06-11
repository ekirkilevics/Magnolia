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
package info.magnolia.module.genuinecentral.dialog;



import info.magnolia.module.genuinecentral.data.MgnlContent;
import info.magnolia.module.genuinecentral.gwt.client.DialogRegistryClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DialogBuilder {
//    private DataSourceProvider dataSourceProvider;
/*
    private DialogRegistryClient dialogRegistry;

    public DialogBuilder(DialogRegistryClient dialogRegistry) {
 //       this.dataSourceProvider = dataSourceProvider;
        this.dialogRegistry = dialogRegistry;
    }


    public void createDialog(final MgnlContent content, final AsyncCallback<Window> callback) {
        final String type = content.getType();
        dialogRegistry.getDialog(type, new AsyncCallback<Dialog>(){
            public void onSuccess(final Dialog dialog) {
//                dataSourceProvider.getDataSource(type, new AsyncCallback<DataSource>(){
//                    public void onSuccess(DataSource dataSource) {
//                        dataSource.addData(new Record(content));
                        Window dialogWindow = createDialog(content, dialog);
                        callback.onSuccess(dialogWindow);
//                    }
//                    public void onFailure(Throwable caught) {
//                        throw new RuntimeException(caught);
//                    }
//                });
            }
            public void onFailure(Throwable caught) {
                throw new RuntimeException(caught);
            }
        });
    }


    private Window createDialog(final MgnlContent content, Dialog dialog) {
        final Window winModal = new Window();
        winModal.setWidth(400);
        winModal.setHeight(300);
        winModal.setTitle("Modal Window");
        winModal.setShowMinimizeButton(false);
        winModal.setIsModal(true);
        winModal.centerInPage();
        winModal.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                winModal.destroy();
            }
        });

        // tabset
        final TabSet topTabSet = new TabSet();
        topTabSet.setTabBarPosition(Side.TOP);
        topTabSet.setWidth(400);
        topTabSet.setHeight(200);

        createTabs(topTabSet, dialog.getControls(), content);

        IButton save = new IButton("Save");
        save.setWidth(120);

        save.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                List<DynamicForm> forms = new ArrayList<DynamicForm>();
                for (Tab tab : topTabSet.getTabs()) {
                    DynamicForm form = (DynamicForm)tab.getPane();
                    forms.add(form);
                }

                for (DynamicForm form : forms) {
                    if(!form.validate()){
                        return;
                    }
                }

                for (DynamicForm form : forms) {
                    GWT.log(""+form.getValues().size(),null);
                    // this does not work as soon we have multiple forms as each from overwrites the values saved by the predecessor
                    // form.saveData();

                    for (FormItem field : form.getFields()) {
                        JSOHelper.setAttribute(content, field.getName(), field.getValue());
                    }
                }
                winModal.destroy();
            }

        });

        winModal.addItem(topTabSet);
        winModal.addItem(save);
        return winModal;
    }


    public void createTabs(final TabSet topTabSet, List<Control> controls, MgnlContent content) {
        for (Iterator iterator = controls.iterator(); iterator.hasNext();) {
            TabControl tabControl = (TabControl) iterator.next();

            Tab tab = new Tab(tabControl.getLabel());
            topTabSet.addTab(tab);
            DynamicForm form = createDynamicForm();
            //form.setDataSource(dataSource);

            List<FormItem> tabcontrols = new ArrayList<FormItem>();

            for (Control controlinTab : tabControl.getControls()) {
                tabcontrols.add(createFormField(controlinTab, content));
            }
            FormItem[] formItems = new FormItem[tabcontrols.size()];
            System.arraycopy(tabcontrols.toArray(), 0, formItems, 0, tabcontrols.size());

//            form.fetchData(new Criteria("uuid", uuid));

            tab.setPane(form);
            form.setFields(formItems);

            //form.selectRecord(1);

            //form.setUseAllDataSourceFields(true);
            //form.setAutoFetchData(true);
            //form.show();
        }
    }

    protected DynamicForm createDynamicForm() {
        DynamicForm form = new DynamicForm();
        // form.setAutoFetchData(true);
        form.setHeight100();
        form.setWidth100();
        form.setPadding(5);
        form.setLayoutAlign(VerticalAlignment.BOTTOM);
        return form;
    }

    protected FormItem createFormField(Control control, MgnlContent content) {
        FormItem item = null;
        if(control.getType().equals("edit") || control.getType().equals("password")) {
            item = new TextItem();
            if(control.getType().equals("password")) {
                item.setType("password");
                item.setRequired(true);
            }
        } else if(control.getType().equals("date")) {
            item = new DateItem();
        } else {
            throw new UnsupportedOperationException(control.getType());
        }

        item.setName(control.getName());
        item.setTitle(control.getLabel());
        String value = JSOHelper.getAttribute(content, control.getName());
        item.setValue(value);

        return item;

    }*/
}
