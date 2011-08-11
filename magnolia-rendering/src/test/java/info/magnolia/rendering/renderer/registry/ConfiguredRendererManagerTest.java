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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockObservationManager;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfiguredRendererManagerTest extends MgnlTestCase {

    private ModuleRegistry moduleRegistry;
    private RendererRegistry rendererRegistry;
    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = SessionTestUtil.createSession(ContentRepository.CONFIG,
                "/modules/fooModule/renderers/a.class=" + MockTestRenderer.class.getName(),
                "/modules/fooModule/renderers/a.someProperty=foo",
                "/modules/barModule/renderers/b.class=" + MockTestRenderer.class.getName(),
                "/modules/barModule/renderers/b.someProperty=bar",
                "/modules/zedModule\n"
        );

        MockUtil.getSystemMockContext().addSession(session.getWorkspace().getName(), session);

        Set<String> moduleNames = new LinkedHashSet<String>();
        moduleNames.add("fooModule");
        moduleNames.add("barModule");
        moduleNames.add("zedModule");
        moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleNames()).thenReturn(moduleNames);

        rendererRegistry = new RendererRegistry();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Components.setProvider(null);
    }

    public static class MockTestRenderer implements Renderer {

        private String someProperty;

        public String getSomeProperty() {
            return someProperty;
        }

        public void setSomeProperty(String someProperty) {
            this.someProperty = someProperty;
        }

        @Override
        public void render(Node content, RenderableDefinition definition, Map<String, Object> contextObjects, Appendable out) throws IOException, RenderException {
        }
    }

    @Test
    public void testRegistersRenderersOnStart() throws RepositoryException, IOException, RegistrationException {

        // GIVEN
        ConfiguredRendererManager manager = new ConfiguredRendererManager(moduleRegistry, rendererRegistry);

        // WHEN
        manager.start();

        // THEN
        assertNotNull(rendererRegistry.get("a"));
        assertEquals("foo", ((MockTestRenderer) rendererRegistry.get("a")).getSomeProperty());
        assertNotNull(rendererRegistry.get("b"));
        assertEquals("bar", ((MockTestRenderer) rendererRegistry.get("b")).getSomeProperty());
    }

    @Test
    public void testReloadsRenderersOnChange() throws RepositoryException, IOException, RegistrationException, InterruptedException {

        // GIVEN
        ConfiguredRendererManager manager = new ConfiguredRendererManager(moduleRegistry, rendererRegistry);

        // WHEN
        manager.start();

        // THEN, make sure that it found renderer 'a'
        assertNotNull(rendererRegistry.get("a"));
        assertEquals("foo", ((MockTestRenderer) rendererRegistry.get("a")).getSomeProperty());

        // WHEN we remove the node for renderer 'a' and add a new one 'c' in zedModule
        session.getNode("/modules/fooModule/renderers/a").remove();
        Node node = session.getNode("/modules/zedModule/").addNode("renderers").addNode("c");
        node.setProperty("class", MockTestRenderer.class.getName());
        node.setProperty("someProperty", "zed");

        // It's enough to fire just this event because it reloads everything for each batch of events
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        observationManager.fireEvent(MockEvent.nodeAdded("/modules/zedModule/renderers/c"));

        Thread.sleep(5000);

        // THEN 'a' must be gone and the 'z' must have been found
        try {
            rendererRegistry.get("a");
            fail();
        } catch (RegistrationException expected) {
        }
        assertNotNull(rendererRegistry.get("b"));
        assertEquals("bar", ((MockTestRenderer) rendererRegistry.get("b")).getSomeProperty());
        assertNotNull(rendererRegistry.get("c"));
        assertEquals("zed", ((MockTestRenderer) rendererRegistry.get("c")).getSomeProperty());
    }
}
