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

import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.integration.view.ComponentViewPort;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickEvent;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;


/**
 * The view to edit a workspace. Provides slots for the tree and detail view.
 */
public class WorkbenchView implements View, IsVaadinComponent{

    private static final long serialVersionUID = 7548338054163224225L;
    private VerticalLayout outerLayout;
    private HorizontalSplitPanel splitPanel;
    private ComponentViewPort itemListViewPort;
    private ComponentViewPort detailViewPort;

    public WorkbenchView() {

        splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition(80, Sizeable.UNITS_PERCENTAGE);
        splitPanel.setSizeFull();
        splitPanel.addListener(new SplitterClickListener() {
            private static final long serialVersionUID = 4837023553542505515L;

            public void splitterClick(SplitterClickEvent event) {
                if(event.isDoubleClick()){
                    HorizontalSplitPanel panel = (HorizontalSplitPanel)event.getSource();
                    panel.setSplitPosition(panel.getSplitPosition() > 0 ? 0:15);
                }
            }
        });

        itemListViewPort = new ComponentViewPort();
        detailViewPort = new ComponentViewPort();

        itemListViewPort.setSizeFull();
        detailViewPort.setSizeFull();

        splitPanel.addComponent(itemListViewPort);
        splitPanel.addComponent(detailViewPort);
        splitPanel.setSizeFull();

        SearchForm searchForm = new SearchForm();

        outerLayout = new  VerticalLayout();
        outerLayout.setSizeFull();

        WorkbenchHeaderViewImpl workbenchHeaderView = new WorkbenchHeaderViewImpl();


        outerLayout.addComponent(searchForm);
        outerLayout.addComponent(workbenchHeaderView);
        outerLayout.addComponent(splitPanel);

        //outerLayout.setExpandRatio(searchForm, 1);
        //outerLayout.setExpandRatio(workbenchHeaderView, 1);
        outerLayout.setExpandRatio(splitPanel, 1);
        //outerLayout.setExpandRatio(searchForm, 1);
        //outerLayout.setExpandRatio(splitPanel, 9);
    }

    public Component asVaadinComponent() {
        return outerLayout;
    }

    public ViewPort getItemListViewPort() {
        return itemListViewPort;
    }

    public ViewPort getDetailViewPort() {
        return detailViewPort;
    }

}
