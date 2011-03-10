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
package info.magnolia.ui.admincentral.application;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.dialog.activity.DialogActivity;
import info.magnolia.ui.admincentral.dialog.place.DialogPlace;
import info.magnolia.ui.admincentral.editworkspace.activity.EditWorkspaceActivity;
import info.magnolia.ui.admincentral.editworkspace.place.EditWorkspacePlace;
import info.magnolia.ui.admincentral.navigation.NavigationActivity;
import info.magnolia.ui.admincentral.showcontent.ShowContentActivity;
import info.magnolia.ui.admincentral.showcontent.place.ShowContentPlace;
import info.magnolia.ui.admincentral.showcontent.place.SomePlace;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.place.PlaceHistoryHandler;
import info.magnolia.ui.framework.place.PlaceHistoryMapper;
import info.magnolia.ui.framework.place.PlaceHistoryMapperImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.UIModel;
import info.magnolia.ui.vaadin.integration.view.ComponentContainerBasedViewPort;

/**
 * Provides the MVP infrastructure for AdminCentral.
 */
public class AdminCentralPresenter {

    private Shell shell;
    private EventBus eventBus;
    private UIModel uiModel;
    private PlaceController placeController;
    private AdminCentralView adminCentralView;
    private ComponentProvider componentProvider;

    public AdminCentralPresenter(ComponentProvider componentProvider, Shell shell, EventBus eventBus, UIModel uiModel, PlaceController placeController, AdminCentralView adminCentralView) {
        this.componentProvider = componentProvider;
        this.shell = shell;
        this.eventBus = eventBus;
        this.uiModel = uiModel;
        this.placeController = placeController;
        this.adminCentralView = adminCentralView;
    }

    public void init() {

        // Browser history integration
        // FIXME make this more dynamic, don't pass the place explicitly
        PlaceHistoryMapper historyMapper = new PlaceHistoryMapperImpl(EditWorkspacePlace.class);
        PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper, shell);
        final EditWorkspacePlace defaultPlace = new EditWorkspacePlace("website");
        historyHandler.register(placeController, eventBus, defaultPlace);

        ActivityManager menuActivityManager = new ActivityManager(new ActivityMapper() {
            Activity menuActivity = componentProvider.newInstance(NavigationActivity.class);
            public Activity getActivity(Place place) {
                return menuActivity;
            }
        }, eventBus);

        ActivityManager mainActivityManager = new ActivityManager(new ActivityMapper() {

            public Activity getActivity(final Place place) {
                if(place instanceof EditWorkspacePlace){
                    EditWorkspacePlace editWorkspacePlace = (EditWorkspacePlace)place;
                    return new EditWorkspaceActivity(editWorkspacePlace.getWorkspace(), shell, uiModel);
                }
                else if(place instanceof ShowContentPlace){
                    ShowContentPlace showContentPlace = (ShowContentPlace)place;
                    return new ShowContentActivity(showContentPlace.getViewTarget(), showContentPlace.getViewName());
                }
                else if(place instanceof DialogPlace){
                    DialogPlace dialogPlace = (DialogPlace)place;
                    return new DialogActivity(dialogPlace, uiModel, componentProvider);
                }
                else if(place instanceof SomePlace){
                    return new AbstractActivity() {
                        public void start(ViewPort viewPort, EventBus eventBus) {
                            shell.showNotification(((SomePlace)place).getName());
                        }
                    };
                }
                else{
                    return null;
                }
            }
        }, eventBus);

        mainActivityManager.setDisplay(new ComponentContainerBasedViewPort("main", adminCentralView.getMainContainer()));
        menuActivityManager.setDisplay(new ComponentContainerBasedViewPort("navigation", adminCentralView.getMenuDisplay()));

        historyHandler.handleCurrentHistory();
    }
}
