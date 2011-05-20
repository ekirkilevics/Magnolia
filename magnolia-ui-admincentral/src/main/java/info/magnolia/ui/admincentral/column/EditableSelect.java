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

import info.magnolia.ui.framework.editor.ValueEditor;

import java.util.Map;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeSelect;

/**
 * UI component that displays a label and on double click opens it for editing by switching the label to a select field.
 *
 * @author tmattsson
 */
public abstract class EditableSelect extends Editable {

    private Map<String, String> options;
    private String path;

    public EditableSelect(Item item, Presenter presenter, final String path, final Map<String, String> options) throws RepositoryException {
        super(item, presenter);
        this.path = path;
        this.options = options;
    }

    private static class SelectEditor extends NativeSelect implements ValueEditor {

        private String path;

        public SelectEditor(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }
    }

    @Override
    protected ComponentAndEditor getComponentAndEditor(Item item) {

        SelectEditor select = new SelectEditor(path);
        select.setNullSelectionAllowed(false);
        select.setNewItemsAllowed(false);

        for (Map.Entry<String, String> entry : options.entrySet()) {
            select.addItem(entry.getValue());
            select.setItemCaption(entry.getValue(), entry.getKey());
        }

        select.focus();
        select.setImmediate(true);
        select.setInvalidAllowed(false);
/*
        select.addListener(new FieldEvents.BlurListener() {

            public void blur(FieldEvents.BlurEvent event) {
                    onSave();
            }
        });
*/
        select.setSizeUndefined();
        select.setWidth(100, UNITS_PERCENTAGE);
        Layout layout = new HorizontalLayout();
        layout.setSizeUndefined();
        layout.setWidth(100, UNITS_PERCENTAGE);
        layout.addComponent(select);

        return new ComponentAndEditor(layout, select);
    }
}
