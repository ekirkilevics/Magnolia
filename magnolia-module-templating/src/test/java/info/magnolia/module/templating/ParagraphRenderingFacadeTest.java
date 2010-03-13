/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.templating;

import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.io.StringWriter;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.PageContext;

import static org.easymock.classextension.EasyMock.*;


/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphRenderingFacadeTest extends MgnlTestCase {

    protected void setUp() throws Exception {
        MgnlContext.setInstance(null);
        super.setUp();
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testRenderCallsTheAppropriateRenderer() throws Exception {
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(getNode(CONFIGNODE1_RENDERER, "/modules/test/paragraph-renderers"));
        final ParagraphRenderingFacade prf = new ParagraphRenderingFacade(prm, null);

        final Paragraph tra = new Paragraph();
        tra.setName("para-one");
        tra.setType("foo");
        final StringWriter res = new StringWriter();
        prf.render(null, tra, res);
        assertEquals("tralala:para-one", res.toString());

        final Paragraph tru = new Paragraph();
        tru.setName("para-two");
        tru.setType("bar");
        final StringWriter res2 = new StringWriter();
        prf.render(null, tru, res2);
        assertEquals("trululu:para-two", res2.toString());
    }

    public void testSetsJspPageContext() throws Exception {
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        prm.onRegister(getNode(CONFIGNODE1_RENDERER, "/modules/test/paragraph-renderers"));
        final ParagraphRenderingFacade prf = new ParagraphRenderingFacade(prm, null);

        final Paragraph tra = new Paragraph();
        tra.setName("para-one");
        tra.setType("foo");
        final StringWriter res = new StringWriter();

        PageContext pageContext = createMock(PageContext.class);
        WebContext webContext = createStrictMock(WebContext.class);

        webContext.setPageContext(pageContext);

        replay(pageContext, webContext);
        MgnlContext.setInstance(webContext);
        prf.render(null, tra, res, pageContext);
        verify(pageContext, webContext);

        assertEquals("tralala:para-one", res.toString());
    }

    public void testUsesTheAppropriateParagraphWhenNotExplicitelyPassed() throws Exception {
        final ParagraphRendererManager prm = new ParagraphRendererManager();
        final ParagraphManager pm = new ParagraphManager();
        final ParagraphRenderingFacade prf = new ParagraphRenderingFacade(prm, pm);

        prm.onRegister(getNode(CONFIGNODE1_RENDERER, "/modules/test/paragraph-renderers"));
        final Content content = getNode(CONTENTNODE1, "/foo/bar/MyPage");

        try {
            prf.render(content, new StringWriter());
            fail("should have failed");
        }
        catch (IllegalStateException e) {
            assertEquals("Paragraph para1 not found for page /foo/bar/MyPage", e.getMessage());
        }

        // now register the paragraphs
        pm.addParagraphToCache(getNode(CONFIG_PARAGRAPH1, "modules/test/paragraph/para1"));
        pm.addParagraphToCache(getNode(CONFIG_PARAGRAPH2, "modules/test/paragraph/para2"));
        final StringWriter out = new StringWriter();
        prf.render(content, out);
        assertEquals("tralala:para1", out.toString());
    }

    private static final String CONFIGNODE1_RENDERER = ""
        + "modules.test.paragraph-renderers.foo.@type=mgnl:contentNode\n"
        + "modules.test.paragraph-renderers.foo.name=foo\n"
        + "modules.test.paragraph-renderers.foo.class=info.magnolia.module.templating.ParagraphRendererManagerTest$DummyParagraphRenderer\n"
        + "modules.test.paragraph-renderers.bar.@type=mgnl:contentNode\n"
        + "modules.test.paragraph-renderers.bar.name=bar\n"
        + "modules.test.paragraph-renderers.bar.class=info.magnolia.module.templating.ParagraphRendererManagerTest$OtherDummyParagraphRenderer";

    private static final String CONFIG_PARAGRAPH1 = ""
        + "modules.test.paragraph.para1.@type=mgnl:contentNode\n"
        + "modules.test.paragraph.para1.name=para1\n"
        + "modules.test.paragraph.para1.type=foo";

    private static final String CONFIG_PARAGRAPH2 = ""
        + "modules.test.paragraph.para2.@type=mgnl:contentNode\n"
        + "modules.test.paragraph.para2.name=para2\n"
        + "modules.test.paragraph.para2.type=foo";

    private static final String CONTENTNODE1 = ""
        + "foo.bar.MyPage.text=hello\n"
        + "foo.bar.MyPage.MetaData.mgnl\\:template=para1";

    private Content getNode(String configNode, String path) throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(configNode).getContent(path);
    }
}
