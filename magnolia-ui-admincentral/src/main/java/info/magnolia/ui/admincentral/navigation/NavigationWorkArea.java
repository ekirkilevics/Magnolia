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
package info.magnolia.ui.admincentral.navigation;

import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;


/**
 * Represents a collection of menus (that is {@link NavigationGroup} objects). The type and number of work areas will depend on user's privileges.
 *
 * @author fgrilli
 */
public class NavigationWorkArea implements IsVaadinComponent {

    private List<NavigationGroup> navigationGroupView;
    private VerticalLayout container = new VerticalLayout();
    private CustomComponent customComponent;

    public NavigationWorkArea(List<NavigationGroup> navigationGroupView) {
        customComponent = new CustomComponent(){{setCompositionRoot(container);}};
        customComponent.setSizeFull();
        this.navigationGroupView = navigationGroupView;

        for(NavigationGroup group: navigationGroupView){
            container.addComponent(group.asVaadinComponent());
            group.setNavigationWorkarea(this);
        }

        //all work areas start not visible
        customComponent.setVisible(false);
    }

    public List<NavigationGroup> getNavigationGroup() {
        return navigationGroupView;
    }

    @Override
    public Component asVaadinComponent() {
        return customComponent;
    }

    public void setVisible(boolean visible){
        customComponent.setVisible(visible);
    }
}
