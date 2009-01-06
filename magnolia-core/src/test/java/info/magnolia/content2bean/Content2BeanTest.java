/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class Content2BeanTest extends MgnlTestCase {

    /**
     *
     */
    public Content2BeanTest() throws IOException {
        super();
    }

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Content2BeanTest.class);

    public void testContentToBeanWithClassDefined() throws RepositoryException, Content2BeanException{
        Content node = MockUtil.createNode("node", new Object[][]{
            {"class", "info.magnolia.content2bean.SimpleBean"},
            {"prop1", "prop1Value"},
            {"prop2", "prop2Value"}});

        Object bean = Content2BeanUtil.toBean(node);
        assertTrue(bean instanceof SimpleBean);
        assertEquals("prop1Value", ((SimpleBean) bean).getProp1());
        assertEquals("prop2Value", ((SimpleBean) bean).getProp2());
    }

    public void testContentToBeanWithDefaultClass() throws RepositoryException, Content2BeanException{
        Content node = MockUtil.createNode("node", new Object[][]{{"prop1", "prop1Value"}, {"prop2", "prop2Value"}});

        Object bean = Content2BeanUtil.toBean(node, SimpleBean.class);
        assertTrue(bean instanceof SimpleBean);
        assertEquals("prop1Value", ((SimpleBean) bean).getProp1());
        assertEquals("prop2Value", ((SimpleBean) bean).getProp2());
    }

    public void testContentToBeanWithSubBean() throws RepositoryException, Content2BeanException {
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

    public void testContentToBeanWithSubBeanAndAutoTypeResolving() throws RepositoryException, Content2BeanException {
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

    public void testContentToBeanWithSubMap() throws RepositoryException, Content2BeanException {
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

    public void testContentToBeanWithSubMapUsingMapping() throws RepositoryException, Content2BeanException {
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

    public void testContentToBeanWithSubMapUsingAdder() throws RepositoryException, Content2BeanException {
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

    public void testContentToBeanWithArraysUsingAdder() throws RepositoryException, Content2BeanException {
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

    public void testClassPropertiesAreConvertedProperly() throws RepositoryException, Content2BeanException {
        Content node = MockUtil.createContent("parent", new String[][]{
                {"class", "info.magnolia.content2bean.BeanWithClass"},
                {"foo", "blah"},
                {"clazz", "java.lang.String"}}, new Content[0]);

        BeanWithClass o = (BeanWithClass) Content2BeanUtil.toBean(node, true);
        assertEquals("blah", o.getFoo());
        assertEquals(String.class, o.getClazz());
    }

    public void testJCRPropertiesTypes() throws RepositoryException, Content2BeanException {
        Content node = MockUtil.createContent("parent", new Object[][]{
                {"class", "info.magnolia.content2bean.BeanWithPrimitiveProperties"},
                {"integer", new Integer(5)},
                {"bool", new Boolean(true)}
        }, new Content[0]);

        BeanWithPrimitiveProperties bean = (BeanWithPrimitiveProperties) Content2BeanUtil.toBean(node, true);
        assertEquals(5, bean.getInteger());
        assertEquals(true, bean.isBool());
    }

    public void testFromStringConversion() throws RepositoryException, Content2BeanException {
        Content node = MockUtil.createContent("parent", new Object[][]{
                {"class", "info.magnolia.content2bean.BeanWithPrimitiveProperties"},
                {"integer", "5"},
                {"bool", "true"}
        }, new Content[0]);

        BeanWithPrimitiveProperties bean = (BeanWithPrimitiveProperties) Content2BeanUtil.toBean(node, true);
        assertEquals(5, bean.getInteger());
        assertEquals(true, bean.isBool());
    }

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

}
