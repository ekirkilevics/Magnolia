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

import com.vaadin.event.FieldEvents;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import info.magnolia.ui.admincentral.dialog.view.DialogView;
import info.magnolia.ui.model.dialog.definition.CheckboxFieldDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.TabDefinition;

/**
 * Dialog field for checkbox.
 * <p/>
 * Note: This is the equivalent of controlType=checkboxSwitch
 */
public class DialogCheckboxField extends AbstractDialogField {

    public DialogCheckboxField(DialogDefinition dialogDefinition, TabDefinition tabDefinition, FieldDefinition fieldDefinition, DialogView.Presenter presenter) {
        super(dialogDefinition, tabDefinition, fieldDefinition, presenter);
    }

    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return Boolean.class;
    }

    @Override
    protected Field getField() {
        CheckboxFieldDefinition def = (CheckboxFieldDefinition) getFieldDefinition();
        CheckBox checkBox = new CheckBox();
        checkBox.setValue(def.isDefaultValue());
        checkBox.addListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(FieldEvents.FocusEvent event) {
                getPresenter().onFocus(DialogCheckboxField.this);
            }
        });
        return checkBox;
    }
}
