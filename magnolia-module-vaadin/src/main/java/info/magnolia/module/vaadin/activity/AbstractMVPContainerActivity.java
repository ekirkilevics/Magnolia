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

import info.magnolia.module.vaadin.event.EventBus;
import info.magnolia.module.vaadin.place.CompositePlace;
import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceChangeEvent;
import info.magnolia.module.vaadin.place.PlaceChangeListener;
import info.magnolia.module.vaadin.place.PlaceController;
import info.magnolia.module.vaadin.region.Region;


/**
 * Builds an inner MVP container having its own {@link ActivityManager}, {@link PlaceController} and
 * {@link EventBus}. {@link PlaceChangeEvent} events are fired to the outer {@link PlaceController}
 * and visa versa.
 */
public abstract class AbstractMVPContainerActivity {

    private final class InnerPlaceChangeListener implements PlaceChangeListener {

        public void onPlaceChange(PlaceChangeEvent event) {
            CompositePlace outerPlace = (CompositePlace) outerPlaceController.getWhere();
            outerPlace.setSubPlace(regionId, event.getNewPlace());
            outerPlaceController.goTo(outerPlace);
        }
    }

    private final class OuterPlaceChangeListerner implements PlaceChangeListener {

        public void onPlaceChange(PlaceChangeEvent event) {
            Place placeToGo = event.getNewPlace();
            if (placeToGo instanceof CompositePlace) {
                placeToGo = ((CompositePlace) placeToGo).getSubPlace(regionId);
                innerPlaceController.goTo(placeToGo);
            }
        }
    }

    private String regionId;

    private PlaceController outerPlaceController;

    private EventBus innerEventBus;

    private PlaceController innerPlaceController;

    public AbstractMVPContainerActivity(PlaceController outerPlaceController) {
        this.outerPlaceController = outerPlaceController;
    }

    public void start(Region region, EventBus outerEventBus) {
        this.regionId = region.getId();

        outerEventBus.addListener(new OuterPlaceChangeListerner());
        innerEventBus = new EventBus();
        innerPlaceController = new PlaceController(innerEventBus);

        innerEventBus.addListener(new InnerPlaceChangeListener());

        // build the container
        onStart(region, innerEventBus);

        // now navigate in the container to the correct place
        innerPlaceController.goTo(((CompositePlace) outerPlaceController.getWhere()).getSubPlace(regionId));
    }

    protected abstract void onStart(Region region, EventBus innerEventBus);

}
