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
package info.magnolia.rendering.engine;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */

public class ModeDependentRenderExceptionHandlerTest {

    private RenderingContext renderingContext;
    private AppendableWriter out;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
        renderingContext = mock(RenderingContext.class);

        Node content = mock(Node.class);
        when(content.getPath()).thenReturn("/curren");
        when(renderingContext.getCurrentContent()).thenReturn(content);

        RenderableDefinition renderableDefinition = mock(RenderableDefinition.class);
        when(renderableDefinition.getId()).thenReturn("the-id");
        when(renderingContext.getRenderableDefinition()).thenReturn(renderableDefinition);

        out = mock(AppendableWriter.class);
        when(renderingContext.getAppendable()).thenReturn(out);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testOnlyExceptionGetsFlushedInPublicMode() throws IOException {
        // GIVEN
        final RenderException ex = mock(RenderException.class);
        final ServerConfiguration serverConfig = new ServerConfiguration();
        serverConfig.setAdmin(false);

        ModeDependentRenderExceptionHandler handler = new ModeDependentRenderExceptionHandler(serverConfig);

        // WHEN
        handler.handleException(ex, renderingContext);

        // THEN
        verify(ex).printStackTrace((PrintWriter) any());
        verify(out).flush();
    }

    @Test
    public void testOnlyExceptionGetsFlushedInAdminPreviewMode() throws IOException {
        // GIVEN
        final RenderException ex = mock(RenderException.class);
        final ServerConfiguration serverConfig = new ServerConfiguration();
        serverConfig.setAdmin(true);

        MgnlContext.getAggregationState().setPreviewMode(true);
        ModeDependentRenderExceptionHandler handler = new ModeDependentRenderExceptionHandler(serverConfig);

        // WHEN
        handler.handleException(ex, renderingContext);

        // THEN
        verify(ex).printStackTrace((PrintWriter) any());
        verify(out).flush();
    }

    @Test
    public void testExceptionAndAdditionalMessageGetsFlushedAdminNonPreviewMode() throws IOException{
        // GIVEN
        // use non-printWriter on purpose here in order to test this setUp as well.
        final RenderException ex = mock(RenderException.class);
        final ServerConfiguration serverConfig = new ServerConfiguration();
        serverConfig.setAdmin(true);

        MgnlContext.getAggregationState().setPreviewMode(false);
        ModeDependentRenderExceptionHandler handler = new ModeDependentRenderExceptionHandler(serverConfig);

        // WHEN
        handler.handleException(ex, renderingContext);

        // THEN
        verify(out).write(ModeDependentRenderExceptionHandler.RENDER_ERROR_MESSAGE_BEGIN, 0, ModeDependentRenderExceptionHandler.RENDER_ERROR_MESSAGE_BEGIN.length());
        verify(ex).printStackTrace((PrintWriter) any());
        verify(out).write(ModeDependentRenderExceptionHandler.RENDER_ERROR_MESSAGE_END, 0, ModeDependentRenderExceptionHandler.RENDER_ERROR_MESSAGE_END.length());
        verify(out).flush();
    }

}
