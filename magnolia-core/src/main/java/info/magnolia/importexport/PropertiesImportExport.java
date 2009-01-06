/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.importexport;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.OrderedProperties;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

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

        final Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            String orgKey = (String) o;
            String valueStr = properties.getProperty(orgKey);

            //if this is a node definition (no property)
            String key = orgKey;
            if (StringUtils.isEmpty(valueStr) && !key.contains(".") && !key.contains("@")) {
                key += "@type";
                valueStr = "nt:base";
            }
            key = StringUtils.replace(key, "/", ".");
            key = StringUtils.removeStart(key, ".");
            // guarantee a dot in front of @ to make it a property
            key = StringUtils.replace(StringUtils.replace(key, "@", ".@"), "..@", ".@");

            String name = StringUtils.substringAfterLast(key, ".");
            String path = StringUtils.substringBeforeLast(key, ".");
            path = StringUtils.replace(path, ".", "/");

            final String type;
            if (name.equals("@type")) {
                type = valueStr;
            } else if (properties.containsKey(orgKey + "@type")) {
                type = properties.getProperty(orgKey + "@type");
            } else {
                type = ItemType.CONTENTNODE.getSystemName();
            }
//            String type = properties.getProperty(orgKey + "@type", ItemType.CONTENTNODE.getSystemName());
            Content c = ContentUtil.createPath(root, path, new ItemType(type));
            populateContent(c, name, valueStr);
        }
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
        Object valueObj = valueStr;

        if (valueStr.contains(":")) {
            String type = StringUtils.substringBefore(valueStr, ":");
            if (type.equals("int")) {
                type = "integer";
            }
            String value = StringUtils.substringAfter(valueStr, ":");
            try {
                valueObj = ConvertUtils.convert(value, Class.forName("java.lang." + StringUtils.capitalize(type)));
            }
            catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("can't convert value [" + valueStr + "]", e);
            }
        }
        return valueObj;
    }

    // TODO : does not take property type into account
    public static Properties toProperties(HierarchyManager hm) throws Exception {
        final Properties out = new OrderedProperties();
        ContentUtil.visit(hm.getRoot(), new ContentUtil.Visitor() {
            public void visit(Content node) throws Exception {
                appendNodeProperties(node, out);
            }
        });
        return out;
    }

    public static void appendNodeProperties(Content node, Properties out) {
        final Collection props = node.getNodeDataCollection();
        final Iterator it = props.iterator();
        while (it.hasNext()) {
            final NodeData prop = (NodeData) it.next();
            final String path = node.getHandle() + "." + prop.getName();
            out.setProperty(path, prop.getString());
        }
    }

}
