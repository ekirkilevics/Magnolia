/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.content2bean;

import static org.junit.Assert.*;
import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * @version $Id$
 */
public class Content2BeanTest extends MgnlTestCase {
    @Test
    public void testContentToBeanWithClassDefined() throws Content2BeanException{
        Content node = MockUtil.createNode("node", new Object[][]{
            {"class", "info.magnolia.content2bean.SimpleBean"},
            {"prop1", "prop1Value"},
            {"prop2", "prop2Value"}});

        Object bean = Content2BeanUtil.toBean(node);
        assertTrue(bean instanceof SimpleBean);
        assertEquals("prop1Value", ((SimpleBean) bean).getProp1());
        assertEquals("prop2Value", ((SimpleBean) bean).getProp2());
    }

    @Test
    public void testContentToBeanWithDefaultClass() throws Content2BeanException{
        Content node = MockUtil.createNode("node", new Object[][]{{"prop1", "prop1Value"}, {"prop2", "prop2Value"}});

        Object bean = Content2BeanUtil.toBean(node, SimpleBean.class);
        assertTrue(bean instanceof SimpleBean);
        assertEquals("prop1Value", ((SimpleBean) bean).getProp1());
        assertEquals("prop2Value", ((SimpleBean) bean).getProp2());
    }

    @Test
    public void testContentToBeanWithSubBean() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithSubBean"},
                {"prop1", "propParent1Value"},
                {"prop2", "propParent2Value"}},
            new Content[]{MockUtil.createNode("sub", new String[][]{
                {"class", "info.magnolia.content2bean.OtherSimpleBean"},
                {"prop1", "propSub1Value"},
                {"prop2", "propSub2Value"}})});

        BeanWithSubBean bean = (BeanWithSubBean) Content2BeanUtil.toBean(node, true);
        OtherSimpleBean sub = (OtherSimpleBean) bean.getSub();

        assertNotNull(sub);
        assertEquals("propParent1Value", bean.getProp1());
        assertEquals("propParent2Value", bean.getProp2());
        assertEquals("propSub1Value", sub.getProp1());
        assertEquals("propSub2Value", sub.getProp2());
    }

    @Test
    public void testContentToBeanWithSubBeanAndAutoTypeResolving() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithSubBean"},
                {"prop1", "propParent1Value"},
                {"prop2", "propParent2Value"}},
            new Content[]{MockUtil.createNode("sub", new String[][]{
                {"prop1", "propSub1Value"},
                {"prop2", "propSub2Value"}})});

        BeanWithSubBean bean = (BeanWithSubBean) Content2BeanUtil.toBean(node, true);
        SimpleBean sub = bean.getSub();

        assertNotNull(sub);
        assertEquals("propParent1Value", bean.getProp1());
        assertEquals("propParent2Value", bean.getProp2());
        assertEquals("propSub1Value", sub.getProp1());
        assertEquals("propSub2Value", sub.getProp2());
    }

    @Test
    public void testContentToBeanWithSubMap() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithMap"},
                {"prop1", "propParent1Value"},
                {"prop2", "propParent2Value"}},
                new Content[]{
                    MockUtil.createContent("beans", new String[][]{},
                        new Content[]{
                            MockUtil.createNode("sub1", new String[][]{
                                {"class", "info.magnolia.content2bean.SimpleBean"},
                                {"prop1", "prop1Sub1Value"},
                                {"prop2", "prop2Sub1Value"}
                            }),
                            MockUtil.createNode("sub2", new String[][]{
                                {"class", "info.magnolia.content2bean.SimpleBean"},
                                {"prop1", "prop1Sub2Value"},
                                {"prop2", "prop2Sub2Value"}
                            }),

                    })

        });

        BeanWithMap bean = (BeanWithMap) Content2BeanUtil.toBean(node, true);
        Map beans = bean.getBeans();

        assertNotNull(beans);
        assertNotNull(beans.get("sub1"));
        assertNotNull(beans.get("sub2"));

        assertEquals("prop1Sub1Value", ((SimpleBean)beans.get("sub1")).getProp1());
        assertEquals("prop2Sub1Value", ((SimpleBean)beans.get("sub1")).getProp2());
        assertEquals("prop1Sub2Value", ((SimpleBean)beans.get("sub2")).getProp1());
        assertEquals("prop2Sub2Value", ((SimpleBean)beans.get("sub2")).getProp2());
    }

    @Test
    public void testContentToBeanWithSubMapUsingMapping() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithMap"},
                {"prop1", "propParent1Value"},
                {"prop2", "propParent2Value"}},
                new Content[]{
                    MockUtil.createContent("beans", new String[][]{},
                        new Content[]{
                            MockUtil.createNode("sub1", new String[][]{
                                {"prop1", "prop1Sub1Value"},
                                {"prop2", "prop2Sub1Value"}
                            }),
                            MockUtil.createNode("sub2", new String[][]{
                                {"prop1", "prop1Sub2Value"},
                                {"prop2", "prop2Sub2Value"}
                            }),

                    })

        });

        Content2BeanUtil.addCollectionPropertyMapping(BeanWithMap.class, "beans", SimpleBean.class);

        BeanWithMap bean = (BeanWithMap) Content2BeanUtil.toBean(node, true);
        Map beans = bean.getBeans();

        assertNotNull(beans);
        assertNotNull(beans.get("sub1"));
        assertNotNull(beans.get("sub2"));

        assertEquals("prop1Sub1Value", ((SimpleBean)beans.get("sub1")).getProp1());
        assertEquals("prop2Sub1Value", ((SimpleBean)beans.get("sub1")).getProp2());
        assertEquals("prop1Sub2Value", ((SimpleBean)beans.get("sub2")).getProp1());
        assertEquals("prop2Sub2Value", ((SimpleBean)beans.get("sub2")).getProp2());
    }

    @Test
    public void testContentToBeanWithSubMapUsingAdder() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithMapAndAdder"},
                {"prop1", "propParent1Value"},
                {"prop2", "propParent2Value"}},
                new Content[]{
                    MockUtil.createContent("beans", new String[][]{},
                        new Content[]{
                            MockUtil.createNode("sub1", new String[][]{
                                {"prop1", "prop1Sub1Value"},
                                {"prop2", "prop2Sub1Value"}
                            }),
                            MockUtil.createNode("sub2", new String[][]{
                                {"prop1", "prop1Sub2Value"},
                                {"prop2", "prop2Sub2Value"}
                            }),

                    })

        });

        BeanWithMapAndAdder bean = (BeanWithMapAndAdder) Content2BeanUtil.toBean(node, true);
        Map beans = bean.getBeans();

        assertNotNull(beans);
        assertNotNull(beans.get("sub1"));
        assertNotNull(beans.get("sub2"));

        assertEquals("prop1Sub1Value", ((SimpleBean)beans.get("sub1")).getProp1());
        assertEquals("prop2Sub1Value", ((SimpleBean)beans.get("sub1")).getProp2());
        assertEquals("prop1Sub2Value", ((SimpleBean)beans.get("sub2")).getProp1());
        assertEquals("prop2Sub2Value", ((SimpleBean)beans.get("sub2")).getProp2());
    }

    @Test
    public void testContentToBeanWithArraysUsingAdder() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithArrayAndAdder"},
                {"prop1", "propParent1Value"},
                {"prop2", "propParent2Value"}},
                new Content[]{
                    MockUtil.createContent("beans", new String[][]{},
                        new Content[]{
                            MockUtil.createNode("sub1", new String[][]{
                                {"prop1", "prop1Sub1Value"},
                                {"prop2", "prop2Sub1Value"}
                            }),
                            MockUtil.createNode("sub2", new String[][]{
                                {"prop1", "prop1Sub2Value"},
                                {"prop2", "prop2Sub2Value"}
                            }),

                    })

        });

        BeanWithArrayAndAdder bean = (BeanWithArrayAndAdder) Content2BeanUtil.toBean(node, true);

        assertEquals(2, bean.getBeans().length);
        assertNotNull(bean.getBeans()[0]);
        assertNotNull(bean.getBeans()[1]);

        assertEquals("prop1Sub1Value", bean.getBeans()[0].getProp1());
        assertEquals("prop2Sub1Value", bean.getBeans()[0].getProp2());
        assertEquals("prop1Sub2Value", bean.getBeans()[1].getProp1());
        assertEquals("prop2Sub2Value", bean.getBeans()[1].getProp2());
    }

    @Test
    public void testClassPropertiesAreConvertedProperly() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithClass"},
                {"foo", "blah"},
                {"clazz", "java.lang.String"}}, new Content[0]);

        BeanWithClass o = (BeanWithClass) Content2BeanUtil.toBean(node, true);
        assertEquals("blah", o.getFoo());
        assertEquals(String.class, o.getClazz());
    }

    @Test
    public void testJCRPropertiesTypes() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new Object[][]{
                {"class", "info.magnolia.content2bean.BeanWithPrimitiveProperties"},
                                {"integer", Integer.valueOf(5)},
 {"bool", Boolean.TRUE}
        }, new Content[0]);

        BeanWithPrimitiveProperties bean = (BeanWithPrimitiveProperties) Content2BeanUtil.toBean(node, true);
        assertEquals(5, bean.getInteger());
        assertEquals(true, bean.isBool());
    }

    @Test
    public void testFromStringConversion() throws Content2BeanException {
        Content node = MockUtil.createContent("parent", new Object[][]{
                {"class", "info.magnolia.content2bean.BeanWithPrimitiveProperties"},
                {"integer", "5"},
                {"bool", "true"}
        }, new Content[0]);

        BeanWithPrimitiveProperties bean = (BeanWithPrimitiveProperties) Content2BeanUtil.toBean(node, true);
        assertEquals(5, bean.getInteger());
        assertEquals(true, bean.isBool());
    }

    @Test
    public void testFlatteningSubNodesToSimpleList() throws RepositoryException, Content2BeanException, IOException {
        String data =
            "/parent.class=" + BeanWithListOfString.class.getName() + "\n" +
            "/parent/values/sub1.value=one\n" +
            "/parent/values/sub2.value=two";

        Content node = MockUtil.createHierarchyManager(data).getContent("/parent");
        //System.out.println(Content2BeanUtil.toMap(node.getContent("values"), true));

        BeanWithListOfString bean = (BeanWithListOfString) Content2BeanUtil.toBean(node, true);
        assertEquals("one", bean.getValues().get(0));
        assertEquals("two", bean.getValues().get(1));
    }

    @Test
    public void testCanConvertStringsToTheAppropriateEnumEquivalent() throws Exception {
        Content node = MockUtil.createContent("myNode", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithEnum"},
                {"foobar", "Why, Hello !"},
                {"sample", "two"}
        }, new Content[0]);

        BeanWithEnum result = (BeanWithEnum) Content2BeanUtil.toBean(node, true);
        assertEquals("Why, Hello !", result.getFoobar());
        assertNotNull(result.getSample());
        assertTrue(result.getSample().getClass().isEnum());
        assertEquals(SampleEnum.two, result.getSample());

    }

    @Test
    public void testCanSpecifySpecificMapImplementation() throws Exception {
        final Content node = MockUtil.createNode("/foo/bar",
                "/foo/bar.class=" + BeanWithMap.class.getName(),
                "/foo/bar/beans.class=" + MyMap.class.getName(),
                "/foo/bar/beans/a.class=" + SimpleBean.class.getName(),
                "/foo/bar/beans/a.prop1=hello",
                "/foo/bar/beans/b.class=" + SimpleBean.class.getName(),
                "/foo/bar/beans/b.prop1=world");

        final BeanWithMap res = (BeanWithMap) Content2BeanUtil.toBean(node, true);
        // sanity checks:
        assertNotNull(res);
        final Map map = res.getBeans();
        assertNotNull(map);
        assertEquals("hello", ((SimpleBean) map.get("a")).getProp1());
        assertEquals("world", ((SimpleBean) map.get("b")).getProp1());

        // actual test:
        assertTrue("we wanted a custom map impl!", map instanceof MyMap);
    }

    /* TODO - MAGNOLIA-3160
    @Test
    public void testCanSpecifySpecificCollectionImplementation() throws Exception {
        final Content node = MockUtil.createNode("/foo/bar",
                "/foo/bar.class=" + BeanWithCollection.class.getName(),
                "/foo/bar/beans.class=" + Vector.class.getName(),
                "/foo/bar/beans/a.class=" + SimpleBean.class.getName(),
                "/foo/bar/beans/a.prop1=hello",
                "/foo/bar/beans/b.class=" + SimpleBean.class.getName(),
                "/foo/bar/beans/b.prop1=world");

        final BeanWithCollection res = (BeanWithCollection) Content2BeanUtil.toBean(node, true);
        // sanity checks:
        assertNotNull(res);
        final Collection coll = res.getBeans();
        assertNotNull(coll);
        assertEquals(2, coll.size());
        final Iterator it = coll.iterator();
        final SimpleBean a = (SimpleBean) it.next();
        final SimpleBean b = (SimpleBean) it.next();
        assertNotSame(a, b);
        assertFalse(a.getProp1().equals(b.getProp1()));
        assertTrue("hello".equals(a.getProp1()) || "hello".equals(b.getProp1()));
        assertTrue("world".equals(a.getProp1()) || "world".equals(b.getProp1()));

        // actual test:
        assertTrue("we wanted a custom collection impl!", coll instanceof Vector);
    }
    */

    @Test
    public void testWillFailToUseACustomMapWhichIsNotConcrete() throws Exception { // DUH !
        final Content node = MockUtil.createNode("/bar",
                "/bar.class=" + BeanWithMap.class.getName(),
                "/bar/beans.class=" + StupidMap.class.getName(),
                "/bar/beans/a.class=" + SimpleBean.class.getName(),
                "/bar/beans/a.prop1=hello",
                "/bar/beans/b.class=" + SimpleBean.class.getName(),
                "/bar/beans/b.prop1=world");

        // TODO - forceCreation true by default - so we can't test via Content2BeanUtil.toBean() here
        final Content2BeanProcessorImpl proc = (Content2BeanProcessorImpl) Content2BeanUtil.getContent2BeanProcessor();
        proc.setForceCreation(false);
        final Content2BeanTransformer trans = Content2BeanUtil.getContent2BeanTransformer();

        try {
            proc.toBean(node, true, trans, new MockComponentProvider());
            fail("should have failed");
        } catch (Content2BeanException t) {
            assertEquals("Can't instantiate bean for /bar/beans", t.getMessage());
            final String causeMsg = t.getCause().getMessage();
            assertTrue(causeMsg.contains("StupidMap"));
            assertTrue(causeMsg.contains("No concrete implementation defined"));
        }
    }

    public static class MyMap extends HashMap {
    }

    public abstract static class StupidMap extends AbstractMap {}
}
