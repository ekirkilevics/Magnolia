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
package info.magnolia.rendering.template.assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionProvider;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.MockWorkspace;

import java.util.Collection;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MetaDataBasedTemplateDefinitionAssignmentTest {

    private static class SimpleTemplateDefinitionProvider implements TemplateDefinitionProvider {

        private final String id;
        private final TemplateDefinition templateDefinition;

        private SimpleTemplateDefinitionProvider(String id, TemplateDefinition templateDefinition) {
            this.id = id;
            this.templateDefinition = templateDefinition;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public TemplateDefinition getTemplateDefinition() throws RegistrationException {
            return templateDefinition;
        }

        @Override
        public String toString() {
            return "SimpleTemplateDefinitionProvider [id=" + id + ", templateDefinition=" + templateDefinition + "]";
        }
    }

    @Before
    public void setUp() {
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear(); // MgnlContext.initMockContext() uses ComponentsTestUtil
    }

    @Test
    public void testGetAssignedTemplateDefinition() throws Exception {
        // GIVEN
        final String templateId = "id";
        MockNode node = new MockNode();
        NodeUtil.setTemplate(node, templateId);

        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry();
        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        TemplateDefinitionProvider provider = mock(TemplateDefinitionProvider.class);
        when(provider.getId()).thenReturn(templateId);
        when(provider.getTemplateDefinition()).thenReturn(templateDefinition);

        registry.register(provider);
        MetaDataBasedTemplateDefinitionAssignment assignment = new MetaDataBasedTemplateDefinitionAssignment(registry);

        // WHEN
        TemplateDefinition result = assignment.getAssignedTemplateDefinition(node);

        // THEN
        assertEquals(templateDefinition, result);
    }

    @Test
    public void testGetAvailableTemplatesForDeletedNode() {

        // GIVEN
        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry();
        MetaDataBasedTemplateDefinitionAssignment assignment = new MetaDataBasedTemplateDefinitionAssignment(registry);

        TemplateDefinition deletedTemplate = mock(TemplateDefinition.class);
        registry.register(new SimpleTemplateDefinitionProvider("adminInterface:mgnlDeleted", deletedTemplate));
        registry.register(new SimpleTemplateDefinitionProvider("some:other/template/that/wont/be/returned", mock(TemplateDefinition.class)));

        MockNode mockNode = new MockNode();
        mockNode.addMixin(MgnlNodeType.MIX_DELETED);

        // WHEN
        Collection<TemplateDefinition> availableTemplates = assignment.getAvailableTemplates(mockNode);

        // TEST
        assertEquals(1, availableTemplates.size());
        assertSame(deletedTemplate, availableTemplates.iterator().next());
    }

    @Test
    public void testGetAvailableTemplatesReturnsOnlyVisibleTemplates() {

        // GIVEN
        MockUtil.initMockContext();
        MockSession session = new MockSession(new MockWorkspace("website"));
        MockUtil.setSessionAndHierarchyManager(session);

        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry();
        MetaDataBasedTemplateDefinitionAssignment assignment = new MetaDataBasedTemplateDefinitionAssignment(registry);

        MockNode mockNode = new MockNode(session);

        TemplateDefinition visibleTemplate = mock(TemplateDefinition.class);
        when(visibleTemplate.getVisible()).thenReturn(Boolean.TRUE);
        when(visibleTemplate.getId()).thenReturn("module:pages/visibleTemplate");
        registry.register(new SimpleTemplateDefinitionProvider("module:pages/visibleTemplate", visibleTemplate));

        TemplateDefinition invisibleTemplate = mock(TemplateDefinition.class);
        when(invisibleTemplate.getVisible()).thenReturn(Boolean.FALSE);
        when(invisibleTemplate.getId()).thenReturn("module:pages/invisibleTemplate");
        registry.register(new SimpleTemplateDefinitionProvider("module:pages/invisibleTemplate", invisibleTemplate));

        // WHEN
        Collection<TemplateDefinition> availableTemplates = assignment.getAvailableTemplates(mockNode);

        // TEST
        assertEquals(1, availableTemplates.size());
        assertSame(visibleTemplate, availableTemplates.iterator().next());
    }

    @Test
    public void testGetDefaultTemplateUsesTemplateFromParent() throws RepositoryException {

        // GIVEN
        MockUtil.initMockContext();
        MockSession session = new MockSession(new MockWorkspace("website"));
        MockUtil.setSessionAndHierarchyManager(session);

        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry();
        MetaDataBasedTemplateDefinitionAssignment assignment = new MetaDataBasedTemplateDefinitionAssignment(registry);

        MockNode parentNode = new MockNode(session);
        NodeUtil.setTemplate(parentNode, "module:pages/template1");
        Node mockNode = parentNode.addNode("child");

        TemplateDefinition template1 = mock(TemplateDefinition.class);
        when(template1.getVisible()).thenReturn(Boolean.TRUE);
        when(template1.getId()).thenReturn("module:pages/template1");
        registry.register(new SimpleTemplateDefinitionProvider("module:pages/template1", template1));

        TemplateDefinition template2 = mock(TemplateDefinition.class);
        when(template2.getVisible()).thenReturn(Boolean.TRUE);
        when(template2.getId()).thenReturn("module:pages/template2");
        registry.register(new SimpleTemplateDefinitionProvider("module:pages/template2", template2));

        // WHEN + THEN
        assertSame(template1, assignment.getDefaultTemplate(mockNode));

        // change template on the parent
        NodeUtil.setTemplate(parentNode, "module:pages/template2");

        // test that it changes the returned template
        assertSame(template2, assignment.getDefaultTemplate(mockNode));
    }

    @Test
    public void testGetDefaultTemplateReturnsFirstAvailableTemplate() throws RepositoryException {

        // GIVEN
        MockContext mockContext = MockUtil.initMockContext();
        mockContext.setLocale(new Locale("en")); // Template titles goes through i18n using MessagesManager and that requires a locale to be set
        MockSession session = new MockSession(new MockWorkspace("website"));
        MockUtil.setSessionAndHierarchyManager(session);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        // configure node2bean because its processor is injected into DefaultMessagesManager constructor
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);

        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry();
        MetaDataBasedTemplateDefinitionAssignment assignment = new MetaDataBasedTemplateDefinitionAssignment(registry);

        Node node = new MockNode(session).addNode("child");

        TemplateDefinition template1 = mock(TemplateDefinition.class);
        when(template1.getVisible()).thenReturn(Boolean.TRUE);
        when(template1.getId()).thenReturn("module:pages/template1");
        when(template1.getTitle()).thenReturn("ZZZ");
        registry.register(new SimpleTemplateDefinitionProvider("module:pages/template1", template1));

        TemplateDefinition template2 = mock(TemplateDefinition.class);
        when(template2.getVisible()).thenReturn(Boolean.TRUE);
        when(template2.getId()).thenReturn("module:pages/template2");
        when(template2.getTitle()).thenReturn("AAA");
        registry.register(new SimpleTemplateDefinitionProvider("module:pages/template2", template2));

        // WHEN
        assertSame(assignment.getDefaultTemplate(node), assignment.getAvailableTemplates(node).iterator().next());
    }
}
