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
package info.magnolia.ui.admincentral.column;

import info.magnolia.ui.admincentral.container.JcrContainer;

import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;


/**
 * Event handler for a table that uses JcrContainer. When the user clicks on a cell that should
 * allow editing, it converts the cell's label into an editable field. When the user finishes
 * editing, it will revert back to a label again.
 * 
 * @author mrichert
 */
public class EditHandler implements ItemClickListener {

    private Table table;

    public EditHandler(Table table) {
        this.table = table;
        table.addListener(this);
    }

    @Override
    public void itemClick(ItemClickEvent event) {
        table = (Table) event.getSource();
        final Object itemId = event.getItemId();
        final String propertyId = (String) event.getPropertyId();
        if (table.isSelected(itemId)) {
            Property containerProperty = table.getContainerProperty(itemId,
                propertyId);
            Object value = containerProperty.getValue();
            JcrContainer jcrContainer = (JcrContainer) table.getContainerDataSource();

            if (value instanceof String) {
                value = jcrContainer.getColumnValue(propertyId, itemId);
            }

            if (value instanceof Editable) {
                final Editable editable = (Editable) value;

                editable.addListener(new EditListener() {

                    @Override
                    public void edit(EditEvent event) {
                        table.getContainerProperty(itemId,
                                propertyId).setValue(editable);
                    }
                });

                Component editorComponent = editable.getEditorComponent();
                containerProperty.setValue(editorComponent);
            }
        }
    }
}
