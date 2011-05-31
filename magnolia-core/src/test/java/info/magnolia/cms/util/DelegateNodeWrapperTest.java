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
package info.magnolia.cms.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.junit.Test;

/**
 * Tests for delegate wrapper.
 * 
 * @author had
 * 
 */
public class DelegateNodeWrapperTest {

    @Test
    public void testDeepUnwrap() {

        DelegateNodeWrapper test = new FirstDelegate(new SecondDelegate(new MockNode("test")));
        // preconditions
        assertTrue(test instanceof FirstDelegate);
        assertTrue(test.getWrappedNode() instanceof SecondDelegate);
        // unwrap
        DelegateNodeWrapper unwrapped = (DelegateNodeWrapper) test.deepUnwrap(SecondDelegate.class);

        // original is NOT modified
        assertTrue(test instanceof FirstDelegate);
        assertTrue(test.getWrappedNode() instanceof SecondDelegate);

        // returned node is unwrapped
        assertTrue(unwrapped instanceof FirstDelegate);
        assertTrue(unwrapped.getWrappedNode() instanceof MockNode);

        // internal pros passed on
        assertEquals(((FirstDelegate) test).isTest(), ((FirstDelegate) unwrapped).isTest());
        // references are not modified
        assertTrue(((FirstDelegate) test).getList() == ((FirstDelegate) unwrapped).getList());

        // second round
        Node bareNode = (unwrapped).deepUnwrap(FirstDelegate.class);
        // original is still not unwrapped
        assertTrue(unwrapped instanceof FirstDelegate);
        // but the delegate of given type is removed
        assertTrue(bareNode instanceof MockNode);

        // try different order
        unwrapped = (DelegateNodeWrapper) test.deepUnwrap(FirstDelegate.class);
        // original is NOT modified
        assertTrue(test instanceof FirstDelegate);
        assertTrue(test.getWrappedNode() instanceof SecondDelegate);

        // returned node is unwrapped
        assertTrue(unwrapped instanceof SecondDelegate);
        assertTrue(unwrapped.getWrappedNode() instanceof MockNode);
    }

    @Test
    public void test4LevelDeepUnwrap() {

        DelegateNodeWrapper test = new FirstDelegate(new SecondDelegate(new ThirdDelegate(new FourthDelegate(new MockNode("test")))));
        // preconditions
        assertTrue(test instanceof FirstDelegate);
        assertTrue(test.getWrappedNode() instanceof SecondDelegate);
        // unwrap
        DelegateNodeWrapper unwrapped = (DelegateNodeWrapper) test.deepUnwrap(ThirdDelegate.class);

        // original is NOT modified
        assertTrue(test instanceof FirstDelegate);
        assertTrue(test.getWrappedNode() instanceof SecondDelegate);
        Node thirdOriginal = ((DelegateNodeWrapper) test.getWrappedNode()).getWrappedNode();
        assertTrue(thirdOriginal instanceof ThirdDelegate);
        Node fourthOriginal = ((DelegateNodeWrapper) thirdOriginal).getWrappedNode();
        assertTrue(fourthOriginal instanceof FourthDelegate);

        // returned node is unwrapped
        assertTrue(unwrapped instanceof FirstDelegate);
        assertTrue(unwrapped.getWrappedNode() instanceof SecondDelegate);
        Node fourthClone = ((DelegateNodeWrapper) unwrapped.getWrappedNode()).getWrappedNode();
        assertTrue(fourthClone instanceof FourthDelegate);

        // nodes between "test" and removed wrapper are not the same
        assertNotSame(test.getWrappedNode(), unwrapped.getWrappedNode());
        // but all the nodes between removed wrapper and the original unwrapped node are the same instance.
        assertSame(fourthOriginal, fourthClone);
    }

    @Test
    public void test4LevelDeepUnwrapAtLastLevel() {

        DelegateNodeWrapper test = new FirstDelegate(new SecondDelegate(new ThirdDelegate(new FourthDelegate(new MockNode("test")))));
        // preconditions
        assertTrue(test instanceof FirstDelegate);
        assertTrue(test.getWrappedNode() instanceof SecondDelegate);
        // unwrap
        DelegateNodeWrapper unwrapped = (DelegateNodeWrapper) test.deepUnwrap(FourthDelegate.class);

        // original is NOT modified
        assertTrue(test instanceof FirstDelegate);
        assertTrue(test.getWrappedNode() instanceof SecondDelegate);
        Node thirdOriginal = ((DelegateNodeWrapper) test.getWrappedNode()).getWrappedNode();
        assertTrue(thirdOriginal instanceof ThirdDelegate);
        Node fourthOriginal = ((DelegateNodeWrapper) thirdOriginal).getWrappedNode();
        assertTrue(fourthOriginal instanceof FourthDelegate);

        // returned node is unwrapped
        assertTrue(unwrapped instanceof FirstDelegate);
        assertTrue(unwrapped.getWrappedNode() instanceof SecondDelegate);
        Node thirdClone = ((DelegateNodeWrapper) unwrapped.getWrappedNode()).getWrappedNode();
        assertTrue(thirdClone instanceof ThirdDelegate);
        Node bareNode = ((DelegateNodeWrapper) thirdClone).getWrappedNode();
        assertTrue(bareNode instanceof MockNode);

        // nodes between "test" and removed wrapper are not the same
        assertNotSame(test.getWrappedNode(), unwrapped.getWrappedNode());
        assertNotSame(thirdOriginal, thirdClone);
    }

    class FirstDelegate extends DelegateNodeWrapper {

        private boolean test = false;

        private final List<String> list;

        public FirstDelegate(Node wrapped) {
            super(wrapped);
            // change values of some internal props
            test = true;
            list = new ArrayList<String>();
        }

        public boolean isTest() {
            return test;
        }

        public List<String> getList() {
            return list;
        }
    }

    class SecondDelegate extends DelegateNodeWrapper {

        public SecondDelegate(Node wrapped) {
            super(wrapped);
        }

    }

    class ThirdDelegate extends DelegateNodeWrapper {

        public ThirdDelegate(Node wrapped) {
            super(wrapped);
        }

    }

    class FourthDelegate extends DelegateNodeWrapper {

        public FourthDelegate(Node wrapped) {
            super(wrapped);
        }

    }
}
