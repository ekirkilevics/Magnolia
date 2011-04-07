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

import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.NodeData;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 * 
 * @author dlipp
 */
public class NodeData2PropertyCollectionWrapperTest {

    private NodeData2PropertyCollectionWrapper wrapper;
    private final NodeData nodeData1 = new MockNodeData("1", PropertyType.BINARY);
    private final NodeData nodeData2 = new MockNodeData("2", PropertyType.NAME);
    private final NodeData nodeData3 = new MockNodeData("3", PropertyType.DATE);

    @Before
    public void initWrapper() {
        Collection<NodeData> testCollection = new ArrayList<NodeData>(3);
        testCollection.add(nodeData1);
        testCollection.add(nodeData2);
        testCollection.add(nodeData3);

        wrapper = new NodeData2PropertyCollectionWrapper(testCollection);
    }

    @Test
    public void testRemoveAll() {
        Collection<Property> propertiesToRemove = new ArrayList<Property>(2);
        propertiesToRemove.add(nodeData1.getJCRProperty());
        propertiesToRemove.add(nodeData3.getJCRProperty());
        wrapper.removeAll(propertiesToRemove);

        assertEquals(1, wrapper.size());
        assertEquals(nodeData2.getJCRProperty(), wrapper.iterator().next());

    }

    @Test
    public void testRetainAll() {
        Collection<Property> propertiesToRetain = new ArrayList<Property>(2);
        propertiesToRetain.add(nodeData1.getJCRProperty());
        wrapper.retainAll(propertiesToRetain);

        assertEquals(1, wrapper.size());
        assertEquals(nodeData1.getJCRProperty(), wrapper.iterator().next());

    }

}
