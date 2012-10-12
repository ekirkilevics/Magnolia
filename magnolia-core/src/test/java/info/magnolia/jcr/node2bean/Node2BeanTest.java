/**
 *
 */
package info.magnolia.jcr.node2bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class Node2BeanTest {

    private TypeMapping typeMapping = new TypeMappingImpl();

    @Before
    public void setUp() {
        ComponentsTestUtil.setInstance(TypeMapping.class, typeMapping);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
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
        Node2BeanProcessorImpl node2bean = new Node2BeanProcessorImpl(typeMapping);

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

        Node2BeanProcessorImpl node2bean = new Node2BeanProcessorImpl(typeMapping);

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
    public void testContentToBeanWithSubMap() throws IOException, RepositoryException, Node2BeanException {
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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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

        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
    public void testClassPropertiesAreConvertedProperly() throws IOException, RepositoryException, Node2BeanException {
        // GIVEN
        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithClass\n" +
                "/parent.foo=blah\n" +
                "/parent.clazz=java.lang.String\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

        final BeanWithMapWithGenerics bean = (BeanWithMapWithGenerics) n2b.toBean(session.getNode("/foo/bar"));

        // WHEN
        final Map<String, SimpleBean> map = bean.getBeans();

        // THEN
        assertNotNull(map);
        assertEquals("Hello", ((SimpleBean) map.get("a")).getString());
        assertEquals("World", ((SimpleBean) map.get("b")).getString());
        assertTrue("we wanted a custom map impl!", map instanceof MyMap);
    }

    @Test
    public void testWillFailToUseACustomMapWhichIsNotConcrete() throws Exception { // DUH !
        // WHEN
        Session session = SessionTestUtil.createSession("/test",
                "/bar.class=" + BeanWithMapWithGenerics.class.getName(),
                "/bar/beans.class=" + StupidMap.class.getName(),
                "/bar/beans/a.class=" + SimpleBean.class.getName(),
                "/bar/beans/a.string=hello",
                "/bar/beans/b.class=" + SimpleBean.class.getName(),
                "/bar/beans/b.string=world"
                );

        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);
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
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        final Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping);

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
        assertEquals(999, bean.getInteger());
        assertEquals("World", bean.getString());
    }

    public static class MyMap extends HashMap {
    }

    public abstract static class StupidMap extends AbstractMap {}
}
