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
package info.magnolia.ui.admincentral.search.view;

import info.magnolia.ui.admincentral.search.view.SearchView.Presenter;

import java.util.Arrays;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;

/**
 * Simple form for searching.
 * TODO: this is a very coarse implementation. Needs revision for layout, naming and most of all it should be built dynamically, much like a dialog to allow for
 * different filters to be plugged in (think of advanced search) and combined with boolean ops in a configurable manner.
 * @author dlipp
 * @author fgrilli
 *
 */
public class SearchForm extends Form implements Handler {

    private static final Action SEARCH_ACTION = new ShortcutAction("", ShortcutAction.KeyCode.ENTER, null);
    private static final Action DISMISS_FORM_ACTION = new ShortcutAction("", ShortcutAction.KeyCode.ESCAPE, null);
    private static final Action[] actions = { SEARCH_ACTION, DISMISS_FORM_ACTION };
    private static final String SEARCH_FIELD = "query";
    private static final String SEARCH = "Search";

    private CustomComponent customComponent;
    private GridLayout gridLayout;
    private HorizontalLayout buttons = new HorizontalLayout();
    private VerticalLayout expandedForm = new VerticalLayout();
    private BeanItem<SearchParameters> searchParameters;
    private Label searchFormLabel = new Label("Basic search");
    private Presenter presenter;

    public SearchForm() {
        //need to wrap into a Panel in order to react on enter key
        final Panel panel = new Panel();
        panel.addActionHandler(this);
        panel.addStyleName(Runo.PANEL_LIGHT);

        gridLayout = new GridLayout(2, 3);
        gridLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.setMargin(false, true, false, false);
        panel.addComponent(gridLayout);
        //FIXME how to get the workspace here?
        searchParameters = new BeanItem<SearchParameters>(new SearchParameters("website", ""));

        setWriteThrough(false);
        setItemDataSource(searchParameters);
        setFormFieldFactory(new SearchFormFieldFactory());
        setVisibleItemProperties(Arrays.asList(new String[] { SEARCH_FIELD }));

        searchFormLabel.setVisible(false);
        searchFormLabel.addStyleName("m-search-form-label");
        gridLayout.addComponent(searchFormLabel,0,0);

        expandedForm.addComponent(new SearchFormRow());
        gridLayout.addComponent(expandedForm,0,1);
        gridLayout.setColumnExpandRatio(0, 5);

        final Button updateResults = new Button("Update results", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                commit();
                presenter.onStartSearch(searchParameters.getBean());
            }
        });
        buttons.addComponent(updateResults);

        final Button doneButton = new Button("Done", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                discard();
                updateUI(false, null);
            }
        });

        buttons.setSpacing(true);
        buttons.setMargin(true, false, false, false);
        buttons.addComponent(doneButton);
        gridLayout.addComponent(buttons, 1, 2);
        gridLayout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
        gridLayout.setColumnExpandRatio(1, 1);

        customComponent = new CustomComponent() {{setCompositionRoot(panel);}};
        updateUI(false, null);
    }

    public Component asVaadinComponent() {
        return customComponent;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Action[] getActions(Object target, Object sender) {
        return actions;
    }

    @Override
    public void handleAction(Action action, Object sender, Object target) {
        if(action == SEARCH_ACTION){
            commit();
            presenter.onStartSearch(searchParameters.getBean());
        } else if (action == DISMISS_FORM_ACTION){
            discard();
            updateUI(false, null);
        }
    }

    @Override
    protected void attachField(Object propertyId, Field field) {
        if(SEARCH_FIELD.equals(propertyId)) {
            gridLayout.addComponent(field, 1, 0);
            gridLayout.setComponentAlignment(field, Alignment.MIDDLE_RIGHT);
        }
    }

    /**
     * Updates the UI by showing or hiding search results and applying/removing styles.
     * @param result
     * @param visible
     */
    public void updateUI(boolean searchResultsVisible, SearchResult result){
        if(searchResultsVisible){
            buttons.setVisible(true);
            expandedForm.setVisible(true);
            searchFormLabel.setVisible(true);
            customComponent.addStyleName("m-search-form-expanded");
            if(result != null){
                //TODO need to be i18nized
                ((SearchFormRow)expandedForm.getComponent(0)).getResultsArea().setValue(result.getItemsFound() + " items found containing the text " + result.getQuery() + " in ");
            }
        } else {
            buttons.setVisible(false);
            expandedForm.setVisible(false);
            searchFormLabel.setVisible(false);
            getField(SEARCH_FIELD).setValue(SEARCH);
            customComponent.removeStyleName("m-search-form-expanded");
        }
    }
    /**
     * A Vaadin-specific field factory for the search form.
     * @author fgrilli
     *
     */
    protected class SearchFormFieldFactory extends DefaultFieldFactory {

        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            if(SEARCH_FIELD.equals(propertyId)) {
                final TextField searchField = new TextField();
                searchField.setInputPrompt(SEARCH);
                searchField.setWidth(200, Sizeable.UNITS_PIXELS);
                searchField.addStyleName("m-search-box");

                searchField.addListener(new FocusListener() {

                    @Override
                    public void focus(FocusEvent event) {
                        ((TextField)event.getSource()).setValue("");
                    }
                });

                searchField.addListener(new BlurListener() {

                    @Override
                    public void blur(BlurEvent event) {
                        TextField text = ((TextField)event.getSource());
                        if("".equals(text.getValue())){
                            text.setValue(SEARCH);
                        }
                    }
                });
                return searchField;
            }
            return null;
        }
    }
    /**
     * Represents a row in the search form. It is typically composed by a label, a filter (in the form of a select) and a button for adding further filters.
     * FIXME filter is hardcoded.
     * @author fgrilli
     *
     */
    protected class SearchFormRow extends CustomComponent {
        final Label resultsArea = new Label("");
        final Select filter = new Select();
        final Button addFilterButton = new Button("+");
        final GridLayout gridLayout = new GridLayout(3,1);

        public SearchFormRow() {
            gridLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            setCompositionRoot(gridLayout);
            gridLayout.addComponent(resultsArea, 0, 0);
            gridLayout.setComponentAlignment(resultsArea, Alignment.MIDDLE_LEFT);
            gridLayout.setColumnExpandRatio(0, 5f);

            filter.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            String defaultSelection = "all pages and resources";
            filter.addItem(defaultSelection);
            filter.addItem("pages only");
            filter.addItem("resources only");
            filter.select(defaultSelection);

            gridLayout.addComponent(filter, 1, 0);
            gridLayout.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);
            gridLayout.setColumnExpandRatio(1, 4f);

            addFilterButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    presenter.onAddFilter();

                }
            });
            gridLayout.addComponent(addFilterButton, 2, 0);
            gridLayout.setComponentAlignment(addFilterButton, Alignment.MIDDLE_LEFT);
            gridLayout.setColumnExpandRatio(2, 1f);

        }
        public Label getResultsArea() {
            return resultsArea;
        }

        public Select getFilter() {
            return filter;
        }

        public Button getAddFilterButton() {
            return addFilterButton;
        }
    }
}
