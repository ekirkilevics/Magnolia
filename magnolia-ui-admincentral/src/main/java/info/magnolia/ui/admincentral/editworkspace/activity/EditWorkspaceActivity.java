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
package info.magnolia.ui.admincentral.editworkspace.activity;

import info.magnolia.ui.admincentral.editworkspace.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.editworkspace.view.EditWorkspaceView;
import info.magnolia.ui.admincentral.tree.activity.TreeActivity;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.activity.MVPSubContainerActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.UIModel;


/**
 * Edit a workspace. Shows the structure view.
 */
public class EditWorkspaceActivity extends MVPSubContainerActivity {

    private UIModel uiModel;
    private String workspace;

    public EditWorkspaceActivity(String workspace, Shell shell, UIModel uiModel) {
        super("edit-workspace-" + workspace, shell);
        this.workspace = workspace;
        this.uiModel = uiModel;
    }

    @Override
    public String mayStop() {
        return "Are you sure you want to leave this page?";
    }

    @Override
    protected void onStart(ViewPort display, EventBus innerEventBus) {

        EditWorkspaceView editWorkspaceView = new EditWorkspaceView();

        // FIXME does it make sense to have activity manager with a single activity? I think no.

        ActivityManager treeActivityManager = new ActivityManager(new ActivityMapper() {

            private TreeActivity treeActivity;

            public Activity getActivity(final Place place) {
                final String path = ((ItemSelectedPlace)place).getPath();
                final String treeName = ((ItemSelectedPlace)place).getWorkspace();
                if(treeActivity == null){
                    treeActivity = new TreeActivity(treeName, path, getInnerPlaceController(), uiModel);
                }
                else{
                    // TODO is this good practice? we can avoid calls to start() but just update the activity to avoid a re-initialization of the tree view
                    treeActivity.update(path);
                }
                return treeActivity;
            }
        }, innerEventBus);

        ActivityManager detailViewActivityManager = new ActivityManager(new ActivityMapper() {
            public Activity getActivity(final Place place) {
                final String path = ((ItemSelectedPlace)place).getPath();
                final String treeName = ((ItemSelectedPlace)place).getWorkspace();
                return new DetailViewActivity(treeName, path, uiModel);
            }
        }, innerEventBus);

        treeActivityManager.setDisplay(editWorkspaceView.getTreeDisplay());
        detailViewActivityManager.setDisplay(editWorkspaceView.getDetailDisplay());

        display.setView(editWorkspaceView);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((workspace == null) ? 0 : workspace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EditWorkspaceActivity other = (EditWorkspaceActivity) obj;
        if (workspace == null) {
            if (other.workspace != null)
                return false;
        } else if (!workspace.equals(other.workspace))
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends Place>[] getSupportedPlaces() {
        // Casts since generic array creation doesn't exist
        return (Class<? extends Place>[]) new Class[] {ItemSelectedPlace.class};
    }

    @Override
    protected Place getDefaultPlace() {
        return new ItemSelectedPlace(workspace, "/");
    }
}
