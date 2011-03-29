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

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.TextField;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.framework.editor.ValueEditor;

/**
 * UI component that displays onSave label and on double click opens it for editing by switching the label to onSave text field.
 *
 * @author tmattsson
 */
public abstract class EditableText extends AbstractEditable {

    private String path;

    public EditableText(Item item, Presenter presenter, String path) throws RepositoryException {
        super(item, presenter);
        this.path = path;
    }

    private static class TextFieldEditor extends TextField implements ValueEditor {

        private String path;

        private TextFieldEditor(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    @Override
    protected Editor getComponentAndEditor(Item item) throws RepositoryException {
        final TextFieldEditor textField = new TextFieldEditor(path);
        textField.addListener(new FieldEvents.BlurListener() {

            public void blur(FieldEvents.BlurEvent event) {
                onCancel();
            }
        });
        textField.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ENTER, new int[]{}) {

            @Override
            public void handleAction(Object sender, Object target) {
                onSave();
            }
        });
        textField.focus();
        textField.setSizeFull();

        return textField;
    }
}
