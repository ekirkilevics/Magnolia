/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.core;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Property;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultContentTest extends TestCase {

    public void testIsNodeTypeForNodeChecksPrimaryType() throws RepositoryException {
        final Node node = createStrictMock(Node.class);
        final Property nodeTypeProp = createStrictMock(Property.class);
        expect(node.getProperty(ItemType.JCR_PRIMARY_TYPE)).andReturn(nodeTypeProp).times(3);
        expect(nodeTypeProp.getString()).andReturn("foo").times(3);

        replay(node, nodeTypeProp);
        final DefaultContent c = new DefaultContent();
        c.setNode(node);
        assertTrue(c.isNodeType(node, "foo"));
        assertTrue(c.isNodeType(node, "fOO"));
        assertFalse(c.isNodeType(node, "bar"));
        verify(node, nodeTypeProp);
    }

    public void testIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes() throws RepositoryException {
        doTestIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes(true, "foo", "foo");
        doTestIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes(false, "bar", "foo");
    }

    private void doTestIsNodeTypeForNodeCheckFrozenTypeIfWereNotLookingForFrozenNodes(boolean expectedResult, String requiredType, String returnedType) throws RepositoryException {
        final Node node = createStrictMock(Node.class);
        final Property nodeTypeProp = createStrictMock(Property.class);
        final Property nodeFrozenTypeProp = createStrictMock(Property.class);

        expect(node.getProperty(ItemType.JCR_PRIMARY_TYPE)).andReturn(nodeTypeProp);
        expect(nodeTypeProp.getString()).andReturn(ItemType.NT_FROZENNODE);
        expect(node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)).andReturn(nodeFrozenTypeProp);
        expect(nodeFrozenTypeProp.getString()).andReturn(returnedType);

        replay(node, nodeTypeProp, nodeFrozenTypeProp);
        final DefaultContent c = new DefaultContent();
        c.setNode(node);
        assertEquals(expectedResult, c.isNodeType(node, requiredType));

        verify(node, nodeTypeProp, nodeFrozenTypeProp);
    }

    public void testIsNodeTypeForNodeDoesNotCheckFrozenTypeIfTheRequestedTypeIsFrozenType()throws RepositoryException {
        final Node node = createStrictMock(Node.class);
        final Property nodeTypeProp = createStrictMock(Property.class);
        expect(node.getProperty(ItemType.JCR_PRIMARY_TYPE)).andReturn(nodeTypeProp);
        expect(nodeTypeProp.getString()).andReturn(ItemType.NT_FROZENNODE);

        replay(node, nodeTypeProp);
        final DefaultContent c = new DefaultContent();
        c.setNode(node);
        assertTrue(c.isNodeType(node, ItemType.NT_FROZENNODE));
        verify(node, nodeTypeProp);
    }

//    public void testIsNodeForThisNodeAlsoWorks()throws RepositoryException {
//        fail();
//    }

}
