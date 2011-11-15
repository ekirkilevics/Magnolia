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
import static org.mockito.Mockito.mock;
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
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for EditMarker.
 *
 * @version $Id$
 */
public class EditElementTest {

    private EditElement marker;
    private StringWriter out;

    @Before
    public void setUp() throws Exception {
        final MockHierarchyManager hm = MockUtil.createHierarchyManager("TestMockHierarchyManager",
                "/foo/bar/baz/paragraphs/01.text=dummy\n" +
                "/foo/bar/baz/paragraphs/01/MetaData.mgnl\\:template=testParagraph0"
        );

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(hm.getContent("/foo/bar/baz"));
        aggregationState.setCurrentContent(hm.getContent("/foo/bar/baz/paragraphs/01"));
        final WebContext ctx = mock(WebContext.class);
        MgnlContext.setInstance(ctx);

        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());

        RenderingContext renderingCtx = new AggregationStateBasedRenderingContext(aggregationState);
        ConfiguredTemplateDefinition renderableDefinition = new ConfiguredTemplateDefinition();

        out = new StringWriter();

        renderingCtx.push(aggregationState.getCurrentContent().getJCRNode(), renderableDefinition, new OutputProvider() {

            @Override
            public OutputStream getOutputStream() throws RenderException, IOException {
                return null;
            }

            @Override
            public Appendable getAppendable() throws RenderException, IOException {
                return out;
            }
        });

        marker = new EditElement(serverCfg, renderingCtx);
    }

    @Test
    public void testDoRender() throws Exception {

        ConfiguredTemplateDefinition testParagraph0 = (ConfiguredTemplateDefinition) marker.getRenderingContext().getRenderableDefinition();

        testParagraph0.setId("testParagraph0");
        testParagraph0.setDialog("testDialog");

        testParagraph0.setTitle("Test Paragraph 0");

        marker.begin(out);

        assertEquals("<cms:edit content=\"TestMockHierarchyManager:/foo/bar/baz/paragraphs/01\" format=\"bar\" dialog=\"testDialog\" template=\"testParagraph0\"></cms:edit>\n", out.toString());

        // now with format & dialog
        marker.setFormat("testFormat");
        marker.setDialog("testDialog");

        out = new StringWriter();
        marker.begin(out);

        assertEquals("<cms:edit content=\"TestMockHierarchyManager:/foo/bar/baz/paragraphs/01\" format=\"testFormat\" dialog=\"testDialog\" template=\"testParagraph0\"></cms:edit>\n", out.toString());

    }

    //@Test(expected=RenderException.class)
    public void testDoRenderMissingDialogDef() throws Exception {

        ConfiguredTemplateDefinition testParagraph0 = (ConfiguredTemplateDefinition) marker.getRenderingContext().getRenderableDefinition();

        testParagraph0.setId("testParagraph0");
        testParagraph0.setName("testParagraph0Name");

        testParagraph0.setTitle("Test Paragraph 0");

        marker.begin(out);
    }

    @Test
    public void testPostRender() throws Exception {
        marker.end(out);
        assertEquals("", out.toString());
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        SystemProperty.clear();
        ComponentsTestUtil.clear();
    }
}
