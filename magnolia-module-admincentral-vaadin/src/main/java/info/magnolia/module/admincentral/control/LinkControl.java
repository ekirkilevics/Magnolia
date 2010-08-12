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
package info.magnolia.module.admincentral.control;

import com.vaadin.ui.*;
import info.magnolia.cms.core.Content;
import info.magnolia.module.admincentral.dialog.DialogItem;

import javax.jcr.RepositoryException;

/**
 * Control for selecting a page to link to.
 *
 * TODO: needs to open subwindow with a treetable where the editor can browse to the desired page.
 */
public class LinkControl extends AbstractDialogControl {

    public void addControl(DialogItem dialogItem, Content storageNode, final VerticalLayout layout) {

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        TextField field = new TextField();
        horizontalLayout.addComponent(field);

        Button button = new Button();
        button.setCaption("Browse..");
        button.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {

                Window window = new Window("Choose page");
                window.setModal(true);
                window.setClosable(true);

                layout.getApplication().getMainWindow().addWindow(window);
            }
        });
        horizontalLayout.addComponent(button);

        layout.addComponent(horizontalLayout);
    }

    public void validate() {
    }

    public void save(Content storageNode) throws RepositoryException {
    }
}