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
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.DummyUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.engine.AppendableOnlyOutputProvider;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.configured.ConfiguredRenderableDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mockrunner.mock.web.MockHttpServletRequest;
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
        webctx.setRequest(new MockHttpServletRequest());
        webctx.setUser(new DummyUser());
        ComponentsTestUtil.setImplementation(RenderingModel.class, RenderingModelImpl.class);
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());
        ComponentsTestUtil.setInstance(SystemContext.class, webctx);
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        webctx.setLocale(new Locale("en"));
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testRender() throws Exception {
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

    @Test
    public void testRenderInPreview() throws Exception {
        final PlainTextTemplateRenderer r = new PlainTextTemplateRenderer();
        final Content content = getNode(CONTENTNODE1, "/foo/bar/MyPage");
        final StringWriter out = new StringWriter();
        AggregationState state = new AggregationState();
        state.setPreviewMode(false);
        ServerConfiguration.getInstance().setAdmin(true);
        webctx.setAggregationState(state);
        state.setCurrentContent(content);
        state.setMainContent(content);
        AggregationStateBasedRenderingContext ctx = new AggregationStateBasedRenderingContext(state);
        ConfiguredRenderableDefinition renderableDefinition = new ConfiguredRenderableDefinition();

        ctx.push(content.getJCRNode(), renderableDefinition, new AppendableOnlyOutputProvider(out));
        r.render(ctx, new HashMap());
        assertEquals("<html>\n"
                + "<body>\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"null/.resources/admin-css/admin-all.css\" />\n"
                + "\n"
                + "<script type=\"text/javascript\" src=\"null/.magnolia/pages/javascript.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"null/.magnolia/pages/messages.en.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"null/.resources/admin-js/dialogs/dialogs.js\"></script>\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"null/.resources/calendar/skins/aqua/theme.css\" title=\"Aqua\" />\n"
                + "<script type=\"text/javascript\" src=\"null/.resources/calendar/calendar.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"null/.resources/calendar/lang/calendar-en.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"null/.resources/calendar/calendar-setup.js\"></script>\n"
                + "\n"
                + "<div class=\"mgnlMainbar\" style=\"top:0px;left:0px;width:100%;\">\n"
                + "<table class=\"mgnlControlBar\"><tr><td class=\"mgnlBtnsLeft\"><span onmousedown=\"mgnlShiftPushButtonDown(this);\" "
                + "onmouseout=\"mgnlShiftPushButtonOut(this);\" onclick=\"mgnlShiftPushButtonClick(this);mgnlPreview(true);\" class=\"mgnlControlButton\" "
                + "style=\"background:transparent;\">???buttons.preview???</span><span onmousedown=\"mgnlShiftPushButtonDown(this);\" "
                + "onmouseout=\"mgnlShiftPushButtonOut(this);\" onclick=\"mgnlShiftPushButtonClick(this);MgnlAdminCentral.showTree('null','//foo/bar/MyPage');\" "
                + "class=\"mgnlControlButton\" style=\"background:transparent;\">???buttons.admincentral???</span></td></tr></table>\n"
                + "</div>\n"
                + "<h2 style=\"padding-top: 30px;\">//foo/bar/MyPage : mgnl:contentNode</h2><pre>\n"
                + "In a crooked little town, they were lost and never found</pre>\n"
                + "</body>\n"
                + "</html>\n", out.toString());
    }

    private Content getNode(String configNode, String path) throws RepositoryException {
        MockContent content = new MockContent(path);
        ((MockNode) content.getJCRNode()).setSession(new MockSession("website"));
        content.setNodeData("text", configNode);
        content.setNodeData("contentType", "mgnl:contentNode");

        return content;
    }
}
