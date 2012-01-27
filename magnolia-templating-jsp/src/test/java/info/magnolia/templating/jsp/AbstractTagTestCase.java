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
package info.magnolia.templating.jsp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.ContextFactory;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.assignment.MetaDataBasedTemplateDefinitionAssignment;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionProvider;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.openutils.testing4web.TestServletOptions;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.servletunit.ServletRunner;


/**
 * @version $Id$
 *
 */
public abstract class AbstractTagTestCase {

    // Global variable
    protected static final String CONTEXT = "/test-context";
    protected ServletRunner runner;
    private WebContext ctx;
    private MockHierarchyManager session;
    private HttpServletRequest req;
    private HttpServletResponse res;
    private AggregationState aggState;
    protected RenderingContext renderingContext;

    @Before
    public void setUp() throws Exception {
        // need to pass a web.xml file to setup servletunit working directory
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL webXmlUrl = classLoader.getResource("WEB-INF/web.xml");
        if (webXmlUrl == null) {
            Assert.fail("Could not find WEB-INF/web.xml");
        }
        final String path = URLDecoder.decode(webXmlUrl.getFile(), "UTF-8");

        HttpUnitOptions.setDefaultCharacterSet("utf-8");
        System.setProperty("file.encoding", "utf-8");

        // check we can write in jasper's scratch directory
        final File jspScratchDir = new File("target/jsp-test-scratch-dir");
        final String jspScratchDirAbs = jspScratchDir.getAbsolutePath();
        if (!jspScratchDir.exists()) {
            Assert.assertTrue("Can't create path " + jspScratchDirAbs + ", aborting test", jspScratchDir.mkdirs());
        }
        final File checkFile = new File(jspScratchDir, "empty");
        Assert.assertTrue("Can't write check file: " + checkFile + ", aborting test", checkFile.createNewFile());
        Assert.assertTrue("Can't remove check file:" + checkFile + ", aborting test", checkFile.delete());

        // start servletRunner
        final Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("javaEncoding", "utf-8");
        params.put("development", "true");
        params.put("keepgenerated", "false");
        params.put("modificationTestInterval", "1000");
        params.put("scratchdir", jspScratchDirAbs);
        params.put("engineOptionsClass", TestServletOptions.class.getName());
        runner = new ServletRunner(new File(path), CONTEXT);
        runner.registerServlet("*.jsp", "org.apache.jasper.servlet.JspServlet", params);

        // setup context
        session = MockUtil.createAndSetHierarchyManager("website", StringUtils.join(new String[]{
            "/foo/bar.@type=mgnl:page",
            "/foo/bar.title=Bar title",
            "/foo/bar/0.text=hello root 1",
            "/foo/bar/MetaData.@type=mgnl:metadata",
            "/foo/bar/MetaData.mgnl\\:template=testPageTemplate",
            "/foo/bar/paragraphs.@type=mgnl:areae",
            "/foo/bar/paragraphs/0.@type=mgnl:component",
            "/foo/bar/paragraphs/0.@uuid=100",
            "/foo/bar/paragraphs/1.@type=mgnl:component",
            "/foo/bar/paragraphs/1.@uuid=101",
            "/foo/bar/paragraphs/1.text=hello 1",
            "/foo/bar/paragraphs/1/MetaData.@type=mgnl:metadata",
            "/foo/bar/paragraphs/1/MetaData.mgnl\\:template=testParagraph1"}, "\n"));
        aggState = new AggregationState();
        // let's make sure we render stuff on an author instance
        aggState.setPreviewMode(false);
        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);

        ConfiguredTemplateDefinition testParagraph0 = new ConfiguredTemplateDefinition();
        testParagraph0.setName("testParagraph0");
        testParagraph0.setTitle("Test Paragraph 0");
        ConfiguredTemplateDefinition testParagraph1 = new ConfiguredTemplateDefinition();
        testParagraph1.setName("testParagraph1");
        testParagraph1.setTitle("Test Paragraph 1");
        testParagraph1.setDialog("testDialog");
        ConfiguredTemplateDefinition testParagraph2 = new ConfiguredTemplateDefinition();
        testParagraph2.setName("testParagraph2");
        testParagraph2.setTitle("Test Paragraph 2");

        final TemplateDefinitionProvider p0provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider p1provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider p2provider = mock(TemplateDefinitionProvider.class);

        when(p0provider.getTemplateDefinition()).thenReturn(testParagraph0);
        when(p0provider.getId()).thenReturn(testParagraph0.getName());
        when(p1provider.getTemplateDefinition()).thenReturn(testParagraph1);
        when(p1provider.getId()).thenReturn(testParagraph1.getName());
        when(p2provider.getTemplateDefinition()).thenReturn(testParagraph2);
        when(p2provider.getId()).thenReturn(testParagraph2.getName());

        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
        ComponentsTestUtil.setImplementation(I18nAuthoringSupport.class, DefaultI18nAuthoringSupport.class);
        ComponentsTestUtil.setImplementation(ContextFactory.class,ContextFactory.class);
        ComponentsTestUtil.setImplementation(TemplateDefinitionAssignment.class, MetaDataBasedTemplateDefinitionAssignment.class);

        MockContext systemContext = new MockContext();
        systemContext.addSession("website", session.getJcrSession());
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);

        aggState.setCurrentContent(session.getContent("/foo/bar/paragraphs/1"));
        renderingContext = new AggregationStateBasedRenderingContext(aggState);
        final RenderingEngine renderingEngine = mock(RenderingEngine.class);
        when(renderingEngine.getRenderingContext()).thenReturn(renderingContext);

        ComponentsTestUtil.setInstance(RenderingEngine.class, renderingEngine);

        final TemplateDefinitionRegistry tdr = new TemplateDefinitionRegistry(null);
        tdr.register(p0provider);
        tdr.register(p1provider);
        tdr.register(p2provider);

        ComponentsTestUtil.setInstance(TemplateDefinitionRegistry.class, tdr);

        req = mock(HttpServletRequest.class);
        req.setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);

        res = mock(HttpServletResponse.class);
        when(res.getWriter()).thenReturn(null);
        when(res.isCommitted()).thenReturn(true);

        ctx = mock(WebContext.class);
        when(ctx.getAggregationState()).thenReturn(aggState);
        when(ctx.getLocale()).thenReturn(Locale.US);
        when(ctx.getResponse()).thenReturn(res);
        when(ctx.getRequest()).thenReturn(req);
        MgnlUser mockUser = mock(MgnlUser.class);
        when(mockUser.getLanguage()).thenReturn("en");
        when(ctx.getUser()).thenReturn(mockUser);


        when(ctx.getHierarchyManager("website")).thenReturn(session);
        when(ctx.getJCRSession("website")).thenReturn(session.getJcrSession());

        setupExpectations(ctx, req);

        MgnlContext.setInstance(ctx);

    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    /**
     * Hook method - overwrite if you want to set up special expectations.
     */
    protected void setupExpectations(WebContext ctx, HttpServletRequest req) {
        // no specific expectations here
    }

    /**
     * Set the rendable definition
     */
    public void setRendableDefinition(ConfiguredTemplateDefinition renderableDefinition) throws Exception {
        renderingContext.push(aggState.getCurrentContent().getJCRNode(), renderableDefinition, new OutputProvider() {

            @Override
            public OutputStream getOutputStream() throws RenderException, IOException {
                return null;
            }

            @Override
            public Appendable getAppendable() throws RenderException, IOException {
                return new StringWriter();
            }
        });
    }

    protected MockSession getSession() {
        return (MockSession) session.getJcrSession();
    }
}
