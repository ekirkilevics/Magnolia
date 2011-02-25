/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.views;

import info.magnolia.ui.component.HasComponent;
import info.magnolia.ui.component.IsComponent;
import info.magnolia.vaadin.component.ComponentDisplay;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickEvent;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;


/**
 * The view to edit a workspace. Provides slots for the tree and detail view.
 */
public class EditWorkspaceView implements IsComponent{

    private HorizontalSplitPanel splitPanel;
    private ComponentDisplay treeDisplay;
    private ComponentDisplay detailViewDisplay;

    public EditWorkspaceView() {

        splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition(80, Sizeable.UNITS_PERCENTAGE);
        splitPanel.setSizeFull();
        splitPanel.addListener(new SplitterClickListener() {
            private static final long serialVersionUID = 4837023553542505515L;

            public void splitterClick(SplitterClickEvent event) {
                if(event.isDoubleClick()){
                    HorizontalSplitPanel panel = (HorizontalSplitPanel)event.getSource();
                    if(panel.getSplitPosition() > 0){
                        panel.setSplitPosition(0);
                    }else {
                        panel.setSplitPosition(15);
                    }
                }
            }
        });

        treeDisplay = new ComponentDisplay();
        detailViewDisplay = new ComponentDisplay();

        treeDisplay.setSizeFull();
        detailViewDisplay.setSizeFull();

        splitPanel.addComponent(treeDisplay);
        splitPanel.addComponent(detailViewDisplay);
    }

    public Component asComponent() {
        return splitPanel;
    }

    public HasComponent getTreeDisplay() {
        return treeDisplay;

    }

    public HasComponent getDetailDisplay() {
        return detailViewDisplay;
    }

}
