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
import info.magnolia.cms.core.Content;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests
 * 
 * @author dlipp
 */
public class Content2NodeCollectionWrapperTest {

    private Content2NodeCollectionWrapper wrapper;
    private final Content content1 = new MockContent("1");
    private final Content content2 = new MockContent("2");
    private final Content content3 = new MockContent("3");

    @Before
    public void initWrapper() {
        Collection<Content> testCollection = new ArrayList<Content>(3);
        testCollection.add(content1);
        testCollection.add(content2);
        testCollection.add(content3);

        wrapper = new Content2NodeCollectionWrapper(testCollection);
    }

    @Test
    public void testRemoveAll() {
        Collection<Node> nodesToRemove = new ArrayList<Node>(2);
        nodesToRemove.add(content1.getJCRNode());
        nodesToRemove.add(content3.getJCRNode());
        wrapper.removeAll(nodesToRemove);

        assertEquals(1, wrapper.size());
        assertEquals(content2.getJCRNode(), wrapper.iterator().next());
    }

    @Test
    public void testRetainAll() {
        Collection<Node> nodeToRetain = new ArrayList<Node>(2);
        nodeToRetain.add(content1.getJCRNode());
        wrapper.retainAll(nodeToRetain);

        assertEquals(1, wrapper.size());
        assertEquals(content1.getJCRNode(), wrapper.iterator().next());
    }

}
