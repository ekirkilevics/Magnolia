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
package info.magnolia.ui.admincentral.toolbar.activity;

import info.magnolia.ui.admincentral.toolbar.action.FunctionToolbarActionFactory;
import info.magnolia.ui.admincentral.toolbar.view.FunctionToolbarView;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.toolbar.ToolbarItemDefinition;

/**
 * Handle selections on the function toolbar.
 * @author fgrilli
 *
 */
public class FunctionToolbarActivity extends AbstractActivity implements FunctionToolbarView.Presenter{

    private FunctionToolbarView functionToolbarView;
    private FunctionToolbarActionFactory actionFactory;
    private Shell shell;

    public FunctionToolbarActivity(FunctionToolbarView functionToolbarView, FunctionToolbarActionFactory actionFactory, Shell shell) {
        this.functionToolbarView = functionToolbarView;
        this.actionFactory = actionFactory;
        this.shell = shell;
        this.functionToolbarView.setPresenter(this);
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        viewPort.setView(functionToolbarView);
    }

    @Override
    public void onToolbarItemSelection(ToolbarItemDefinition itemDefinition) {
        final ActionDefinition actionDefinition = itemDefinition.getActionDefinition();
        if(actionDefinition != null){
            final Action action = actionFactory.createAction(actionDefinition);
            try {
                action.execute();
            }
            catch (ActionExecutionException e) {
                shell.showError("Can't execute action", e);
            }
        }
        else{
            shell.showNotification("No action defined for " + itemDefinition.getName());
        }
    }

}
