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
package info.magnolia.ui.admincentral.workbench.activity;


import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.MutableComponentProvider;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.tree.activity.TreeActivity;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilderProvider;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.workbench.place.WorkbenchPlace;
import info.magnolia.ui.admincentral.workbench.view.WorkbenchView;
import info.magnolia.ui.admincentral.workbench.view.WorkbenchViewImpl;
import info.magnolia.ui.framework.activity.AbstractMVPSubContainer;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchRegistry;


/**
 * The isolated MVP container for workspace editing.
 */
public class WorkbenchMVPSubContainer extends AbstractMVPSubContainer<WorkbenchActivity>{

    private WorkbenchPlace place;
    private WorkbenchRegistry workbenchRegistry;

    public WorkbenchMVPSubContainer(WorkbenchPlace place, WorkbenchRegistry workbenchRegistry, Shell shell, ComponentProvider componentProvider) {
        super("workbench-" + place.getWorkbenchName(), shell, componentProvider);
        this.place = place;
        this.workbenchRegistry = workbenchRegistry;

    }

    @Override
    protected Class<WorkbenchActivity> getActivityClass() {
        return WorkbenchActivity.class;
    }

    @Override
    protected Object[] getActivityParameters() {
        return new Object[]{place};
    }

    @Override
    protected void configureComponentProvider(MutableComponentProvider componentProvider) {

        componentProvider.registerInstance(WorkbenchDefinition.class, workbenchRegistry.getWorkbench(place.getWorkbenchName()));

        componentProvider.registerImplementation(WorkbenchView.class, WorkbenchViewImpl.class);
        componentProvider.registerImplementation(EditWorkspaceActionFactory.class, EditWorkspaceActionFactory.class);

        componentProvider.registerImplementation(ItemListActivityMapper.class, ItemListActivityMapper.class);
        componentProvider.registerImplementation(TreeActivity.class, TreeActivity.class);

        componentProvider.registerImplementation(DetailViewActivityMapper.class, DetailViewActivityMapper.class);
        componentProvider.registerImplementation(DetailViewActivity.class, DetailViewActivity.class);

        componentProvider.registerConfiguredComponent(TreeBuilderProvider.class, ContentRepository.CONFIG, "/modules/admin-central/components/treeBuilderProvider", false);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends Place>[] getSupportedPlaces() {
        // Casts since generic array creation doesn't exist
        return (Class<? extends Place>[]) new Class[] {ItemSelectedPlace.class};
    }

    @Override
    protected Place getDefaultPlace() {
        return new ItemSelectedPlace(place.getWorkbenchName(), "/");
    }

}
