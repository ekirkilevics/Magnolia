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
package info.magnolia.ui.admincentral.workbench.view;

import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.integration.view.ComponentContainerViewPort;

import javax.inject.Singleton;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;


/**
 * Implementation for {@link WorkbenchView}.
 */
@Singleton
public class WorkbenchViewImpl implements WorkbenchView {

    private CssLayout outerLayout = new CssLayout();

    private ComponentContainerViewPort functionToolbarViewPort;

    private ComponentContainerViewPort sidebarViewPort;

    private ComponentContainerViewPort itemListViewPort;

    public WorkbenchViewImpl() {
        outerLayout.setSizeFull();
        outerLayout.setStyleName("workbench-view");
        functionToolbarViewPort = new ComponentContainerViewPort(outerLayout);

        CssLayout innerLayout = new CssLayout();
        innerLayout.setSizeFull();
        innerLayout.setStyleName("workbench-view-inner");
        outerLayout.addComponent(innerLayout);
        sidebarViewPort = new ComponentContainerViewPort(innerLayout);
        itemListViewPort = new ComponentContainerViewPort(innerLayout);
    }

    @Override
    public Component asVaadinComponent() {
        return outerLayout;
    }

    @Override
    public ViewPort getItemListViewPort() {
        return itemListViewPort;
    }

    @Override
    public ViewPort getSidebarViewPort() {
        return sidebarViewPort;
    }

    @Override
    public ViewPort getFunctionToolbarViewPort() {
        return functionToolbarViewPort;
    }

}
