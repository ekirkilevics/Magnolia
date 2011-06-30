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
package info.magnolia.ui.admincentral;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.model.builder.FactoryBase;

/**
 * Maps the main places to main activities.
 */
@Singleton
public class MainActivityMapperImpl extends FactoryBase<Place, Activity> implements MainActivityMapper {

    /**
     * Defines a mapping from a place to an activity.
     *
     * @param <P> type of the place
     * @param <A> type of the activity
     */
    public static class PlaceToActivityMapping<P, A> {

        private Class<P> place;

        private Class<A> activity;

        public void setPlace(Class<P> place) {
            this.place = place;
        }

        public Class<P> getPlace() {
            return place;
        }

        public void setActivity(Class<A> activity) {
            this.activity = activity;
        }

        public Class<A> getActivity() {
            return activity;
        }
    }

    private List<PlaceToActivityMapping<Place, Activity>> placeToActivityMappings = new ArrayList<PlaceToActivityMapping<Place, Activity>>();

    @Inject
    public MainActivityMapperImpl(ComponentProvider componentProvider) {
        super(componentProvider);
    }

    public List<PlaceToActivityMapping<Place, Activity>> getPlaceToActivityMappings() {
        return this.placeToActivityMappings;
    }

    public void setPlaceToActivityMappings(List<PlaceToActivityMapping<Place, Activity>> placeToActivityMappings) {
        this.placeToActivityMappings = placeToActivityMappings;
        for (PlaceToActivityMapping<Place, Activity> placeToActivityMapping : placeToActivityMappings) {
            addPlaceToActivityMapping(placeToActivityMapping);
        }
    }

    public void addPlaceToActivityMapping(PlaceToActivityMapping<Place, Activity> mapping) {
        addMapping(mapping.getPlace(), mapping.getActivity());
    }

    @Override
    public Activity getActivity(final Place place) {
        return create(place);
    }
}
