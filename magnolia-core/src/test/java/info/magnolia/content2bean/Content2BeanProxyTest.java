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
package info.magnolia.content2bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Content2BeanProxyTest {

    @Before
    public void setUp() {
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Content2BeanTransformer.class, Content2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(Content2BeanProcessor.class, Content2BeanProcessorImpl.class);
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testContentToBeanWithClassDefined() throws RepositoryException, Content2BeanException {
        Content node = MockUtil.createNode("node", new Object[][]{
                {"class", "info.magnolia.content2bean.SimpleBean"},
                {"prop1", "prop1Value"},
                {"prop2", "prop2Value"}});

        Object bean = Content2BeanUtil.toBean(node, false, new ProxyingContent2BeanTransformer());
        assertTrue(bean instanceof SimpleBean);
        assertEquals("proxied: prop1Value", ((SimpleBean) bean).getProp1());
        assertEquals("proxied: prop2Value", ((SimpleBean) bean).getProp2());
    }

    private class ProxyingContent2BeanTransformer extends Content2BeanTransformerImpl {

        @Override
        public void initBean(TransformationState state, Map properties) throws Content2BeanException {
            super.initBean(state, properties);
            Object bean = state.getCurrentBean();
            if (bean instanceof SimpleBean) {
                state.setCurrentBean(new ProxyingSimpleBean((SimpleBean) bean));
            }
        }
    }

    private class ProxyingSimpleBean extends SimpleBean {

        private final SimpleBean target;

        public ProxyingSimpleBean(SimpleBean target) {
            this.target = target;
        }

        @Override
        public String getProp1() {
            return "proxied: " + target.getProp1();
        }

        @Override
        public String getProp2() {
            return "proxied: " + target.getProp2();
        }
    }
}
