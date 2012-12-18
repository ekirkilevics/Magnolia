/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.jcr.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Properties;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;

import info.magnolia.cms.util.OrderedProperties;
import info.magnolia.jcr.predicate.AbstractPredicate;

/**
 * Utility class providing support for properties-like format to import/export jcr data. Useful when data regularly
 * needs to be bootstrapped, for instance, and the jcr xml format is too cumbersome to maintain.
 *
 * Caution: Binary data is represented as ByteArrayInputStream because of the lack of a proper javax.jcr.Binary implementation
 *
 * TODO : handle conflicts (already existing nodes, properties, what to do with existing properties if we don't create
 * new nodes, ...)
 */
public class PropertiesImportExport {

    /**
     * Each property is one item in the properties varargs passed in.
     */
    public void createNodes(Node root, String... properties) throws IOException, RepositoryException {
        createNodes(root, IOUtils.toInputStream(StringUtils.join(Arrays.asList(properties), "\n")));
    }

    /**
     * Each property or node in the stream has to be separated by the \n.
     */
    public void createNodes(Node root, InputStream propertiesStream) throws IOException, RepositoryException {
        Properties properties = new OrderedProperties();

        properties.load(propertiesStream);

        properties = keysToInnerFormat(properties);

        for (Object o : properties.keySet()) {
            String key = (String) o;
            String valueStr = properties.getProperty(key);

            String propertyName = StringUtils.substringAfterLast(key, ".");
            String path = StringUtils.substringBeforeLast(key, ".");

            String type = null;
            if (propertyName.equals("@type")) {
                type = valueStr;
            } else if (properties.containsKey(path + ".@type")) {
                type = properties.getProperty(path + ".@type");
            }

            type = StringUtils.defaultIfEmpty(type, NodeTypes.ContentNode.NAME);
            Node c = NodeUtil.createPath(root, path, type);
            populateNode(c, propertyName, valueStr);
        }
    }

    /**
     * Transforms the keys to the following inner notation: <code>/some/path/node.prop</code> or
     * <code>/some/path/node.@type</code>.
     */
    private Properties keysToInnerFormat(Properties properties) {
        Properties cleaned = new OrderedProperties();

        for (Object o : properties.keySet()) {
            String orgKey = (String) o;
            // explicitly enforce certain syntax
            if (!orgKey.startsWith("/")) {
                throw new IllegalArgumentException("Missing trailing '/' for key: " + orgKey);
            }
            if (StringUtils.countMatches(orgKey, ".") > 1) {
                throw new IllegalArgumentException("Key must not contain more than one '.': " + orgKey);
            }
            if (orgKey.contains("@") && !orgKey.contains(".@")) {
                throw new IllegalArgumentException("Key containing '@' must be preceded by a '.': " + orgKey);
            }
            // if this is a node definition (no property)
            String newKey = orgKey;

            String propertyName = StringUtils.substringAfterLast(newKey, ".");
            String keySuffix = StringUtils.substringBeforeLast(newKey, ".");
            String path = StringUtils.removeStart(keySuffix, "/");

            // if this is a path (no property)
            if (StringUtils.isEmpty(propertyName)) {
                // no value --> is a node
                if (StringUtils.isEmpty(properties.getProperty(orgKey))) {
                    // make this the type property if not defined otherwise
                    if (!properties.containsKey(orgKey + ".@type")) {
                        cleaned.put(path + ".@type", NodeTypes.ContentNode.NAME);
                    }
                    continue;
                }
                throw new IllegalArgumentException("Key for a path (everything without a '.' is considered to be a path) must not contain a value ('='): " + orgKey);
            }
            cleaned.put(path + "." + propertyName, properties.get(orgKey));
        }
        return cleaned;
    }

    protected void populateNode(Node node, String name, String valueStr) throws RepositoryException {
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(valueStr)) {
            // happens if the input properties file just created a node with no properties
            return;
        }
        if (name.equals("@type")) {
            // do nothing, this has been taken into account when creating the node.
        } else if (name.equals("@uuid") || name.equals("uuid")) {
            setIdentifier(node, valueStr);
        } else {
            Object valueObj = convertPropertyStringToObject(valueStr);
            PropertyUtil.setProperty(node, name, valueObj);
        }
    }

    /**
     * Intentionally created this method to allow simple creation of subclasses actually setting the identifier (e.g. in
     * tests).
     */
    protected void setIdentifier(Node ignoredNode, String ignoredString) {
        throw new UnsupportedOperationException("Can't see UUIDs on real node.");
    }

    protected Object convertPropertyStringToObject(String valueStr) {
        if (contains(valueStr, ':')) {
            final String type = StringUtils.substringBefore(valueStr, ":");
            final String value = StringUtils.substringAfter(valueStr, ":");

            // there is no beanUtils converter for Calendar
            if (type.equalsIgnoreCase("date")) {
                return ISO8601.parse(value);
            } else if (type.equalsIgnoreCase("binary")) {
                return new ByteArrayInputStream(value.getBytes());
            } else {
                try {
                    final Class<?> typeCl;
                    if (type.equals("int")) {
                        typeCl = Integer.class;
                    } else {
                        typeCl = Class.forName("java.lang." + StringUtils.capitalize(type));
                    }
                    return ConvertUtils.convert(value, typeCl);
                } catch (ClassNotFoundException e) {
                    // possibly a stray :, let's ignore it for now
                    return valueStr;
                }
            }
        }
        // no type specified, we assume it's a string, no conversion
        return valueStr;
    }

    private static boolean contains(String s, char ch) {
        return s.indexOf(ch) > -1;
    }

    public Properties toProperties(Node node, final AbstractPredicate<Node> nodePredicate) throws RepositoryException {
        final Properties out = new OrderedProperties();
        NodeUtil.visit(
                node,
                new NodeVisitor() {
                    @Override
                    public void visit(Node node) throws RepositoryException {
                        appendNodeTypeAndIdentifier(node, out);
                        appendNodeProperties(node, out);
                    }
                }, nodePredicate
        );
        return out;
    }

    private void appendNodeTypeAndIdentifier(Node node, Properties out) throws RepositoryException {

        // we don't need to export the JCR root node.
        if (node.getDepth() == 0) {
            return;
        }

        String path = getExportPath(node);

        String nodeTypeName = node.getPrimaryNodeType().getName();
        if (nodeTypeName != null && StringUtils.isNotEmpty(nodeTypeName)) {
            out.put(path + ".@type", nodeTypeName);
        }

        String nodeIdentifier = node.getIdentifier();
        if (nodeIdentifier != null && StringUtils.isNotEmpty(nodeIdentifier)) {
            out.put(path + ".@uuid", nodeIdentifier);
        }
    }

    private void appendNodeProperties(Node node, Properties out) throws RepositoryException {
        PropertyIterator propertyIterator = node.getProperties();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            String path = getExportPath(node) + "." + property.getName();

            String propertyValue = getPropertyString(property);

            if (propertyValue != null) {
                out.setProperty(path, propertyValue);
            }
        }
    }

    private String getExportPath(Node node) throws RepositoryException {
        return node.getPath();
    }

    private String getPropertyString(Property property) throws RepositoryException {

        switch (property.getType()) {
            case (PropertyType.STRING): {
                return property.getString();
            }
            case (PropertyType.BOOLEAN): {
                return convertBooleanToExportString(property.getBoolean());
            }
            case (PropertyType.BINARY): {
                return convertBinaryToExportString(property.getValue());
            }
            case (PropertyType.PATH): {
                return property.getString();
            }
            case (PropertyType.DATE): {
                return convertCalendarToExportString(property.getDate());
            }
            case (PropertyType.LONG): {
                return "" + property.getLong();
            }
            case (PropertyType.DOUBLE): {
                return "" + property.getDouble();
            }
            default: {
                return property.getString();
            }
        }
    }

    private String convertBooleanToExportString(boolean b) {
        return "boolean:" + (b ? "true" : "false");
    }

    private String convertBinaryToExportString(Value value) throws RepositoryException {
        return "binary:" + ConvertUtils.convert(value.getString());
    }

    private String convertCalendarToExportString(Calendar calendar) {
        return "date:" + ISO8601.format(calendar);
    }
}
