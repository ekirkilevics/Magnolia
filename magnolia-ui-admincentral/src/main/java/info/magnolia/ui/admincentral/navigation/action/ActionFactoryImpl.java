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
package info.magnolia.ui.admincentral.navigation.action;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.pico.PicoComponentProvider;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionFactory;
import info.magnolia.ui.model.action.PlaceChangeAction;
import info.magnolia.ui.model.action.PlaceChangeActionDefinition;

/**
 * A factory for {@link Action}s.
 * @author fgrilli
 *
 */
public class ActionFactoryImpl implements ActionFactory {

    private PlaceController placeController;
    private ComponentProvider componentProvider;

    /**
     * TODO remove me: just a quick workaround for M3 Sprint II release.
     */
    public static final class NowhereActionDefinition implements PlaceChangeActionDefinition {

        public Place getPlace() {
            return Place.NOWHERE;
        }

    }

    public ActionFactoryImpl(ComponentProvider componentProvider, PlaceController placeController) {
        this.componentProvider = componentProvider;
        this.placeController = placeController;
    }

    public Action createAction(final ActionDefinition definition){
        if(definition == null){
            //TODO this will actually have to throw an exception but as a quick workaround for the M3 Sprint II release we use a NowhereActionDefinition to avoid annoying IAE.
           //throw new IllegalArgumentException("action definition cannot be null");
            return new PlaceChangeAction((PlaceChangeActionDefinition) new NowhereActionDefinition(), placeController);
        }

        if(definition instanceof PlaceChangeActionDefinition) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("definition", definition);
            return ((PicoComponentProvider)componentProvider).newInstance(PlaceChangeAction.class, map);
        }

        return null;
    }
}
