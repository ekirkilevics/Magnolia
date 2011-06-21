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
package info.magnolia.templating.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.engine.DefaultRenderingEngine;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredParagraphAvailability;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.templating.components.AbstractContentComponent;
import info.magnolia.templating.components.AreaComponent;
import info.magnolia.templating.components.EditComponent;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

/**
 * Tests for AreaMarker.
 *
 * @version $Id$
 */
public class AreaComponentTest {

    private TemplateDefinitionAssignment templateDefinitionAssignment;
    private ConfiguredTemplateDefinition templateDefinition;
    private Node paragraph01;
    private AggregationState aggregationState;
    private ServerConfiguration serverCfg;
    private AreaComponent areaComponent;
    private AggregationStateBasedRenderingContext context;
    private AreaDefinition area;
    private Node currentPage;

    @Before
    public void setUp() throws Exception {
        final MockSession session = SessionTestUtil.createSession("testRepository", "/foo/bar/baz/paragraphsArea/01.text=dummy"
                + "/foo/bar/baz/paragraphs/02.text=dummy2\n");

        aggregationState = new AggregationState();
        currentPage = session.getNode("/foo/bar/baz");
        paragraph01 = session.getNode("/foo/bar/baz/paragraphsArea/01");

        final WebContext ctx = mock(WebContext.class);
        when(ctx.getJCRSession("testRepository", "testRepository")).thenReturn(session);
        MgnlContext.setInstance(ctx);

        serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());

        templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        templateDefinition = new ConfiguredTemplateDefinition();

        ComponentsTestUtil.setInstance(TemplateDefinitionAssignment.class, templateDefinitionAssignment);
        DefaultRenderingEngine engine = new DefaultRenderingEngine(new RendererRegistry(), templateDefinitionAssignment);
        context = new AggregationStateBasedRenderingContext(aggregationState);
        areaComponent = new AreaComponent(serverCfg, context, engine);
        area = new ConfiguredAreaDefinition();
        ((ConfiguredAreaDefinition) area).setEnabled(true);
        areaComponent.setArea(area);
        areaComponent.setName("paragraphsArea");

        when(templateDefinitionAssignment.getAssignedTemplateDefinition(paragraph01)).thenReturn(templateDefinition);

    }

    @Test
    public void testComponent() throws Exception {
        // input TYPE_SINLGE, output componentMap of current area/paragraph
        // confusing bit about this test is that the area IS paragraph and it's named 01 NOT the paragraphsArea
        context.push(paragraph01, templateDefinition);

        DefaultRenderingEngine engine = mock(DefaultRenderingEngine.class);
        areaComponent = new AreaComponent(serverCfg, context, engine);
        areaComponent.setArea(area);

        areaComponent.setType(AreaComponent.TYPE_SINGLE);

        final StringWriter out = new StringWriter();
        areaComponent.postRender(out);

        verify(engine).render(eq(paragraph01), eq(area), argThat(new ArgumentMatcher<Map<String, Object>>() {
            @Override
            public boolean matches(Object componentsMap) {
                Object component = ((Map<String, Object>) componentsMap).get(AreaComponent.COMPONENT);
                boolean result = false;
                try {
                    result = component != null && ((ContentMap) component).getJCRNode().getName().equals(paragraph01.getName());
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }), eq(out));

        String outString = out.toString();

        assertEquals(outString, "<!-- cms:end cms:content=\"testRepository:/foo/bar/baz/paragraphsArea/01\" -->"
                + AbstractContentComponent.LINEBREAK, outString);
    }

    @Test
    public void testComponentsResolvedFromPathAndWorkspace() throws Exception {
        // input TYPE_LIST, output componentMap with all paragraphs

        DefaultRenderingEngine engine = mock(DefaultRenderingEngine.class);
        areaComponent = new AreaComponent(serverCfg, context, engine);
        areaComponent.setArea(area);
        // areaComponent.setName("paragraphsArea");
        areaComponent.setWorkspace("testRepository");
        areaComponent.setPath("/foo/bar/baz/paragraphsArea");

        areaComponent.setType(AreaComponent.TYPE_LIST);

        final StringWriter out = new StringWriter();
        areaComponent.postRender(out);

        verify(engine).render(eq(paragraph01.getParent()), eq(area), argThat(new ArgumentMatcher<Map<String, Object>>() {
            @Override
            public boolean matches(Object componentsMap) {
                List<ContentMap> componentList = (List<ContentMap>) ((Map<String, Object>) componentsMap).get(AreaComponent.COMPONENTS);
                boolean result = false;
                try {
                    result = componentList != null && componentList.size() == 1 && (componentList.get(0)).getJCRNode().getName().equals(paragraph01.getName());
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }), eq(out));

        String outString = out.toString();

        assertEquals(outString, "<!-- cms:end cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + AbstractContentComponent.LINEBREAK, outString);
    }

    @Test
    public void testComponentsResolvedFromAggregationState() throws Exception {
        // input TYPE_LIST, output componentMap with all paragraphs
        context.push(paragraph01.getParent(), templateDefinition);

        DefaultRenderingEngine engine = mock(DefaultRenderingEngine.class);
        areaComponent = new AreaComponent(serverCfg, context, engine);
        areaComponent.setArea(area);

        areaComponent.setType(AreaComponent.TYPE_LIST);

        final StringWriter out = new StringWriter();
        areaComponent.postRender(out);

        verify(engine).render(eq(paragraph01.getParent()), eq(area), argThat(new ArgumentMatcher<Map<String, Object>>() {
            @Override
            public boolean matches(Object componentsMap) {
                List<ContentMap> componentList = (List<ContentMap>) ((Map<String, Object>) componentsMap).get(AreaComponent.COMPONENTS);
                boolean result = false;
                try {
                    result = componentList != null && componentList.size() == 1 && (componentList.get(0)).getJCRNode().getName().equals(paragraph01.getName());
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }), eq(out));

        String outString = out.toString();

        assertEquals(outString, "<!-- cms:end cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + AbstractContentComponent.LINEBREAK, outString);
    }

    @Test
    public void testDefault() throws Exception {
        // default input type is TYPE_LIST, output componentMap with all paragraphs
    }

    @Test
    public void testDoRender() throws Exception {
        context.push(paragraph01.getParent(), templateDefinition);

        StringWriter out = new StringWriter();
        areaComponent.setName("paragraphsArea");
        areaComponent.doRender(out);

        assertEquals("<!-- cms:begin cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + EditComponent.LINEBREAK
                + "<cms:area content=\"testRepository:/foo/bar/baz/paragraphsArea\" name=\"paragraphsArea\" availableComponents=\"\" type=\"list\" showAddButton=\"true\"></cms:area>"
                + EditComponent.LINEBREAK, out.toString());

        // with paragraph set
        out = new StringWriter();
        areaComponent.setAvailableComponents("paragraphs/myParagraph");
        areaComponent.doRender(out);

        assertEquals("<!-- cms:begin cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + EditComponent.LINEBREAK
                + "<cms:area content=\"testRepository:/foo/bar/baz/paragraphsArea\" name=\"paragraphsArea\" availableComponents=\"paragraphs/myParagraph\" type=\"list\" showAddButton=\"true\"></cms:area>"
                + EditComponent.LINEBREAK, out.toString());

        // as collection == false (= singleton)
        out = new StringWriter();
        areaComponent.setType(AreaComponent.TYPE_SINGLE);
        areaComponent.doRender(out);

        assertEquals(
                "<!-- cms:begin cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + EditComponent.LINEBREAK
                + "<cms:area content=\"testRepository:/foo/bar/baz/paragraphsArea\" name=\"paragraphsArea\" availableComponents=\"paragraphs/myParagraph\" type=\"single\" showAddButton=\"true\"></cms:area>"
                + EditComponent.LINEBREAK, out.toString());
    }

    @Test
    public void testPostRender() throws Exception {
        context.push(paragraph01.getParent(), templateDefinition);

        ((ConfiguredAreaDefinition) area).setEnabled(false);

        final StringWriter out = new StringWriter();
        areaComponent.postRender(out);

        String outString = out.toString();

        assertEquals(outString, "<!-- cms:end cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + AbstractContentComponent.LINEBREAK, outString);
    }

    @Test
    public void testResolveMethods() throws Exception {
        context.push(paragraph01.getParent(), templateDefinition);
        AreaDefinition areaDef = new AreaDefinition() {

            @Override
            public Object clone() {
                try {
                    return super.clone();
                } catch (CloneNotSupportedException e) {
                    return null;
                }
            }

            public String getDialog() {
                return null;
            }

            public Map<String, AreaDefinition> getAreas() {
                return null;
            }

            public String getId() {
                return null;
            }

            public void setId(String id) {
            }

            public String getName() {
                return null;
            }

            public String getRenderType() {
                return null;
            }

            public String getTitle() {
                return null;
            }

            public String getDescription() {
                return null;
            }

            public String getI18nBasename() {
                return null;
            }

            public String getTemplateScript() {
                return null;
            }

            public Map<String, Object> getParameters() {
                return null;
            }

            public RenderingModel<?> newModel(Node content, RenderableDefinition definition, RenderingModel<?> parentModel) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
                return null;
            }

            public boolean isAvailable(Node content) {
                return false;
            }

            public Map<String, ConfiguredParagraphAvailability> getAvailableParagraphs() {
                return Collections.EMPTY_MAP;
            }

            public Boolean getEnabled() {
                return null;
            }

            public void setName(String resolvedName) {
            }

            public void setAvailableComponentNames(String availableComponentNames) {
            }

            public String getAvailableComponentNames() {
                return null;
            }

            public void setDialog(String resolvedDialog) {
            }

            public void setRenderType(String renderType) {
            }
        };
        areaComponent.setArea(areaDef);
        assertFalse(areaComponent.resolveAreaDefinition() instanceof ConfiguredAreaDefinition);
        areaComponent.setArea(null);
        assertTrue(areaComponent.resolveAreaDefinition() instanceof ConfiguredAreaDefinition);
    }

    @Test
    public void testNotCloningOfExplicitlySetAreaDefinition() throws Exception {
        context.push(paragraph01.getParent(), templateDefinition);
        ConfiguredAreaDefinition defaultArea = new ConfiguredAreaDefinition();
        defaultArea.setDescription("unmodified_description");
        defaultArea.setDialog("unmodified_dialog");
        defaultArea.setName("unmodified_name");

        areaComponent.setArea(defaultArea);
        areaComponent.setDialog("boo");
        areaComponent.setName("baz");

        final StringWriter out = new StringWriter();
        areaComponent.doRender(out);

        assertEquals("boo", defaultArea.getDialog());
        assertEquals("baz", defaultArea.getName());
        assertEquals("list", defaultArea.getRenderType());
        assertEquals("<!-- cms:begin cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + EditComponent.LINEBREAK
                + "<cms:area content=\"testRepository:/foo/bar/baz/paragraphsArea\" name=\"baz\" availableComponents=\"\" type=\"list\" dialog=\"boo\" showAddButton=\"true\"></cms:area>"
                + EditComponent.LINEBREAK, out.toString());
    }

    @Test
    public void testCloningOfDynamicallyResolvedAreaDefinition() throws Exception {
        context.push(paragraph01.getParent(), templateDefinition);

        areaComponent.setName("boo");

        final StringWriter out = new StringWriter();
        areaComponent.doRender(out);

        assertEquals("<!-- cms:begin cms:content=\"testRepository:/foo/bar/baz/paragraphsArea\" -->"
                + EditComponent.LINEBREAK
                + "<cms:area content=\"testRepository:/foo/bar/baz/paragraphsArea\" name=\"boo\" availableComponents=\"\" type=\"list\" showAddButton=\"true\"></cms:area>"
                + EditComponent.LINEBREAK, out.toString());
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }
}
