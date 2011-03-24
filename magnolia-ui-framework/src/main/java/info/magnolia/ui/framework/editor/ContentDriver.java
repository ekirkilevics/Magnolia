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
package info.magnolia.ui.framework.editor;

import java.util.Calendar;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRMetadataUtil;


/**
 * Automates editing of entities defined by the content model.
 *
 * @author tmattsson
 */
public class ContentDriver extends AbstractDriver<Item> {

    public void edit(final Item item) {

        // TODO default values also need to be set, but we don't have the definition available here

        visitEditors(getView(), new EditorVisitor() {
            public void visit(Editor editor) {
                try {
                    if (editor instanceof ValueEditor) {
                        ValueEditor valueEditor = (ValueEditor) editor;
                        String path = valueEditor.getPath();
                        Class type = valueEditor.getType();
                        if (item instanceof Node)
                            setNodeValue(valueEditor, path, type, (Node) item);
                        if (item instanceof Property)
                            setPropertyValue(valueEditor, path, type, (Property) item);
                    }

                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }
        });
    }

    public void flush(final Item item) {

        // Clear any errors from a previous flush
        super.clearErrors();

        try {
            visitEditors(getView(), new EditorVisitor() {
                public void visit(Editor editor) {
                    if (editor instanceof ValueEditor) {
                        ValueEditor valueEditor = (ValueEditor) editor;
                        String path = valueEditor.getPath();
                        Object value = valueEditor.getValue();
                        if (!hasErrors(path)) {
                            try {
                                if (item instanceof Node)
                                    flushNode(path, value, (Node) item);
                                if (item instanceof Property)
                                    flushProperty(path, value, (Property) item);
                            } catch (RepositoryException e) {
                                throw new RuntimeRepositoryException(e);
                            }
                        }
                    }
                }
            });
            if (!hasErrors()) {
                if (item instanceof Node)
                    JCRMetadataUtil.updateMetaData((Node) item);
                item.getSession().save();
            } else {

                visitEditors(getView(), new EditorVisitor() {
                    public void visit(Editor editor) {
                        if (editor instanceof HasEditorErrors) {
                            ((HasEditorErrors) editor).showErrors(getAllErrors());
                        }
                    }
                });
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    private void flushProperty(String path, Object value, Property property) throws RepositoryException {
        if ("@name".equals(path)) {
            Node node = property.getParent();
            node.setProperty((String) value, property.getValue());
            property.remove();
        } else if ("".equals(path)) {
            if (value instanceof String) {
                property.setValue((String) value);
            } else if (value instanceof Calendar) {
                property.setValue((Calendar) value);
            }
        }
    }

    private void flushNode(String path, Object value, Node node) throws RepositoryException {
        if ("@name".equals(path)) {
            String newPath = (node.getParent().getDepth() > 0 ? node.getParent().getPath() : "") + "/" + value;
            node.getSession().move(node.getPath(), newPath);
        } else if (value instanceof String) {
            node.getProperty(path).setValue((String) value);
        } else if (value instanceof Calendar) {
            node.setProperty(path, (Calendar) value);
        }
    }

    private void setPropertyValue(ValueEditor valueEditor, String path, Class type, Property property) throws RepositoryException {
        if ("@name".equals(path)) {
            valueEditor.setValue(property.getName());
        } else if (type.equals(String.class)) {
            valueEditor.setValue(property.getString());
        } else if (type.equals(Calendar.class)) {
            valueEditor.setValue(property.getDate());
        }
    }

    private void setNodeValue(ValueEditor valueEditor, String path, Class type, Node node) throws RepositoryException {
        if ("@name".equals(path)) {
            valueEditor.setValue(node.getName());
        } else if (type.equals(String.class)) {
            if (node.hasProperty(path))
                valueEditor.setValue(node.getProperty(path).getString());
        } else if (type.equals(Calendar.class)) {
            if (node.hasProperty(path))
                valueEditor.setValue(node.getProperty(path).getDate());
        }
    }
}
