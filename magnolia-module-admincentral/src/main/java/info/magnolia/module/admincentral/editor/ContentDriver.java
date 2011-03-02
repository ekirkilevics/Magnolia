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
package info.magnolia.module.admincentral.editor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.RuntimeRepositoryException;
import info.magnolia.module.admincentral.dialog.DialogDefinition;
import info.magnolia.module.admincentral.dialog.DialogField;
import info.magnolia.module.admincentral.dialog.DialogTab;

/**
 * Automates editing of entities defined by the content model.
 *
 * @author tmattsson
 */
public class ContentDriver extends AbstractDriver<Node> {

    /**
     * Describes a connection between an editor and a property on the entity.
     *
     * @author tmattsson
     */
    private static class EditorMapping {

        private String name;
        private Editor editor;
        private Class<?> type;

        public EditorMapping(String name, Editor editor, Class<?> type) {
            this.name = name;
            this.editor = editor;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Editor getEditor() {
            return editor;
        }

        public Class<?> getType() {
            return type;
        }
    }

    private List<EditorMapping> editorMappings = new ArrayList<EditorMapping>();

    public void initialize(DialogBuilder builder, DialogDefinition dialogDefinition) {

        for (DialogTab dialogTab : dialogDefinition.getTabs()) {

            builder.addTab(dialogDefinition, dialogTab);

            for (final DialogField field : dialogTab.getFields()) {

                // TODO it also needs to be give more explicit instructions like 'richText' and things like options.
                // TODO some things might not be a good match with a java type, for instance nt:file
                // TODO some dialog fields dont even have a type, controlType=static for instance

                Class<?> type = getTypeFromDialogControl(field);

                final Editor editor = builder.addField(
                        dialogDefinition,
                        dialogTab,
                        field,
                        type);

                if (editor == null)
                    continue;

                if (editor instanceof HasEditorDelegate) {
                    ((HasEditorDelegate)editor).setDelegate(new EditorDelegate() {
                        public void recordError(String message, Object value) {
                            addError(field.getName(), editor, message, value);
                        }
                    });
                }

                editorMappings.add(new EditorMapping(field.getName(), editor, type));
            }
        }
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

    public void edit(Node node) {

        // TODO default values should also be set here

        try {
            for (EditorMapping mapping : editorMappings) {
                String name = mapping.getName();
                Editor editor = mapping.getEditor();
                Class<?> type = mapping.getType();
                if (type.equals(String.class)) {
                    if (node.hasProperty(name))
                        editor.setValue(node.getProperty(name).getString());
                } else if (type.equals(Calendar.class)) {
                    if (node.hasProperty(name))
                        editor.setValue(node.getProperty(name).getDate());
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void flush(Node node) {
        try {
            for (EditorMapping mapping : editorMappings) {
                String name = mapping.getName();
                Editor editor = mapping.getEditor();
                Class<?> type = mapping.getType();
                Object value = editor.getValue();
                if (!hasErrors(name)) {
                    if (type.equals(String.class)) {
                        node.setProperty(name, (String) value);
                    } else if (type.equals(Calendar.class)) {
                        node.setProperty(name, (Calendar) value);
                    }
                }
            }
            if (!hasErrors())
                node.getSession().save();
            else {
                for (EditorError error : super.getErrors()) {
                    if (error.getEditor() instanceof HasEditorErrors) {
                        HasEditorErrors has = (HasEditorErrors) error.getEditor();
                        has.showErrors(getErrors());
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
