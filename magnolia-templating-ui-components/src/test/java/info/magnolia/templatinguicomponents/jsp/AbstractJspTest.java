/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.templatinguicomponents.jsp;

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
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;
import net.sourceforge.openutils.testing4web.TestServletOptions;
import org.apache.commons.lang.StringUtils;
import org.w3c.tidy.Tidy;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;

import static org.easymock.EasyMock.*;

/**
 * Subclass this and create a corresponding .jsp
 * (i.e for info.magnolia.templatinguicomponents.jsp.FooBarTest.java, create a info/magnolia/templatinguicomponents/jsp/FooBarTest.jsp)
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public abstract class AbstractJspTest extends TestCase {
    protected static final String CONTEXT = "/test-context";
    protected ServletRunner runner;
    private WebContext ctx;
    private AccessManager accessManager;
    protected MockHierarchyManager hm;

    /**
     * Test implementations can check both the response object from httpunit and the page parsed via htmlunit.
     * Fancy, I know.
     */
    abstract void check(WebResponse response, HtmlPage page) throws Exception;

    public void testDo() throws Exception {
        // servletunit/httpunit unfortunately wraps and hides the original exceptions, so we can't really nicely check if the test jsp is present 
        final String jspPath = getClass().getName().replace('.', '/') + ".jsp";
        final String jspUrl = "http://localhost" + CONTEXT + "/" + jspPath;

        final WebRequest request = new GetMethodWebRequest(jspUrl);

        // our patched version of ServletUnitClient does not wrap and hide the ServletExceptions (throws them wrapped in a RuntimeException instead)
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

        // start servletRunner
        final Hashtable<String, String> params = new Hashtable<String, String>();
        params.put("javaEncoding", "utf-8");
        params.put("development", "true");
        params.put("keepgenerated", "false");
        params.put("modificationTestInterval", "1000");
        params.put("engineOptionsClass", TestServletOptions.class.getName());
        runner = new ServletRunner(new File(path), CONTEXT);
        runner.registerServlet("*.jsp", "org.apache.jasper.servlet.JspServlet", params);

        // setup context
        hm = MockUtil.createHierarchyManager(StringUtils.join(Arrays.asList(
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
        accessManager = createMock(AccessManager.class);
        // for finer-but-not-too-verbose checks, use the contains() constraint
        expect(accessManager.isGranted(isA(String.class), anyLong())).andReturn(true).anyTimes();
        hm.setAccessManager(accessManager);

        final AggregationState aggState = new AggregationState();
        // depending on tests, we'll set the main content and current content to the same or a different node
        aggState.setMainContent(hm.getContent("/foo/bar"));
        aggState.setCurrentContent(hm.getContent("/foo/bar/paragraphs/1"));

        // let's make sure we render stuff on an author instance
        aggState.setPreviewMode(false);

        final ServerConfiguration serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);
        // register some default components used internally
        ComponentsTestUtil.setInstance(MessagesManager.class, new DefaultMessagesManager());
        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());

        ctx = createMock(WebContext.class);
        expect(ctx.getAggregationState()).andReturn(aggState).anyTimes();
        expect(ctx.getLocale()).andReturn(Locale.US).anyTimes();
        expect(ctx.getContextPath()).andReturn("/lol").anyTimes();
        expect(ctx.getServletContext()).andStubReturn(createMock(ServletContext.class));
        expect(ctx.getRequest()).andStubReturn(createMock(HttpServletRequest.class));
        MgnlContext.setInstance(ctx);

        setupExpectations(ctx, accessManager);

        replay(ctx, accessManager);
    }

    protected abstract void setupExpectations(WebContext ctx, AccessManager accessManager);

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

    @Override
    public void tearDown() throws Exception {
        verify(ctx, accessManager);
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.getProperties().clear();

        runner.shutDown();
        runner = null;
        super.tearDown();
    }
}
