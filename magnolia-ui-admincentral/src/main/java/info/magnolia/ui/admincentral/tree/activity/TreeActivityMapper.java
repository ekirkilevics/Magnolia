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
package info.magnolia.ui.admincentral.tree.activity;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.editworkspace.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilder;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.UIModel;

/**
 * Returns the {@link Activity} to perform when the current selected item on a tree has changed.
 * @author fgrilli
 *
 */
public class TreeActivityMapper implements ActivityMapper {

    private TreeActivity treeActivity;
    private PlaceController placeController;
    private UIModel uiModel;
    private TreeBuilder builder;
    private ComponentProvider componentProvider;
    private Shell shell;


    public TreeActivityMapper(PlaceController placeController, UIModel uiModel, TreeBuilder builder, ComponentProvider componentProvider, Shell shell) {
        this.placeController = placeController;
        this.uiModel = uiModel;
        this.builder = builder;
        this.shell = shell;
        this.componentProvider = componentProvider;
    }

    public Activity getActivity(final Place place) {
        final String path = ((ItemSelectedPlace)place).getPath();
        final String treeName = ((ItemSelectedPlace)place).getWorkspace();
        if(treeActivity == null){
            treeActivity = new TreeActivity(treeName, path, placeController, uiModel, builder, componentProvider, shell);
        }
        else{
            // TODO is this good practice? we can avoid calls to start() but just update the activity to avoid a re-initialization of the tree view
            treeActivity.update(path);
        }
        return treeActivity;
    }

}