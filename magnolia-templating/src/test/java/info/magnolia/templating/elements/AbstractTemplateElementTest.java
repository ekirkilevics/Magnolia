/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.templating.elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionProvider;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.ComponentsTestUtil;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for AbstractAuthoringUiComponent.
 *
 * @version $Id$
 */
public class AbstractTemplateElementTest extends AbstractElementTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        final ConfiguredTemplateDefinition p0 = new ConfiguredTemplateDefinition();
        p0.setName("testParagraph0");
        final ConfiguredTemplateDefinition p1 = new ConfiguredTemplateDefinition();
        p1.setName("testParagraph1");
        p1.setI18nBasename("info.magnolia.templating.test_messages");

        final TemplateDefinitionProvider p0provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider p1provider = mock(TemplateDefinitionProvider.class);

        when(p0provider.getTemplateDefinition()).thenReturn(p0);
        when(p0provider.getId()).thenReturn(p0.getName());
        when(p1provider.getTemplateDefinition()).thenReturn(p1);
        when(p1provider.getId()).thenReturn(p1.getName());

        final TemplateDefinitionRegistry pman = new TemplateDefinitionRegistry();
        pman.register(p0provider);
        pman.register(p1provider);

        ComponentsTestUtil.setInstance(TemplateDefinitionRegistry.class, pman);

        final ConfiguredTemplateDefinition t0 = new ConfiguredTemplateDefinition();
        t0.setName("testPageTemplate0");
        final ConfiguredTemplateDefinition t1 = new ConfiguredTemplateDefinition();
        t1.setName("testPageTemplate1");
        t1.setI18nBasename("info.magnolia.templating.test_messages");

        final TemplateDefinitionProvider t0provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider t1provider = mock(TemplateDefinitionProvider.class);

        when(t0provider.getTemplateDefinition()).thenReturn(t0);
        when(t0provider.getId()).thenReturn(t0.getName());
        when(t1provider.getTemplateDefinition()).thenReturn(t1);
        when(t1provider.getId()).thenReturn(t1.getName());

        pman.register(t0provider);
        pman.register(t1provider);
    }

    @Test
    public void testGetsCustomMessageCustomBundleWithPageTemplate() throws Exception {
        doTestMessage("Incredibly custom Foo label", "/pouet/lol", "custom.foo.label");
    }

    @Test
    public void testDefaultMessageFromCustomBundleWithPageTemplate() throws Exception {
        // the template's i18nBasename overrides a key from the the default bundle
        doTestMessage("Customized edit button", "/pouet/lol", "buttons.edit");
    }

    @Test
    public void testGetsCustomMessageCustomBundleWithParagraph() throws Exception {
        doTestMessage("Incredibly custom Foo label", "/foo/bar/paragraphs/1", "custom.foo.label");
    }

    @Test
    public void testDefaultMessageFromCustomBundleWithParagraph() throws Exception {
        doTestMessage("Customized edit button", "/foo/bar/paragraphs/1", "buttons.edit");
    }

    @Test
    public void testUsesDefaultBundleIfNotSpecified() throws Exception {
        doTestMessage("Edit", "/foo/bar/paragraphs/0", "buttons.edit");
    }

    @Ignore("TODO: check expectation here")
    @Test
    public void testUsesDefaultBundleIfNotRenderableDefinition() throws Exception {
        // testParagraph2 is not known by ParagraphManager
        doTestMessage("Edit", "/foo/bar/paragraphs/2", "buttons.edit");
    }

    @Ignore("TODO: check expectation here")
    @Test
    public void testUsesDefaultBundleIfNoMetadata() throws Exception {
        doTestMessage("Edit", "/no/metadata/here", "buttons.edit");
    }

    @Test
    public void testCurrentContent() throws Exception {

        final RenderingContext aggregationState = mock(RenderingContext.class);
        when(aggregationState.getMainContent()).thenReturn(getSession().getNode("/foo/bar"));

        final AbstractTemplatingElement compo = new DummyComponent(null, aggregationState);

        assertNull(compo.currentContent());

        final Node expectedNode = getSession().getNode("/foo/bar/paragraphs/1");

        when(aggregationState.getCurrentContent()).thenReturn(expectedNode);

        Node current = compo.currentContent();
        assertEquals(expectedNode, current);
    }

    private void doTestMessage(String expected, String contentPath, String key) throws RepositoryException, RegistrationException {
        final AbstractTemplatingElement compo = new DummyComponent();

        Node content = getSession().getNode(contentPath);

        TemplateDefinitionRegistry registry = Components.getComponent(TemplateDefinitionRegistry.class);
        String template = info.magnolia.jcr.util.MetaDataUtil.getMetaData(content).getTemplate();
        TemplateDefinition definition = registry.getTemplateDefinition(template);

        assertEquals(expected, compo.getDefinitionMessage(definition, key));
    }

    private static class DummyComponent extends AbstractTemplatingElement {

        public DummyComponent() {
            super(new ServerConfiguration(), null);
        }

        public DummyComponent(ServerConfiguration serverConfig, RenderingContext aggregationState) {
            super(serverConfig, aggregationState);
        }

        @Override
        public void begin(Appendable out) throws IOException {
            out.append("hello world");
        }
    }
}
