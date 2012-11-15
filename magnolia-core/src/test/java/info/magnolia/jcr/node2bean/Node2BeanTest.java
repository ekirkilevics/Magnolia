/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.jcr.node2bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.impl.CollectionPropertyHidingTransformer;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ChainBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Node2Bean tests.
 */
public class Node2BeanTest {

    private final TypeMapping typeMapping = new TypeMappingImpl();

    private final Node2BeanTransformer transformer = new Node2BeanTransformerImpl();

    @Before
    public void setUp() {
        ComponentsTestUtil.setInstance(TypeMapping.class, typeMapping);
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testNodeToBeanWithClassDefined() throws Node2BeanException, PathNotFoundException, RepositoryException, IOException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/test/node.class=info.magnolia.jcr.node2bean.SimpleBean\n" +
                "/test/node.integer=999\n" +
                "/test/node.string=Hello\n"
                );
        Node2BeanProcessorImpl node2bean = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        Object bean = node2bean.toBean(session.getNode("/test/node"));

        // THEN
        assertTrue(bean instanceof SimpleBean);
        assertEquals(999, ((SimpleBean) bean).getInteger());
        assertEquals("Hello", ((SimpleBean) bean).getString());

    }

    @Test
    public void testNodeToBeanWithSubBean() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/test/node.class=info.magnolia.jcr.node2bean.BeanWithSubBean\n" +
                "/test/node.integer=999\n" +
                "/test/node.string=Hello\n" +
                "/test/node/sub.class=info.magnolia.jcr.node2bean.OtherSimpleBean\n" +
                "/test/node/sub.integer=111\n" +
                "/test/node/sub.string=World\n"
                );

        Node2BeanProcessorImpl node2bean = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithSubBean bean = (BeanWithSubBean) node2bean.toBean(session.getNode("/test/node"));
        OtherSimpleBean sub = (OtherSimpleBean) bean.getSub();

        // THEN
        assertNotNull(sub);
        assertEquals(999, bean.getInteger());
        assertEquals("Hello", bean.getString());
        assertEquals(111, sub.getInteger());
        assertEquals("World", sub.getString());
    }

    @Test
    public void testNodeToBeanWithSubMap() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                "/parent.integer=1\n" +
                "/parent.string=Hello\n" +
                "/parent/beans/sub1.integer=2\n" +
                "/parent/beans/sub1.string=World\n" +
                "/parent/beans/sub2.integer=3\n" +
                "/parent/beans/sub2.string=:)\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        BeanWithMapWithGenerics bean = (BeanWithMapWithGenerics) n2b.toBean(session.getNode("/parent"));

        // WHEN
        Map<String, SimpleBean> beans = bean.getBeans();

        // THEN
        assertNotNull(beans);
        assertNotNull(beans.get("sub1"));
        assertNotNull(beans.get("sub2"));

        assertEquals(2, beans.get("sub1").getInteger());
        assertEquals("World", beans.get("sub1").getString());
        assertEquals(3, beans.get("sub2").getInteger());
        assertEquals(":)", beans.get("sub2").getString());
    }

    @Test
    public void testNode2BeanWithCollection() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithCollectionOfString\n" +
                "/parent/values.val1=test\n" +
                "/parent/values.val2=str\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithCollectionOfString bean = (BeanWithCollectionOfString) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean);
        assertEquals(2, bean.getValues().size());
        assertTrue(bean.getValues().contains("test"));
        assertTrue(bean.getValues().contains("str"));
    }

    @Test
    public void testNodeToBeanWithList() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithListOfString\n" +
                "/parent/values.val1=test\n" +
                "/parent/values.val2=str\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithListOfString bean = (BeanWithListOfString) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean);
        assertTrue(bean.getValues() instanceof LinkedList);
        assertEquals(2, bean.getValues().size());
        assertEquals("test", bean.getValues().get(0));
        assertEquals("str", bean.getValues().get(1));

    }

    @Test
    public void testNode2BeanWithSet() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithSetOfString\n" +
                "/parent/values.val1=test\n" +
                "/parent/values.val2=str\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithSetOfString bean = (BeanWithSetOfString) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean);
        assertTrue(bean.getValues() instanceof HashSet);
        assertEquals(2, bean.getValues().size());
        assertTrue(bean.getValues().contains("test"));
        assertTrue(bean.getValues().contains("str"));
    }

    @Test
    public void testNode2BeanWithAraryList() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithArrayListOfString\n" +
                "/parent/values.val1=test\n" +
                "/parent/values.val2=str\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithArrayListOfString bean = (BeanWithArrayListOfString) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean);
        assertTrue(bean.getValues() instanceof ArrayList);
        assertEquals(2, bean.getValues().size());
        assertEquals("test", bean.getValues().get(0));
        assertEquals("str", bean.getValues().get(1));
    }

    @Test
    public void testNode2BeanWithTreeSet() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithTreeSetOfString\n" +
                "/parent/values.val1=test\n" +
                "/parent/values.val2=str\n"
                );

        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithTreeSetOfString bean = (BeanWithTreeSetOfString) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean);
        assertTrue(bean.getValues() instanceof TreeSet);
        assertEquals(2, bean.getValues().size());
        assertTrue(bean.getValues().contains("test"));
        assertTrue(bean.getValues().contains("str"));
    }

    @Test
    public void testNodeToBeanWithArray() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithArrayOfSimpleBean\n" +
                "/parent.integer=1\n" +
                "/parent.string=Hello\n" +
                "/parent/beans/sub1.integer=2\n" +
                "/parent/beans/sub1.string=World\n" +
                "/parent/beans/sub2.integer=3\n" +
                "/parent/beans/sub2.string=:)\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        BeanWithArrayOfSimpleBean bean = (BeanWithArrayOfSimpleBean) n2b.toBean(session.getNode("/parent"));

        // WHEN
        SimpleBean[] beans = bean.getBeans();

        // THEN
        assertNotNull(beans);
        assertEquals(2, beans.length);

        assertNotNull(bean.getBeans()[0]);
        assertNotNull(bean.getBeans()[1]);

        assertEquals(2, beans[0].getInteger());
        assertEquals("World", beans[0].getString());
        assertEquals(3, beans[1].getInteger());
        assertEquals(":)", beans[1].getString());
    }

    @Test
    public void testNodeToBeanWithHashMap() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithHashMap\n" +
                "/parent.integer=1\n" +
                "/parent.string=Hello\n" +
                "/parent/beans/sub1.integer=2\n" +
                "/parent/beans/sub1.string=World\n" +
                "/parent/beans/sub2.integer=3\n" +
                "/parent/beans/sub2.string=:)\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithHashMap bean = (BeanWithHashMap) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean.getBeans());
        assertEquals(2, bean.getBeans().size());
    }

    @Test
    public void testNodeToBeanWithCollectionWithAdder() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithCollectionAndAdder\n" +
                "/parent/messages.val1=Hello\n" +
                "/parent/messages.val2=World\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithCollectionAndAdder bean = (BeanWithCollectionAndAdder) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean.getMessages());
        assertEquals(2, bean.getMessages().size());
        Iterator<String> it = bean.getMessages().iterator();
        assertEquals("Hello", it.next());
        assertEquals("World", it.next());
    }

    @Test
    public void testNodeToBeanWithMapWithAdder() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithMapAndAdder\n" +
                "/parent/beans/val1.string=Hello\n" +
                "/parent/beans/val2.string=World\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithMapAndAdder bean = (BeanWithMapAndAdder) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean.getBeans());
        assertEquals(2, bean.getBeans().size());
        assertEquals("Hello", bean.getBeans().get("val1").getString());
        assertEquals("World", bean.getBeans().get("val2").getString());
    }

    @Test
    public void testNodeToBeanWithArrayWithAdder() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithArrayAndAdder\n" +
                "/parent/messages.val1=Hello\n" +
                "/parent/messages.val2=World\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithArrayAndAdder bean = (BeanWithArrayAndAdder) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean.getMessages());
        assertEquals(2, bean.getMessages().length);
        assertEquals("Hello", bean.getMessages()[0]);
        assertEquals("World", bean.getMessages()[1]);
    }

    @Test
    public void testClassPropertiesAreConvertedProperly() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithClass\n" +
                "/parent.foo=blah\n" +
                "/parent.clazz=java.lang.String\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithClass o = (BeanWithClass) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertEquals("blah", o.getFoo());
        assertEquals(String.class, o.getClazz());
    }

    @Test
    public void testJCRPropertiesTypes() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithPrimitiveProperties\n" +
                "/parent.integer=5\n" +
                "/parent.bool=true\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithPrimitiveProperties bean = (BeanWithPrimitiveProperties) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertEquals(5, bean.getInteger());
        assertEquals(true, bean.isBool());
    }

    @Test
    public void testFlatteningSubNodesToSimpleList() throws RepositoryException, IOException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
            "/parent.class=info.magnolia.jcr.node2bean.BeanWithListOfString\n" +
            "/parent/values/sub1.value=one\n" +
            "/parent/values/sub2.value=two"
            );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithListOfString bean = (BeanWithListOfString) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertEquals("one", bean.getValues().get(0));
        assertEquals("two", bean.getValues().get(1));
    }

    @Test
    public void testCanConvertStringsToTheAppropriateEnumEquivalent() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithEnum\n" +
                "/parent.value=Hello\n" +
                "/parent.sample=two\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithEnum bean = (BeanWithEnum) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean);
        assertTrue(bean.getSample().getClass().isEnum());
        assertEquals("Hello", bean.getValue());
        assertEquals(SampleEnum.two, bean.getSample());
    }

    @Test
    public void testCanSpecifySpecificMapImplementation() throws Exception {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/foo/bar.class=" + BeanWithMapWithGenerics.class.getName(),
                "/foo/bar/beans.class=" + MyMap.class.getName(),
                "/foo/bar/beans/a.class=" + SimpleBean.class.getName(),
                "/foo/bar/beans/a.string=Hello",
                "/foo/bar/beans/b.class=" + SimpleBean.class.getName(),
                "/foo/bar/beans/b.string=World");
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        final BeanWithMapWithGenerics bean = (BeanWithMapWithGenerics) n2b.toBean(session.getNode("/foo/bar"));

        // WHEN
        final Map<String, SimpleBean> map = bean.getBeans();

        // THEN
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("Hello", ((SimpleBean) map.get("a")).getString());
        assertEquals("World", ((SimpleBean) map.get("b")).getString());
        assertTrue("we wanted a custom map impl!", map instanceof MyMap);
    }

    @Test
    public void testPopulateBeanPropertyIfNoGenericsUsedInSetter() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/foo/bar.class=info.magnolia.jcr.node2bean.Node2BeanTest$StupidBean\n" +
                "/foo/bar/messages/1.string=Hello\n" +
                "/foo/bar/messages/2.string=World\n"
                );
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        StupidBean bean = (StupidBean) n2b.toBean(session.getNode("/foo/bar"));

        assertNotNull(bean.getMessages());
        assertEquals(2, bean.getMessages().size());
        assertEquals("Hello", ((SimpleBean) bean.getMessages().get(0)).getString());
        assertEquals("World", ((SimpleBean) bean.getMessages().get(1)).getString());
    }

    @Test
    public void testWillFailToUseACustomMapWhichIsNotConcrete() throws Exception { // DUH !
        // GIVEN
        Session session = SessionTestUtil.createSession("/test",
                "/bar.class=" + BeanWithMapWithGenerics.class.getName(),
                "/bar/beans.class=" + StupidMap.class.getName(),
                "/bar/beans/a.class=" + SimpleBean.class.getName(),
                "/bar/beans/a.string=hello",
                "/bar/beans/b.class=" + SimpleBean.class.getName(),
                "/bar/beans/b.string=world"
                );

        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);
        n2b.setForceCreation(false);

        try {
            // WHEN
            n2b.toBean(session.getNode("/bar"));
            fail("should have failed");
        } catch (Node2BeanException t) {
            // THEN
            assertEquals("Can't instantiate bean for /bar/beans", t.getMessage());
            final String causeMsg = t.getCause().getMessage();
            assertTrue(causeMsg.contains("StupidMap"));
            assertTrue(causeMsg.contains("No concrete implementation defined"));
        }
    }

    @Test
    public void testBeanExtendsAnotherBean() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.SimpleBean\n" +
                "/parent.string=Hello\n" +
                "/parent.integer=10\n" +
                "/sub/bean.class=info.magnolia.jcr.node2bean.OtherSimpleBean\n" +
                "/sub/bean.string=World\n" +
                "/sub/bean.value=foo\n" +
                "/sub/bean.extends=../../parent\n"
                );
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        OtherSimpleBean bean = (OtherSimpleBean) n2b.toBean(session.getNode("/sub/bean"));

        // THEN
        assertNotNull(bean);
        assertEquals("World", bean.getString()); // overwritten
        assertEquals(10, bean.getInteger()); // inherited
        assertEquals("foo", bean.getValue()); // new
    }

    @Test
    public void testBeanExtendsAnotherBean2() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                        "/parent.string=Hello\n" +
                        "/parent.integer=10\n" +
                        "/parent/beans/sub1.string=foo\n" +
                        "/parent/beans/sub2.string=bar\n" +
                        "/parent/beans/sub3.string=baz\n" +
                        "/sub/bean.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                        "/sub/bean.string=World\n" +
                        "/sub/bean.integer=999\n" +
                        "/sub/bean.extends=/parent\n" +
                        "/another/sub/bean.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                        "/another/sub/bean.extends=../../../sub/bean\n" +
                        "/another/sub/bean/beans/sub3.string=bla\n" +
                        "/another/sub/bean/beans/sub4.string=blah\n"
                );
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithMapWithGenerics bean = (BeanWithMapWithGenerics) n2b.toBean(session.getNode("/another/sub/bean"));

        // THEN
        assertNotNull(bean);
        assertEquals(999, bean.getInteger());
        assertEquals("World", bean.getString());
        assertEquals(4, bean.getBeans().size());
        assertEquals("foo", bean.getBeans().get("sub1").getString());
        assertEquals("bar", bean.getBeans().get("sub2").getString());
        assertEquals("bla", bean.getBeans().get("sub3").getString());
        assertEquals("blah", bean.getBeans().get("sub4").getString());
    }

    @Test
    @Ignore // jsimak: MAGNOLIA-4631
    public void testBeansWithEnabledPropertySetToFalseAreExcludedFromCollection() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithCollectionOfSimpleBean\n" +
                "/parent/beans/sub1.string=Hello\n" +
                "/parent/beans/sub2.string=World\n" +
                "/parent/beans/sub2.enabled=false\n"
                );
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithCollectionOfSimpleBean bean = (BeanWithCollectionOfSimpleBean) n2b.toBean(session.getNode("/parent"));

        // THEN
        assertNotNull(bean);
        assertEquals(1, bean.getBeans().size());

        // WHEN
        SimpleBean simple = Iterables.get(bean.getBeans(), 0);

        // THEN
        assertEquals(true, simple.isEnabled());
        assertEquals("Hello", simple.getString());
    }

    @Test
    @Ignore // jsimak: MAGNOLIA-4631
    public void testBeansWithEnabledPropertySetToFalseAreExcludedFromMap() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                        "/parent.string=Hello\n" +
                        "/parent.integer=10\n" +
                        "/parent/beans/sub1.string=foo\n" +
                        "/parent/beans/sub1.enabled=false\n" +
                        "/parent/beans/sub2.string=bar\n" +
                        "/parent/beans/sub3.string=baz\n" +
                        "/sub/bean.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                        "/sub/bean.string=World\n" +
                        "/sub/bean.integer=999\n" +
                        "/sub/bean.extends=/parent\n" +
                        "/sub/bean.enabled=false\n" +
                        "/another/sub/bean.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                        "/another/sub/bean.extends=../../../sub/bean\n" +
                        "/another/sub/bean/beans/sub3.string=bla\n" +
                        "/another/sub/bean/beans/sub4.string=blah\n" +
                        "/another/sub/bean/beans/sub4.enabled=false\n"
                );
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithMapWithGenerics bean = (BeanWithMapWithGenerics) n2b.toBean(session.getNode("/another/sub/bean"));

        // THEN
        assertNotNull(bean);
        assertEquals(999, bean.getInteger());
        assertEquals("World", bean.getString());
        assertEquals(2, bean.getBeans().size());
        assertEquals("bar", bean.getBeans().get("sub2").getString());
        assertEquals("bla", bean.getBeans().get("sub3").getString());
    }

    @Test
    public void testCollectionPropertyIsHidden() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithCollectionOfSimpleBean\n" +
                "/parent/beans/sub1.string=ahoj\n" +
                "/parent/beans/sub2.string=hello\n"
                );
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        final BeanWithCollectionOfSimpleBean bean = (BeanWithCollectionOfSimpleBean) n2b.toBean(
                session.getNode("/parent"),
                true,
                new CollectionPropertyHidingTransformer(BeanWithCollectionOfSimpleBean.class, "beans"),
                Components.getComponentProvider()
                );

        // THEN
        assertEquals(0, bean.getBeans().size());
    }

    @Test
    public void testNodeToBeanWithClassDefined2() throws RepositoryException, Node2BeanException, IOException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.OtherSimpleBean\n" +
                "/parent.string=hello\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        OtherSimpleBean bean = (OtherSimpleBean) n2b.toBean(session.getNode("/parent"), false, new ProxyingNode2BeanTransformer(), Components.getComponentProvider());

        // THEN
        assertTrue(bean instanceof SimpleBean);
        assertEquals("proxied: hello", bean.getString());
    }

    @Test
    public void testCanSpecifySpecificCollectionImplementation() throws Exception {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithCollectionOfSimpleBean\n" +
                "/parent/beans.class=java.util.Vector\n" +
                "/parent/beans/a.class=info.magnolia.jcr.node2bean.SimpleBean\n" +
                "/parent/beans/a.string=hello\n" +
                "/parent/beans/b.class=info.magnolia.jcr.node2bean.SimpleBean\n" +
                "/parent/beans/b.string=world\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);
        final BeanWithCollectionOfSimpleBean bean = (BeanWithCollectionOfSimpleBean) n2b.toBean(session.getNode("/parent"));

        // WHEN
        final Collection<SimpleBean> coll = bean.getBeans();

        // THEN
        assertNotNull(coll);
        assertEquals(2, coll.size());
        final Iterator it = coll.iterator();
        final SimpleBean a = (SimpleBean) it.next();
        final SimpleBean b = (SimpleBean) it.next();
        assertNotSame(a, b);
        assertFalse(a.getString().equals(b.getString()));
        assertTrue("hello".equals(a.getString()) || "hello".equals(b.getString()));
        assertTrue("world".equals(a.getString()) || "world".equals(b.getString()));
        assertTrue("we wanted a custom collection impl!", coll instanceof Vector);
    }

    @Test
    public void testSimpleUrlPatternIsConvertedAutomagically() throws Exception {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithSimpleUrlPattern\n" +
                "/parent.myPattern=H?llo*\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        final BeanWithSimpleUrlPattern res = (BeanWithSimpleUrlPattern) n2b.toBean(session.getNode("parent"));

        // THEN
        assertNotNull(res);
        assertNotNull(res.getMyPattern());
        assertTrue(res.getMyPattern() instanceof SimpleUrlPattern);
        assertTrue(res.matches("Hello world"));
        assertTrue(res.matches("Hallo weld"));
        assertFalse(res.matches("Haaaallo weeeeeld"));
        assertFalse(res.matches("Bonjour monde"));
    }

    @Test
    public void testMessageFormatIsConvertedAutomagically() throws Exception {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithMessageFormat\n" +
                "/parent.myFormat=plop {0} plop {1} plop\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        final BeanWithMessageFormat res = (BeanWithMessageFormat) n2b.toBean(session.getNode("parent"));

        // THEN
        assertNotNull(res);
        assertNotNull(res.getMyFormat());
        assertTrue(res.getMyFormat() instanceof MessageFormat);
        assertEquals("plop hey plop ho plop", res.formatIt("hey", "ho"));
    }

    @Test
    public void testRegexPatternIsConvertedAutomagically() throws Exception {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithRegexPattern\n" +
                "/parent.myPattern=a*b\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        final BeanWithRegexPattern res = (BeanWithRegexPattern) n2b.toBean(session.getNode("parent"));

        // THEN
        assertNotNull(res);
        assertNotNull(res.getMyPattern());
        assertTrue(res.getMyPattern() instanceof Pattern);
        assertTrue(res.matches("aaaaab"));
        assertFalse(res.matches("baaaaa"));
    }

    @Test
    public void testBeanWillUseTransformerFromAnnotatedSetter() throws Exception {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/listener.class=info.magnolia.jcr.node2bean.Node2BeanTest$BeanWithAnnotation\n" +
                "/listener/command/version.class=info.magnolia.test.TestCommand\n" +
                "/listener/command/alert.class=info.magnolia.test.TestCommand\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, transformer);

        // WHEN
        BeanWithAnnotation bean = (BeanWithAnnotation) n2b.toBean(session.getNode("/listener"));

        // THEN
        assertTrue(bean.getCommand() instanceof MyChain);
        assertEquals(2, ((MyChain) bean.getCommand()).getCommands().length);
    }

    private class ProxyingNode2BeanTransformer extends Node2BeanTransformerImpl {

        @Override
        public void initBean(TransformationState state, Map properties) throws Node2BeanException {
            super.initBean(state, properties);
            Object bean = state.getCurrentBean();
            if (bean instanceof SimpleBean) {
                state.setCurrentBean(new ProxyingSimpleBean((SimpleBean) bean));
            }
        }
    }

    private class ProxyingSimpleBean extends OtherSimpleBean {

        private final SimpleBean target;

        public ProxyingSimpleBean(SimpleBean target) {
            this.target = target;
        }

        @Override
        public String getString() {
            return "proxied: " + target.getString();
        }
    }

    public static class MyMap extends HashMap {
    }

    public abstract static class StupidMap extends AbstractMap {}

    public final class StupidBean {
        private List messages = new ArrayList();

        public void addMessage(SimpleBean str) {
            this.messages.add(str);
        }

        public void setMessages(List messages) {
            this.messages = messages;
        }

        public List getMessages() {
            return this.messages;
        }
    }

    public class BeanWithAnnotation {
        private Command command;

        @N2B(transformer = SomeCommandTransformer.class)
        public void setCommand(Command command) {
            this.command = command;
        }

        public Command getCommand() {
            return command;
        }
    }

    public static class MyChain extends ChainBase {

        public Command[] getCommands() {
            return commands;
        }

    }

}
