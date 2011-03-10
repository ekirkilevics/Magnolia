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

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

import com.vaadin.Application;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.ui.admincentral.dialog.view.DialogView;
import info.magnolia.ui.admincentral.dialog.view.DialogViewImpl;
import info.magnolia.ui.admincentral.dialog.view.VaadinDialogField;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.DialogField;
import info.magnolia.ui.model.dialog.definition.DialogTab;

/**
 * Builder for DialogViewImpl.
 *
 * @author tmattsson
 */
public class VaadinDialogBuilder implements DialogBuilder {

    private Application application;

    public VaadinDialogBuilder(Application application) {
        this.application = application;
    }

    public DialogView build(DialogDefinition dialogDefinition) {

        DialogViewImpl dialog = new DialogViewImpl();

        application.getMainWindow().addWindow(dialog);

        dialog.setCaption(getMessages(dialogDefinition).getWithDefault(dialogDefinition.getLabel(), dialogDefinition.getLabel()));

        for (DialogTab dialogTab : dialogDefinition.getTabs()) {


            addTab(dialog, dialogDefinition, dialogTab);

            for (final DialogField field : dialogTab.getFields()) {

                // TODO it also needs to be give more explicit instructions like 'richText' and things like options.
                // TODO some things might not be a good match with a java type, for instance nt:file
                // TODO some dialog fields dont even have a type, controlType=static for instance

                Class<?> type = getTypeFromDialogControl(field);

                final Editor<?> editor = addField(
                        dialog,
                        dialogDefinition,
                        dialogTab,
                        field,
                        type);

                if(editor != null){
                    //FIXME we add the field, so we could add the editor then?
                    dialog.addEditor(editor);
                }
            }
        }
        return dialog;
    }

    private Class<?> getTypeFromDialogControl(DialogField field) {
        if (field.getControlType().equals("edit"))
            return String.class;
        if (field.getControlType().equals("date"))
            return Calendar.class;
        if (field.getControlType().equals("richText"))
            return String.class;
        if (field.getControlType().equals("password"))
            return String.class;
        if (field.getControlType().equals("checkboxSwitch"))
            return Boolean.class;
        return null;
//        throw new IllegalArgumentException("Unsupported type " + dialogControl.getClass());
    }

    public void addTab(DialogViewImpl dialog, DialogDefinition dialogDefinition, DialogTab tab) {
        Messages messages = getMessages(dialogDefinition, tab);
        String label = messages.getWithDefault(tab.getLabel(), tab.getLabel());
        dialog.addTab(tab.getName(), label);
    }

    public Editor addField(DialogViewImpl dialog, DialogDefinition dialogDefinition, DialogTab tab, DialogField fieldDefinition, Class<?> type) {
        Messages messages = getMessages(dialogDefinition, tab, fieldDefinition);

        // TODO for controlType=static we need something completely different, it isnt even an editor...

        if (fieldDefinition.getControlType().equals("static")) {
            dialog.addField(tab.getName(), fieldDefinition.getLabel());
            return null;
        }

        Field field = createFieldForType(fieldDefinition, type);

        if (field == null) {
            dialog.addField(tab.getName(), "Missing UI component for controlType=" + fieldDefinition.getControlType());
            return null;
        }

        String label = messages.getWithDefault(fieldDefinition.getLabel(), fieldDefinition.getLabel());
        String description = messages.getWithDefault(fieldDefinition.getDescription(), fieldDefinition.getDescription());
        VaadinDialogField vaadinDialogField = dialog.addField(tab.getName(), fieldDefinition.getName(), label, description, field);

        // XXX: the editor adapter has to keep references to more ui components than just the field because it needs to display error messages

        return new VaadinEditorAdapter(field, fieldDefinition, type, vaadinDialogField);
    }

    private Field createFieldForType(DialogField field, Class<?> type) {
        if (field.getControlType().equals("edit"))
            return new TextField();
        if (field.getControlType().equals("date"))
            return new DateField();
        if (field.getControlType().equals("richText"))
            return new RichTextArea();
        if (field.getControlType().equals("password"))
            return new PasswordField();
        if (field.getControlType().equals("checkboxSwitch"))
            return new CheckBox();
        return null;
    }

    public Messages getMessages(DialogDefinition dialogDefinition) {
        Messages messages = MessagesManager.getMessages();
        if (StringUtils.isNotEmpty(dialogDefinition.getI18nBasename())) {
            messages = MessagesUtil.chain(dialogDefinition.getI18nBasename(), messages);
        }
        return messages;
    }

    private Messages getMessages(DialogDefinition dialogDefinition, DialogTab tab) {
        Messages messages = getMessages(dialogDefinition);
        if (StringUtils.isNotEmpty(tab.getI18nBasename())) {
            messages = MessagesUtil.chain(tab.getI18nBasename(), messages);
        }
        return messages;
    }

    private Messages getMessages(DialogDefinition dialogDefinition, DialogTab tab, DialogField field) {
        Messages messages = getMessages(dialogDefinition, tab);
        if (StringUtils.isNotEmpty(field.getI18nBasename())) {
            messages = MessagesUtil.chain(field.getI18nBasename(), messages);
        }
        return messages;
    }
}
