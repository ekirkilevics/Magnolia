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
package info.magnolia.module.genuinecentral.data;


import info.magnolia.module.genuinecentral.gwt.client.DialogRegistryClient;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class DataSourceProvider {

//    private Map<String, DataSource> cache = new HashMap<String, DataSource>();

    private DialogRegistryClient dialogRegistry;

    public DataSourceProvider(DialogRegistryClient dialogRegistry) {
        this.dialogRegistry = dialogRegistry;
    }


//    public void getDataSource(String type, final AsyncCallback<DataSource> callback){
//        if(cache.containsKey(type)){
//            callback.onSuccess(cache.get(type));
//        }
//        else{
//            final DataSource dataSource = new DataSource();
//            dataSource.setClientOnly(true);
//            cache.put(type, dataSource);
//
//            dialogRegistry.getDialog(type, new AsyncCallback<Dialog>(){
//                public void onSuccess(Dialog dialog) {
//                    DataSourceTextField uuid = new DataSourceTextField("uuid", "UUID");
//                    uuid.setPrimaryKey(true);
//                    dataSource.addField(uuid);
//
//                    for (Control tab : dialog.getControls()) {
//                        for(Control control : ((TabControl)tab).getControls()){
//                            dataSource.addField(new DataSourceTextField (control.getName(), control.getLabel()));
//                        }
//                    }
//
//                    callback.onSuccess(dataSource);
//                }
//
//                public void onFailure(Throwable caught) {
//                    callback.onFailure(caught);
//                }
//            });
//        }
//    }

}
