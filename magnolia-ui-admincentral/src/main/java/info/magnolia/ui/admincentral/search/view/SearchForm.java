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
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
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

    private static final Action ENTER_ACTION = new ShortcutAction("", ShortcutAction.KeyCode.ENTER, null);
    private static final Action[] actions = {ENTER_ACTION};
    private  static final String SEARCH_FIELD = "query";
    private CustomComponent customComponent;
    private GridLayout gridLayout;
   // The done / update results buttons
    private HorizontalLayout buttons = new HorizontalLayout();
    private Presenter presenter;
    private BeanItem<SearchParameters> searchParameters;

    public SearchForm() {
        //need to wrap into a Panel in order to react on enter key
        final Panel panel = new Panel();
        panel.addActionHandler(this);
        panel.addStyleName(Runo.PANEL_LIGHT);

        gridLayout = new GridLayout(2, 2);
        gridLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        gridLayout.setMargin(false, true, false, false);
        gridLayout.setColumnExpandRatio(0, 5);
        gridLayout.setColumnExpandRatio(1, 1);

        panel.addComponent(gridLayout);

        SearchParameters params = new SearchParameters();
        searchParameters = new BeanItem<SearchParameters>(params);

        setWriteThrough(false);
        setItemDataSource(searchParameters);
        setFormFieldFactory(new SearchFormFieldFactory());
        setVisibleItemProperties(Arrays.asList(new String[] { SEARCH_FIELD }));


        buttons.setSpacing(true);
        buttons.setMargin(true, false, false, false);

        final Button updateResults = new Button("Update Results", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                presenter.onSearch(searchParameters.getBean());
                buttons.setVisible(true);
            }
        });

        this.addField("updateResults", updateResults);
        buttons.addComponent(updateResults);

        final Button doneButton = new Button("Done", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                buttons.setVisible(false);
                discard();
            }
        });
        this.addField("done", doneButton);
        buttons.addComponent(doneButton);
        buttons.setComponentAlignment(doneButton, Alignment.MIDDLE_LEFT);
        buttons.setVisible(false);

        gridLayout.addComponent(buttons, 1, 1);
        gridLayout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);

        customComponent = new CustomComponent() {{setCompositionRoot(panel);}};
    }

    public Component asVaadinComponent() {
        return customComponent;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public Action[] getActions(Object target, Object sender) {
        return actions;
    }

    public void handleAction(Action action, Object sender, Object target) {
        if(action == ENTER_ACTION){
            commit();
            buttons.setVisible(true);
            presenter.onSearch(searchParameters.getBean());
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
     * A Vaadin-specific field factory for the search form.
     * @author fgrilli
     *
     */
    protected class SearchFormFieldFactory extends DefaultFieldFactory {
        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            if(SEARCH_FIELD.equals(propertyId)) {
                final TextField searchField = new TextField();
                searchField.setInputPrompt("Search");
                searchField.setWidth(200, Sizeable.UNITS_PIXELS);
                searchField.addStyleName("m-search-box");

                searchField.addListener(new FocusListener() {

                    public void focus(FocusEvent event) {
                        ((TextField)event.getSource()).setValue("");
                    }
                });
                searchField.addListener(new BlurListener() {

                    public void blur(BlurEvent event) {
                        ((TextField)event.getSource()).setValue("Search");
                    }
                });
                return searchField;
            }
            return null;
        }
    }
}
