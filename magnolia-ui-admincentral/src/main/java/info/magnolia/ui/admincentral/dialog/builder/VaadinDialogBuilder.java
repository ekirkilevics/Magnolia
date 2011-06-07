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
package info.magnolia.ui.admincentral.dialog.builder;

import com.vaadin.Application;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.ui.admincentral.dialog.field.DialogField;
import info.magnolia.ui.admincentral.dialog.field.DialogFieldFactory;
import info.magnolia.ui.admincentral.dialog.support.DialogLocalizationUtil;
import info.magnolia.ui.admincentral.dialog.view.DialogView;
import info.magnolia.ui.admincentral.dialog.view.DialogViewImpl;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;

/**
 * Builder for DialogViewImpl.
 *
 * @version $Id$
 */
public class VaadinDialogBuilder implements DialogBuilder {

    private Application application;
    private DialogFieldFactory dialogFieldFactory;

    public VaadinDialogBuilder(Application application, DialogFieldFactory dialogFieldFactory) {
        this.application = application;
        this.dialogFieldFactory = dialogFieldFactory;
    }

    @Override
    public DialogView build(DialogDefinition dialogDefinition, DialogView.Presenter presenter) {

        // TODO: shouldn't we use IoC here? (Not really, this is all vaadin specific and that's why it has a hard dependency on DialogViewImpl)
        DialogViewImpl dialog = new DialogViewImpl();

        String label = DialogLocalizationUtil.getMessages(dialogDefinition).getWithDefault(dialogDefinition.getLabel(), dialogDefinition.getLabel());

        dialog.setCaption(label);

        for (TabDefinition tabDefinition : dialogDefinition.getTabs()) {

            addTab(dialog, dialogDefinition, tabDefinition);

            for (FieldDefinition fieldDefinition : tabDefinition.getFields()) {

                addField(dialog, dialogDefinition, tabDefinition, fieldDefinition, presenter);
            }
        }

        // This shouldn't be in the builder
        application.getMainWindow().addWindow(dialog);

        return dialog;
    }

    private void addTab(DialogViewImpl dialog, DialogDefinition dialogDefinition, TabDefinition tabDefinition) {
        Messages messages = DialogLocalizationUtil.getMessages(dialogDefinition, tabDefinition);
        String label = messages.getWithDefault(tabDefinition.getLabel(), tabDefinition.getLabel());
        dialog.addTab(tabDefinition.getName(), label);
    }

    private void addField(DialogViewImpl dialog, DialogDefinition dialogDefinition, TabDefinition tabDefinition, FieldDefinition fieldDefinition, DialogView.Presenter presenter) {

        DialogField dialogField = dialogFieldFactory.getDialogField(dialogDefinition, tabDefinition, fieldDefinition, presenter);

        if (dialogField == null) {
            dialog.addField(tabDefinition.getName(), "Missing UI component for field description " + fieldDefinition.getClass().getName());
            return;
        }

        dialog.addField(tabDefinition.getName(), dialogField.getComponent());
        Editor editor = dialogField.getEditor();
        if (editor != null)
            dialog.addEditor(tabDefinition, editor);
    }
}
