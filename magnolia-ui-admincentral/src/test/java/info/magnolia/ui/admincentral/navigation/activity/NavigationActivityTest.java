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
package info.magnolia.ui.admincentral.navigation.activity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import info.magnolia.ui.admincentral.navigation.NavigationView;
import info.magnolia.ui.admincentral.navigation.action.NavigationActionFactory;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.action.PlaceChangeAction;
import info.magnolia.ui.model.action.PlaceChangeActionDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationItemDefinition;
import info.magnolia.ui.model.navigation.registry.NavigationProvider;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @version $Id$
 *
 */
public class NavigationActivityTest {

    private NavigationActivity activity;
    private PlaceChangeActionDefinition actionDefinition;
    private PlaceChangeAction action;
    private NavigationActionFactory actionFactory;


    @Before
    public void setUp() throws Exception {
        actionFactory = mock(NavigationActionFactory.class);
        actionDefinition = mock(PlaceChangeActionDefinition.class);
        action = mock(PlaceChangeAction.class);

        NavigationDefinition definition  = mock(NavigationDefinition.class);
        NavigationProvider navigationProvider = mock(NavigationProvider.class);
        when(navigationProvider.getNavigation()).thenReturn(definition);

        NavigationView view = mock(NavigationView.class);
        Shell shell = mock(Shell.class);

        activity = new NavigationActivity(view, actionFactory, navigationProvider, shell);
    }

    @Test
    public void testOnMenuSelectionNonNullActionIsExecuted() throws Exception {

        //GIVEN that action definition isn't null
        NavigationItemDefinition navigationItemDefinition = mock(NavigationItemDefinition.class);
        when(navigationItemDefinition.getActionDefinition()).thenReturn(actionDefinition);
        when(actionFactory.createAction(actionDefinition)).thenReturn(action);

        //WHEN onMenuSelection() is called
        activity.onMenuSelection(navigationItemDefinition);

        //THEN action is executed once
        verify(action).execute();
    }

}