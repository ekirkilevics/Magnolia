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
package info.magnolia.ui.admincentral.editworkspace.activity;

import info.magnolia.ui.admincentral.editworkspace.place.EditWorkspacePlace;
import info.magnolia.ui.admincentral.editworkspace.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.editworkspace.view.EditWorkspaceView;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.tree.activity.TreeActivity;
import info.magnolia.ui.admincentral.tree.activity.TreeActivityMapper;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.MVPSubContainer;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.shell.Shell;


/**
 * The isolated MVP container for workspace editing.
 */
public class EditWorkspaceMVPSubContainer extends MVPSubContainer{

    private EditWorkspacePlace place;

    public EditWorkspaceMVPSubContainer(EditWorkspacePlace place,Shell shell) {
        super("edit-workspace-" + place.getWorkspace(), shell);
        this.place = place;
    }

    @Override
    protected Class<? extends Activity> getActivityClass() {
        return EditWorkspaceActivity.class;
    }

    @Override
    protected Object[] getAdditionalConstructorParameters() {
        return new Object[]{place};
    }

    @Override
    protected void populateComponentProvider(MutableComponentProvider componentProvider) {
        componentProvider.addComponent(EditWorkspaceView.class, EditWorkspaceView.class);
        componentProvider.addComponent(EditWorkspaceActionFactory.class, EditWorkspaceActionFactory.class);

        componentProvider.addComponent(TreeActivityMapper.class, TreeActivityMapper.class);
        componentProvider.addComponent(TreeActivity.class, TreeActivity.class);
        componentProvider.addComponent(DetailViewActivityMapper.class, DetailViewActivityMapper.class);
        componentProvider.addComponent(DetailViewActivity.class, DetailViewActivity.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends Place>[] getSupportedPlaces() {
        // Casts since generic array creation doesn't exist
        return (Class<? extends Place>[]) new Class[] {ItemSelectedPlace.class};
    }

    @Override
    protected Place getDefaultPlace() {
        return new ItemSelectedPlace(place.getWorkspace(), "/");
    }

}
