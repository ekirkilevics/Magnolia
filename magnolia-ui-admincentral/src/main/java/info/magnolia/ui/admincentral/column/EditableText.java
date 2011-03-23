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
import javax.jcr.Node;
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

/**
 * UI component that displays a label and on double click opens it for editing by switching the label to a text field.
 *
 * @author tmattsson
 */
public abstract class EditableText extends CustomComponent {

    private final String workspace;
    private final String nodeIdentifier;

    public EditableText(Item item) throws RepositoryException {
        this.workspace = item.getSession().getWorkspace().getName();
        this.nodeIdentifier = ((Node) item).getIdentifier();

        final HorizontalLayout layout = new HorizontalLayout();
        final Label label = new Label(getValue(item));

        // TODO the double click event should be removed when the text field is visible, otherwise its not possible to double click to mark words

        layout.addListener(new LayoutEvents.LayoutClickListener() {
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.isDoubleClick()) {
                    final TextField textField = new TextField();
                    try {
                        textField.setValue(getValue(getItem(workspace, nodeIdentifier)));
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }
                    textField.addListener(new FieldEvents.BlurListener() {
                        public void blur(FieldEvents.BlurEvent event) {
                            layout.removeComponent(textField);
                            layout.addComponent(label);
                        }
                    });
                    textField.addShortcutListener(new ShortcutListener("", ShortcutAction.KeyCode.ENTER, new int[]{}) {
                        @Override
                        public void handleAction(Object sender, Object target) {

                            Object value = textField.getValue();

                            try {
                                Item item1 = getItem(workspace, nodeIdentifier);
                                setValue(item1, value);
                            } catch (RepositoryException e) {
                                throw new RuntimeRepositoryException(e);
                            }

                            layout.removeComponent(textField);
                            layout.addComponent(label);
                        }
                    });
                    layout.removeComponent(label);
                    layout.addComponent(textField);
                    textField.focus();
                    textField.setWidth("100%");
                    textField.setHeight("100%");
                }
            }
        });
        layout.addComponent(label);
        layout.setSizeFull();
        setCompositionRoot(layout);
        setSizeFull();
    }

    protected abstract String getValue(Item item) throws RepositoryException;

    protected abstract void setValue(Item item, Object value) throws RepositoryException;

    private Item getItem(String workspace, String nodeIdentifier) throws RepositoryException {
        return JCRUtil.getSession(workspace).getNodeByIdentifier(nodeIdentifier);
    }
}

