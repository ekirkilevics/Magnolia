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

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;

/**
 * UI component that displays a label and on double click opens it for editing by switching the label to save text field.
 *
 * @author tmattsson
 */
public abstract class EditableText extends Editable {

    private String path;

    public EditableText(Item item, Presenter presenter, String path) throws RepositoryException {
        super(item, presenter);
        this.path = path;
    }

    private static class TextFieldEditor extends TextField implements ValueEditor<Object> {

        private String path;

        private TextFieldEditor(String path) {
            // setImmediate(false);
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }
    }

    @Override
    protected ComponentAndEditor getComponentAndEditor(Item item) throws RepositoryException {
        final TextFieldEditor textField = new TextFieldEditor(path);
        textField.addListener(new FieldEvents.BlurListener() {

            @Override
            public void blur(FieldEvents.BlurEvent event) {
                cancel();
            }
        });
        textField.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ENTER, new
            int[]{}) {

            @Override
            public void handleAction(Object sender, Object target) {
                save();
            }
        });
        textField.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ESCAPE, new
            int[]{}) {

            @Override
            public void handleAction(Object sender, Object target) {
                cancel();
            }
        });
        textField.focus();
        textField.setSizeUndefined();
        textField.setWidth(100, UNITS_PERCENTAGE);
        Layout layout = new HorizontalLayout();
        layout.setSizeUndefined();
        layout.setWidth(100, UNITS_PERCENTAGE);
        layout.addComponent(textField);
        layout.addStyleName("m-inline-div");
        return new ComponentAndEditor(layout, textField);
    }

}
