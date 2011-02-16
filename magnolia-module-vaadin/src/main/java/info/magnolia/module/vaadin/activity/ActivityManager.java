/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.vaadin.activity;

import info.magnolia.module.vaadin.event.EventBus;
import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceChangeEvent;
import info.magnolia.module.vaadin.place.PlaceChangeListener;
import info.magnolia.module.vaadin.place.PlaceChangeRequestEvent;
import info.magnolia.module.vaadin.place.PlaceChangeRequestListener;
import info.magnolia.module.vaadin.region.Region;

/**
 * Manages {@link Activity} objects that should be kicked off in response to
 * {@link PlaceChangeEvent} events. Each activity provides a widget to be shown when it's ready to run.
 *
 * Inspired by {@link com.google.gwt.activity.shared.ActivityManager}.
 */
public class ActivityManager implements PlaceChangeListener, PlaceChangeRequestListener {

    private static final Activity NULL_ACTIVITY = new AbstractActivity() {
        public void start(Region region, EventBus eventBus) {
            region.setComponent(null);
        }
    };

    private final ActivityMapper mapper;
    private final EventBus eventBus;
    private Region region;
    private Activity currentActivity = NULL_ACTIVITY;

    public ActivityManager(ActivityMapper mapper, EventBus eventBus) {
        this.mapper = mapper;
        this.eventBus = eventBus;

        eventBus.addListener(this);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        Place newPlace = event.getNewPlace();
        Activity nextActivity = mapper.getActivity(newPlace);


        if (currentActivity.equals(nextActivity)) {
            return;
        }

        if(nextActivity == null){
            nextActivity = NULL_ACTIVITY;
        }

        currentActivity = nextActivity;

        currentActivity.start(region, eventBus);

   }

    public void setDisplay(Region region){
        this.region = region;
    }

    public void onPlaceChangeRequest(final PlaceChangeRequestEvent event) {

        if (!currentActivity.equals(NULL_ACTIVITY)) {
            final String message = currentActivity.mayStop();
            if(message != null) {
                event.setWarning(message);
            }
        }
    }
}
