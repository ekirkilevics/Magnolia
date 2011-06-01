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

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.MetaDataUtil;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Automates editing of entities defined by the content model.
 *
 * @author tmattsson
 */
public class ContentDriver extends AbstractDriver<Node> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void edit(final Node node) {

        // TODO default values also need to be set, but we don't have the definition available here

        visitEditors(getView(), new EditorVisitor() {
            @Override
            public void visit(Editor editor) {
                try {
                    if (editor instanceof ValueEditor) {
                        ValueEditor valueEditor = (ValueEditor) editor;
                        String path = valueEditor.getPath();
                        Class type = valueEditor.getType();
                        setEditorValue(valueEditor, path, type, node);
                    }

                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }
        });
    }

    @Override
    public void flush(final Node node) {

        // Clear any errors from a previous flush
        super.clearErrors();

        try {
            visitEditors(getView(), new EditorVisitor() {
                @Override
                public void visit(Editor editor) {
                    if (editor instanceof ValueEditor) {
                        ValueEditor valueEditor = (ValueEditor) editor;
                        String path = valueEditor.getPath();
                        Object value = valueEditor.getValue();
                        if (!hasErrors(path)) {
                            try {
                                flushEditor(path, value, node);
                            } catch (RepositoryException e) {
                                throw new RuntimeRepositoryException(e);
                            }
                        }
                    }
                }
            });
            if (!hasErrors()) {

                // TODO it's probably not a good idea to always force this behaviour here

                MetaDataUtil.updateMetaData(node);
                node.getSession().save();
            } else {

                visitEditors(getView(), new EditorVisitor() {
                    @Override
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

    private void flushEditor(String path, Object value, Node node) throws RepositoryException {

        if (path.endsWith("@name")) {
            path = StringUtils.substringBefore(path, "@name");
            Item item = getItemByRelPath(node, path);
            if (item.isNode()) {
                NodeUtil.renameNode(node, (String) value);
            } else {
                PropertyUtil.renameProperty((Property) item, (String) value);
            }
            return;
        }

        if (node.hasProperty(path)) {
            // This works for path='MetaData/mgnl:template' and 'title'
            Property property = node.getProperty(path);

            if (value == null) {
                property.setValue(StringUtils.EMPTY);
            } else if (value instanceof String) {
                property.setValue((String) value);
            } else if (value instanceof Calendar) {
                property.setValue((Calendar) value);
            } else if (value instanceof Date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) value);
                node.setProperty(path, calendar);
            } else if (value instanceof Long) {
                property.setValue((Long) value);
            } else if (value instanceof Double) {
                property.setValue((Double) value);
            } else if (value instanceof Boolean) {
                property.setValue((Boolean) value);
            } else if (value instanceof BigDecimal) {
                property.setValue((BigDecimal) value);
            } else {
                logger.error("Unable to write value [" + value +
                        "] of type [" + value.getClass().getName() +
                        "] at [" + path +
                        "]");
            }

        } else {
            // TODO This only works when path is a simple property name
            if (value == null) {
                node.setProperty(path, StringUtils.EMPTY);
            } else if (value instanceof String) {
                node.setProperty(path, (String) value);
            } else if (value instanceof Calendar) {
                node.setProperty(path, (Calendar) value);
            } else if (value instanceof Date) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) value);
                node.setProperty(path, calendar);
            } else if (value instanceof Long) {
                node.setProperty(path, (Long) value);
            } else if (value instanceof Double) {
                node.setProperty(path, (Double) value);
            } else if (value instanceof Boolean) {
                node.setProperty(path, (Boolean) value);
            } else if (value instanceof BigDecimal) {
                node.setProperty(path, (BigDecimal) value);
            } else {
                logger.error("Unable to write value [" + value +
                        "] of type [" + value.getClass().getName() +
                        "] at [" + path +
                        "]");
            }
        }
    }

    private void setEditorValue(ValueEditor valueEditor, String path, Class type, Node node) throws RepositoryException {

        if (path.endsWith("@name")) {
            path = StringUtils.substringBefore(path, "@name");
            valueEditor.setValue(getItemByRelPath(node, path).getName());
            return;
        }

        if (node.hasProperty(path)) {
            if (type.equals(String.class)) {
                valueEditor.setValue(node.getProperty(path).getString());
            } else if (type.equals(Calendar.class)) {
                valueEditor.setValue(node.getProperty(path).getDate());
            } else if (type.equals(Date.class)) {
                // TODO when null is saved it becomes an empty string and this fails
                valueEditor.setValue(node.getProperty(path).getDate().getTime());
            } else if (type.equals(Long.class)) {
                valueEditor.setValue(node.getProperty(path).getLong());
            } else if (type.equals(Double.class)) {
                valueEditor.setValue(node.getProperty(path).getDouble());
            } else if (type.equals(Boolean.class)) {
                valueEditor.setValue(node.getProperty(path).getBoolean());
            } else if (type.equals(BigDecimal.class)) {
                valueEditor.setValue(node.getProperty(path).getDecimal());
            } else {
                logger.error("Unable to read value at [" + path +
                        "] as expected type [" + type.getName() +
                        "]");
            }
        }
    }

    private Item getItemByRelPath(Node node, String relPath) throws RepositoryException {
        if (relPath.equals("")) {
            return node;
        }
        if (node.hasNode(relPath)) {
            return node.getNode(relPath);
        }
        if (node.hasProperty(relPath)) {
            return node.getProperty(relPath);
        }
        throw new PathNotFoundException(node.getPath() + " "  + relPath);
    }
}
