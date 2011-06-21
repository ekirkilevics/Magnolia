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
package info.magnolia.ui.framework.place;

import info.magnolia.ui.framework.event.Event;
import info.magnolia.ui.framework.event.EventHandler;


/**
 * Event thrown when the user may go to a new place in the app, or tries to leave it. Receivers can
 * call {@link #setWarning(String)} request that the user be prompted to confirm the change.
 */
public class PlaceChangeRequestEvent implements Event<PlaceChangeRequestEvent.Handler> {

    /**
     * Listens to {@link PlaceChangeRequestEvent}s.
     */
    public interface Handler extends EventHandler{

        void onPlaceChangeRequest(PlaceChangeRequestEvent event);

    }

    private String warning;

    private final Place newPlace;

    public PlaceChangeRequestEvent(Place newPlace) {
        this.newPlace = newPlace;
    }

    /**
     * Returns the place we may navigate to, or null on window close.
     */
    public Place getNewPlace() {
        return newPlace;
    }

    /**
     * Returns the warning message to show the user before allowing the place change, or null if
     * none has been set.
     */
    public String getWarning() {
        return warning;
    }

    /**
     * Set a message to warn the user that it might be unwise to navigate away from the current
     * place, i.e. due to unsaved changes. If the user clicks okay to that message, navigation will
     * proceed to the requested place.
     * <p>
     * Calling with a null warning is the same as not calling the method at all -- the user will not
     * be prompted.
     * <p>
     * Only the first non-null call to setWarning has any effect. That is, once the warning message
     * has been set it cannot be cleared.
     */
    public void setWarning(String warning) {
        if (this.warning == null) {
            this.warning = warning;
        }
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onPlaceChangeRequest(this);
    }

}
