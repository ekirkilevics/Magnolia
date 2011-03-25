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

import java.util.ArrayList;
import java.util.Collection;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.ui.admincentral.workbench.event.ContentChangedEvent;
import info.magnolia.ui.framework.editor.ContentDriver;
import info.magnolia.ui.framework.editor.Editor;
import info.magnolia.ui.framework.editor.HasEditors;
import info.magnolia.ui.framework.editor.ValueEditor;
import info.magnolia.ui.framework.event.EventBus;

/**
 * UI component that displays a label and on double click opens it for editing by switching the label to a text field.
 *
 * @author tmattsson
 *
 * TODO notifying eventBus not yet implemented
 */
public abstract class EditableText extends CustomComponent {

    private static final long serialVersionUID = -3126541389513880027L;
    private final String workspace;
    private final String nodeIdentifier;
    private final String propertyName;
    private ContentDriver driver;
    private ValueEditor<String> editor;
    private EventBus eventBus;

    public EditableText(Item item, final EventBus eventBus, final String path) throws RepositoryException {

        this.eventBus = eventBus;

        this.workspace = item.getSession().getWorkspace().getName();
        this.nodeIdentifier = item instanceof Node ? ((Node) item).getIdentifier() : item.getParent().getIdentifier();
        this.propertyName = item instanceof Property ? (item).getName() : null;

        final HorizontalLayout layout = new HorizontalLayout();
        final Label label = new Label(getValue(item));

        // TODO the double click event should be removed when the text field is visible, otherwise its not possible to double click to mark words

        layout.addListener(new LayoutEvents.LayoutClickListener() {
            private static final long serialVersionUID = -7068955179985809239L;

            public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
                if (event.isDoubleClick()) {
                    final TextField textField = new TextField();
                    textField.addListener(new FieldEvents.BlurListener() {
                        private static final long serialVersionUID = -510289267548470775L;

                        public void blur(FieldEvents.BlurEvent event) {
                            // TODO should we save on blur
                            layout.removeComponent(textField);
                            layout.addComponent(label);
                        }
                    });
                    textField.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ENTER, new int[]{}) {
                        private static final long serialVersionUID = -9182888572843517832L;

                        @Override
                        public void handleAction(Object sender, Object target) {

                            try {
                                Item item1 = getItem();
                                driver.flush(item1);

                                if (driver.hasErrors())
                                    // TODO show validation errors
                                    return;

                                eventBus.fireEvent(new ContentChangedEvent(item1.getSession().getWorkspace().getName(), item1.getPath()));

                                layout.removeComponent(textField);
                                layout.addComponent(label);

                            } catch (RepositoryException e) {
                                throw new RuntimeRepositoryException(e);
                            }
                        }
                    });
                    layout.removeComponent(label);
                    layout.addComponent(textField);
                    textField.focus();
                    textField.setSizeFull();

                    // TODO most probably of common interest - think about Providing proper type "StringEditor"
                    editor = new ValueEditor<String>() {
                        public void setValue(String object) {
                            textField.setValue(object);
                        }

                        public String getValue() {
                            return (String)textField.getValue();
                        }

                        public String getPath() {
                            return path;
                        }

                        public Class<String> getType() {
                            return String.class;
                        }
                    };

                    driver = new ContentDriver();
                    driver.initialize(new HasEditors() {
                        public Collection<? extends Editor> getEditors() {
                            ArrayList<Editor> list = new ArrayList<Editor>();
                            list.add(editor);
                            return list;
                        }
                    });

                    try {
                        driver.edit(getItem());
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }

                }
            }
        });
        layout.addComponent(label);
        layout.setSizeUndefined();
        setCompositionRoot(layout);
        setSizeUndefined();
    }

    protected abstract String getValue(Item item) throws RepositoryException;

    private Item getItem() throws RepositoryException {
        Node node = JCRUtil.getSession(this.workspace).getNodeByIdentifier(this.nodeIdentifier);
        if (propertyName != null)
            return node.getProperty(propertyName);
        return node;
    }
}
