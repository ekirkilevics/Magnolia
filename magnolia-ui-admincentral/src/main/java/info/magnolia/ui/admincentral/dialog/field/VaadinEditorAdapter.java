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
package info.magnolia.ui.admincentral.dialog.field;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Field;
import info.magnolia.ui.framework.editor.EditorDelegate;
import info.magnolia.ui.framework.editor.EditorError;
import info.magnolia.ui.framework.editor.HasEditorDelegate;
import info.magnolia.ui.framework.editor.HasEditorErrors;
import info.magnolia.ui.framework.editor.ValueEditor;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;

/**
 * Adapter class that adapts a Vaadin Field as an Editor.
 *
 * @param <T> the type of the value that the editor works with.
 * @author tmattsson
 */
public class VaadinEditorAdapter<T> implements ValueEditor<T>, HasEditorDelegate, HasEditorErrors {

    private Field field;
    private FieldDefinition fieldDefinition;
    private EditorDelegate delegate;
    private Class<T> type;
    private AbstractDialogField abstractDialogField;

    public VaadinEditorAdapter(Field field, FieldDefinition fieldDefinition, Class<T> type, AbstractDialogField abstractDialogField) {
        this.field = field;
        this.fieldDefinition = fieldDefinition;
        this.type = type;
        this.abstractDialogField = abstractDialogField;
    }

    @Override
    public void setValue(T object) {
        field.setValue(object);
    }

    @Override
    public T getValue() {
        T value = (T) field.getValue();

        // TODO This is very rudimentary validation

        if (type.equals(String.class)) {
            // TODO quick hack to get us something to test with
            if ((fieldDefinition.isRequired() || fieldDefinition.getName().equals("title")) && StringUtils.isEmpty((String) value)) {
                delegate.recordError("Required", value);
            }
        }
        return value;
    }

    @Override
    public void setDelegate(EditorDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void showErrors(List<EditorError> errors) {

        // Clear any previous error
        abstractDialogField.setError(null);

        for (EditorError error : errors) {
            if (error.getEditor() == this) {
                abstractDialogField.setError(error.getMessage());
                error.setConsumed(true);
            }
        }
    }

    @Override
    public String getPath() {
        return fieldDefinition.getName();
    }

    @Override
    public Class<T> getType() {
        return type;
    }
}
