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
package info.magnolia.ui.admincentral.activity;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.dialog.activity.DialogActivity;
import info.magnolia.ui.admincentral.dialog.place.DialogPlace;
import info.magnolia.ui.admincentral.editworkspace.activity.EditWorkspaceActivity;
import info.magnolia.ui.admincentral.editworkspace.place.EditWorkspacePlace;
import info.magnolia.ui.admincentral.showcontent.activity.ShowContentActivity;
import info.magnolia.ui.admincentral.showcontent.activity.SomePlaceActivity;
import info.magnolia.ui.admincentral.showcontent.place.ShowContentPlace;
import info.magnolia.ui.admincentral.showcontent.place.SomePlace;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilder;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.UIModel;
import info.magnolia.ui.model.dialog.registry.DialogRegistry;

/**
 * TODO: write javadoc.
 * @author fgrilli
 *
 */
public class MainActivityMapper implements ActivityMapper {
    private Shell shell;
    private UIModel uiModel;
    private DialogRegistry dialogRegistry;
    private ComponentProvider componentProvider;

    public MainActivityMapper(Shell shell, UIModel uiModel, DialogRegistry dialogRegistry, ComponentProvider componentProvider) {
        this.shell = shell;
        this.uiModel = uiModel;
        this.dialogRegistry = dialogRegistry;
        this.componentProvider = componentProvider;
    }

    public Activity getActivity(final Place place) {
        if(place instanceof EditWorkspacePlace){
            EditWorkspacePlace editWorkspacePlace = (EditWorkspacePlace)place;
            // FIXME lets inject the tree builder! byt workspace is a paramter and we need something more flexible
            return new EditWorkspaceActivity(editWorkspacePlace.getWorkspace(), shell, uiModel, componentProvider.getComponent(TreeBuilder.class));
        }
        else if(place instanceof ShowContentPlace){
            ShowContentPlace showContentPlace = (ShowContentPlace)place;
            return new ShowContentActivity(showContentPlace.getViewTarget(), showContentPlace.getViewName());
        }
        else if(place instanceof DialogPlace){
            DialogPlace dialogPlace = (DialogPlace)place;
            return new DialogActivity(componentProvider, dialogPlace, dialogRegistry);
        }
        else if(place instanceof SomePlace){
            SomePlace somePlace = (SomePlace)place;
            return new SomePlaceActivity(shell, somePlace.getName());
        }
        else{
            return null;
        }
    }
}
