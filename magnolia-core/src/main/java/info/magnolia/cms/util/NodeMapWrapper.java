/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.link.LinkException;
import info.magnolia.link.LinkTransformerManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for a content Node which exposes a Map interface, used to access its content using jstl.
 * @author fguist
 * @version $Revision: 2161 $ ($Author: fgiust $)
 */
public class NodeMapWrapper extends ContentWrapper implements Map {

    private static final Logger log = LoggerFactory.getLogger(NodeMapWrapper.class);

    /**
     * Handle used to construct links.
     */
    private final String handle;

    /**
     * Instantiates a new NodeMapWrapper for the given node.
     * @param node Content node
     * @param handle Parent page handle or other prefix for links.
     */
    public NodeMapWrapper(Content node, String handle) {
        super(node);
        this.handle = handle;
    }

    @Override
    protected Content wrap(Content node) {
        return new NodeMapWrapper(node, handle);
    }

    /**
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return getWrappedContent().getNodeDataCollection().size();
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return getWrappedContent().getNodeDataCollection().isEmpty();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return this.getWrappedContent().getNodeData((String) key).isExist() || hasProperty(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        // not implemented, only get() is needed
        return false;
    }

    /**
     * Shortcut for Content.getNodeData(name).getString() or Content.getNodeData(name).getName().
     * @see java.util.Map#get(Object)
     */
    @Override
    public Object get(Object key) {
        try {
            if (!getWrappedContent().hasNodeData((String) key)) {
                // support the old lower case value
                if ("uuid".equalsIgnoreCase((String) key)) {
                    key = "UUID";
                }
                if (hasProperty(key)) {
                    try {
                        return PropertyUtils.getProperty(this.getWrappedContent(), (String) key);
                    }
                    catch (Exception e) {
                        log.error("can't read property " + key + " from the node " + this.getWrappedContent(), e);
                    }
                }
            }
        }
        catch (RepositoryException e) {
            // should really not happen
            log.error("can't check for node data {" + key + "}", e);
        }

        NodeData nodeData = getWrappedContent().getNodeData((String) key);
        Object value;
        int type = nodeData.getType();
        if (type == PropertyType.DATE) {
            value = nodeData.getDate();
        }
        else if (type == PropertyType.BINARY) {
            // only file path is supported
            FileProperties props = new FileProperties(getWrappedContent(), (String) key);
            value = props.getProperty(FileProperties.PATH);
        }
        else if (nodeData.isMultiValue() == NodeData.MULTIVALUE_TRUE) {

            Value[] values = nodeData.getValues();

            String[] valueStrings = new String[values.length];

            for (int j = 0; j < values.length; j++) {
                try {
                    valueStrings[j] = values[j].getString();
                }
                catch (RepositoryException e) {
                    log.debug(e.getMessage());
                }
            }

            value = valueStrings;
        }
        else {
            try {
                value = info.magnolia.link.LinkUtil.convertLinksFromUUIDPattern(nodeData.getString(), LinkTransformerManager.getInstance().getBrowserLink(handle));
            }
            catch (LinkException e) {
                log.warn("Failed to parse links with from " + nodeData.getName(), e);
                value = nodeData.getString();
            }
        }
        return value;
    }

    protected boolean hasProperty(Object key) {
        try {
            return PropertyUtils.getPropertyDescriptor(this.getWrappedContent(), (String) key) != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(Object arg0, Object arg1) {
        // not implemented, only get() is needed
        return null;
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        // not implemented, only get() is needed
        return null;
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map t) {
        // not implemented, only get() is needed
    }

    /**
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        // not implemented, only get() is needed
    }

    /**
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        Collection<NodeData> nodeDataCollection = getWrappedContent().getNodeDataCollection();
        Set<String> keys = new HashSet<String>();
        for (Iterator<NodeData> iter = nodeDataCollection.iterator(); iter.hasNext();) {
            keys.add(iter.next().getName());
        }

        return keys;
    }

    /**
     * @see java.util.Map#values()
     */
    @Override
    public Collection<String> values() {
        Collection<NodeData> nodeDataCollection = getWrappedContent().getNodeDataCollection();
        Collection<String> values = new ArrayList<String>();
        for (Iterator<NodeData> iter = nodeDataCollection.iterator(); iter.hasNext();) {
            values.add((iter.next()).getString());
        }

        return values;
    }

    /**
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set entrySet() {
        Collection<NodeData> nodeDataCollection = getWrappedContent().getNodeDataCollection();
        Set<Map.Entry> keys = new HashSet<Map.Entry>();
        for (Iterator<NodeData> iter = nodeDataCollection.iterator(); iter.hasNext();) {
            NodeData nd = iter.next();
            final String key = nd.getName();
            final String value = nd.getString();
            keys.add(new Map.Entry() {

                @Override
                public Object getKey() {
                    return key;
                }

                @Override
                public Object getValue() {
                    return value;
                }

                @Override
                public Object setValue(Object value) {
                    return value;
                }
            });
        }

        return keys;
    }
}
