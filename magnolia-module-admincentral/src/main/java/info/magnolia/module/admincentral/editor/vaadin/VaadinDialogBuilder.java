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
package info.magnolia.module.admincentral.editor.vaadin;

import java.util.Calendar;

import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import info.magnolia.module.admincentral.editor.DialogBuilder;
import info.magnolia.module.admincentral.editor.Editor;

/**
 * Builder for VaadinDialog.
 *
 * @author tmattsson
 */
public class VaadinDialogBuilder implements DialogBuilder {

    /**
     * Adapter class that adapts a Vaadin Field as an Editor.
     *
     * @author tmattsson
     */
    private class EditorAdapter implements Editor {

        private Field field;

        private EditorAdapter(Field field) {
            this.field = field;
        }

        public void setValue(Object object) {
            field.setValue(object);
        }

        public Object getValue() {
            return field.getValue();
        }
    }

    private VaadinDialog dialog = new VaadinDialog();

    public VaadinDialog getDialog() {
        return dialog;
    }

    public void addTab(String name, String label) {
        dialog.addTab(name, label);
    }

    public Editor addField(String tabName, String name, String label, String description, Class<?> type) {

        Field field = createFieldForType(type);

        dialog.addField(tabName, name, label, description, field);

        return new EditorAdapter(field);
    }

    private Field createFieldForType(Class<?> type) {
        if (type.equals(String.class))
            return new TextField();
        if (type.equals(Calendar.class))
            return new DateField();
        return null;
    }
}
