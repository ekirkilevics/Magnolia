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
package info.magnolia.module.admincentral.dialog;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.vaadin.activity.AbstractActivity;
import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;

/**
 * Activity for dialogs.
 *
 * @author tmattsson
 */
public class DialogActivity extends AbstractActivity implements DialogView.Presenter {

    private DialogPlace place;
    private UIModel uiModel;
    private DialogView dialogView;

    public DialogActivity(DialogPlace place, UIModel uiModel) {
        this.place = place;
        this.uiModel = uiModel;
    }

    public void start(HasComponent display, EventBus eventBus) {
        try {

            String dialogName = place.getDialogName();
            DialogDefinition dialogDefinition = uiModel.getDialogDefinition(dialogName);

            Item item = uiModel.getItem("usersAdmin", place.getPath());
            dialogView = new DialogView(this, item.getSession().getWorkspace().getName(), item.getPath(), null, null, dialogName, dialogDefinition);
            display.setComponent(dialogView);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String mayStop() {
        return "You might have unsaved changes, do you really want to leave this page?";
    }

    public void onSave() {
    }

    public void onCancel() {
    }

    public void onClose() {
        // TODO at this point we should go back to where we came from
    }
}
