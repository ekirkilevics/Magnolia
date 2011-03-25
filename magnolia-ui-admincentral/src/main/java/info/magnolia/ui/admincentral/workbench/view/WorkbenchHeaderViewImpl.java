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

import javax.jcr.Item;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

/**
 * TODO write javadoc.
 * @author fgrilli
 *
 */
public class WorkbenchHeaderViewImpl extends CustomComponent implements WorkbenchHeaderView, WorkbenchHeaderView.Presenter{

    private static final long serialVersionUID = 1L;

    private GridLayout outerContainer;

    public WorkbenchHeaderViewImpl() {
        outerContainer = new GridLayout(3,1);
        outerContainer.setMargin(false,true,false,true);
        outerContainer.addStyleName("workbench-header");
        outerContainer.setHeight(50, Sizeable.UNITS_PIXELS);
        outerContainer.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        setCompositionRoot(outerContainer);

        final Label viewAsLabel = new Label("View as");
        final Button treeButton = new Button("Tree");
        final Button listButton = new Button("List");
        final GridLayout viewGroup = new GridLayout(3,1);

        viewGroup.addComponent(viewAsLabel, 0, 0);
        viewGroup.setComponentAlignment(viewAsLabel, Alignment.MIDDLE_CENTER);

        viewGroup.addComponent(treeButton, 1, 0);
        viewGroup.setComponentAlignment(treeButton, Alignment.MIDDLE_CENTER);

        viewGroup.addComponent(listButton, 2, 0);
        viewGroup.setComponentAlignment(listButton, Alignment.MIDDLE_CENTER);

        viewGroup.setSpacing(true);
        viewGroup.setMargin(true, false, false, false);
        outerContainer.addComponent(viewGroup, 0, 0);

        /*final Label groupByLabel = new Label("Group by");
        final ComboBox groupByComboBox = new ComboBox();
        //TODO get these data dynamically
        groupByComboBox.addItem("(don't group)");
        groupByComboBox.addItem("name");
        groupByComboBox.addItem("title");
        groupByComboBox.addItem("template");
        groupByComboBox.addItem("date");
        groupByComboBox.addItem("status");

        final GridLayout groupByGroup = new GridLayout(2,1);
        groupByGroup.addComponent(groupByLabel, 0, 0);
        groupByGroup.addComponent(groupByComboBox, 1, 0);

        groupByGroup.setSpacing(true);
        outerContainer.addComponent(groupByGroup, 1, 0);

        final CheckBox showParentsCheckbox = new CheckBox();
        final Label showParentsLabel = new Label("show parents");
        final GridLayout showParentsGroup = new GridLayout(2,1);

        showParentsGroup.addComponent(showParentsCheckbox, 0, 0);
        showParentsGroup.addComponent(showParentsLabel, 1, 0);
        outerContainer.addComponent(showParentsGroup, 2, 0);*/

        /*outerContainer.setColumnExpandRatio(0, 20);
        outerContainer.setColumnExpandRatio(1, 10);
        outerContainer.setColumnExpandRatio(2, 90);*/

    }

    public void onViewSelection(String viewName) {
        // TODO Auto-generated method stub

    }

    public void onShowParents(Item item) {
        // TODO Auto-generated method stub

    }

}
