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
package info.magnolia.cms.core;

import java.util.List;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockHierarchyManager;
import static org.easymock.EasyMock.*;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Property;

import org.easymock.IAnswer;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DefaultContentTest extends RepositoryTestCase {

    public void testPermissionCheckedOnDeleteNodeData() throws Exception {
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        AccessManager am = createStrictMock(AccessManager.class);
        ((DefaultHierarchyManager) hm).setAccessManager(am);
        
        // create foo
        expect(am.isGranted("/foo", 11)).andReturn(true);
        expect(am.isGranted("/foo", 2)).andReturn(true);
        expect(am.isGranted("/foo/MetaData", 11)).andReturn(true).anyTimes();
        // create bar
        expect(am.isGranted("/foo/bar", 11)).andReturn(true);
        // get foo
        expect(am.isGranted("/foo", 8)).andReturn(true);

        // delete("bar")
        expect(am.isGranted("/foo/bar", 4)).andReturn(true);
        expect(am.isGranted("/foo/bar", 8)).andReturn(true).times(2);
        expect(am.isGranted("/foo/bar", 4)).andReturn(true);
        expect(am.isGranted("/foo/bar", 8)).andReturn(true);

        // create bar again
        expect(am.isGranted("/foo/bar", 11)).andReturn(true);

        // deleteNodeData("bar");
        expect(am.isGranted("/foo/bar", 4)).andReturn(true);
        expect(am.isGranted("/foo/bar", 8)).andReturn(true);
        

        Object[] objs = new Object[] {am};
        replay(objs);
        Content node = hm.createContent("/", "foo", ItemType.CONTENTNODE.getSystemName());
        node.createNodeData("bar");
        node = hm.getContent("/foo");
        assertTrue(node.hasNodeData("bar"));
        
        node.delete("bar");
        assertFalse(node.hasNodeData("bar"));
        
        node.createNodeData("bar");
        assertTrue(node.hasNodeData("bar"));
        
        node.deleteNodeData("bar");
        assertFalse(node.hasNodeData("bar"));
        verify(objs);
    }

    public void testIsNodeTypeForNodeChecksPrimaryType() throws RepositoryException {
        final Node node = createMock(Node.class);
        final Property nodeTypeProp = createStrictMock(Property.class);
        expect(node.getProperty(ItemType.JCR_PRIMARY_TYPE)).andReturn(nodeTypeProp).times(2);
        expect(node.isNodeType((String)anyObject())).andAnswer(new IAnswer<Boolean>(){
            public Boolean answer() throws Throwable {
                return getCurrentArguments()[0].equals("foo");
            }
        }).times(2);
        expect(nodeTypeProp.getString()).andReturn("foo").times(2);
        replay(node, nodeTypeProp);

        final DefaultContent c = new DefaultContent();
        c.setNode(node);
        assertTrue(c.isNodeType(node, "foo"));
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
        expect(node.isNodeType(ItemType.NT_FROZENNODE)).andReturn(true);

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
