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
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.IOException;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

/**
 * @version $Id$
 */
public class ConfiguredRendererProviderTest extends MgnlTestCase {

    public static class TestRenderer implements Renderer {
        private String someProperty;

        public String getSomeProperty() {
            return someProperty;
        }

        public void setSomeProperty(String someProperty) {
            this.someProperty = someProperty;
        }

        @Override
        public void render(RenderingContext ctx, Map<String, Object> contextObjects) throws RenderException {
        }
    }

    @Test
    public void testGetDefinition() throws RepositoryException, Content2BeanException, IOException {
        // GIVEN
        Session session = SessionTestUtil.createSession(ContentRepository.CONFIG,
                "/test.class=" + TestRenderer.class.getName(),
                "/test.someProperty=foobar123"
        );
        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        // WHEN
        ConfiguredRendererProvider provider = new ConfiguredRendererProvider("test", session.getNode("/test"));
        TestRenderer renderer = (TestRenderer) provider.getDefinition();

        // THEN
        assertEquals("foobar123", renderer.getSomeProperty());
    }
}
