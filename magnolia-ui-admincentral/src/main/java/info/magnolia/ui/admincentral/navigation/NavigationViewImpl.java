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
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.model.navigation.definition.NavigationDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationGroupDefinition;
import info.magnolia.ui.model.navigation.definition.NavigationWorkareaDefinition;
import info.magnolia.ui.model.navigation.registry.NavigationPermissionSchema;
import info.magnolia.ui.model.navigation.registry.NavigationProvider;
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
 * ImplementationConfiguration of {@link NavigationView}. It represents the app main navigation and holds {@link NavigationWorkArea}(s).
 * @author fgrilli
 *
 */
public class NavigationViewImpl implements NavigationView, IsVaadinComponent{

    private static final Logger log = LoggerFactory.getLogger(NavigationViewImpl.class);

    private CustomComponent customComponent;
    private VerticalLayout outerNavigationContainer = new VerticalLayout();
    private Presenter presenter;
    private Map<WorkareaSelector, NavigationWorkArea> registeredNavigationAreas = new HashMap<WorkareaSelector, NavigationWorkArea>();

    //TODO don't pass the registry but the navigation itself
    public NavigationViewImpl(NavigationProvider navigationProvider, NavigationPermissionSchema permissions) {

        // Wrapping in a custom component to make it appear in the top of the area
        customComponent = new CustomComponent() {{setCompositionRoot(outerNavigationContainer);}};
        customComponent.setSizeFull();

        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setMargin(false, false, true, false);

        final VerticalLayout navigationWorkareaContainer = new VerticalLayout();
        navigationWorkareaContainer.setSizeFull();
        navigationWorkareaContainer.setMargin(false, false, true, false);

        final NavigationDefinition navigation = navigationProvider.getNavigation();

        for(NavigationWorkareaDefinition definition : navigation.getWorkareas()){
            log.debug("creating navigation workarea {}", definition.getName());
            List<NavigationGroup> groups = new ArrayList<NavigationGroup>();

            for(NavigationGroupDefinition group : definition.getGroups()){
                log.debug("creating navigation group {}", group.getName());
                groups.add(new NavigationGroup(group.getItems(), permissions));
            }

            final WorkareaSelector button = new WorkareaSelector(definition);
            buttons.addComponent(button);

            final NavigationWorkArea navigationWorkArea = new NavigationWorkArea(groups);

            if(definition.isVisible()){
                navigationWorkArea.setVisible(true);
            }
            registeredNavigationAreas.put(button, navigationWorkArea);
            navigationWorkareaContainer.addComponent(navigationWorkArea.asVaadinComponent());
        }
        outerNavigationContainer.addComponent(buttons);
        outerNavigationContainer.addComponent(navigationWorkareaContainer);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        for(NavigationWorkArea navigationWorkArea: registeredNavigationAreas.values()){
            for(NavigationGroup navigationGroup: navigationWorkArea.getNavigationGroup()){
                navigationGroup.setPresenter(presenter);
            }
        }
    }

    @Override
    public void update(Place place) {
        for(NavigationWorkArea workarea: registeredNavigationAreas.values()){
            //the navigation group will set the correct navigation  area as visible
            workarea.setVisible(false);
            for(NavigationGroup group: workarea.getNavigationGroup()){
                group.update(place);
            }
        }
    }

    @Override
    public Component asVaadinComponent() {
        return customComponent;
    }

    /**
     * WorkareaSelector.
     * @author fgrilli
     *
     */
    protected class WorkareaSelector extends Button {

        public WorkareaSelector(final NavigationWorkareaDefinition definition) {

            addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    for(NavigationWorkArea navigationWorkArea : registeredNavigationAreas.values()){
                        navigationWorkArea.setVisible(false);
                    }
                    NavigationWorkArea selectedNavigationWorkarea = registeredNavigationAreas.get(event.getButton());
                    selectedNavigationWorkarea.setVisible(true);
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
