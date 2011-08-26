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
package info.magnolia.rendering.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.test.mock.jcr.MockNode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class AbstractRendererTest {

    private static final String CONTENT_IDENTIFIER = "12345";
    private MockNode content;
    private RenderingModel<?> parentModel;
    private RenderingContext ctx;

    @Before
    public void setUp() throws RepositoryException {
        MockNode root = new MockNode();
        content = (MockNode) root.addNode("content");
        content.setIdentifier(CONTENT_IDENTIFIER);
        Context context = mock(Context.class);
        MgnlContext.setInstance(context);

        parentModel = mock(RenderingModel.class);
        when(context.getAttribute(AbstractRenderer.MODEL_ATTRIBUTE)).thenReturn(parentModel);
        ctx = mock(RenderingContext.class);
    }

    @Test(expected = RenderException.class)
    public void testRenderWhenGetIdentifierThrowsException() throws IOException, RepositoryException, RenderException {
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
        RenderingModel newModel = mock(RenderingModel.class);
        when(definition.newModel(content, definition, parentModel)).thenReturn(newModel);
        when(newModel.execute()).thenReturn("keepOnGoing");
        when(ctx.getCurrentContent()).thenReturn(content);

        AbstractRenderer renderer = new DummyRenderer() {
            @Override
            protected String determineTemplatePath(Node content, RenderableDefinition definition, RenderingModel<?> model, String actionResult) {
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
        RenderingModel newModel = mock(RenderingModel.class);
        when(definition.newModel(content, definition, parentModel)).thenReturn(newModel);
        when(newModel.execute()).thenReturn(RenderingModel.SKIP_RENDERING);
        when(ctx.getCurrentContent()).thenReturn(content);
        when(ctx.getRenderableDefinition()).thenReturn(definition);

        AbstractRenderer renderer = new DummyRenderer() {
            @Override
            protected String determineTemplatePath(Node content, RenderableDefinition definition, RenderingModel<?> model, String actionResult) {
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
        RenderingModel newModel = mock(RenderingModel.class);
        when(definition.newModel(content, definition, parentModel)).thenReturn(newModel);
        when(newModel.execute()).thenReturn("keepOnGoing");
        when(ctx.getCurrentContent()).thenReturn(content);
        when(ctx.getRenderableDefinition()).thenReturn(definition);

        DummyRenderer renderer = new DummyRenderer();
        // WHEN
        renderer.render(ctx, contextObjects);

        // THEN
        assertTrue(renderer.wasOnRenderCalled());
        assertEquals(parentModel, MgnlContext.getAttribute(AbstractRenderer.MODEL_ATTRIBUTE));
    }
}
