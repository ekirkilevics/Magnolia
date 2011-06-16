/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.admincentral.navigation;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.model.navigation.definition.NavigationDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationGroupDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationWorkareaDefinition;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchema;
import info.magnolia.ui.model.navigation.registry.NavigationProvider;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;


/**
 * ImplementationConfiguration of {@link NavigationView}. It represents the app main navigation and
 * holds {@link NavigationWorkArea}(s).
 *
 * @author fgrilli
 * @author mrichert
 */
public class NavigationViewImpl implements NavigationView, IsVaadinComponent {

    private static final Logger log = LoggerFactory.getLogger(NavigationViewImpl.class);

    private TabSheet outerNavigationContainer = new TabSheet();

    private Presenter presenter;

    private Set<NavigationWorkArea> registeredNavigationAreas = new HashSet<NavigationWorkArea>();

    // TODO don't pass the registry but the navigation itself
    public NavigationViewImpl(NavigationProvider navigationProvider, NavigationPermissionSchema permissions) {
        final NavigationDefinition navigation = navigationProvider.getNavigation();
        createNavigation(navigation, permissions);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        for (NavigationWorkArea navigationWorkArea : registeredNavigationAreas) {
            for (NavigationGroup navigationGroup : navigationWorkArea.getNavigationGroup()) {
                navigationGroup.setPresenter(presenter);
            }
        }
    }

    @Override
    public Component asVaadinComponent() {
        return outerNavigationContainer;
    }

    private void createNavigation(NavigationDefinition navigation, NavigationPermissionSchema permissions) {

        for (final NavigationWorkareaDefinition definition : navigation.getWorkareas()) {
            log.debug("creating navigation workarea {}", definition.getName());
            List<NavigationGroup> groups = new ArrayList<NavigationGroup>();

            for (NavigationGroupDefinition group : definition.getGroups()) {
                log.debug("creating navigation group {}", group.getName());
                NavigationGroup navigationGroup = new NavigationGroup(group.getItems(), permissions);
                groups.add(navigationGroup);
            }

            final NavigationWorkArea navigationWorkArea = new NavigationWorkArea(groups);

            registeredNavigationAreas.add(navigationWorkArea);

            final Component component = navigationWorkArea.asVaadinComponent();
            component.addStyleName(definition.getName());
            outerNavigationContainer.addTab(
                component,
                definition.getLabel(),
                new ExternalResource(MgnlContext.getContextPath() + definition.getIcon()));

            outerNavigationContainer.addListener(new SelectedTabChangeListener() {

                @Override
                public void selectedTabChange(SelectedTabChangeEvent event) {
                    if (component == event.getTabSheet().getSelectedTab() && presenter != null) {
                        presenter.onMenuSelection(definition);
                    }
                }
            });
        }
    }
}
