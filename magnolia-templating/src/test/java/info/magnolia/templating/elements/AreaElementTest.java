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

import java.io.IOException;
//import java.io.StringWriter;
//import java.lang.reflect.InvocationTargetException;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
//import org.mockito.ArgumentMatcher;
//
//import info.magnolia.cms.beans.config.ServerConfiguration;
//import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
//import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
//import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
//import info.magnolia.cms.i18n.DefaultI18nContentSupport;
//import info.magnolia.cms.i18n.DefaultMessagesManager;
//import info.magnolia.cms.i18n.I18nContentSupport;
//import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
//import info.magnolia.context.WebContext;
//import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.SessionTestUtil;
//import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
//import info.magnolia.rendering.engine.DefaultRenderingEngine;
//import info.magnolia.rendering.model.RenderingModel;
//import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.AreaDefinition;
//import info.magnolia.rendering.template.ComponentAvailability;
//import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
//import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;
//import static org.junit.Assert.*;
//import static org.mockito.Matchers.argThat;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.*;

/**
 * Tests for AreaMarker.
 *
 * @version $Id$
 */
public class AreaElementTest {

//    private TemplateDefinitionAssignment templateDefinitionAssignment;
//    private ConfiguredTemplateDefinition templateDefinition;
//    private Node pageNode;
//    private Node areaNode;
//    private Node componentNode;
//    private AggregationState aggregationState;
//    private ServerConfiguration serverCfg;
//    private AreaElement areaElement;
//    private AggregationStateBasedRenderingContext context;
//    private AreaDefinition areaDefinition;

    private static final String TEST_WORKSPACE = "test";
    private static final String PAGE_PATH = "/foo/bar/baz";
    private static final String AREA_NAME = "area";

    @Before
    public void setUp() throws Exception {
        final MockSession session = SessionTestUtil.createSession(TEST_WORKSPACE,
            PAGE_PATH + "/" + AREA_NAME,
            "/foo/bar/baz/area/component");

//        aggregationState = new AggregationState();
//        pageNode = session.getNode(PAGE_PATH);
//        singleAreaNode = session.getNode("/foo/bar/baz/area");
//        listAreaNode = session.getNode("/foo/bar/baz/listArea");
//        component01Node = session.getNode("/foo/bar/baz/listArea/01");
//
//        final WebContext ctx = mock(WebContext.class);
//        when(ctx.getJCRSession(TEST_WORKSPACE, TEST_WORKSPACE)).thenReturn(session);
//        MgnlContext.setInstance(ctx);
//
//        serverCfg = new ServerConfiguration();
//        serverCfg.setAdmin(true);
//        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
//        // register some default components used internally
//        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
//        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
//        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());
//
//        templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
//        templateDefinition = new ConfiguredTemplateDefinition();
//
//        ComponentsTestUtil.setInstance(TemplateDefinitionAssignment.class, templateDefinitionAssignment);
//        DefaultRenderingEngine engine = new DefaultRenderingEngine(new RendererRegistry(), templateDefinitionAssignment);
//        context = new AggregationStateBasedRenderingContext(aggregationState);
//        areaElement = new AreaElement(serverCfg, context, engine);
//        areaDefinition = new ConfiguredAreaDefinition();
//        ((ConfiguredAreaDefinition) areaDefinition).setEnabled(true);
//
//        when(templateDefinitionAssignment.getAssignedTemplateDefinition(component01Node)).thenReturn(templateDefinition);

    }

    @Test @Ignore
    public void testThatTheRenderingEngineIsCalledWithTheCorrectContentAndDefinition() throws Exception {
//        // GIVEN
//        Node page = createPageContent(AreaDefinition.TYPE_SINGLE);
//        TemplateDefinition templateDefinition = createTemplateDefinition(AreaDefinition.TYPE_SINGLE);
//
//        // WHEN
//        render(page, templateDefinition);
//
//        // THEN
//        assertTheRenderingEngineWasCalled(areaNode, templateDefinition.getAreas().get(AREA_NAME));
//        assertRenderWithContextObject(AreaElement.ATTRIBUTE_COMPONENT, areaNode);
//        assertRenderedOutputContains();
//
//
//        context.push(pageNode, templateDefinition);
//
//        DefaultRenderingEngine engine = mock(DefaultRenderingEngine.class);
//        areaElement = new AreaElement(serverCfg, context, engine);
//        areaElement.setArea(areaDefinition);
//        areaElement.setName("singleArea");
//
//        areaElement.setType(AreaDefinition.TYPE_SINGLE);
//
//        final StringWriter out = new StringWriter();
//        areaElement.begin(out);
//        areaElement.end(out);
//
//        verify(engine).render(eq(singleAreaNode), eq(areaDefinition), argThat(new ArgumentMatcher<Map<String, Object>>() {
//            @Override
//            public boolean matches(Object componentsMap) {
//                Object component = ((Map<String, Object>) componentsMap).get(AreaElement.ATTRIBUTE_COMPONENT);
//                boolean result = false;
//                try {
//                    // single: the passed area is the component
//                    result = component != null && ((ContentMap) component).getJCRNode().getName().equals(singleAreaNode.getName());
//                } catch (RepositoryException e) {
//                    e.printStackTrace();
//                }
//                return result;
//            }
//        }), eq(out));
//
//        String outString = out.toString();
//
//        assertEquals(outString,
//                "<!-- cms:begin cms:content=\"test:/foo/bar/baz/singleArea\" -->" +
//                "\r\n" +
//                "<cms:area content=\"test:/foo/bar/baz\" name=\"singleArea\" availableComponents=\"\" type=\"single\" showAddButton=\"false\"></cms:area>" +
//                "\r\n" +
//                "<!-- cms:end cms:content=\"test:/foo/bar/baz/singleArea\" -->"
//                + "\r\n", outString);
    }

    private TemplateDefinition createTemplateDefinition(String areaType) {
        ConfiguredTemplateDefinition templateDefinition = new ConfiguredTemplateDefinition();
        ConfiguredAreaDefinition areaDefinition = new ConfiguredAreaDefinition();
        areaDefinition.setEnabled(true);
        areaDefinition.setType(areaType);
        templateDefinition.addArea(AREA_NAME, areaDefinition);
        return templateDefinition;
    }

    private Node createPageContent(String areaType) throws IOException, RepositoryException {
        final MockSession session;
        if(areaType.equals(AreaDefinition.TYPE_LIST)){
            session = SessionTestUtil.createSession(TEST_WORKSPACE,
                PAGE_PATH + "/" + AREA_NAME,
                PAGE_PATH + "/" + AREA_NAME + "/" + "component1",
                PAGE_PATH + "/" + AREA_NAME + "/" + "component2");
        }
        else{
            session = SessionTestUtil.createSession(TEST_WORKSPACE,
                PAGE_PATH + "/" + AREA_NAME);
        }
        return session.getNode(PAGE_PATH);
    }

/*
    @Test
    public void testComponentsResolvedFromPathAndWorkspace() throws Exception {
        // input TYPE_LIST, output componentMap with all paragraphs

        DefaultRenderingEngine engine = mock(DefaultRenderingEngine.class);
        areaElement = new AreaElement(serverCfg, context, engine);
        areaElement.setArea(areaDefinition);
        // areaComponent.setName("area");
        areaElement.setWorkspace("test");
        areaElement.setPath("/foo/bar/baz");

        areaElement.setType(AreaDefinition.TYPE_LIST);

        final StringWriter out = new StringWriter();
        areaElement.begin(out);
        areaElement.end(out);

        verify(engine).render(eq(component01Node.getParent()), eq(areaDefinition), argThat(new ArgumentMatcher<Map<String, Object>>() {
            @Override
            public boolean matches(Object componentsMap) {
                List<ContentMap> componentList = (List<ContentMap>) ((Map<String, Object>) componentsMap).get(AreaElement.ATTRIBUTE_COMPONENTS);
                boolean result = false;
                try {
                    result = componentList != null && componentList.size() == 1 && (componentList.get(0)).getJCRNode().getName().equals(component01Node.getName());
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }), eq(out));

        String outString = out.toString();

        assertEquals(outString,
            "<!-- cms:begin cms:content=\"test:/foo/bar/baz/listArea\" -->" +
            "\r\n" +
            "<cms:area content=\"test:/foo/bar/baz/listArea\" availableComponents=\"\" type=\"list\" showAddButton=\"true\"></cms:area>" +
            "\r\n" +
            "<!-- cms:end cms:content=\"test:/foo/bar/baz/listArea\" -->"
            + "\r\n", outString);
    }

    @Test
    public void testComponentsResolvedFromAggregationState() throws Exception {
        // input TYPE_LIST, output componentMap with all paragraphs
        context.push(component01Node.getParent(), templateDefinition);

        DefaultRenderingEngine engine = mock(DefaultRenderingEngine.class);
        areaElement = new AreaElement(serverCfg, context, engine);
        areaElement.setArea(areaDefinition);

        areaElement.setType(AreaDefinition.TYPE_LIST);

        final StringWriter out = new StringWriter();
        areaElement.begin(out);
        areaElement.end(out);

        verify(engine).render(eq(component01Node.getParent()), eq(areaDefinition), argThat(new ArgumentMatcher<Map<String, Object>>() {
            @Override
            public boolean matches(Object componentsMap) {
                List<ContentMap> componentList = (List<ContentMap>) ((Map<String, Object>) componentsMap).get(AreaElement.ATTRIBUTE_COMPONENTS);
                boolean result = false;
                try {
                    result = componentList != null && componentList.size() == 1 && (componentList.get(0)).getJCRNode().getName().equals(component01Node.getName());
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }), eq(out));

        String outString = out.toString();

        assertEquals(outString,
            "<!-- cms:begin cms:content=\"test:/foo/bar/baz/listArea\" -->" +
            "\r\n" +
            "<cms:area content=\"test:/foo/bar/baz/listArea\" availableComponents=\"\" type=\"list\" showAddButton=\"true\"></cms:area>" +
            "\r\n"+
            "<!-- cms:end cms:content=\"test:/foo/bar/baz/listArea\" -->"
            + "\r\n", outString);
    }

    @Test
    public void testDefault() throws Exception {
        // default input type is TYPE_LIST, output componentMap with all paragraphs
    }

    @Test
    public void testDoRender() throws Exception {
        context.push(component01Node.getParent(), templateDefinition);

        StringWriter out = new StringWriter();
        areaElement.setName("listArea");
        areaElement.begin(out);

        assertEquals("<!-- cms:begin cms:content=\"test:/foo/bar/baz/listArea\" -->"
                + "\r\n"
                + "<cms:area content=\"test:/foo/bar/baz/listArea\" name=\"listArea\" availableComponents=\"\" type=\"list\" showAddButton=\"true\"></cms:area>"
                + "\r\n", out.toString());

        // with paragraph set
        out = new StringWriter();
        areaElement.setAvailableComponents("paragraphs/myParagraph");
        areaElement.begin(out);

        assertEquals("<!-- cms:begin cms:content=\"test:/foo/bar/baz/listArea\" -->"
                + "\r\n"
                + "<cms:area content=\"test:/foo/bar/baz/listArea\" name=\"listArea\" availableComponents=\"paragraphs/myParagraph\" type=\"list\" showAddButton=\"true\"></cms:area>"
                + "\r\n", out.toString());

        // as collection == false (= singleton)
        out = new StringWriter();
        areaElement.setType(AreaDefinition.TYPE_SINGLE);
        areaElement.begin(out);

        assertEquals(
                "<!-- cms:begin cms:content=\"test:/foo/bar/baz/listArea\" -->"
                + "\r\n"
                + "<cms:area content=\"test:/foo/bar/baz/listArea\" name=\"listArea\" availableComponents=\"paragraphs/myParagraph\" type=\"single\" showAddButton=\"true\"></cms:area>"
                + "\r\n", out.toString());
    }

    @Test
    public void testPostRender() throws Exception {
        context.push(component01Node.getParent(), templateDefinition);

        ((ConfiguredAreaDefinition) areaDefinition).setEnabled(false);

        final StringWriter out = new StringWriter();
        areaElement.end(out);

        String outString = out.toString();

        assertEquals(outString, "<!-- cms:end cms:content=\"test:/foo/bar/baz/listArea\" -->"
                + "\r\n", outString);
    }

    @Test
    public void testResolveMethods() throws Exception {
        context.push(component01Node.getParent(), templateDefinition);
        AreaDefinition areaDef = new AreaDefinition() {

            @Override
            public String getDialog() {
                return null;
            }

            @Override
            public Map<String, AreaDefinition> getAreas() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public void setId(String id) {
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getRenderType() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public String getI18nBasename() {
                return null;
            }

            @Override
            public String getTemplateScript() {
                return null;
            }

            @Override
            public Map<String, Object> getParameters() {
                return null;
            }

            @Override
            public RenderingModel<?> newModel(Node content, RenderableDefinition definition, RenderingModel<?> parentModel) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
                return null;
            }

            @Override
            public boolean isAvailable(Node content) {
                return false;
            }

            @Override
            public Map<String, ComponentAvailability> getAvailableComponents() {
                return Collections.emptyMap();
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public String getType() {
                return null;
            }

        };
        areaElement.setArea(areaDef);
        assertFalse(areaElement.resolveAreaDefinition() instanceof ConfiguredAreaDefinition);
        areaElement.setArea(null);
        assertTrue(areaElement.resolveAreaDefinition() instanceof ConfiguredAreaDefinition);
    }

    @Test
    public void testNotCloningOfExplicitlySetAreaDefinition() throws Exception {
        context.push(component01Node.getParent(), templateDefinition);
        ConfiguredAreaDefinition defaultArea = new ConfiguredAreaDefinition();
        defaultArea.setDescription("unmodified_description");
        defaultArea.setDialog("unmodified_dialog");
        defaultArea.setName("unmodified_name");

        areaElement.setArea(defaultArea);
        areaElement.setDialog("boo");
        areaElement.setName("baz");

        final StringWriter out = new StringWriter();
        areaElement.begin(out);

        assertEquals("boo", defaultArea.getDialog());
        assertEquals("baz", defaultArea.getName());
        assertEquals("list", defaultArea.getType());
        assertEquals("<!-- cms:begin cms:content=\"test:/foo/bar/baz/listArea\" -->"
                + "\r\n"
                + "<cms:area content=\"test:/foo/bar/baz/listArea\" name=\"baz\" availableComponents=\"\" type=\"list\" dialog=\"boo\" showAddButton=\"true\"></cms:area>"
                + "\r\n", out.toString());
    }

    @Test
    public void testCloningOfDynamicallyResolvedAreaDefinition() throws Exception {
        context.push(component01Node.getParent(), templateDefinition);

        areaElement.setName("boo");

        final StringWriter out = new StringWriter();
        areaElement.begin(out);

        assertEquals("<!-- cms:begin cms:content=\"test:/foo/bar/baz/listArea\" -->"
                + "\r\n"
                + "<cms:area content=\"test:/foo/bar/baz/listArea\" name=\"boo\" availableComponents=\"\" type=\"list\" showAddButton=\"true\"></cms:area>"
                + "\r\n", out.toString());
    }
    */

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }
}
