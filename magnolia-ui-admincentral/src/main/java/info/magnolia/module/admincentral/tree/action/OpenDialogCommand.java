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
package info.magnolia.module.admincentral.tree.action;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.dialog.view.DialogPresenter;
import info.magnolia.module.admincentral.tree.JcrBrowser;

/**
 * Opens a dialog for editing a node in a tree.
 *
 * TODO: add support for configuring supported itemTypes, maybe in base class where no config means all
 *
 * @author tmattsson
 */
public class OpenDialogCommand extends Command {

    private DialogPresenter dialogPresenter;
    private String dialog;

    public OpenDialogCommand(DialogPresenter dialogPresenter) {
        this.dialogPresenter = dialogPresenter;
    }

    @Override
    public boolean isAvailable(Item item) {
        return item instanceof Node;
    }

    @Override
    public void execute(JcrBrowser jcrBrowser, Item item) throws RepositoryException {

        // We need to send the workspace as well

        dialogPresenter.showDialog((Node) item, "userpreferences");
//        AdminCentralApplication.placeController.goTo(new DialogPlace("howTo", item.getPath()));
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }
}
