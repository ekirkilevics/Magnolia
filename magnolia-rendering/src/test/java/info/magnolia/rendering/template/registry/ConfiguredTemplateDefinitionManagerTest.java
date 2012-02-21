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
package info.magnolia.rendering.template.registry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockObservationManager;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConfiguredTemplateDefinitionManagerTest extends MgnlTestCase {

    private ModuleRegistry moduleRegistry;
    private TemplateDefinitionRegistry templateDefinitionRegistry;
    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(TemplateDefinition.class, ConfiguredTemplateDefinition.class);

        session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                "/modules/fooModule/templates/a.title=fooTitle",
                "/modules/barModule/templates/articles.@type=" + MgnlNodeType.NT_CONTENT,
                "/modules/barModule/templates/articles/b.title=barTitle",
                "/modules/zedModule\n"
        );

        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        Set<String> moduleNames = new LinkedHashSet<String>();
        moduleNames.add("fooModule");
        moduleNames.add("barModule");
        moduleNames.add("zedModule");
        moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleNames()).thenReturn(moduleNames);

        templateDefinitionRegistry = new TemplateDefinitionRegistry();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Components.setComponentProvider(null);
    }

    @Test
    public void testRegistersTemplatesOnStart() throws RegistrationException {

        // GIVEN
        ConfiguredTemplateDefinitionManager manager = new ConfiguredTemplateDefinitionManager(moduleRegistry, templateDefinitionRegistry);

        // WHEN
        manager.start();

        // THEN
        TemplateDefinition a = templateDefinitionRegistry.getTemplateDefinition("fooModule:a");
        assertNotNull(a);
        assertEquals("fooModule:a", a.getId());
        assertEquals("fooTitle", a.getTitle());

        TemplateDefinition b = templateDefinitionRegistry.getTemplateDefinition("barModule:articles/b");
        assertNotNull(b);
        assertEquals("barModule:articles/b", b.getId());
        assertEquals("barTitle", b.getTitle());
    }

    @Test
    public void testReloadsTemplatesOnChange() throws RepositoryException, RegistrationException, InterruptedException {

        // GIVEN
        ConfiguredTemplateDefinitionManager manager = new ConfiguredTemplateDefinitionManager(moduleRegistry, templateDefinitionRegistry);

        // WHEN
        manager.start();

        // THEN, make sure that it found template 'a'
        TemplateDefinition a = templateDefinitionRegistry.getTemplateDefinition("fooModule:a");
        assertNotNull(a);
        assertEquals("fooModule:a", a.getId());

        // WHEN we remove the node for template 'a' and add a new one 'c' in zedModule
        session.getNode("/modules/fooModule/templates/a").remove();
        Node node = session.getNode("/modules/zedModule/").addNode("templates").addNode("c");
        node.setProperty("title", "zedTitle");

        // It's enough to fire just this event because it reloads everything for each batch of events
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        observationManager.fireEvent(MockEvent.nodeAdded("/modules/zedModule/templates/c"));

        Thread.sleep(5000);

        // THEN 'a' must be gone and the 'z' must have been found
        try {
            templateDefinitionRegistry.getTemplateDefinition("a");
            fail();
        } catch (RegistrationException expected) {
        }

        TemplateDefinition b = templateDefinitionRegistry.getTemplateDefinition("barModule:articles/b");
        assertNotNull(b);
        assertEquals("barModule:articles/b", b.getId());
        assertEquals("barTitle", b.getTitle());

        TemplateDefinition c = templateDefinitionRegistry.getTemplateDefinition("zedModule:c");
        assertNotNull(c);
        assertEquals("zedModule:c", c.getId());
        assertEquals("zedTitle", c.getTitle());
    }
}
