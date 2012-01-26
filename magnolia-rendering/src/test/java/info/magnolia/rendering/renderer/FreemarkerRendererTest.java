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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import freemarker.template.TemplateException;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.configured.ConfiguredRenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class FreemarkerRendererTest {

    private FreemarkerRenderer renderer;

    @Before
    public void setUp() throws Exception {
        final FreemarkerHelper helper = mock(FreemarkerHelper.class);
        renderer = new FreemarkerRenderer(helper);
        Components.setComponentProvider(new MockComponentProvider());

        MgnlContext.setInstance(new MockWebContext());
        ComponentsTestUtil.setInstance(SystemContext.class, new MockContext());
    }

    @After
    public void tearDown() {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testOnRender() throws Exception {
        // GIVEN
        final RenderingContext rctx = mock(RenderingContext.class);
        final AppendableWriter writer = mock(AppendableWriter.class);
        when(rctx.getAppendable()).thenReturn(writer);

        final ConfiguredRenderableDefinition rd = new ConfiguredRenderableDefinition();
        final String i18nBasename = "basename";
        rd.setI18nBasename(i18nBasename);
        // WHEN
        renderer.onRender(null, rd, rctx, null, null);

        // THEN
        verify(renderer.getFmHelper()).render(null, null, i18nBasename, null, writer);
    }

    @Test
    public void testOnRenderDoesntBubbleUpNonIOException() throws Exception {
        // GIVEN
        final RenderingContext rctx = mock(RenderingContext.class);
        final AppendableWriter writer = mock(AppendableWriter.class);
        when(rctx.getAppendable()).thenReturn(writer);

        final ConfiguredRenderableDefinition rd = new ConfiguredRenderableDefinition();
        final String i18nBasename = "basename";
        rd.setI18nBasename(i18nBasename);
        doThrow(new TemplateException(null)).when(renderer.getFmHelper()).render(null, null, i18nBasename, null, writer);

        // WHEN
        try {
            renderer.onRender(null, rd, rctx, null, null);
            // THEN
            assertTrue("should always get there - no Exception expected", true);
        } catch (Throwable t) {
            fail("Should never get here!");
        }
    }

    @Test(expected = RenderException.class)
    public void testOnRenderThrowsRenderExceptionOnInternalIOException() throws Exception {
        // GIVEN
        final RenderingContext rctx = mock(RenderingContext.class);
        doThrow(new IOException()).when(rctx).getAppendable();

        // WHEN
        renderer.onRender(null, null, rctx, null, null);

        // THEN
        // no then here - expect an RenderException as defined in @Test
    }

}
