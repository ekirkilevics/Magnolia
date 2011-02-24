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
package info.magnolia.module.admincentral.dialog;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.event.MouseEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * TODO: write javadoc.
 *
 * @author tmattsson
 *
 */
public class VerticalTabSheet extends CustomComponent {

    private static class Tab {

        private String label;
        private Component tabComponent;
        private Component component;
        private String name;

        public Component getTabComponent() {
            return tabComponent;
        }

        public void setTabComponent(Component tabComponent) {
            this.tabComponent = tabComponent;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Component getComponent() {
            return component;
        }

        public void setComponent(Component component) {
            this.component = component;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private VerticalLayout canvas = new VerticalLayout();
    private VerticalLayout tabList = new VerticalLayout();
    private List<Tab> tabs = new ArrayList<Tab>();
    private int selected = -1;

    public VerticalTabSheet() {
        HorizontalLayout h = new HorizontalLayout();
        h.setSizeFull();
        h.setSpacing(false);
        h.setMargin(false);
        canvas = new VerticalLayout();
        canvas.setSpacing(false);
        canvas.setMargin(false);
        canvas.setSizeFull();
        canvas.addStyleName("m-vtabsheet-canvas");
        tabList = new VerticalLayout();
        tabList.setSpacing(false);
        tabList.setMargin(false);
        tabList.setSizeFull();
        h.addComponent(canvas);
        h.addComponent(tabList);
        h.setExpandRatio(canvas, 7);
        h.setExpandRatio(tabList, 1);
        setCompositionRoot(h);
    }

    public void addTab(String name, String label, final Component component) {

        Panel tabComponent = new Panel();
        tabComponent.setWidth("100%");
        tabComponent.setSizeFull();
        Label labelComponent = new Label(name);
        labelComponent.setWidth("100%");
        labelComponent.setSizeFull();
        tabComponent.addComponent(labelComponent);

        Tab tab = new Tab();
        tab.setName(name);
        tab.setLabel(label);
        tab.setComponent(component);
        tab.setTabComponent(tabComponent);

        tabs.add(tab);
        final int index = tabs.size() - 1;

        component.setVisible(false);
        tabComponent.addStyleName("vtabsheet-tab");

        tabComponent.addListener(new MouseEvents.ClickListener() {
            public void click(MouseEvents.ClickEvent event) {
                selectTab(index);
            }
        });

        tabList.addComponent(tabComponent);
        canvas.addComponent(component);

        if (selected == -1)
            selectTab(index);
    }

    public Component getTab(String name) {
        for (Tab tab : tabs) {
            if (tab.getName().equals(name))
                return tab.getComponent();
        }
        throw new IllegalArgumentException("No tab found for name [" + name+ "]");
    }

    public void selectTab(int position) {
        if (position == selected)
            return;

        if (selected != -1) {
            Tab previous = tabs.get(selected);
            previous.getComponent().setVisible(false);
            previous.getTabComponent().removeStyleName("vtabsheet-selectedtab");
            previous.getTabComponent().addStyleName("vtabsheet-tab");
        }

        Tab next = tabs.get(position);
        next.getComponent().setVisible(true);
        next.getTabComponent().addStyleName("vtabsheet-selectedtab");
        next.getTabComponent().removeStyleName("vtabsheet-tab");

        selected = position;
    }
}
