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
package info.magnolia.module.wcm;

import info.magnolia.module.wcm.place.PageEditorPlace;
import info.magnolia.ui.admincentral.MainActivityMapper;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.place.PlaceHistoryHandler;
import info.magnolia.ui.framework.place.PlaceHistoryMapper;
import info.magnolia.ui.framework.place.PlaceHistoryMapperImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.vaadin.integration.view.ComponentContainerBasedViewPort;

/**
 * Presenter logic for page editor.
 */
public class PageEditorPresenter {

    private Shell shell;
    private EventBus eventBus;
    private PlaceController placeController;
    private MainActivityMapper mainActivityMapper;
    private PageEditorView pageEditorView;

    public PageEditorPresenter(Shell shell, EventBus eventBus, PlaceController placeController, MainActivityMapper mainActivityMapper, PageEditorView pageEditorView) {
        this.shell = shell;
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.mainActivityMapper = mainActivityMapper;
        this.pageEditorView = pageEditorView;
    }

    public void init() {

        PlaceHistoryMapper historyMapper = new PlaceHistoryMapperImpl(PageEditorPlace.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper, shell);
        Place defaultPlace = Place.NOWHERE;
        historyHandler.register(placeController, eventBus, defaultPlace);

        ActivityManager activityManager = new ActivityManager(mainActivityMapper, eventBus);
        activityManager.setViewPort(new ComponentContainerBasedViewPort("main", pageEditorView.getMainContainer()));

        historyHandler.handleCurrentHistory();
    }
}
