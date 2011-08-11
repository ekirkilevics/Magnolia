/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.templating.test;

import static org.easymock.classextension.EasyMock.*;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerConfig;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.module.templating.ParagraphRenderer;
import info.magnolia.module.templating.ParagraphRendererManager;
import info.magnolia.module.templating.RenderException;
import info.magnolia.module.templating.RenderingModel;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.module.templating.TemplateRenderer;
import info.magnolia.module.templating.TemplateRendererManager;
import info.magnolia.module.templating.engine.DefaultRenderingEngine;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.module.templating.paragraphs.FreemarkerParagraphRenderer;
import info.magnolia.module.templating.renderers.FreemarkerTemplateRenderer;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.IAnswer;


/**
 * This abstract test can be extended to write tests in which a complete rendering process has to be
 * executed. The class sets everything up for freemarker rendering.
 */
public abstract class AbstractRenderTestCase extends MgnlTestCase {

    protected static final String TEST_CONTEXT = "/test-context";

    protected static final String TEST_BASE_URL = "http://testdomain.com:1234" + TEST_CONTEXT;

    protected static final String FREEMARKER_TYPE = "freemarker";

    protected static final byte[] DUMMY_BYTES = new byte[]{'D', 'U', 'M', 'M', 'Y'};

    /**
     * Maps which contain the actual definitions and renderers. The mocked managers will use those entries.
     */
    private Map<String, Paragraph> paragraphs;
    private Map<String, Template> templates;
    private Map<String, ParagraphRenderer> paragraphRenderers;
    private Map<String, TemplateRenderer> templateRenderers;

    private MockWebContext mockWebContext;

    private ServerConfiguration serverConfiguration;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        paragraphs = new HashMap<String, Paragraph>();
        templates = new HashMap<String, Template>();
        paragraphRenderers = new HashMap<String, ParagraphRenderer>();
        templateRenderers = new HashMap<String, TemplateRenderer>();

        serverConfiguration = new ServerConfiguration();
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);

        ServletContext servletContext = createFreemarkerFriendlyServletContext();

        // the mock context is setup by MgnlTestCase
        mockWebContext = (MockWebContext) MgnlContext.getWebContext();
        mockWebContext.setServletContext(servletContext);
        mockWebContext.setRequest(createNiceMock(HttpServletRequest.class));
        mockWebContext.setResponse(createNiceMock(HttpServletResponse.class));

        // exceptions are thrown to let tests fail
        FreemarkerConfig freemarkerConfig = new FreemarkerConfig();
        freemarkerConfig.setTemplateExceptionHandler(new TemplateExceptionHandler() {

            @Override
            public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
                // we have to throw RuntimeException as the renderers swallow TemplateExceptions
                throw new RuntimeException(te);
            }
        });
        ComponentsTestUtil.setInstance(FreemarkerConfig.class, freemarkerConfig);

        setUpManagers();

        setRenderingEngine(new DefaultRenderingEngine());

        // some basic default components involved in the 'standard' rendering
        setI18NContentSupport(new DefaultI18nContentSupport());
        setLinkTransformerManager(new LinkTransformerManager());
        setURI2RepositoryManager(new URI2RepositoryManager());

        // register the freemarker renderers
        registerParagraphRenderer(FREEMARKER_TYPE, new FreemarkerParagraphRenderer());
        registerTemplateRenderer(FREEMARKER_TYPE, new FreemarkerTemplateRenderer());

        // some valid default values
        setDefaultBaseUrl(TEST_BASE_URL);
        setContextPath(TEST_CONTEXT);
        setLocale(Locale.ENGLISH);
        setPublicInstance();

        MockUtil.createAndSetHierarchyManager(ContentRepository.WEBSITE);
    }

    /**
     * Get the current {@link MockWebContext} which can get manipulated.
     */
    protected MockWebContext getWebContext() {
        return mockWebContext;
    }

    protected Paragraph registerParagraph(String name, Paragraph paragraph) {
        return paragraphs.put(name, paragraph);
    }

    protected Paragraph registerParagraph(String name, String templatePath, String type, Class<? extends RenderingModel<?>> modelClass) {
        Paragraph paragraph = new Paragraph();
        paragraph.setName(name);
        paragraph.setTemplatePath(templatePath);
        paragraph.setType(type);
        if (modelClass != null) {
            paragraph.setModelClass(modelClass);
        }
        registerParagraph(name, paragraph);
        return paragraph;
    }

    protected void registerParagraphRenderer(String name, ParagraphRenderer paragraphRenderer) {
        paragraphRenderers.put(name, paragraphRenderer);
    }

    protected Template registerTemplate(String name, String templatePath, String type, Class<? extends RenderingModel<?>> modelClass) {
        Template template = new Template();
        template.setName(name);
        template.setTemplatePath(templatePath);
        template.setType(type);
        if (modelClass != null) {
            template.setModelClass(modelClass);
        }
        registerTemplate(name, template);
        return template;
    }

    protected Template registerTemplate(String name, Template template) {
        return templates.put(name, template);
    }

    protected void registerTemplateRenderer(String name, TemplateRenderer templateRenderer) {
        templateRenderers.put(name, templateRenderer);
    }

    protected String render(Content content) throws RenderException {
        RenderingEngine renderingEngine = Components.getSingleton(RenderingEngine.class);
        StringWriter out = new StringWriter();
        renderingEngine.render(content, out);
        return out.toString();
    }

    protected void setAuthorInstance() {
        serverConfiguration.setAdmin(true);
    }

    protected void setContextPath(String contextPath) {
        getWebContext().setContextPath(contextPath);
    }

    protected void setDefaultBaseUrl(String baseUrl) {
        serverConfiguration.setDefaultBaseUrl(baseUrl);
    }

    protected void setEditMode(boolean editMode) {
        MgnlContext.getAggregationState().setPreviewMode(!editMode);
    }

    protected void setI18NContentSupport(I18nContentSupport i18nContentSupport) {
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);
    }

    protected void setLinkTransformerManager(LinkTransformerManager linkTransformerManager) {
        ComponentsTestUtil.setInstance(LinkTransformerManager.class, linkTransformerManager);
    }

    protected void setLocale(Locale locale) {
        getWebContext().setLocale(locale);
    }

    protected void setPublicInstance() {
        serverConfiguration.setAdmin(false);
    }

    protected void setRenderingEngine(RenderingEngine renderingEngine) {
        ComponentsTestUtil.setInstance(RenderingEngine.class, renderingEngine);
    }

    protected void setURI2RepositoryManager(URI2RepositoryManager uri2repositoryManager) {
        ComponentsTestUtil.setInstance(URI2RepositoryManager.class, uri2repositoryManager);
    }

    /**
     * Freemarker will lookup the tlds using the servlet context. Freemarker only considers
     * libraries in the WEB-INF/lib folder and doesn't use the classpath. This is why we fake the
     * existence of this jars.
     */
    private ServletContext createFreemarkerFriendlyServletContext() throws MalformedURLException {
        ServletContext servletContext = createNiceMock(ServletContext.class);

        // return all jars on the classpath instead of the empty WEB-INF/lib folder
        URLClassLoader urlClassLoader = (URLClassLoader) getClass().getClassLoader();
        Set<String> pathes = new HashSet<String>();
        URL[] urls = urlClassLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            pathes.add(url.toExternalForm());
        }
        expect(servletContext.getResourcePaths("/WEB-INF/lib")).andStubReturn(pathes);

        // ignored the web.xml file
        expect(servletContext.getResourceAsStream((String) anyObject())).andStubAnswer(new IAnswer<InputStream>() {

            @Override
            public InputStream answer() throws Throwable {
                String path = (String) getCurrentArguments()[0];
                if (path.equals("/WEB-INF/web.xml")) {
                    return null;
                }
                return new URL(path).openStream();
            }
        });

        // when freemarker wants to access the jars
        expect(servletContext.getResource((String) anyObject())).andStubAnswer(new IAnswer<URL>() {

            @Override
            public URL answer() throws Throwable {
                String path = (String) getCurrentArguments()[0];
                return new URL(path);
            }
        });
        replay(servletContext);
        return servletContext;
    }

    private void setUpManagers() {
        // for renderers and definitions we maintain maps. the managers are mocked.
        ParagraphRendererManager paragraphRendererManager = createMock(ParagraphRendererManager.class);
        expect(paragraphRendererManager.getRenderer((String) anyObject())).andStubAnswer(new IAnswer<ParagraphRenderer>() {

            @Override
            public ParagraphRenderer answer() throws Throwable {
                return paragraphRenderers.get(getCurrentArguments()[0]);
            }
        });
        replay(paragraphRendererManager);
        ComponentsTestUtil.setInstance(ParagraphRendererManager.class, paragraphRendererManager);

        TemplateRendererManager templateRendererManager = createMock(TemplateRendererManager.class);
        expect(templateRendererManager.getRenderer((String) anyObject())).andStubAnswer(new IAnswer<TemplateRenderer>() {

            @Override
            public TemplateRenderer answer() throws Throwable {
                return templateRenderers.get(getCurrentArguments()[0]);
            }
        });
        replay(templateRendererManager);
        ComponentsTestUtil.setInstance(TemplateRendererManager.class, templateRendererManager);

        ParagraphManager paragraphManager = createMock(ParagraphManager.class);
        expect(paragraphManager.getParagraphDefinition((String) anyObject())).andStubAnswer(new IAnswer<Paragraph>() {

            @Override
            public Paragraph answer() throws Throwable {
                return paragraphs.get(getCurrentArguments()[0]);
            }
        });
        replay(paragraphManager);
        ComponentsTestUtil.setInstance(ParagraphManager.class, paragraphManager);

        TemplateManager templateManager = createMock(TemplateManager.class);
        expect(templateManager.getTemplateDefinition((String) anyObject())).andStubAnswer(new IAnswer<Template>() {

            @Override
            public Template answer() throws Throwable {
                return templates.get(getCurrentArguments()[0]);
            }
        });
        replay(templateManager);
        ComponentsTestUtil.setInstance(TemplateManager.class, templateManager);
    }

    protected String render(String website, String path) throws RenderException {
        return render(ContentUtil.getContent(ContentRepository.WEBSITE, path));
    }

}
