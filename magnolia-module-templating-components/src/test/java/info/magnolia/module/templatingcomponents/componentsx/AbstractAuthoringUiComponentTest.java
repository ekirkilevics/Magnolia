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
package info.magnolia.module.templatingcomponents.componentsx;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.ParagraphManager;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AbstractAuthoringUiComponentTest {
    private static final String CONTENT = StringUtils.join(Arrays.asList(
            "/foo/bar@type=mgnl:content",
            "/foo/bar/MetaData@type=mgnl:metadata",
            "/foo/bar/MetaData/mgnl\\:template=testPageTemplate0",
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
            "/pouet/lol@type=mgnl:content",
            "/pouet/lol/MetaData@type=mgnl:metadata",
            "/pouet/lol/MetaData/mgnl\\:template=testPageTemplate1",
            "/no/metadata/here@type=mgnl:content",
            ""
    ), "\n");
    private MockHierarchyManager hm;

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        SystemProperty.clear();
    }

    @Before
    public void setUp() throws Exception {
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);

        final Context ctx = createMock(WebContext.class);
        expect(ctx.getLocale()).andReturn(Locale.US).anyTimes();
        replay(ctx); // no need to verify this so far
        MgnlContext.setInstance(ctx);

        final Paragraph p0 = new Paragraph();
        p0.setName("testParagraph0");
        final Paragraph p1 = new Paragraph();
        p1.setName("testParagraph1");
        p1.setI18nBasename("info.magnolia.module.templatingcomponents.test_messages");
        final ParagraphManager pman = new ParagraphManager();
        pman.addParagraphToCache(p0);
        pman.addParagraphToCache(p1);
        ComponentsTestUtil.setInstance(ParagraphManager.class, pman);

        final Template t0 = new Template();
        t0.setName("testPageTemplate0");
        final Template t1 = new Template();
        t1.setName("testPageTemplate1");
        t1.setI18nBasename("info.magnolia.module.templatingcomponents.test_messages");

        final TestableTemplateManager tman = new TestableTemplateManager();
        tman.register(t0);
        tman.register(t1);
        ComponentsTestUtil.setInstance(TemplateManager.class, tman);

        hm = MockUtil.createHierarchyManager(CONTENT);
    }

    @Test
    public void testGetsCustomMessageCustomBundleWithPageTemplate() throws Exception {
        doTestMessage("Incredibly custom Foo label", "/pouet/lol", "custom.foo.label");
    }

    @Test
    public void testDefaultMessageFromCustomBundleWithPageTemplate() throws Exception {
        // the template's i18nBasename overrides a key from the the default bundle
        doTestMessage("Customized edit button", "/pouet/lol", "buttons.edit");
    }

    @Test
    public void testGetsCustomMessageCustomBundleWithParagraph() throws Exception {
        doTestMessage("Incredibly custom Foo label", "/foo/bar/paragraphs/1", "custom.foo.label");
    }

    @Test
    public void testDefaultMessageFromCustomBundleWithParagraph() throws Exception {
        doTestMessage("Customized edit button", "/foo/bar/paragraphs/1", "buttons.edit");
    }

    @Test
    public void testUsesDefaultBundleIfNotSpecified() throws Exception {
        doTestMessage("Edit", "/foo/bar/paragraphs/0", "buttons.edit");
    }

    @Test
    public void testUsesDefaultBundleIfNotRenderableDefinition() throws Exception {
        // testParagraph2 is not known by ParagraphManager
        doTestMessage("Edit", "/foo/bar/paragraphs/2", "buttons.edit");
    }

    @Test
    public void testUsesDefaultBundleIfNoMetadata() throws Exception {
        doTestMessage("Edit", "/no/metadata/here", "buttons.edit");
    }

    private void doTestMessage(String expected, String contentPath, String key) throws RepositoryException {
        final AbstractAuthoringUiComponent compo = new DummyComponent();
        assertEquals(expected, compo.getMessage(hm.getContent(contentPath), key));
    }


    public static class TestableTemplateManager extends TemplateManager {
        private final Map<String, Template> templates = new HashMap<String, Template>();

        public void register(Template t) {
            templates.put(t.getName(), t);
        }

        @Override
        public Template getTemplateDefinition(String key) {
            return templates.get(key);
        }
    }

    private static class DummyComponent extends AbstractAuthoringUiComponent {
        public DummyComponent() {
            super(null, null);
        }

        @Override
        protected void doRender(Appendable out) throws IOException, RepositoryException {
            out.append("hello world");
        }
    }
}
