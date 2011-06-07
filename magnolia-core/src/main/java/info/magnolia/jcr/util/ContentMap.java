/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import info.magnolia.link.LinkException;
import info.magnolia.link.LinkTransformerManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Map based representation of JCR content. This class is for instance used in template scripts to
 * allow notations like <code>content.propName</code>. It first tries to read a property with name
 * (key) and if not present checks for the presence of child node. Few special property names map to
 * the JCR methods: \@name, \@id, \@path, \@level, \@nodeType
 *
 *
 * @version $Id$
 *
 */
public class ContentMap implements Map<String, Object> {

    private final static Logger log = LoggerFactory.getLogger(ContentMap.class);

    private final Node content;

    /**
     * Represents getters of the node itself.
     */
    private final Map<String, Method> attributeNames = new HashMap<String, Method>();

    public ContentMap(Node content) {
        if (content == null) {
            throw new NullPointerException("ContentMap doesn't accept null content");
        }

        this.content = content;

        // Supported special types are: @nodeType @name, @path @level (and their deprecated forms - see convertDeprecatedProps() for details)
        Class<? extends Node> clazz = content.getClass();
        try {
            attributeNames.put("id", clazz.getMethod("getIdentifier", null));
            attributeNames.put("path", clazz.getMethod("getPath", null));
            attributeNames.put("level", clazz.getMethod("getLevel", null));
            attributeNames.put("nodeType", clazz.getMethod("getPrimaryNodeType", null));
        } catch (SecurityException e) {
            log.debug("Failed to gain access to Node get***() method. Check VM security settings. " + e.getLocalizedMessage(), e);
        } catch (NoSuchMethodException e) {
            log.debug("Failed to retrieve get***() method of Node class. Check the classpath for conflicting version of JCR classes. " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public boolean containsKey(Object key) {

        String strKey = convertKey(key);

        if (!isValidKey(strKey)) {
            return false;
        }

        if (isSpecialProperty(strKey)) {
            return true;
        }

        try {
            return content.hasProperty(strKey);
        } catch (RepositoryException e) {
            // ignore, most likely invalid name
        }
        return false;
    }

    private String convertKey(Object key) {
        if (key == null) {
            return null;
        }
        try {
            return (String) key;
        } catch (ClassCastException e) {
            log.debug("Invalid key. Expected String, but got {}.", key.getClass().getName());
        }
        return null;
    }

    private boolean isValidKey(String strKey) {
        return !StringUtils.isBlank(strKey);
    }

    private boolean isSpecialProperty(String strKey) {
        if (!strKey.startsWith("@")) {
            return false;
        }
        strKey = convertDeprecatedProps(strKey);
        return attributeNames.containsKey(StringUtils.removeStart(strKey, "@"));
    }

    private String convertDeprecatedProps(String strKey) {
        // in the past we allowed both lower and upper case notation ...
        if ("@UUID".equals(strKey) || "@uuid".equals(strKey)) {
            return "identifier";
        } else if ("@handle".equals(strKey)) {
            return "path";
        } else if ("@depth".equals(strKey)) {
            return "level";
        }
        return strKey;
    }

    @Override
    public Object get(Object key) {
        String keyStr;
        try {
            keyStr = (String) key;
        } catch (ClassCastException e) {
            throw new ClassCastException("ContentMap accepts only String as a parameters, provided object was of type " + (key == null ? "null" : key.getClass().getName()));
        }

        Object prop = getNodeProperty(keyStr);
        if (prop == null) {
            keyStr = convertDeprecatedProps(keyStr);
            return getNodeAttribute(StringUtils.removeStart(keyStr, "@"));
        }
        return prop;
    }

    private Object getNodeAttribute(String keyStr) {

        try {
            return content.getClass().getMethod("get" + StringUtils.capitalize(keyStr), null).invoke(content, null);
        } catch (IllegalArgumentException e) {
            // ignore
        } catch (SecurityException e) {
            // ignore
        } catch (IllegalAccessException e) {
            // ignore
        } catch (InvocationTargetException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            try {
                return content.getClass().getMethod("is" + StringUtils.capitalize(keyStr), null).invoke(content, null);
            } catch (IllegalArgumentException e1) {
                // ignore
            } catch (SecurityException e1) {
                // ignore
            } catch (IllegalAccessException e1) {
                // ignore
            } catch (InvocationTargetException e1) {
                // ignore
            } catch (NoSuchMethodException e1) {
                // ignore
            }
        }
        return null;
    }

    private Object getNodeProperty(String keyStr) {
        try {
            if (!content.hasProperty(keyStr)) {
                // property doesn't exist, but maybe child of that name does
                if (content.hasNode(keyStr)) {
                    return new ContentMap(content.getNode(keyStr));
                }
            }

            Property prop = content.getProperty(keyStr);
            int type = prop.getType();
            if (type == PropertyType.DATE) {
                return prop.getDate();
            } else if (type == PropertyType.BINARY) {
                // this should actually never happen. there is no reason why anyone should stream binary data into template ... or is there?
            } else if (prop.isMultiple()) {

                Value[] values = prop.getValues();

                String[] valueStrings = new String[values.length];

                for (int j = 0; j < values.length; j++) {
                    try {
                        valueStrings[j] = values[j].getString();
                    } catch (RepositoryException e) {
                        log.debug(e.getMessage());
                    }
                }

                return valueStrings;
            } else {
                try {
                    return info.magnolia.link.LinkUtil.convertLinksFromUUIDPattern(prop.getString(), LinkTransformerManager.getInstance().getBrowserLink(content.getPath()));
                } catch (LinkException e) {
                    log.warn("Failed to parse links with from " + prop.getName(), e);
                }
            }
            // don't we want to honor other types (e.g. numbers? )
            return prop.getString();

        } catch (PathNotFoundException e) {
            // ignore, property doesn't exist
        } catch (RepositoryException e) {
            log.warn("Failed to retrieve {} on {} with {}", new Object[] { keyStr, content, e.getMessage() });
        }
        return null;
    }

    @Override
    public int size() {
        try {
            return (int) (content.getProperties().getSize() + attributeNames.size());
        } catch (RepositoryException e) {
            // ignore ... no rights to read properties.
        }
        return attributeNames.size();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        try {
            PropertyIterator props = content.getProperties();
            while (props.hasNext()) {
                keys.add(props.nextProperty().getName());
            }
        } catch (RepositoryException e) {
            // ignore - has no access
        }
        for (String name : attributeNames.keySet()) {
            keys.add(name);
        }
        return keys;
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("Entry collections are not supported");
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException("Value collections are not supported");
    }

    @Override
    public boolean containsValue(Object arg0) {
        throw new UnsupportedOperationException("Value checks are not supported");
    }

    @Override
    public boolean isEmpty() {
        // can never be empty because of the node props themselves (name, uuid, ...)
        return false;
    }

    @Override
    public void clear() {
        // ignore, read only
    }

    @Override
    public Object put(String arg0, Object arg1) {
        // ignore, read only
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> arg0) {
        // ignore, read only
    }

    @Override
    public Object remove(Object arg0) {
        // ignore, read only
        return null;
    }

    public Node getJCRNode() {
        return content;
    }
}
