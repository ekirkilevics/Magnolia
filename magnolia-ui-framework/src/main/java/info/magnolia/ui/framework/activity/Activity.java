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
package info.magnolia.ui.framework.activity;

import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.ViewPort;


/**
 * Implemented by objects that control a piece of user interface, with a life cycle managed by an
 * {@link ActivityManager}, in response to
 * {@link info.magnolia.ui.framework.place.PlaceChangeEvent} events as the user navigates through
 * the app.
 *
 * Inspired by {@link com.google.gwt.activity.shared.Activity}.
 */
public interface Activity {

    /**
     * Called when the Activity should ready its widget for the user. When the widget is ready it
     * should present it by calling {@link ViewPort#setView(com.vaadin.ui.Component)} on
     * the display.
     * <p>
     * Any eventHandlers attached to the provided event bus will be de-registered when the activity is
     * stopped.
     */
    void start(ViewPort viewPort, EventBus eventBus);

    /**
     * Called when the user is trying to navigate away from this activity.
     *
     * @return A message to display to the user, e.g. to warn of unsaved work, or null to say
     * nothing
     */
    String mayStop();

    /**
     * Called when the Activity's widget has been removed from view. All event eventHandlers it
     * registered will have been removed before this method is called.
     */
    void onStop();
}
