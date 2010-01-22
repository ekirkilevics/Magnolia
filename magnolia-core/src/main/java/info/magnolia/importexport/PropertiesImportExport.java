/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.importexport;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.OrderedProperties;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

/**
 * Utility class providing support for properties-like format to import/export jcr data. Useful when
 * data regularly needs to be bootstrapped, for instance, and the jcr xml format is too cumbersome to maintain.
 *
 * TODO : handle conflicts (already existing nodes, properties, what to do with existing properties if we don't create new nodes, ...)
 * TODO : consolidate syntax
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertiesImportExport {

    public void createContent(Content root, InputStream propertiesStream) throws IOException, RepositoryException {
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

            type = StringUtils.defaultIfEmpty(type, ItemType.CONTENTNODE.getSystemName());
            Content c = ContentUtil.createPath(root, path, new ItemType(type));
            populateContent(c, propertyName, valueStr);
        }
    }

    /**
     * Transforms the keys to the following inner notation: some/path/node.prop or some/path/node.@type 
     */
    private Properties keysToInnerFormat(Properties properties) {
        Properties cleaned = new OrderedProperties();
        
        for (Object o : properties.keySet()) {
            String orgKey = (String) o;

            //if this is a node definition (no property)
            String newKey = orgKey;
            
            // make sure we have a dot as a property separator
            newKey = StringUtils.replace(newKey, "@", ".@");
            // avoid double dots
            newKey = StringUtils.replace(newKey, "..@", ".@");

            String propertyName = StringUtils.substringAfterLast(newKey, ".");
            String keySuffix = StringUtils.substringBeforeLast(newKey, ".");
            String path = StringUtils.replace(keySuffix, ".", "/");
            path = StringUtils.removeStart(path, "/");

            // if this is a path (no property)
            if (StringUtils.isEmpty(propertyName)){
                // no value --> is a node
                if(StringUtils.isEmpty(properties.getProperty(orgKey))) {
                    // make this the type property if not defined otherwise
                    if(!properties.containsKey(orgKey + "@type")){
                        cleaned.put(path + ".@type", "nt:base");
                    }
                    continue;
                }
                // /some/path/prop = hello  will be treated as a property
                else{
                    propertyName = StringUtils.substringAfterLast(path, "/");
                    path = StringUtils.substringBeforeLast(path, "/");
                }
            }
            cleaned.put(path + "." + propertyName, properties.get(orgKey));
        }
        return cleaned;
    }

    protected void populateContent(Content c, String name, String valueStr) throws RepositoryException {
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(valueStr)) {
            // happens if the input properties file just created a node with no properties
            return;
        }
        if (name.equals("@type")) {
            // do nothing, this has been taken into account when creating the node.
        } else if (name.equals("@uuid")) {
            throw new UnsupportedOperationException("Can't see UUIDs on real node. Use MockUtil if you are using MockContent instances.");
        } else {
            Object valueObj = convertNodeDataStringToObject(valueStr);
            c.createNodeData(name, valueObj);
        }
    }

    protected Object convertNodeDataStringToObject(String valueStr) {
        if (contains(valueStr, ':')) {
            final String type = StringUtils.substringBefore(valueStr, ":");
            final String value = StringUtils.substringAfter(valueStr, ":");

            // there is no beanUtils converter for Calendar
            if (type.equalsIgnoreCase("date")) {
                return ISO8601.parse(value);
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
        } else {
            // no type specified, we assume it's a string, no conversion
            return valueStr;
        }
    }

    /**
     * @deprecated since 4.3
     *
     * This method is deprecated, it returns results in a format that does not match
     * the format that the import method uses (doesn't include @uuid or @type properties)
     *
     * It is kept here to support existing test and applications that might break
     * as a result of these changes (i.e. unit tests that are expecting a specific number of
     * properties returned, etc)
     *
     * For new applications use the contentToProperties methods instead.
     */
    public static Properties toProperties(HierarchyManager hm) throws Exception {
        return toProperties(hm.getRoot());
    }

    public static Properties toProperties(Content rootContent) throws Exception {
        return toProperties(rootContent, ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER, true);
    }

    public static Properties contentToProperties(HierarchyManager hm) throws Exception {
        return contentToProperties(hm.getRoot());
    }

    public static Properties contentToProperties(Content rootContent) throws Exception {
        return toProperties(rootContent, ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER, false);
    }

    public static Properties contentToProperties(Content rootContent, Content.ContentFilter filter) throws Exception {
        return toProperties(rootContent, filter, false);
    }

    /**
     * This method is private because it includes the boolean "legacymode" filter which
     * shouldn't be exposed as part of the API because when "legacymode" is removed, it will
     * force an API change.
     *
     * @param rootContent root node to convert into properties
     * @param contentFilter a content filter to use in selecting what content to export
     * @param legacyMode if true, will not include @uuid and @type nodes
     * @return a Properties object representing the content starting at rootContent
     * @throws Exception
     */
    private static Properties toProperties(Content rootContent, Content.ContentFilter contentFilter, final boolean legacyMode) throws Exception {
        final Properties out = new OrderedProperties();
        ContentUtil.visit(rootContent, new ContentUtil.Visitor() {
            public void visit(Content node) throws Exception {
                if (!legacyMode) {
                    appendNodeTypeAndUUID(node, out, true);
                }
                appendNodeProperties(node, out);
            }
        }, contentFilter);
        return out;
    }

    private static void appendNodeTypeAndUUID(Content node, Properties out, final boolean dumpMetaData) throws RepositoryException {
        String path = getExportPath(node);
        // we don't need to export the JCR root node.
        if (path.equals("/jcr:root")) {
            return;
        }

        String nodeTypeName = node.getNodeTypeName();
        if (nodeTypeName != null && StringUtils.isNotEmpty(nodeTypeName)) {
            out.put(path + "@type", nodeTypeName);
        }
        String nodeUUID = node.getUUID();
        if (nodeUUID != null && StringUtils.isNotEmpty(nodeUUID)) {
            out.put(path + "@uuid", node.getUUID());
        }

        // dumping the metaData of a MetaData node is silly
        if (dumpMetaData && !(nodeTypeName.equals("mgnl:metaData"))) {
            Content metaDataNode = (node.getChildByName(MetaData.DEFAULT_META_NODE));
            if (metaDataNode != null) {
                // append the UUID and the type with a single recursive call
                appendNodeTypeAndUUID(metaDataNode, out, false);

                String baseMetadataPath = getExportPath(metaDataNode);
                MetaData nodeMetaData = node.getMetaData();
                // dump each metadata property one by one.
                addStringProperty(out, baseMetadataPath + ".mgnl\\:template", nodeMetaData.getTemplate());
                addStringProperty(out, baseMetadataPath + ".mgnl\\:authorid", nodeMetaData.getAuthorId());
                addStringProperty(out, baseMetadataPath + ".mgnl\\:activatorid", nodeMetaData.getActivatorId());
                addStringProperty(out, baseMetadataPath + ".mgnl\\:title", nodeMetaData.getTitle());
                addDateProperty(out, baseMetadataPath + ".mgnl\\:creationdate", nodeMetaData.getCreationDate());
                addDateProperty(out, baseMetadataPath + ".mgnl\\:lastaction", nodeMetaData.getLastActionDate());
                addDateProperty(out, baseMetadataPath + ".mgnl\\:lastmodified", nodeMetaData.getLastActionDate());
                addBooleanProeprty(out, baseMetadataPath + ".mgnl\\:activated", nodeMetaData.getIsActivated());
            }
        }
    }

    private static void addBooleanProeprty(Properties out, String path, boolean prop) {
        out.put(path, convertBooleanToExportString(prop));
    }

    private static void addDateProperty(Properties out, String path, Calendar date) {
        if (date != null) {
            out.put(path, convertCalendarToExportString(date));
        }
    }

    private static void addStringProperty(Properties out, String path, String stringProperty) {
        if (StringUtils.isNotEmpty(stringProperty)) {
            out.put(path, stringProperty);
        }
    }

    public static void appendNodeProperties(Content node, Properties out) {
        final Collection<NodeData> props = node.getNodeDataCollection();
        for (NodeData prop : props) {
            final String path = getExportPath(node) + "." + prop.getName();

            String propertyValue = getPropertyString(prop);

            if (propertyValue != null) {
                out.setProperty(path, propertyValue);
            }
        }
    }

    private static String getExportPath(Content node) {
        return node.getHandle();
    }

    private static String getPropertyString(NodeData prop) {
        int propType = prop.getType();

        switch (propType) {
            case (PropertyType.STRING): {
                return prop.getString();
            }
            case (PropertyType.BOOLEAN): {
                return convertBooleanToExportString(prop.getBoolean());
            }
            case (PropertyType.BINARY): {
                return convertBinaryToExportString(prop.getValue());
            }
            case (PropertyType.PATH): {
                return prop.getString();
            }
            case (PropertyType.DATE): {
                return convertCalendarToExportString(prop.getDate());
            }
            default: {
                return prop.getString();
            }
        }
    }

    private static String convertBooleanToExportString(boolean b) {
        return "boolean:" + (b ? "true" : "false");
    }

    private static String convertBinaryToExportString(Value value) {
        return "binary:" + ConvertUtils.convert(value);
    }

    private static String convertCalendarToExportString(Calendar calendar) {
        return "date:" + ISO8601.format(calendar);
    }

    /**
     * Dumps content starting at the content node out to a string in the format that matches the
     * import method.
     */
    public static String dumpPropertiesToString(Content content, Content.ContentFilter filter) throws Exception {
        Properties properties = PropertiesImportExport.contentToProperties(content, filter);
        return dumpPropertiesToString(properties);
    }

    public static String dumpPropertiesToString(Properties properties) {
        final StringBuilder sb = new StringBuilder();
        final Set<Object> propertyNames = properties.keySet();
        for (Object propertyKey : propertyNames) {
            final String name = propertyKey.toString();
            final String value = properties.getProperty(name);
            sb.append(name);
            sb.append("=");
            sb.append(value);
            sb.append("\n");
        }
        return sb.toString();
    }

    private static boolean contains(String s, char ch) {
        return s.indexOf(ch) > -1;
    }
}
