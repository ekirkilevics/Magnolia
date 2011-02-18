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
package info.magnolia.module.vaadin.place;

import info.magnolia.module.vaadin.event.EventBus;

import org.vaadin.dialogs.ConfirmDialog;

/**
 * In charge of the user's location in the app.
 */
public class PlaceController {

    private final EventBus eventBus;
    private Delegate delegate;

    private Place where = Place.NOWHERE;

    /**
     * Optional delegate in charge of Window related events. Provides nice
     * isolation for unit testing, and allows customization of confirmation
     * handling.
     */
    public interface Delegate {
      //HandlerRegistration addWindowClosingHandler(ClosingHandler handler);
      void confirm(String message, ConfirmDialog.Listener listener);
    }

    public PlaceController(final EventBus eventBus, Delegate delegate) {
        this.eventBus = eventBus;
        this.delegate = delegate;
    }

    /**
     * Returns the current place.
     */
    public Place getWhere() {
      return where;
    }

    /**
     * Request a change to a new place.
     */
    public void goTo(final Place newPlace) {
      if (getWhere().equals(newPlace)) {
        return;
      }
      PlaceChangeRequestEvent willChange = new PlaceChangeRequestEvent(newPlace);
      eventBus.fire(willChange);
      if(willChange.getWarning() != null){
          delegate.confirm(willChange.getWarning(), new ConfirmDialog.Listener() {
              public void onClose(ConfirmDialog dialog) {
                  if(dialog.isConfirmed()){
                      goToWithoutChecks(newPlace);
                  }
               }
           });
      }
      else{
          goToWithoutChecks(newPlace);
      }

    }

    protected void goToWithoutChecks(final Place newPlace) {
        this.where = newPlace;
        eventBus.fire(new PlaceChangeEvent(newPlace));
    }
}
