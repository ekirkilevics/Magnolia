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
package info.magnolia.test.mock;

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

    public MockNodeType(String nodeTypeName) {
        this.name = nodeTypeName;
    }

    public boolean canAddChildNode(String childNodeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean canRemoveItem(String itemName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean canRemoveNode(String nodeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean canRemoveProperty(String propertyName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean canSetProperty(String propertyName, Value value) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean canSetProperty(String propertyName, Value[] values) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public NodeDefinition[] getChildNodeDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public NodeTypeIterator getDeclaredSubtypes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public NodeType[] getDeclaredSupertypes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public PropertyDefinition[] getPropertyDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public NodeTypeIterator getSubtypes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public NodeType[] getSupertypes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean isNodeType(String nodeTypeName) {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public NodeDefinition[] getDeclaredChildNodeDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public PropertyDefinition[] getDeclaredPropertyDefinitions() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public String[] getDeclaredSupertypeNames() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public String getName() {
        return name;
    }

    public String getPrimaryItemName() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean hasOrderableChildNodes() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean isAbstract() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean isMixin() {
        throw new UnsupportedOperationException("Not Implemented");


    }

    public boolean isQueryable() {
        throw new UnsupportedOperationException("Not Implemented");


    }

}
