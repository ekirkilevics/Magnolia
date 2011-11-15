/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.beanmerger;

import info.magnolia.test.ComponentsTestUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;


/**
 * @author pbracher
 * @version $Id$
 *
 */
public class BeanMergerTest extends TestCase {

    public interface InterfaceA {

        String getPropA();
    }

    public interface InterfaceB {

        String getPropB();
    }

    public static class BeanA implements InterfaceA {

        private String propA;

        public BeanA() {
        }

        public BeanA(String propA) {
            this.propA = propA;
        }

        @Override
        public String getPropA() {
            return propA;
        }
    }

    public static class BeanB implements InterfaceB {

        private String propB;

        public BeanB() {
        }

        public BeanB(String propB) {
            this.propB = propB;
        }

        @Override
        public String getPropB() {
            return propB;
        }
    }

    public static class BeanAB extends BeanA implements InterfaceB {

        private String propB;

        public BeanAB() {
        }

        public BeanAB(String propA, String propB) {
            super(propA);
            this.propB = propB;
        }

        @Override
        public String getPropB() {
            return propB;
        }

    }

    public static class BeanWithSubBean extends BeanAB {

        private BeanAB subBean;

        public BeanWithSubBean() {
        }

        public BeanWithSubBean(String propA, String propB, BeanAB subBean) {
            super(propA, propB);
            this.subBean = subBean;
        }

        public BeanAB getSubBean() {
            return subBean;
        }
    }

    public static class BeanWithMap {

        private Map<String, BeanAB> beans = new HashMap<String, BeanMergerTest.BeanAB>();

        public BeanWithMap() {
        }

        public Map<String, BeanAB> getBeans() {
            return beans;
        }

        public void addBean(String name, BeanAB bean) {
            beans.put(name, bean);
        }

    }

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(BeanMerger.class, ProxyBasedBeanMerger.class);
    }

    public void testMergeOfInterfaces() {
        Object result = BeanMergerUtil.merge(new BeanA("a"), new BeanB("b"));
        // both interfaces are implemented
        assertTrue(result instanceof InterfaceA);
        assertTrue(result instanceof InterfaceB);
        // but is an instance of BeanA
        assertTrue(result instanceof BeanA);
        assertFalse(result instanceof BeanB);
    }

    public void testMergeUsesSubClassIfAssignable() {
        Object result = BeanMergerUtil.merge(new BeanA("a"), new BeanB("b"), new BeanAB("a", "b"));
        // both interfaces are implemented
        assertTrue(result instanceof InterfaceA);
        assertTrue(result instanceof InterfaceB);
        // but is an instance of BeanAB
        assertTrue(result instanceof BeanAB);
        assertTrue(result instanceof BeanA);
        assertFalse(result instanceof BeanB);
    }

    @Test
    public void testMergedProperties() {
        Object current = new BeanWithSubBean(null, "currentB", new BeanAB("currentSubA", null));
        Object fallback = new BeanWithSubBean("fallbackA", "fallbackB", new BeanAB("subFallbackA", "subFallbackB"));

        BeanWithSubBean result = BeanMergerUtil.merge(current, fallback);

        assertEquals("fallbackA", result.getPropA());
        assertEquals("currentB", result.getPropB());
        assertEquals("currentSubA", result.getSubBean().getPropA());
        assertEquals("subFallbackB", result.getSubBean().getPropB());
    }

    @Test
    public void testMergeMap() {
        BeanWithMap one = new BeanWithMap();
        one.addBean("entry1", new BeanAB("1A", "1B"));

        BeanWithMap two = new BeanWithMap();
        two.addBean("entry2", new BeanAB("2A", null));

        BeanWithMap three = new BeanWithMap();
        three.addBean("entry2", new BeanAB(null, "2B"));
        three.addBean("entry3", new BeanAB("3A", "3B"));
        BeanWithMap result = BeanMergerUtil.merge(one, two, three);

        Map<String, BeanAB> beans = result.getBeans();
        assertEquals(3, beans.size());
        assertEquals("1A", beans.get("entry1").getPropA());
        assertEquals("1B", beans.get("entry1").getPropB());
        assertEquals("2A", beans.get("entry2").getPropA());
        assertEquals("2B", beans.get("entry2").getPropB());
        assertEquals("3A", beans.get("entry3").getPropA());
        assertEquals("3B", beans.get("entry3").getPropB());
    }

    @Test
    public void testIsSimpleType() {

        class BeanMergerWithPublicIsSimpleType extends BeanMergerBase {
            @Override
            protected Object mergeBean(List sources) {
                return null;
            }

            @Override
            public boolean isSimpleType(Class type) {
                return super.isSimpleType(type);
            }
        }

        BeanMergerWithPublicIsSimpleType merger = new BeanMergerWithPublicIsSimpleType();

        assertTrue(merger.isSimpleType(int.class));
        assertTrue(merger.isSimpleType(Integer.class));
        assertTrue(merger.isSimpleType(String.class));
        assertFalse(merger.isSimpleType(Map.class));
        assertFalse(merger.isSimpleType(Collection.class));
        assertFalse(merger.isSimpleType(Object.class));
    }

}
