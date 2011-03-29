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
import info.magnolia.ui.model.navigation.registry.NavigationRegistry;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Implementation of {@link NavigationView}. It represents the app main navigation and holds {@link NavigationWorkArea}(s).
 * @author fgrilli
 *
 */
// FIXME don't extend CustomComponent, make it composite.
public class NavigationViewImpl extends CustomComponent implements NavigationView, IsVaadinComponent{

    private static final Logger log = LoggerFactory.getLogger(NavigationViewImpl.class);

    private VerticalLayout outerNavigationContainer = new VerticalLayout();
    private Presenter presenter;
    private Map<WorkareaChooser, NavigationWorkArea> registeredNavigationAreas = new HashMap<WorkareaChooser, NavigationWorkArea>();

    //TODO don't pass the registry but the navigation itself
    public NavigationViewImpl(NavigationRegistry navigationRegistry, NavigationPermissionSchema permissions) {
        setCompositionRoot(outerNavigationContainer);
        setSizeFull();

        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false, false, true, false);

        final VerticalLayout navigationWorkareaContainer = new VerticalLayout();
        navigationWorkareaContainer.setSizeFull();
        navigationWorkareaContainer.setMargin(false, false, true, false);

        final NavigationDefinition navigation = navigationRegistry.getNavigation();

        for(NavigationWorkareaDefinition definition : navigation.getWorkareas()){
            log.debug("creating navigation workarea {}", definition.getName());
            List<NavigationGroup> groups = new ArrayList<NavigationGroup>();

            for(NavigationGroupDefinition group : definition.getGroups()){
                log.debug("creating navigation group {}", group.getName());
                groups.add(new NavigationGroup(group.getItems(), permissions));
            }

            final WorkareaChooser button = new WorkareaChooser(definition);
            buttons.addComponent(button);

            final NavigationWorkArea navigationWorkArea = new NavigationWorkArea(groups);

            if(definition.isVisible()){
                navigationWorkArea.setVisible(true);
            }
            registeredNavigationAreas.put(button, navigationWorkArea);
            navigationWorkareaContainer.addComponent(navigationWorkArea);
        }
        outerNavigationContainer.addComponent(buttons);
        outerNavigationContainer.addComponent(navigationWorkareaContainer);
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        for(NavigationWorkArea navigationWorkArea: registeredNavigationAreas.values()){
            for(NavigationGroup navigationGroup: navigationWorkArea.getNavigationGroup()){
                navigationGroup.setPresenter(presenter);
            }
        }

    }

    public Component asVaadinComponent() {
        return this;
    }

    /**
     * WorkareaChooser.
     * TODO naming?
     * @author fgrilli
     *
     */
    protected class WorkareaChooser extends Button {

        public WorkareaChooser(final NavigationWorkareaDefinition definition) {

            addListener(new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    for(NavigationWorkArea navigationWorkArea : registeredNavigationAreas.values()){
                        navigationWorkArea.setVisible(false);
                    }
                    NavigationWorkArea selected = registeredNavigationAreas.get(event.getButton());
                    selected.setVisible(true);
                    presenter.onMenuSelection(definition);
                }
            });

            String icon = definition.getIcon();
            if(StringUtils.isNotBlank(icon)) {
                setIcon(new ExternalResource(MgnlContext.getContextPath() + icon));
            }
        }
    }
}
