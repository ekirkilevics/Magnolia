/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.templating.renderers;

import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.engine.AppendableOnlyOutputProvider;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.configured.ConfiguredRenderableDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockWebContext;

import java.io.StringWriter;
import java.util.HashMap;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletResponse;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PlainTextRendererTest {

    private static final String CONTENTNODE1 = "In a crooked little town, they were lost and never found";
    private MockWebContext webctx;

    @Before
    public void setup() {
        webctx = new MockWebContext();
        MgnlContext.setInstance(webctx);
        webctx.setResponse(new MockHttpServletResponse());
        ComponentsTestUtil.setImplementation(RenderingModel.class, RenderingModelImpl.class);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testJustWorks() throws Exception {
        final PlainTextTemplateRenderer r = new PlainTextTemplateRenderer();
        final Content content = getNode(CONTENTNODE1, "/foo/bar/MyPage");
        final StringWriter out = new StringWriter();
        AggregationState state = new AggregationState();
        webctx.setAggregationState(state);
        state.setCurrentContent(content);
        state.setMainContent(content);
        AggregationStateBasedRenderingContext ctx = new AggregationStateBasedRenderingContext(state);
        ConfiguredRenderableDefinition renderableDefinition = new ConfiguredRenderableDefinition();

        ctx.push(content.getJCRNode(), renderableDefinition, new AppendableOnlyOutputProvider(out));
        r.render(ctx, new HashMap());
        assertEquals("In a crooked little town, they were lost and never found", out.toString());
    }

    private Content getNode(String configNode, String path) throws RepositoryException {
        MockContent content = new MockContent(path);
        content.setNodeData("text", configNode);
        content.setNodeData("contentType", "mgnl:contentNode");

        return content;
    }
}
