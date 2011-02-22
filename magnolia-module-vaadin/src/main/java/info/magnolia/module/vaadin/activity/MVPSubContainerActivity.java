/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.vaadin.activity;

import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;
import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceChangeEvent;
import info.magnolia.module.vaadin.place.PlaceChangeListener;
import info.magnolia.module.vaadin.place.PlaceChangeRequestEvent;
import info.magnolia.module.vaadin.place.PlaceChangeRequestListener;
import info.magnolia.module.vaadin.place.PlaceController;
import info.magnolia.module.vaadin.place.PlaceHistoryHandler;
import info.magnolia.module.vaadin.place.PlaceHistoryMapperImpl;
import info.magnolia.module.vaadin.shell.Shell;
import info.magnolia.module.vaadin.shell.SubShell;


/**
 * Builds an inner MVP container having its own {@link ActivityManager}, {@link PlaceController} and
 * {@link EventBus}. {@link PlaceChangeEvent} events are fired to the outer {@link PlaceController}
 * and visa versa.
 */
public abstract class MVPSubContainerActivity extends AbstractActivity {

    private String id;

    private EventBus innerEventBus;

    private PlaceController innerPlaceController;

    private Shell shell;

    public MVPSubContainerActivity(String id, Shell shell) {
        this.id = id;
        this.shell = shell;
    }

    public void start(HasComponent display, EventBus outerEventBus) {

        Shell subShell = new SubShell(id, shell);

        innerEventBus = new EventBus();
        innerPlaceController = new PlaceController(innerEventBus, shell);

        //FIXME, we should not have to register all the events manually
        innerEventBus.register(PlaceChangeListener.class, PlaceChangeEvent.class);
        innerEventBus.register(PlaceChangeRequestListener.class, PlaceChangeRequestEvent.class);

        // build the container
        onStart(display, innerEventBus);

        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(new PlaceHistoryMapperImpl(getSupportedPlaces()), subShell);
        historyHandler.register(innerPlaceController, innerEventBus, getDefaultPlace());
        historyHandler.handleCurrentHistory();
    }

    protected abstract Class< ? extends Place>[] getSupportedPlaces();

    protected abstract Place getDefaultPlace();

    protected abstract void onStart(HasComponent display, EventBus innerEventBus);

    public String getId() {
        return id;
    }


    protected EventBus getInnerEventBus() {
        return innerEventBus;
    }


    protected PlaceController getInnerPlaceController() {
        return innerPlaceController;
    }

}
