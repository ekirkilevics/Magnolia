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

import javax.inject.Singleton;

import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.integration.view.ComponentViewPort;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickEvent;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;


/**
 * Implementation for {@link WorkbenchView}.
 */
@Singleton
public class WorkbenchViewImpl implements WorkbenchView{

    private VerticalLayout outerLayout;
    private HorizontalSplitPanel splitPanel;
    private ComponentViewPort itemListViewPort;
    private ComponentViewPort sidebarViewPort;
    private ComponentViewPort functionToolbarViewPort;
    private ComponentViewPort searchViewPort;

    public WorkbenchViewImpl() {

        splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition(20, Sizeable.UNITS_PERCENTAGE, true);
        splitPanel.setSizeFull();
        splitPanel.addListener(new SplitterClickListener() {

            @Override
            public void splitterClick(SplitterClickEvent event) {
                if(event.isDoubleClick()){
                    HorizontalSplitPanel panel = (HorizontalSplitPanel)event.getSource();
                    panel.setSplitPosition(panel.getSplitPosition() > 0 ? 0 : 20, Sizeable.UNITS_PERCENTAGE, true);
                }
            }
        });

        itemListViewPort = new ComponentViewPort();
        sidebarViewPort = new ComponentViewPort();
        functionToolbarViewPort = new ComponentViewPort();
        searchViewPort = new ComponentViewPort();

        itemListViewPort.setSizeFull();
        sidebarViewPort.setSizeFull();

        splitPanel.addComponent(itemListViewPort);
        splitPanel.addComponent(sidebarViewPort);
        splitPanel.setSizeFull();

        outerLayout = new  VerticalLayout();
        outerLayout.setSizeFull();

        outerLayout.addComponent(functionToolbarViewPort);
        outerLayout.addComponent(splitPanel);

        outerLayout.setExpandRatio(splitPanel, 1);

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
