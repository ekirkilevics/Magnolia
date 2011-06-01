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

import java.math.BigDecimal;
import java.util.Date;
import javax.jcr.PropertyType;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Component;
import info.magnolia.ui.admincentral.dialog.view.DialogView;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;

/**
 * Abstract base class for implementing dialog fields using the default visual style.
 *
 * @version $Id$
 */
public abstract class AbstractDialogField implements DialogField {

    protected DialogDefinition dialogDefinition;
    protected TabDefinition tabDefinition;
    protected FieldDefinition fieldDefinition;
    protected DialogView.Presenter presenter;
    protected DialogFieldView view;
    protected DialogFieldEditorStrategy editor;

    protected AbstractDialogField(DialogDefinition dialogDefinition, TabDefinition tabDefinition, FieldDefinition fieldDefinition, DialogView.Presenter presenter) {
        this.dialogDefinition = dialogDefinition;
        this.tabDefinition = tabDefinition;
        this.fieldDefinition = fieldDefinition;
        this.presenter = presenter;
        this.view = new DialogFieldView(dialogDefinition, tabDefinition, fieldDefinition);
    }

    @Override
    public Component getComponent() {
        return this.view;
    }

    @Override
    public Editor getEditor() {
        return this.editor;
    }

    @Override
    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public DialogView.Presenter getDialogPresenter() {
        return presenter;
    }

    protected Class<?> getFieldType(FieldDefinition fieldDefinition) {

        if (StringUtils.isNotEmpty(fieldDefinition.getType())) {
            int valueType = PropertyType.valueFromName(fieldDefinition.getType());
            switch (valueType) {
                case PropertyType.STRING:
                    return String.class;
                case PropertyType.LONG:
                    return Long.class;
                case PropertyType.DOUBLE:
                    return Double.class;
                case PropertyType.DATE:
                    // TODO we use Date here instead of Calendar simply because the vaadin DateField uses Date not Calendar
                    return Date.class;
                case PropertyType.BOOLEAN:
                    return Boolean.class;
                case PropertyType.DECIMAL:
                    return BigDecimal.class;
                default:
                    throw new IllegalArgumentException("Unsupported property type " + PropertyType.nameFromValue(valueType));
            }
        }
        return getDefaultFieldType(fieldDefinition);
    }

    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        throw new IllegalArgumentException("Unsupported type " + fieldDefinition.getClass().getName());
    }
}
