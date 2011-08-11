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
package info.magnolia.ui.admincentral.toolbar.view;

import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.search.place.SearchPlace;
import info.magnolia.ui.admincentral.search.view.SearchParameters;
import info.magnolia.ui.admincentral.search.view.SearchResult;
import info.magnolia.ui.admincentral.search.view.SearchView;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.PlaceChangeActionDefinition;
import info.magnolia.ui.model.toolbar.ToolbarDefinition;
import info.magnolia.ui.model.toolbar.ToolbarItemDefinition;
import info.magnolia.ui.model.toolbar.ToolbarItemGroupDefinition;
import info.magnolia.ui.model.toolbar.registry.ToolbarPermissionSchema;
import info.magnolia.ui.model.toolbar.registry.ToolbarProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;


/**
 * TODO show search results and advanced search options. Implementation for
 * {@link FunctionToolbarView}.
 * @author fgrilli
 * @author mrichert
 */
@Singleton
public class FunctionToolbarViewImpl implements FunctionToolbarView, IsVaadinComponent {

    private static final Logger log = LoggerFactory.getLogger(FunctionToolbarViewImpl.class);

    private CssLayout outerContainer = new CssLayout();

    private FunctionToolbarView.Presenter presenter;

    private Map<ViewType, Button> viewButtons = new HashMap<ViewType, Button>();

    private SearchView.Presenter searchPresenter;

    @Inject
    public FunctionToolbarViewImpl(ToolbarProvider toolbarProvider, ToolbarPermissionSchema permissions, WorkbenchDefinition definition) {
        outerContainer.setStyleName("toolbar");
        final String workspace = definition.getWorkspace();
        final ToolbarDefinition toolbarDefinition = toolbarProvider.getToolbar();
        if (toolbarDefinition == null) {
            log.warn("No function toolbar definition found, won't render it.");
            final Label label = new Label("No function toolbar definition found. Please, check your configuration.");
            label.setSizeFull();
            outerContainer.addComponent(label);
            return;
        }

        // TODO build UI filters for advanced search
        // final List<ToolbarItemFilterDefinition> filters = toolbarDefinition.getFilters();

        final List<ToolbarItemGroupDefinition> groupItems = toolbarDefinition.getGroups();

        if (groupItems == null || groupItems.isEmpty()) {
            log.warn("No function toolbar groups found, won't render them.");
            final Label label = new Label("No function toolbar groups found. Please, check your configuration.");
            label.setSizeFull();
            outerContainer.addComponent(label);
            return;
        }

        for (int i = 0; i < groupItems.size(); i++) {

            final ToolbarItemGroupDefinition itemGroupDefinition = groupItems.get(i);
            final List<ToolbarItemDefinition> items = itemGroupDefinition.getItems();
            final Segment viewGroup = new Segment();

            viewGroup.setStyleName("segment");
            viewGroup.addStyleName("segment-alternate");

            final Label label = new Label(itemGroupDefinition.getGroupLabel() + "&nbsp;", Label.CONTENT_XML);
            viewGroup.addComponent(label);
            viewGroup.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

            for (int j = 0; j < items.size(); j++) {
                final ToolbarItemDefinition item = items.get(j);

                final Button button = new Button(item.getLabel());
                button.addListener(new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        presenter.onToolbarItemSelection(item);
                        setViewButtonStyle(button);
                    }
                });

                ActionDefinition actionDefinition = item.getActionDefinition();
                if (actionDefinition instanceof PlaceChangeActionDefinition) {
                    Place place = ((PlaceChangeActionDefinition) actionDefinition).getPlace();
                    if (place instanceof ItemSelectedPlace) {
                        ViewType viewType = ((ItemSelectedPlace) place).getViewType();

                        log.debug("{}.viewType = {}", item.getLabel(), viewType);

                        viewButtons.put(viewType, button);
                    }
                }

                viewGroup.addButton(button);
                viewGroup.setComponentAlignment(button, Alignment.MIDDLE_CENTER);
            }
            viewGroup.getComponent(1).addStyleName("first");
            // viewGroup.getComponent(viewGroup.getComponentCount() - 1).addStyleName("last");
            outerContainer.addComponent(viewGroup);

            final Button advancedSearchButton = new Button("+");
            advancedSearchButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    // TODO show advanced search
                }
            });
            outerContainer.addComponent(advancedSearchButton);

            final TextField searchBox = new TextField();
            // FIXME get the text from messages.properties
            searchBox.setInputPrompt("Search");
            searchBox.addStyleName("search");
            searchBox.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ENTER, null) {

                @Override
                public void handleAction(Object sender, Object target) {

                    SearchParameters params = new SearchParameters(workspace, searchBox.getValue().toString());
                    // TODO add filters to params if search started in advanced mode.
                    // presumably iterate over UI select widgets, get the selected options, get the
                    // boolean op chosen for a given selection and build the filters. Something like
                    // the following:
                    // params.addFilter(new SearchFilter(selection, SearchFilter.LIKE, foo))
                    // //addFilter(..), or(..) etc. could be chainable and return the current
                    // instance of SearchParameters
                    // .or()
                    // .addFilter(new SearchFilter(anotherSelection, SearchFilter.EQ, bar))
                    // .and()
                    // .addFilter(new SearchFilter(yetAnotherSelection, SearchFilter.NEQ, baz));
                    searchPresenter.onStartSearch(params);
                    // TODO show expanded/advanced search
                }
            });
            outerContainer.addComponent(searchBox);
        }
    }

    @Override
    public Component asVaadinComponent() {
        return outerContainer;
    }

    @Override
    public void setPresenter(FunctionToolbarView.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setPresenter(SearchView.Presenter presenter) {
        searchPresenter = presenter;

    }

    @Override
    public void update(Place newPlace) {
        if (newPlace instanceof ItemSelectedPlace) {
            ViewType viewType = ((ItemSelectedPlace) newPlace).getViewType();
            setViewButtonStyle(viewButtons.get(viewType));
        }
        else if (newPlace instanceof SearchPlace) {
            setViewButtonStyle(viewButtons.get(ViewType.LIST));
        }
        else {
            setViewButtonStyle(viewButtons.get(ViewType.TREE));
        }
    }

    @Override
    public void search(SearchParameters searchParameters, JcrView jcrView) {
        searchPresenter.onPerformSearch(searchParameters, jcrView);
    }

    private void setViewButtonStyle(Button button) {
        if (button == null) {
            return;
        }
        for (Button btn : viewButtons.values()) {
            btn.removeStyleName("v-button-down");
        }
        // log.debug("applying 'down' style to button {}", button.getCaption());
        // FIXME adding a style with the complete name is not recommended (shoudl be simply "down"),
        // however it's the only way it works now.
        button.addStyleName("v-button-down");
    }

    @Override
    public void update(SearchResult result) {
        // TODO update UI showing the number of results
    }

    /**
     * The segment control is just a set of buttons inside a HorizontalLayout.
     * 
     * @author mrichert
     */
    public static class Segment extends HorizontalLayout {

        public Segment() {
            addStyleName("segment");
        }

        public Segment addButton(Button b) {
            addComponent(b);
            b.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (event.getButton().getStyleName().indexOf("down") == -1) {
                        event.getButton().addStyleName("down");
                    }
                    else {
                        event.getButton().removeStyleName("down");
                    }
                }
            });
            updateButtonStyles();
            return this;
        }

        private void updateButtonStyles() {
            int i = 0;
            Component c = null;
            for (Iterator<Component> iterator = getComponentIterator(); iterator
                    .hasNext();) {
                c = iterator.next();
                c.removeStyleName("first");
                c.removeStyleName("last");
                if (i == 0) {
                    c.addStyleName("first");
                }
                i++;
            }
            if (c != null) {
                c.addStyleName("last");
            }
        }
    }

}
