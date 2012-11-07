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
package info.magnolia.freemarker;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.test.ComponentsTestUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * @version $Id$
 */
public abstract class AbstractFreemarkerTestCase {
    protected StringTemplateLoader tplLoader;
    protected FreemarkerHelper fmHelper;
    protected FreemarkerConfig fmConfig;

    @Before
    public void setUp() throws Exception {
        tplLoader = new StringTemplateLoader();
        fmConfig = new FreemarkerConfig();
        fmConfig.getTemplateLoaders().clear();
        fmConfig.addTemplateLoader(tplLoader);

        fmHelper = new FreemarkerHelper(fmConfig);
        fmHelper.getConfiguration().setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        ComponentsTestUtil.setInstance(LinkTransformerManager.class, new LinkTransformerManager());
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        // configure node2bean because its processor is injected into DefaultMessagesManager constructor
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);

        // seems useless when running tests from maven (?), so we'll shunt log4j as well
        freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);
        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    protected void assertRendereredContent(String expectedOutput, Object o, String templateName) throws TemplateException, IOException {
        assertRendereredContent(expectedOutput, Locale.US, o, templateName);
    }

    protected void assertRendereredContent(String expectedOutput, Locale l, Object o, String templateName) throws TemplateException, IOException {
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(l);
//        if (o instanceof Map) {
//            expect(context.getAttribute("msg", 1)).andReturn(null);
//        }
        replay(context);
        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(expectedOutput, o, templateName);
        verify(context);
    }

    protected void assertRendereredContentWithoutCheckingContext(String expectedOutput, Object o, String templateName) throws TemplateException, IOException {
        final StringWriter out = new StringWriter();
        LinkTransformerManager.getInstance().setMakeBrowserLinksRelative(true);
        fmHelper.render(templateName, o, out);

        assertEquals(expectedOutput, out.toString());
    }

    protected void assertRendereredContentWithSpecifiedLocale(String expectedOutput, Locale l, Object o, String templateName) throws TemplateException, IOException {
        // FreemarkerHelper currently doesn't use the SystemContext locale. Only the current context's locale, and falls back to Locale.getDefault()
        final SystemContext sysMockCtx = createStrictMock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, sysMockCtx);

        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(new Locale("es")).anyTimes();
        MgnlContext.setInstance(context);

        replay(sysMockCtx, context);

        final StringWriter out = new StringWriter();
        fmHelper.render(templateName, l, "info.magnolia.freemarker.test", o, out);

        assertEquals(expectedOutput, out.toString());
        verify(sysMockCtx, context);
    }

    // we use this method, since FreemarkerHelper adds objects to our root map (Collections.singletonMap returns an immutable map).
    protected Map<String, Object> createSingleValueMap(String key, Object value) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }
}
