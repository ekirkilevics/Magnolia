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
package info.magnolia.rendering.renderer.registry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import info.magnolia.cms.core.Content;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class ConfiguredRendererProviderTest {

    private Renderer renderer;

    @Before
    public void setUp() {
        renderer = mock(Renderer.class);

        Content2BeanProcessor mockProcessor = new Content2BeanProcessor() {
            @Override
            public Object toBean(Content node, boolean recursive, Content2BeanTransformer transformer, ComponentProvider componentProvider) throws Content2BeanException {
                return renderer;
            }
            @Override
            public Object setProperties(Object bean, Content node, boolean recursive, Content2BeanTransformer transformer, ComponentProvider componentProvider) throws Content2BeanException {
                return null;
            }

        };

        ComponentsTestUtil.setInstance(Content2BeanProcessor.class, mockProcessor);
    }


    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
    }

    @Test
    public void testGetDefinition() throws RepositoryException, Content2BeanException, RegistrationException {
        // GIVEN
        MockNode root = new MockNode();
        root.setSession(new MockSession("test"));
        Node rendererConfig = root.addNode("test");
        root.setProperty("class", "info.magnolia.rendering.renderer.FreemarkerRenderer");
        root.setProperty("type", "freemarker");

        // WHEN
        ConfiguredRendererProvider provider = new ConfiguredRendererProvider("test", rendererConfig);

        // THEN
        assertEquals(renderer, provider.getDefinition());
    }
}
