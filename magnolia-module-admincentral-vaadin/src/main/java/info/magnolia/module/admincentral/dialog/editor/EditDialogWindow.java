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
package info.magnolia.module.admincentral.dialog.editor;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Dialog editor - old style.
 * @author had
 * @version $Id: $
 */
public class EditDialogWindow extends Window {

    private String nodeName;
    private String parentModule;

    public EditDialogWindow(String dialogName, String parentModule) throws RepositoryException {

        this.nodeName = nodeName;
        this.parentModule = parentModule;

        HorizontalLayout buttons = new HorizontalLayout();
        Component mainViewArea;

        Content storageNode = getOrCreateDialogNode(dialogName, parentModule);

        setModal(true);
        setResizable(true);
        setScrollable(false);
        setClosable(false);
        setWidth("600px");
        setHeight("600px");
        setCaption("Edit Dialog Configuration");

        //  noch nicht
        // TabSheet sheet = new TabSheet();

        // for (DialogTab dialogTab : dialog.getTabs()) {
        //
        // GridLayout grid = new GridLayout(2, 1);
        // grid.setSpacing(true);
        // grid.setMargin(true);
        //
        // for (DialogControl dialogItem : dialogTab.getFields()) {
        //
        // dialogItem.create(storageNode, grid);
        //
        // grid.newLine();
        // }
        //
        // sheet.addTab(grid, dialogTab.getLabel(), null);
        // }

        Button close = new Button("buttons.save", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                if (save()) {
                    getApplication().getMainWindow().executeJavaScript("location.reload(true);");
                    closeWindow();
                }
            }
        });
        close.addStyleName("primary");
        close.setClickShortcut(ShortcutAction.KeyCode.ENTER, ShortcutAction.ModifierKey.CTRL);
        buttons.addComponent(close);
        buttons.setComponentAlignment(close, "right");

        mainViewArea = new Droppings(dialogName);

        final String key = "buttons.cancel";
        final String label = MessagesManager.getMessages().getWithDefault(key, key);
        Button cancel = new Button(label, new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                closeWindow();
            }
        });
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, "right");

        VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(mainViewArea);
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, "right");
    }

    private Content getOrCreateDialogNode(String dialogName, String parentModule) throws AccessDeniedException, RepositoryException {
        Content storageNode = ContentUtil.getOrCreateContent(MgnlContext.getHierarchyManager(ContentRepository.CONFIG).getContent("/modules/" + parentModule + "/dialogs"), dialogName, ItemType.CONTENTNODE, true);
        return storageNode;
    }

    private void closeWindow() {
        // close the window by removing it from the parent window
        ((Window) getParent()).removeWindow(this);
    }

    private boolean save() {
        try {

            Content storageNode = getOrCreateDialogNode(this.nodeName, this.parentModule);

            // noch nicht
            // // Validate
            // for (DialogTab dialogTab : dialog.getTabs()) {
            // for (DialogControl control : dialogTab.getFields()) {
            // try {
            // control.validate();
            // } catch (Validator.InvalidValueException e) {
            // getParent().showNotification(e.getMessage(),
            // Notification.TYPE_WARNING_MESSAGE);
            // return false;
            // }
            // }
            // }
            //
            // // Save
            // for (DialogTab dialogTab : dialog.getTabs()) {
            // for (DialogControl control : dialogTab.getFields()) {
            // control.save(storageNode);
            // }
            // }

            saveContent(storageNode);

        } catch (RepositoryException e) {
            e.printStackTrace(); // To change body of catch statement use File |
                                 // Settings | File Templates.
        }

        return true;
    }

    /**
     * Steps up the node hierarchy to find the node at which level we need to
     * perform save on.
     */
    private void saveContent(Content content) throws RepositoryException {
        while (content.getJCRNode().isNew())
            content = content.getParent();
        content.save();
    }

}