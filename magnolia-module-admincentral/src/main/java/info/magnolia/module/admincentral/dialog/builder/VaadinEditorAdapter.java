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
package info.magnolia.module.admincentral.dialog.builder;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Field;

import info.magnolia.module.admincentral.dialog.definition.DialogField;
import info.magnolia.module.admincentral.dialog.view.VaadinDialogField;
import info.magnolia.ui.editor.Editor;
import info.magnolia.ui.editor.EditorDelegate;
import info.magnolia.ui.editor.EditorError;
import info.magnolia.ui.editor.HasEditorDelegate;
import info.magnolia.ui.editor.HasEditorErrors;

/**
 * Adapter class that adapts a Vaadin Field as an Editor.
 *
 * @author tmattsson
 */
public class VaadinEditorAdapter implements Editor, HasEditorDelegate, HasEditorErrors {

    private Field field;
    private DialogField fieldDefinition;
    private EditorDelegate delegate;
    private Class<?> type;
    private VaadinDialogField vaadinDialogField;

    public VaadinEditorAdapter(Field field, DialogField fieldDefinition, Class<?> type, VaadinDialogField vaadinDialogField) {
        this.field = field;
        this.fieldDefinition = fieldDefinition;
        this.type = type;
        this.vaadinDialogField = vaadinDialogField;
    }

    public void setValue(Object object) {
        field.setValue(object);
    }

    public Object getValue() {
        Object value = field.getValue();

        // TODO This is very rudimentary validation

        if (type.equals(String.class)) {
            if (fieldDefinition.isRequired() && StringUtils.isEmpty((String) value)) {
                delegate.recordError("Required", value);
            }
        }
        return value;
    }

    public void setDelegate(EditorDelegate delegate) {
        this.delegate = delegate;
    }

    public void showErrors(List<EditorError> errors) {
        for (EditorError error : errors) {
            if (error.getEditor() == this) {
                vaadinDialogField.setError(error.getMessage());
            }
        }
        // TODO should it clear any error(s) if there's none for this editor in the list?
    }

    public String getName() {
        return fieldDefinition.getName();
    }

    public Class getType() {
        return type;
    }
}
