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
package info.magnolia.module.templatingcomponents.components;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.util.SessionTestUtil;
import info.magnolia.templating.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.templating.template.registry.TemplateDefinitionProvider;
import info.magnolia.templating.template.registry.TemplateDefinitionRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;

/**
 * @version $Id$
 */
public abstract class AbstractComponentTestCase {
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
            "/pouet/lol.@type=mgnl:content",
            "/pouet/lol/MetaData.@type=mgnl:metadata",
            "/pouet/lol/MetaData.mgnl\\:template=testPageTemplate1",
            "/no/metadata/here.@type=mgnl:content", ""), "\n");
    private MockSession session;

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }

    @Before
    public void setUp() throws Exception {
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);

        final Context ctx = mock(WebContext.class);
        when(ctx.getLocale()).thenReturn(Locale.US);

        MgnlContext.setInstance(ctx);
        final ConfiguredTemplateDefinition p0 = new ConfiguredTemplateDefinition();
        p0.setName("testParagraph0");
        final ConfiguredTemplateDefinition p1 = new ConfiguredTemplateDefinition();
        p1.setName("testParagraph1");
        p1.setI18nBasename("info.magnolia.module.templatingcomponents.test_messages");

        final TemplateDefinitionProvider p0provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider p1provider = mock(TemplateDefinitionProvider.class);

        when(p0provider.getTemplateDefinition()).thenReturn(p0);
        when(p1provider.getTemplateDefinition()).thenReturn(p1);

        final TemplateDefinitionRegistry pman = new TemplateDefinitionRegistry();
        pman.registerTemplateDefinition(p0.getName(), p0provider);
        pman.registerTemplateDefinition(p1.getName(), p1provider);

        ComponentsTestUtil.setInstance(TemplateDefinitionRegistry.class, pman);

        final ConfiguredTemplateDefinition t0 = new ConfiguredTemplateDefinition();
        t0.setName("testPageTemplate0");
        final ConfiguredTemplateDefinition t1 = new ConfiguredTemplateDefinition();
        t1.setName("testPageTemplate1");
        t1.setI18nBasename("info.magnolia.module.templatingcomponents.test_messages");

        final TemplateDefinitionProvider t0provider = mock(TemplateDefinitionProvider.class);
        final TemplateDefinitionProvider t1provider = mock(TemplateDefinitionProvider.class);

        when(t0provider.getTemplateDefinition()).thenReturn(t0);
        when(t1provider.getTemplateDefinition()).thenReturn(t1);

        pman.registerTemplateDefinition(t0.getName(), t0provider);
        pman.registerTemplateDefinition(t1.getName(), t1provider);

        session = SessionTestUtil.createSession(CONTENT);
    }

    protected MockSession getHM() {
        return session;
    }
}
