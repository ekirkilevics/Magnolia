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
package info.magnolia.templating.elements;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.gui.i18n.DefaultI18nAuthoringSupport;
import info.magnolia.cms.gui.i18n.I18nAuthoringSupport;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.rendering.context.AggregationStateBasedRenderingContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.DefaultRenderingEngine;
import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.renderer.registry.RendererRegistry;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.variation.NoopVariationResolver;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Locale;

import javax.inject.Provider;
import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;


/**
 * @version $Id$
 */
public abstract class AbstractElementTestCase {

    private static final String CONTENT = StringUtils.join(Arrays.asList(
            "/foo/bar.@type=mgnl:content",
            "/foo/bar/MetaData.@type=mgnl:metadata",
            "/foo/bar/MetaData.mgnl\\:template=testPageTemplate0",
            "/foo/bar/paragraphs.@type=mgnl:contentNode",
            "/foo/bar/paragraphs/0.@type=mgnl:contentNode",
            "/foo/bar/paragraphs/0.text=hello 0",
            "/foo/bar/paragraphs/0/MetaData.@type=mgnl:metadata",
            "/foo/bar/paragraphs/0/MetaData.mgnl\\:template=testParagraph0",
            "/foo/bar/paragraphs/1.@type=mgnl:contentNode",
            "/foo/bar/paragraphs/1.text=hello 1",
            "/foo/bar/paragraphs/1/MetaData.@type=mgnl:metadata",
            "/foo/bar/paragraphs/1/MetaData.mgnl\\:template=testParagraph1",
            "/foo/bar/paragraphs/2.@type=mgnl:contentNode",
            "/foo/bar/paragraphs/2.text=hello 2",
            "/foo/bar/paragraphs/2/MetaData.@type=mgnl:metadata",
            "/foo/bar/paragraphs/2/MetaData.mgnl\\:template=testParagraph2",
            "/foo/bar/paragraphs/stage.text=stage",
            "/pouet/lol.@type=mgnl:content",
            "/pouet/lol/MetaData.@type=mgnl:metadata",
            "/pouet/lol/MetaData.mgnl\\:template=testPageTemplate1",
            "/no/metadata/here.@type=mgnl:content", ""), "\n");

    private MockSession session;

    private Node mainNode;

    private Node currentNode;

    private Node componentNode;

    private AggregationState aggregationState;

    private ConfiguredTemplateDefinition templateDefinition;

    public StringWriter out;

    private ServerConfiguration serverCfg;

    private AggregationStateBasedRenderingContext context;

    private DefaultRenderingEngine engine;

    private TemplateDefinitionAssignment templateDefinitionAssignment;

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Before
    public void setUp() throws Exception {
        // Create Session
        session = SessionTestUtil.createSession("website", CONTENT);

        // Init Aggregation State
        aggregationState = new AggregationState();
        mainNode = session.getNode("/foo/bar");
        currentNode = session.getNode("/foo/bar/paragraphs");
        componentNode = session.getNode("/foo/bar/paragraphs/0");
        aggregationState.setMainContent(ContentUtil.asContent(mainNode));
        aggregationState.setCurrentContent(ContentUtil.asContent(currentNode));

        // Init WebContext
        MockWebContext webContext = new MockWebContext();
        webContext.addSession("website", session);
        User user = mock(User.class);
        webContext.setUser(user);
        Locale localeEn = new Locale("en");
        webContext.setLocale(localeEn);
        MgnlContext.setInstance(webContext);

        ComponentsTestUtil.setImplementation(SystemContext.class, MockContext.class);
        MockContext sysCtx = (MockContext)MgnlContext.getSystemContext();
        sysCtx.addSession("website", session);
        ComponentsTestUtil.setInstance(SystemContext.class, sysCtx);

        // Init ServerConfiguration
        serverCfg = new ServerConfiguration();
        serverCfg.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverCfg);

        // Init I18nSupport
        I18nContentSupport defSupport = mock(I18nContentSupport.class);
        when(defSupport.getLocale()).thenReturn(localeEn);
        ComponentsTestUtil.setInstance(I18nContentSupport.class, defSupport);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setInstance(I18nAuthoringSupport.class, new DefaultI18nAuthoringSupport());
        RenderingEngine renderingEngine = mock(RenderingEngine.class);
        ComponentsTestUtil.setInstance(RenderingEngine.class, renderingEngine);
        // configure node2bean because its processor is injected into DefaultMessagesManager constructor
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);

        // Set TemplateDefinition
        final TemplateDefinitionAssignment templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        templateDefinition = new ConfiguredTemplateDefinition();
        templateDefinition.setName("testComponent");
        templateDefinition.setTitle("Title");
        templateDefinition.setI18nBasename("info.magnolia.templating.test_messages");
        templateDefinition.setDialog("dialog");
        templateDefinition.setRenderType("renderType");
        templateDefinition.setDescription("Description");
        when(templateDefinitionAssignment.getAssignedTemplateDefinition(componentNode)).thenReturn(templateDefinition);

        RendererRegistry registry = mock(RendererRegistry.class);
        Renderer renderer = mock(Renderer.class);
        when(registry.getRenderer("renderType")).thenReturn(renderer);

        final AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState, null);
        out = new StringWriter();
        context.push(currentNode, templateDefinition, new OutputProvider() {

            @Override
            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            @Override
            public Appendable getAppendable() throws IOException {
                return out;
            }
        });

        engine = new DefaultRenderingEngine(registry, templateDefinitionAssignment, new NoopVariationResolver(), new Provider<RenderingContext>() {

            @Override
            public RenderingContext get() {
                return context;
            }
        });
        this.context = context;
        this.templateDefinitionAssignment = templateDefinitionAssignment;

    }

    protected MockSession getSession() {
        return session;
    }

    public Node getComponentNode() {
        return componentNode;
    }

    public ConfiguredTemplateDefinition getTemplateDefinition() {
        return templateDefinition;
    }

    public StringWriter getOut() {
        return out;
    }

    public ServerConfiguration getServerCfg() {
        return serverCfg;
    }

    public AggregationStateBasedRenderingContext getContext() {
        return context;
    }

    public DefaultRenderingEngine getEngine() {
        return engine;
    }

    public TemplateDefinitionAssignment getTemplateDefinitionAssignment() {
        return templateDefinitionAssignment;
    }

    public AggregationState getAggregationState() {
        return aggregationState;
    }

}
