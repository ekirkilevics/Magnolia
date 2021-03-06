/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.module.templatingcomponents.jspx;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.LocaleDefinition;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.module.templating.engine.DefaultRenderingEngine;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.module.templatingcomponents.componentsx.AbstractAuthoringUiComponentTest;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.sourceforge.openutils.testing4web.TestServletOptions;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.tidy.Tidy;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;

/**
 * Subclass this and create a corresponding .jsp (i.e for info.magnolia.module.templatingcomponents.jsp.FooBarTest.java,
 * create a info/magnolia/templatingcomponents/jsp/FooBarTest.jsp)
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractJspTest {
    protected static final String CONTEXT = "/test-context";
    protected ServletRunner runner;
    private WebContext ctx;
    protected MockHierarchyManager websiteHM;
    private HttpServletRequest req;

    /**
     * Test implementations can check both the response object from httpunit and the page parsed via htmlunit.
     * Fancy, I know.
     */
    abstract void check(WebResponse response, HtmlPage page) throws Exception;

    @Test
    public void testDo() throws Exception {
        final String jspPath = getClass().getName().replace('.', '/') + ".jsp";
        final String jspUrl = "http://localhost" + CONTEXT + "/" + jspPath;

        final WebRequest request = new GetMethodWebRequest(jspUrl);

        final WebResponse response = runner.getResponse(request);

        final String responseStr = response.getText();
        // preliminary sanity check
        assertFalse(responseStr.contains("<ui:"));
        assertFalse(responseStr.contains("<cms:"));
        assertFalse(responseStr.contains("<cmsu:"));

        // now switch to HtmlUnit
        final StringWebResponse res = new StringWebResponse(responseStr, new URL(jspUrl));
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
        final HtmlPage page = HTMLParser.parseHtml(res, webClient.getCurrentWindow());

        check(response, page);
    }

    @Before
    public void setUp() throws Exception {
        // this was mainly copied from displaytag's org.displaytag.test.DisplaytagCase

        // need to pass a web.xml file to setup servletunit working directory
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL webXmlUrl = classLoader.getResource("WEB-INF/web.xml");
        if (webXmlUrl == null) {
            fail("Could not find WEB-INF/web.xml");
        }
        final String path = URLDecoder.decode(webXmlUrl.getFile(), "UTF-8");

        HttpUnitOptions.setDefaultCharacterSet("utf-8");
        System.setProperty("file.encoding", "utf-8");

        // check we can write in jasper's scratch directory
        final File jspScratchDir = new File("target/jsp-test-scratch-dir");
        final String jspScratchDirAbs = jspScratchDir.getAbsolutePath();
        if (!jspScratchDir.exists()) {
            assertTrue("Can't create path " + jspScratchDirAbs + ", aborting test", jspScratchDir.mkdirs());
        }
        final File checkFile = new File(jspScratchDir, "empty");
        assertTrue("Can't write check file: " + checkFile + ", aborting test", checkFile.createNewFile());
        assertTrue("Can't remove check file:" + checkFile + ", aborting test", checkFile.delete());

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
        websiteHM = MockUtil.createHierarchyManager(StringUtils.join(Arrays.asList(
                "/foo/bar@type=mgnl:content",
                "/foo/bar/MetaData@type=mgnl:metadata",
                "/foo/bar/MetaData/mgnl\\:template=testPageTemplate",
                "/foo/bar/paragraphs@type=mgnl:contentNode",
                "/foo/bar/paragraphs/0@type=mgnl:contentNode",
                "/foo/bar/paragraphs/0/text=hello 0",
                "/foo/bar/paragraphs/0/MetaData@type=mgnl:metadata",
                "/foo/bar/paragraphs/0/MetaData/mgnl\\:template=testParagraph0",
                "/foo/bar/paragraphs/1@type=mgnl:contentNode",
                "/foo/bar/paragraphs/1/text=hello 1",
                "/foo/bar/paragraphs/1/MetaData@type=mgnl:metadata",
                "/foo/bar/paragraphs/1/MetaData/mgnl\\:template=testParagraph1",
                "/foo/bar/paragraphs/2@type=mgnl:contentNode",
                "/foo/bar/paragraphs/2/text=hello 2",
                "/foo/bar/paragraphs/2/MetaData@type=mgnl:metadata",
                "/foo/bar/paragraphs/2/MetaData/mgnl\\:template=testParagraph2",
                ""
        ), "\n"));

        final AggregationState aggState = new AggregationState();
        setupAggregationState(aggState);

        // let's make sure we render stuff on an author instance
        aggState.setPreviewMode(false);

        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);

        // register some default components used internally
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        final DefaultI18nContentSupport i18nContentSupport = new DefaultI18nContentSupport();
        i18nContentSupport.setEnabled(true);
        i18nContentSupport.addLocale(LocaleDefinition.make("fr", "CH", true));
        i18nContentSupport.addLocale(LocaleDefinition.make("de", "CH", true));
        i18nContentSupport.addLocale(LocaleDefinition.make("de", null, true));
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);
        final DefaultI18nAuthoringSupport i18nAuthoringSupport = new DefaultI18nAuthoringSupport();
        i18nAuthoringSupport.setEnabled(true);
        // TODO - tests with i18AuthoringSupport disabled/enabled
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, i18nAuthoringSupport);

        final Template t1 = new Template();
        t1.setName("testPageTemplate");
        t1.setI18nBasename("info.magnolia.module.templatingcomponents.test_messages");
        final AbstractAuthoringUiComponentTest.TestableTemplateManager tman = new AbstractAuthoringUiComponentTest.TestableTemplateManager();
        tman.register(t1);
        ComponentsTestUtil.setInstance(TemplateManager.class, tman);
        ComponentsTestUtil.setInstance(RenderingEngine.class, new DefaultRenderingEngine());

        req = createMock(HttpServletRequest.class);
        // cheating - this mock request is NOT the same that's used by htmlunit to do the ACTUAL request
        expect(req.getAttribute(Sources.REQUEST_LINKS_DRAWN)).andReturn(Boolean.FALSE).anyTimes();//times(0, 1);
        req.setAttribute(Sources.REQUEST_LINKS_DRAWN, Boolean.TRUE);
        expectLastCall().anyTimes();//times(0, 1);

        ctx = createMock(WebContext.class);
        expect(ctx.getAggregationState()).andReturn(aggState).anyTimes();
        expect(ctx.getLocale()).andReturn(Locale.US).anyTimes();
        expect(ctx.getContextPath()).andReturn("/lol").anyTimes();
        expect(ctx.getServletContext()).andStubReturn(createMock(ServletContext.class));
        expect(ctx.getRequest()).andStubReturn(req);
        MgnlContext.setInstance(ctx);

        setupExpectations(ctx, websiteHM, req);

        replay(ctx, req);
    }

    // depending on tests, we'll set the main content and current content to the same or a different node

    protected abstract void setupAggregationState(AggregationState aggState) throws RepositoryException;

    protected abstract void setupExpectations(WebContext ctx, MockHierarchyManager hm, HttpServletRequest req);

    @After
    public void tearDown() throws Exception {
        verify(ctx, req);
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();

        runner.shutDown();
        runner = null;
    }

    protected void prettyPrint(WebResponse response, OutputStream out) throws IOException {
        final Tidy tidy = new Tidy();
        tidy.setXHTML(false);
        tidy.setDropEmptyParas(false);
        tidy.setDropFontTags(false);
        tidy.setWrapAttVals(false);
        tidy.setWraplen(0);
        tidy.setSmartIndent(false);
        tidy.setIndentAttributes(false);
        tidy.setIndentContent(true);
        tidy.setSpaces(2);
        tidy.setTabsize(8);
        // tidy.setQuiet(!printWarnings);

        tidy.parse(response.getInputStream(), out);
    }
}
