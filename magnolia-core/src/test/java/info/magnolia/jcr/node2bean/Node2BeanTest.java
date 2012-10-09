/**
 *
 */
package info.magnolia.jcr.node2bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.IOException;
import java.util.Map;

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
        Node2BeanProcessorImpl node2bean = new Node2BeanProcessorImpl(typeMapping, false);

        // WHEN
        Object bean = node2bean.toBean(session.getNode("/test/node"));

        // THEN
        assertTrue(bean instanceof SimpleBean);
        assertEquals(999, ((SimpleBean) bean).getInteger());
        assertEquals("Hello", ((SimpleBean) bean).getString());

    }

    @Test
    public void testNodeToBeanWithSubBean() throws IOException, RepositoryException, Node2BeanException {
        Session session = SessionTestUtil.createSession("test",
                "/test/node.class=info.magnolia.jcr.node2bean.BeanWithSubBean\n" +
                "/test/node.integer=999\n" +
                "/test/node.string=Hello\n" +
                "/test/node/sub.class=info.magnolia.jcr.node2bean.OtherSimpleBean\n" +
                "/test/node/sub.integer=111\n" +
                "/test/node/sub.string=World\n"
                );

        Node2BeanProcessorImpl node2bean = new Node2BeanProcessorImpl(typeMapping, true);

        // WHEN
        BeanWithSubBean bean = (BeanWithSubBean) node2bean.toBean(session.getNode("/test/node"));
        OtherSimpleBean sub = (OtherSimpleBean) bean.getSub();

        assertNotNull(sub);
        assertEquals(999, bean.getInteger());
        assertEquals("Hello", bean.getString());
        assertEquals(111, sub.getInteger());
        assertEquals("World", sub.getString());
    }

    @Test
    public void testContentToBeanWithSubMap() throws IOException, RepositoryException, Node2BeanException {

        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithMapWithGenerics\n" +
                "/parent.integer=1\n" +
                "/parent.string=Hello\n" +
                /*"/parent/beans/sub1.class=info.magnolia.jcr.node2bean.SimpleBean\n" +*/
                "/parent/beans/sub1.integer=2\n" +
                "/parent/beans/sub1.string=World\n" +
                /*"/parent/beans/sub2.class=info.magnolia.jcr.node2bean.SimpleBean\n" +*/
                "/parent/beans/sub2.integer=3\n" +
                "/parent/beans/sub2.string=:)\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, true);

        BeanWithMapWithGenerics bean = (BeanWithMapWithGenerics) n2b.toBean(session.getNode("/parent"));
        Map<String, SimpleBean> beans = bean.getBeans();

        assertNotNull(beans);
        assertNotNull(beans.get("sub1"));
        assertNotNull(beans.get("sub2"));

        assertEquals(2, beans.get("sub1").getInteger());
        assertEquals("World", beans.get("sub1").getString());
        assertEquals(3, beans.get("sub2").getInteger());
        assertEquals(":)", beans.get("sub2").getString());
    }

    @Test
    public void testContentToBeanWithArray() throws IOException, RepositoryException, Node2BeanException {

        Session session = SessionTestUtil.createSession("test",
                "/parent.class=info.magnolia.jcr.node2bean.BeanWithArray\n" +
                "/parent.integer=1\n" +
                "/parent.string=Hello\n" +
                /*"/parent/beans/sub1.class=info.magnolia.jcr.node2bean.SimpleBean\n" +*/
                "/parent/beans/sub1.integer=2\n" +
                "/parent/beans/sub1.string=World\n" +
                /*"/parent/beans/sub2.class=info.magnolia.jcr.node2bean.SimpleBean\n" +*/
                "/parent/beans/sub2.integer=3\n" +
                "/parent/beans/sub2.string=:)\n"
                );
        Node2BeanProcessorImpl n2b = new Node2BeanProcessorImpl(typeMapping, true);

        BeanWithArray bean = (BeanWithArray) n2b.toBean(session.getNode("/parent"));
        SimpleBean[] beans = bean.getBeans();

        assertNotNull(beans);
        assertEquals(2, beans.length);

        assertNotNull(bean.getBeans()[0]);
        assertNotNull(bean.getBeans()[1]);

        assertEquals(2, beans[0].getInteger());
        assertEquals("World", beans[0].getString());
        assertEquals(3, beans[1].getInteger());
        assertEquals(":)", beans[1].getString());
    }
}
