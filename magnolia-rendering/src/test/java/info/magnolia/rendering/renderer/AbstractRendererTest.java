/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.rendering.renderer;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.test.AbstractMagnoliaTestCase;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class AbstractRendererTest extends AbstractMagnoliaTestCase {

    public static class DummyModel extends RenderingModelImpl<RenderableDefinition> {

        public DummyModel(Node content, RenderableDefinition definition, RenderingModel<?> parent) {
            super(content, definition, parent);
        }

        @Override
        public String execute() {
            return "keepOnGoing";
        }
    }

    public static class SkipRenderingDummyModel extends RenderingModelImpl<RenderableDefinition> {

        public SkipRenderingDummyModel(Node content, RenderableDefinition definition, RenderingModel<?> parent) {
            super(content, definition, parent);
        }

        @Override
        public String execute() {
            return RenderingModel.SKIP_RENDERING;
        }
    }

    private static final String CONTENT_IDENTIFIER = "12345";
    private MockNode content;
    private RenderingModel<?> parentModel;
    private RenderingContext ctx;
    private HttpServletRequest request;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockNode root = new MockNode();
        content = (MockNode) root.addNode("content");
        content.setIdentifier(CONTENT_IDENTIFIER);
        request = mock(HttpServletRequest.class);
        parentModel = mock(RenderingModel.class);

        MockWebContext context = new MockWebContext();
        context.setAttribute(AbstractRenderer.MODEL_ATTRIBUTE, parentModel, 3);
        context.setRequest(request);
        context.getAggregationState().setMainContent(new MockContent("content"));
        MgnlContext.setInstance(context);

        ctx = mock(RenderingContext.class);

        Components.setComponentProvider(new MockComponentProvider());
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
    }

    @Override
    @After
    public void tearDown() throws Exception{
        super.tearDown();
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test(expected = RenderException.class)
    public void testRenderWhenGetIdentifierThrowsException() throws RepositoryException, RenderException {
        // GIVEN
        Node content = mock(Node.class);
        doThrow(new RepositoryException()).when(content).getIdentifier();
        AbstractRenderer renderer = new DummyRenderer();
        when(ctx.getCurrentContent()).thenReturn(content);
        // WHEN
        renderer.render(ctx, null);

        // THEN - expect Exception in line above...
    }

    @Test(expected=RenderException.class)
    public void testRenderWithTemplateScriptBeingNull() throws Exception {
        // GIVEN
        Map<String, Object> contextObjects = new LinkedHashMap<String, Object>();
        RenderableDefinition definition = mock(RenderableDefinition.class);
        when(definition.getModelClass()).thenReturn(DummyModel.class);
        when(ctx.getCurrentContent()).thenReturn(content);
        when(ctx.getRenderableDefinition()).thenReturn(definition);

        AbstractRenderer renderer = new DummyRenderer() {
            @Override
            protected String resolveTemplateScript(Node content, RenderableDefinition definition, RenderingModel<?> model, String actionResult) {
                return null;
            }
        };
        // WHEN
        renderer.render(ctx, contextObjects);

        // THEN - expected Exception
    }

    @Test
    public void testRenderWithModelAttributePrefixBeingNullAndRenderingSkipped() throws Exception {
        // GIVEN
        RenderableDefinition definition = mock(RenderableDefinition.class);
        when(definition.getModelClass()).thenReturn(SkipRenderingDummyModel.class);
        when(ctx.getCurrentContent()).thenReturn(content);
        when(ctx.getRenderableDefinition()).thenReturn(definition);

        AbstractRenderer renderer = new DummyRenderer() {
            @Override
            protected String resolveTemplateScript(Node content, RenderableDefinition definition, RenderingModel<?> model, String actionResult) {
                throw new RuntimeException("Should have exited method before this call!");
            }
        };
        // WHEN
        renderer.render(ctx, null);

        // THEN - didn't get a RuntimeException on call above so execution stopped before calling determineTemplatePath...

    }

    @Test
    public void testRender() throws Exception {
        // GIVEN
        Map<String, Object> contextObjects = new LinkedHashMap<String, Object>();
        RenderableDefinition definition = mock(RenderableDefinition.class);
        when(definition.getModelClass()).thenReturn(DummyModel.class);
        when(ctx.getCurrentContent()).thenReturn(content);
        when(ctx.getRenderableDefinition()).thenReturn(definition);

        DummyRenderer renderer = new DummyRenderer();
        // WHEN
        renderer.render(ctx, contextObjects);

        // THEN
        assertTrue(renderer.wasOnRenderCalled());
        assertEquals(parentModel, MgnlContext.getAttribute(AbstractRenderer.MODEL_ATTRIBUTE));
    }

    @Test
    public void testWillNotFailModelCreationWhenSquareBracketIsInQueryString() throws RenderException {
        // GIVEN
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put("checkbox[]", new String[] { "a", "b", "c" });
        map.put("param", new String[] { "value" });
        map.put("number", new String[] { "10" });
        when(request.getParameterMap()).thenReturn(map);
        DummyRenderer renderer = new DummyRenderer();
        RenderableDefinition definition = mock(RenderableDefinition.class);

        // WHEN
        MyDummyModel model = renderer.newModel(MyDummyModel.class, content, definition, parentModel);

        // THEN
        assertEquals("value", model.getParam());
        assertEquals(10, model.getNumber());
        assertArrayEquals(new String[] { "a", "b", "c" }, model.getCheckbox());
    }

    public class MyDummyModel extends DummyModel {

        private String[] checkbox = new String[3];

        private String param;

        private int number;

        public MyDummyModel() {
            super(null, null, null);
        }

        public String[] getCheckbox() {
            return checkbox;
        }

        public void setCheckbox(String[] checkbox) {
            this.checkbox = checkbox;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }
}
