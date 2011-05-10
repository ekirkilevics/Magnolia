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

import info.magnolia.ui.model.toolbar.ToolbarDefinition;
import info.magnolia.ui.model.toolbar.ToolbarItemDefinition;
import info.magnolia.ui.model.toolbar.ToolbarItemGroupDefinition;
import info.magnolia.ui.model.toolbar.registry.ToolbarPermissionSchema;
import info.magnolia.ui.model.toolbar.registry.ToolbarProvider;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Implementation for {@link FunctionToolbarView}.
 * @author fgrilli
 *
 */
public class FunctionToolbarViewImpl implements FunctionToolbarView, IsVaadinComponent{

    private static final Logger log = LoggerFactory.getLogger(FunctionToolbarViewImpl.class);
    private HorizontalLayout outerContainer = new HorizontalLayout();
    private CustomComponent customComponent;
    private Presenter presenter;

    public FunctionToolbarViewImpl(ToolbarProvider toolbarProvider, ToolbarPermissionSchema permissions) {
        outerContainer.setMargin(false,true,false,true);
        outerContainer.addStyleName("m-workbench-header");
        outerContainer.setHeight(50, Sizeable.UNITS_PIXELS);
        outerContainer.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        customComponent = new CustomComponent(){{ setCompositionRoot(outerContainer); }};

        final ToolbarDefinition toolbarDefinition = toolbarProvider.getToolbar();
        if(toolbarDefinition == null){
            log.warn("No function toolbar definition found, won't render it.");
            final Label label = new Label("No function toolbar definition found. Please, check your configuration.");
            label.setSizeFull();
            outerContainer.addComponent(label);
            return;
        }

        final List<ToolbarItemGroupDefinition> groupItems = toolbarDefinition.getGroups();

        if(groupItems == null || groupItems.isEmpty()){
            log.warn("No function toolbar groups found, won't render them.");
            final Label label = new Label("No function toolbar groups found. Please, check your configuration.");
            label.setSizeFull();
            outerContainer.addComponent(label);
            return;
        }

        for(int i = 0; i < groupItems.size(); i++){

            final ToolbarItemGroupDefinition itemGroupDefinition = groupItems.get(i);
            final List<ToolbarItemDefinition> items = itemGroupDefinition.getItems();
            final GridLayout viewGroup = new GridLayout(items.size() + 1, 1);

            viewGroup.setSpacing(true);
            viewGroup.setMargin(true, false, false, false);

            final Label label = new Label(itemGroupDefinition.getGroupLabel());
            viewGroup.addComponent(label, 0, 0);
            viewGroup.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

            for(int j = 0; j < items.size(); j++){
                 final ToolbarItemDefinition item = items.get(j);

                 final Button button = new Button(item.getLabel());
                 button.addListener(new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                       presenter.onToolbarItemSelection(item);
                    }
                });
                viewGroup.addComponent(button, j + 1, 0);
                viewGroup.setComponentAlignment(button, Alignment.MIDDLE_CENTER);
            }
            outerContainer.addComponent(viewGroup);
        }
    }

    @Override
    public Component asVaadinComponent() {
       return customComponent;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

}
