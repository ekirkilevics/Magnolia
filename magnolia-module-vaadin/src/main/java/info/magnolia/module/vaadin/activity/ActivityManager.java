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

import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;
import info.magnolia.module.vaadin.event.ResettableEventBus;
import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceChangeEvent;
import info.magnolia.module.vaadin.place.PlaceChangeRequestEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages {@link Activity} objects that should be kicked off in response to
 * {@link PlaceChangeEvent} events. Each activity provides a widget to be shown when it's ready to run.
 *
 * Inspired by {@link com.google.gwt.activity.shared.ActivityManager}.
 */
public class ActivityManager implements PlaceChangeEvent.Handler, PlaceChangeRequestEvent.Handler {

    private static Logger log = LoggerFactory.getLogger(ActivityManager.class);

    private static final Activity NULL_ACTIVITY = new AbstractActivity() {
        public void start(HasComponent display, EventBus eventBus) {
            display.setComponent(null);
        }
        public String toString() {
            return "NULL_ACTIVITY";
        }
    };

    private final ActivityMapper mapper;
    private final ResettableEventBus isolatedEventBus;

    private Activity currentActivity = NULL_ACTIVITY;

    private HasComponent display;

    public ActivityManager(ActivityMapper mapper, EventBus eventBus) {
        this.mapper = mapper;
        this.isolatedEventBus = new ResettableEventBus(eventBus);

        eventBus.addHandler(PlaceChangeEvent.class, this);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        Place newPlace = event.getNewPlace();
        Activity nextActivity = mapper.getActivity(newPlace);


        if (currentActivity.equals(nextActivity)) {
            return;
        }

        display.setComponent(null);
        isolatedEventBus.removeHandlers();

        currentActivity.onStop();

        if(nextActivity == null){
            nextActivity = NULL_ACTIVITY;
        }

        currentActivity = nextActivity;

        log.debug("starting activity: {}", currentActivity);

        currentActivity.start(display, isolatedEventBus);

   }

    public void setDisplay(HasComponent display){
        this.display = display;
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
