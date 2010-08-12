/**
 * This file Copyright (c) 2010 Magnolia International
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

import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;

/**
 * Abstract DialogProvider that creates the dialog from a DialogDefinition.
 */
public abstract class AbstractDialogProvider implements DialogProvider {

    public Window createDialog(Content storageNode) throws RepositoryException {

        final Window subwindow = new Window("Edit paragraph");
        subwindow.setModal(true);
        subwindow.setResizable(false);
        subwindow.setScrollable(false);
        subwindow.setClosable(false);
        subwindow.setWidth("400px");

        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);

        TabSheet sheet = new TabSheet();
        layout.addComponent(sheet);

        final DialogDefinition dialog = getDialogDefinition();

        for (DialogTab dialogTab : dialog.getTabs()) {

            GridLayout grid = new GridLayout(2, 1);
            grid.setSpacing(true);
            grid.setMargin(true);

            for (DialogControl dialogItem : dialogTab.getControls()) {

                dialogItem.create(storageNode, grid);

                grid.newLine();
            }

            sheet.addTab(grid, dialogTab.getLabel(), null);
        }

        HorizontalLayout buttons = new HorizontalLayout();

        final String uuid = storageNode.getUUID();

        Button close = new Button("Save", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                try {
                    final HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
                    Content storageNode = hm.getContentByUUID(uuid);

                    // Validate
                    for (DialogTab dialogTab : dialog.getTabs()) {
                        for (DialogControl control : dialogTab.getControls()) {
                            try {
                                control.validate();
                            } catch (Validator.InvalidValueException e) {
                                subwindow.getParent().showNotification(e.getMessage(), Window.Notification.TYPE_WARNING_MESSAGE);
                                return;
                            }
                        }
                    }

                    // Save
                    for (DialogTab dialogTab : dialog.getTabs()) {
                        for (DialogControl control : dialogTab.getControls()) {
                            control.save(storageNode);
                        }
                    }

                    storageNode.save();

                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                ((Window) subwindow.getParent()).removeWindow(subwindow);
            }
        });
        buttons.addComponent(close);
        buttons.setComponentAlignment(close, "right");

        Button cancel = new Button("Cancel", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                // close the window by removing it from the parent window
                ((Window) subwindow.getParent()).removeWindow(subwindow);
            }
        });
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, "right");

        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, "right");

        return subwindow;
    }

    protected abstract DialogDefinition getDialogDefinition() throws RepositoryException;
}
