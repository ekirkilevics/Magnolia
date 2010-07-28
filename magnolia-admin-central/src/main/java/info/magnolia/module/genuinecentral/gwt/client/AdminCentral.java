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

import info.magnolia.module.genuinecentral.gwt.client.presenter.AdminCentralPresenter;
import info.magnolia.module.genuinecentral.gwt.client.presenter.DialogPresenter;
import info.magnolia.module.genuinecentral.gwt.client.presenter.Presenter;
import info.magnolia.module.genuinecentral.gwt.client.tree.DefaultTreeConfig;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;


/**
 * @author had
 */
public class AdminCentral implements ValueChangeHandler<String> {

    public static final String ADMIN_CENTRAL = "adminCentral";
    public static final String MODEL = "model";
    public static final String DEFAULT_TREE_CONFIG = "defaultTreeConfig";

    private HasWidgets container;
    private final MagnoliaService service;
    private final HandlerManager eventBus;


    public AdminCentral(MagnoliaService aService, HandlerManager eventBus) {
        this.service = aService;
        this.eventBus = eventBus;
        bind();
    }

    public void init(final HasWidgets container) {

        this.container = container;

        System.out.println("Initializing Admin central");

        // remove this
        createDefaultTreeConfig();

        // use history handler to initialize the views
        if ("".equals(History.getToken())) {
            History.newItem("central");
        } else {
            History.fireCurrentHistoryState();
        }
    }

    private void bind() {
        History.addValueChangeHandler(this);
    }

    public void onValueChange(ValueChangeEvent<String> event) {
        String token = event.getValue();

        if (token != null) {
            Presenter presenter = null;

            // main view dispatcher
            if (token.equals("central")) {
                presenter = new AdminCentralPresenter(service, eventBus, new AdminCentralView());
            } else if (token.equals("dialog")) {
                presenter = new DialogPresenter(service, eventBus, new DialogView());
            }

            if (presenter != null) {
                presenter.go(container);
            }
        }
    }

    private void createDefaultTreeConfig() {
        Registry.register(DEFAULT_TREE_CONFIG, new DefaultTreeConfig());
    }

}
