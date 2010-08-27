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
package info.magnolia.module.wcm.pageeditor.server;

import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.dialog.EditParagraphWindow;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

/**
 * The edit bar. Knows the uuid it is assigned to.
 */
public class EditBar extends CustomComponent {
    private String uuid;

    public EditBar(final String uuid) {
        super();

        this.uuid = uuid;

        VerticalLayout layout = new VerticalLayout();
        Button editButton = new Button("Edit");
        editButton.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    EditParagraphWindow dialog = Hacks.getDialogWindow(uuid);
                    getApplication().getMainWindow().addWindow(dialog);

                }
                catch (RepositoryException e) {
                    e.printStackTrace();
                    getWindow().showNotification(e.getMessage(), Notification.TYPE_ERROR_MESSAGE );
                }

            }
        });
        layout.addComponent(editButton);

        setCompositionRoot(layout);
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("uuid", uuid);
        super.paintContent(target);
    }

}
