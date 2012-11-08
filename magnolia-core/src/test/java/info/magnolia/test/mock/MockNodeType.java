/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.test.mock;

import info.magnolia.cms.core.MgnlNodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * Mock implementation of JCR NodeType.
 * @author had
 * @version $Id: $
 */
public class MockNodeType implements NodeType {

    private final String name;
    //Key is the supertype, value is a list of its subtypes.
    private static final Map<String, List<String>> nodeTypeHierarchy = new HashMap<String, List<String>>();
    private NodeType[] superTypes = new NodeType[]{};

    static {
        nodeTypeHierarchy.put(MgnlNodeType.NT_CONTENT, Arrays.asList(new String[]{ MgnlNodeType.NT_PAGE }));
        nodeTypeHierarchy.put(MgnlNodeType.NT_CONTENTNODE, Arrays.asList(new String[]{ MgnlNodeType.NT_AREA, MgnlNodeType.NT_COMPONENT, MgnlNodeType.USER, MgnlNodeType.GROUP, MgnlNodeType.ROLE}));
    }

    public MockNodeType(String nodeTypeName) {
        this.name = nodeTypeName;
        if(NT_BASE.equals(nodeTypeName)) {
            return;
        }
        List<MockNodeType> superTypes = new ArrayList<MockNodeType>();
        superTypes.add(new MockNodeType(NT_BASE));

        for(Entry<String, List<String>> entry : nodeTypeHierarchy.entrySet()) {
            if(entry.getValue().contains(nodeTypeName)) {
               superTypes.add(new MockNodeType(entry.getKey()));
            }
        }
        this.superTypes = superTypes.toArray(this.superTypes);

    }

    @Override
    public boolean canAddChildNode(String childNodeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean canRemoveItem(String itemName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean canRemoveNode(String nodeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean canRemoveProperty(String propertyName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean canSetProperty(String propertyName, Value value) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean canSetProperty(String propertyName, Value[] values) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public NodeDefinition[] getChildNodeDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public NodeTypeIterator getDeclaredSubtypes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public NodeType[] getDeclaredSupertypes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public PropertyDefinition[] getPropertyDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public NodeTypeIterator getSubtypes() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public NodeType[] getSupertypes() {
        return superTypes;
    }

    @Override
    public boolean isNodeType(String nodeTypeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public NodeDefinition[] getDeclaredChildNodeDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public PropertyDefinition[] getDeclaredPropertyDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public String[] getDeclaredSupertypeNames() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPrimaryItemName() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean hasOrderableChildNodes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean isAbstract() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean isMixin() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    @Override
    public boolean isQueryable() {
        throw new UnsupportedOperationException("Not Implemented");


    }

}
