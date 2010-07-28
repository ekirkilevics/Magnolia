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
package info.magnolia.module.genuinecentral.gwt.client.presenter;

import info.magnolia.module.genuinecentral.gwt.client.MagnoliaService;
import info.magnolia.module.genuinecentral.gwt.client.ServerConnector;
import info.magnolia.module.genuinecentral.gwt.client.models.DialogModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.data.Loader;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class DialogPresenter implements Presenter {

    private final MagnoliaService service;
    private final HandlerManager eventBus;
    private final Display display;

    public interface Display {
        void setData(Loader<List<DialogModel>> store);

        Widget asWidget();
    }

    public DialogPresenter(MagnoliaService aService, HandlerManager eventBus, Display view) {
        this.service = aService;
        this.eventBus = eventBus;
        this.display = view;
    }

    public void go(final HasWidgets container) {
        bind();
        container.clear();
        container.add(display.asWidget());
    }

    public void bind() {
        // TODO Auto-generated method stub

    }

    public void showDialog() {
        Map<String,String> params = new HashMap<String, String>();
        params.put("mgnlPath", "/howTo-freemarker");
        //params.put("mgnlNode", "");
        params.put("mgnlNodeCollection", "main");
        params.put("mgnlRepository", "website");

        display.setData(ServerConnector.getDialogLoader("controlsShowRoom", params));

    }
}
