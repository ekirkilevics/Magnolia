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
package info.magnolia.module.admincentral.activity;

import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.admincentral.place.EditItemPlace;
import info.magnolia.module.admincentral.tree.TreeActivity;
import info.magnolia.module.admincentral.views.EditWorkspaceView;
import info.magnolia.module.vaadin.activity.Activity;
import info.magnolia.module.vaadin.activity.ActivityManager;
import info.magnolia.module.vaadin.activity.ActivityMapper;
import info.magnolia.module.vaadin.activity.MVPSubContainerActivity;
import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;
import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceController;

import com.vaadin.Application;


/**
 * Edit a workspace. Shows the structure view.
 */
public class EditWorkspaceActivity extends MVPSubContainerActivity {

    private UIModel uiModel;
    private String workspace;

    public EditWorkspaceActivity(String workspace, PlaceController outerPlaceController, Application application, UIModel uiModel) {
        super("edit-workspace", outerPlaceController, application);
        this.workspace = workspace;
        this.uiModel = uiModel;
    }

    @Override
    public String mayStop() {
        //TODO retrieve this from properties file.
        return "Are you sure you want to leave this page?";
    }

    @Override
    protected void onStart(HasComponent display, EventBus innerEventBus) {

        EditWorkspaceView editWorkspaceView = new EditWorkspaceView();

        ActivityManager treeActivityManager = new ActivityManager(new ActivityMapper() {

            public Activity getActivity(final Place place) {
                String path = ((EditItemPlace)place).getPath();
                return new TreeActivity(workspace, path, getInnerPlaceController(), uiModel);
            }
        }, innerEventBus);

        ActivityManager detailViewActivityManager = new ActivityManager(new ActivityMapper() {
            public Activity getActivity(final Place place) {
                String path = ((EditItemPlace)place).getPath();
                return new DetailViewActivity(workspace, path, uiModel);
            }
        }, innerEventBus);

        treeActivityManager.setDisplay(editWorkspaceView.getTreeDisplay());
        detailViewActivityManager.setDisplay(editWorkspaceView.getDetailDisplay());

        display.setComponent(editWorkspaceView.asComponent());

        getInnerPlaceController().goTo(new EditItemPlace(workspace, "/"));
    }

}
