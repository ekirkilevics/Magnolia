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
package info.magnolia.ui.admincentral.dialog.activity;

import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.dialog.place.DialogPlace;
import info.magnolia.ui.admincentral.dialog.view.DialogView;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.editor.ContentDriver;
import info.magnolia.ui.framework.editor.EditorError;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogRegistry;

/**
 * Activity for dialogs.
 * FIXME very similar to DialogWindow
 *
 * @author tmattsson
 */
public class DialogActivity extends AbstractActivity implements DialogView.Presenter {

    private ComponentProvider componentProvider;
    private DialogPlace place;
    private ContentDriver driver;
    private DialogRegistry dialogRegistry;

    public DialogActivity(ComponentProvider componentProvider, DialogPlace place, DialogRegistry dialogRegistry) {
        this.componentProvider = componentProvider;
        this.place = place;
        this.dialogRegistry = dialogRegistry;
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        try {

            Node node = getNode();

            String dialogName = place.getDialogName();
            DialogDefinition dialogDefinition = dialogRegistry.getDialog(dialogName);

            DialogBuilder builder = componentProvider.newInstance(DialogBuilder.class);
            DialogView dialog = builder.build(dialogDefinition);

            driver = new ContentDriver();
            driver.initialize(dialog);
            driver.edit(node);

            dialog.setPresenter(this);

            viewPort.setView(dialog);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private Node getNode() throws RepositoryException {
        return (Node) JCRUtil.getSession("users").getItem(this.place.getPath());
    }

    public String mayStop() {
        return "You might have unsaved changes, do you really want to leave this page?";
    }

    public void onSave() {
        try {
            driver.flush(getNode());

            List<EditorError> unconsumedErrors = driver.getErrors();

            // TODO unconsumed validation errors that occurred should be displayed here

            // TODO if there was no errors then the dialog should close and we return to the previous place

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void onCancel() {
    }
}
