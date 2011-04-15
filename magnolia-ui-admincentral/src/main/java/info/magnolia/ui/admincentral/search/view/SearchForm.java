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

import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

/**
 * Simple form for searching.
 *
 * @author dlipp
 *
 * TODO: check where to move, make nice design, animate
 */
public class SearchForm implements IsVaadinComponent, Property.ValueChangeListener {

    private CustomComponent customComponent;

    public SearchForm() {
        final GridLayout grid = new GridLayout(2, 1);

        grid.setHeight(40, Sizeable.UNITS_PIXELS);
        grid.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        customComponent = new CustomComponent(){{ setCompositionRoot(grid); }};

        // Note: right now the only component in the first row - prepare for fading in the other stuff only after
        // someone started a search...
        TextField searchField = new TextField();
        searchField.addListener(this);
        searchField.setImmediate(false);
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

        grid.addComponent(searchField, 1, 0);
        grid.setComponentAlignment(searchField, Alignment.MIDDLE_RIGHT);
        grid.setMargin(false, true, false, false);

        /*Label basicSearch = new Label("Basic search");
        grid.addComponent(basicSearch, 0, 1);
        grid.setComponentAlignment(basicSearch, Alignment.MIDDLE_LEFT);

        Label resultLabel = new Label("5 pages where found containing yout text...");
        resultLabel.setSizeFull();
        grid.addComponent(resultLabel, 0, 2);
        grid.setComponentAlignment(resultLabel, Alignment.MIDDLE_LEFT);

        Button updateResultsButton = new Button("Update results");
        Button doneButton = new Button("Done");
        HorizontalLayout buttonForm = new HorizontalLayout();
        buttonForm.addComponent(updateResultsButton);
        buttonForm.addComponent(doneButton);
        buttonForm.setSizeFull();

        grid.addComponent(buttonForm, 1, 2);
        grid.setComponentAlignment(buttonForm, Alignment.MIDDLE_RIGHT);*/

        grid.setColumnExpandRatio(0, 5);
        grid.setColumnExpandRatio(1, 1);
    }

    public void valueChange(ValueChangeEvent event) {
        System.out.println(event.getProperty().getValue());
    }

    public Component asVaadinComponent() {
        return customComponent;
    }

}
