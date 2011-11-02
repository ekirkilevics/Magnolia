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
package info.magnolia.link;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class LinkUtilTest extends BaseLinkTest {

    private static final String HTML_WITH_ABSOLUTE_LINK = "this is a <a href=\"" + HREF_ABSOLUTE_LINK + "\">test</a>";

    private static final String HTML_WITH_UUIDS = "this is a <a href=\"" + UUID_PATTERN_SIMPLE + "\">test</a>";

    private static final String HTML_WITH_ABSOLUTE_LINK_AND_CONTEXT_PATH = "this is a <a href=\"" + SOME_CONTEXT + HREF_ABSOLUTE_LINK + "\">test</a>";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        replay(allMocks.toArray());
    }

    @Override
    @After
    public void tearDown() throws Exception {
        verify(allMocks.toArray());
        super.tearDown();
    }

    @Test
    public void testParsingLinks() {
        String res = LinkUtil.convertAbsoluteLinksToUUIDs(HTML_WITH_ABSOLUTE_LINK);
        assertEquals(HTML_WITH_UUIDS, res);
    }

    @Test
    public void testParsingLinksWithBackslashInQueryParam() {
        String res = LinkUtil.convertAbsoluteLinksToUUIDs("look <a href=\"/parent/sub.html?p4if_p=\\File%20Box\\Quick%20Reference%20Guides\\Strategy%20Management\\WIT\">here</a> for results");
        assertEquals("look <a href=\"${link:{uuid:{2},repository:{website},handle:{/parent/sub},nodeData:{},extension:{html}}}?p4if_p=\\File%20Box\\Quick%20Reference%20Guides\\Strategy%20Management\\WIT\">here</a> for results", res);
    }

    @Test
    public void testParsingLinksShouldNotTouchNonContentAbsoluteLinks() {
        doTestParsingLinksShouldNotParse("/somthing/else.html");
    }

    @Test
    public void testParsingLinksDoesNotTryToConvertExternalLinks() {
        doTestParsingLinksShouldNotParse("http://www.magnolia-cms.com");
        doTestParsingLinksShouldNotParse("http://foo.bar.org/File%20Box/Quick%20Reference%20Guides/EMR/upgrade_brief_2007.pdf");
        doTestParsingLinksShouldNotParse("http://foo.bar.org/File Box/Quick Reference Guides/EMR/upgrade_brief_2007.pdf");
    }

    @Test
    public void testParsingLinksDoesNotTryToConvertPageAnchors() {
        doTestParsingLinksShouldNotParse("#");
        doTestParsingLinksShouldNotParse("#foo");
    }

    @Test
    public void testParsingLinksShouldPreserverParameters() {
        doTestParsingLinks("http://www.magnolia-cms.com/foo?bar=baz", "http://www.magnolia-cms.com/foo?bar=baz");
        doTestParsingLinks(UUID_PATTERN_SIMPLE + "?bar=baz", HREF_ABSOLUTE_LINK + "?bar=baz");
    }

    @Test
    public void testParsingLinksShouldPreserveAnchors() {
        doTestParsingLinks(UUID_PATTERN_SIMPLE + "#bar", HREF_ABSOLUTE_LINK + "#bar");
        doTestParsingLinks("http://www.magnolia-cms.com/foo#bar", "http://www.magnolia-cms.com/foo#bar");
    }

    private void doTestParsingLinksShouldNotParse(String href) {
        doTestParsingLinks(href, href);
    }

    private void doTestParsingLinks(String expectedHref, String originalHref) {
        final String originalHtml = "this is a <a href=\"" + originalHref + "\">test</a>, yo.";
        final String expectedHtml = "this is a <a href=\"" + expectedHref + "\">test</a>, yo.";
        final String res = LinkUtil.convertAbsoluteLinksToUUIDs(originalHtml);
        assertEquals(expectedHtml, res);
    }

    @Test
    public void testUUIDToAbsoluteLinks() throws LinkException {
        String res = LinkUtil.convertLinksFromUUIDPattern(HTML_WITH_UUIDS, LinkTransformerManager.getInstance().getAbsolute(false));
        assertEquals(HTML_WITH_ABSOLUTE_LINK, res);
    }

    @Test
    public void testUUIDToInternalLinks() throws LinkException {
        String res = LinkUtil.convertLinksFromUUIDPattern(HTML_WITH_UUIDS, LinkTransformerManager.getInstance().getEditorLink());
        assertEquals(HTML_WITH_ABSOLUTE_LINK_AND_CONTEXT_PATH, res);
    }

    @Test
    public void testUUIDToRootLinks() throws LinkException {
        String res = LinkUtil.convertLinksFromUUIDPattern("<p>Large article pages have a <a href=\"${link:{uuid:{2a98b29f-b514-4949-9cb3-e1162171a2ca},repository:{website},handle:{/features/special-templates},nodeData:{},extension:{html}}}\">Table Of Contents</a> (<a href=\"${link:{uuid:{},repository:{website},handle:{/},nodeData:{},extension:{html}}}\">TOC</a>) navigation.</p>", LinkTransformerManager.getInstance().getEditorLink());
        assertEquals("<p>Large article pages have a <a href=\"/some-context/features/special-templates.html\">Table Of Contents</a> (<a href=\"/some-context/\">TOC</a>) navigation.</p>", res);
    }

    @Test
    public void testUUIDToRelativeLinks() throws LinkException {
        String res = LinkUtil.convertLinksFromUUIDPattern(HTML_WITH_UUIDS, LinkTransformerManager.getInstance().getRelative("/parent/sub2"));
        assertEquals(StringUtils.replace(HTML_WITH_ABSOLUTE_LINK, "/parent/sub.html", "sub.html"), res);
    }

    @Test
    public void testUUIDToAbsoluteLinkWithDollar() throws LinkException {
        String htmlAbsoluteWithDollar = "this is a <a href=\"" + HREF_ABSOLUTE_LINK + "?var=${some_var}\">test</a>";
        String htmlUuidWithDollar = "this is a <a href=\"" + UUID_PATTERN_SIMPLE + "?var=${some_var}\">test</a>";

        String res = LinkUtil.convertLinksFromUUIDPattern(htmlUuidWithDollar, LinkTransformerManager.getInstance().getAbsolute(false));
        assertEquals(htmlAbsoluteWithDollar, res);
    }

    private LinkUtil getLinkUtil(){
        return Components.getSingleton(LinkUtil.class);
    }

    @Test
    public void testMakeUUIDFromAbsolutePath() throws LinkException {
        String uuid = LinkFactory.parseLink("/parent/sub").getUUID();
        assertEquals("2", uuid);
    }

    @Test
    public void testMakingRelativeLinks() {
        assertEquals("d.html", LinkUtil.makePathRelative("/a/b/c.html", "/a/b/d.html"));
        assertEquals("c/e.html", LinkUtil.makePathRelative("/a/b/c.html", "/a/b/c/e.html"));
        assertEquals("../x/y.html", LinkUtil.makePathRelative("/a/b/c.html", "/a/x/y.html"));
        assertEquals("../../z/x/y.html", LinkUtil.makePathRelative("/a/b/c.html", "/z/x/y.html"));
        assertEquals("../../../b.html", LinkUtil.makePathRelative("/a/b/c/d/e.html", "/a/b.html"));
        assertEquals("a/b.html", LinkUtil.makePathRelative("/a.html", "/a/b.html"));
        assertEquals(".", LinkUtil.makePathRelative("/a.html", "/"));
        assertEquals(".", LinkUtil.makePathRelative("/b/a.html", "/b/"));
        assertEquals("../", LinkUtil.makePathRelative("/c/b/a.html", "/c/"));
    }

    @Test
    public void testInternalRelativeLinksAreProperlyDetermined() {
        assertTrue(LinkUtil.isInternalRelativeLink("foo"));
        assertTrue(LinkUtil.isInternalRelativeLink("foo/bar"));
        assertTrue(LinkUtil.isInternalRelativeLink("foo/bar.gif"));

        assertFalse(LinkUtil.isInternalRelativeLink("/foo"));
        assertFalse(LinkUtil.isInternalRelativeLink("/foo/bar"));
        assertFalse(LinkUtil.isInternalRelativeLink("/foo/bar.gif"));

        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar.gif"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar/baz.gif"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar/"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/bar"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com/"));
        assertFalse(LinkUtil.isInternalRelativeLink("http://foo.com"));
        assertFalse(LinkUtil.isInternalRelativeLink("https://foo.com"));
        assertFalse(LinkUtil.isInternalRelativeLink("https://foo.com/bar"));
        assertFalse(LinkUtil.isInternalRelativeLink("ftp://user:pass@server.com/foo/bar"));

        assertFalse(LinkUtil.isInternalRelativeLink("mailto:murdock@a-team.org"));

        assertFalse(LinkUtil.isInternalRelativeLink("#anchor"));
        assertFalse(LinkUtil.isInternalRelativeLink("#another-anchor"));

        assertFalse(LinkUtil.isInternalRelativeLink("javascript:void(window.open('http://www.google.com','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
        assertFalse(LinkUtil.isInternalRelativeLink("javascript:void(window.open('/foo/bar','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
    }

    @Test
    public void testExternalLinksAreProperlyDetermined() {
        assertFalse(LinkUtil.isExternalLinkOrAnchor("foo"));
        assertFalse(LinkUtil.isExternalLinkOrAnchor("foo/bar"));
        assertFalse(LinkUtil.isExternalLinkOrAnchor("foo/bar.gif"));

        assertFalse(LinkUtil.isExternalLinkOrAnchor("/foo"));
        assertFalse(LinkUtil.isExternalLinkOrAnchor("/foo/bar"));
        assertFalse(LinkUtil.isExternalLinkOrAnchor("/foo/bar.gif"));

        assertTrue(LinkUtil.isExternalLinkOrAnchor("http://foo.com/bar.gif"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("http://foo.com/bar/baz.gif"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("http://foo.com/bar/"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("http://foo.com/bar"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("http://foo.com/"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("http://foo.com"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("https://foo.com"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("https://foo.com/bar"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("ftp://user:pass@server.com/foo/bar"));

        assertTrue(LinkUtil.isExternalLinkOrAnchor("mailto:murdock@a-team.org"));

        assertTrue(LinkUtil.isExternalLinkOrAnchor("#anchor"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("#another-anchor"));

        assertTrue(LinkUtil.isExternalLinkOrAnchor("javascript:void(window.open('http://www.google.com','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
        assertTrue(LinkUtil.isExternalLinkOrAnchor("javascript:void(window.open('/foo/bar','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
    }

    @Test
    public void testMakeAbsolutePathFromUUID() throws LinkException {
        String absolutePath = LinkFactory.createLink(RepositoryConstants.WEBSITE, "2").getHandle();
        assertEquals("/parent/sub", absolutePath);
    }

    @Test
    public void testMakeCompleteURL() throws Exception {
        ServerConfiguration serverConfiguration = Components.getSingleton(ServerConfiguration.class);
        String base = serverConfiguration.getDefaultBaseUrl();
        serverConfiguration.setDefaultBaseUrl("http://some.site/yay/");
        String url = null;
        try {
            MockSession session = new MockSession("website");
            MockContent c = new MockContent((MockNode) session.getRootNode());
            url = LinkTransformerManager.getInstance().getCompleteUrl().transform(LinkFactory.createLink(c));
        } finally {
            // restore
            serverConfiguration.setDefaultBaseUrl(base);
        }
        assertNotNull(url);
        assertEquals(-1, StringUtils.substringAfter(url, "http://").indexOf("//"));
    }

    @Test
    public void testCreateUndefinedLinkIfUUIDIsNonExistentOrFallbackHandleIsEmpty() {
        try {
            String link = LinkUtil.convertLinksFromUUIDPattern("<p>Large article pages have a <a href=\"${link:{uuid:{00060890-0220-4544-b6a4-320325dcfd86},repository:{website},handle:{},nodeData:{},extension:{html}}}\">Table Of Contents</a></p>", LinkTransformerManager.getInstance().getEditorLink());
            assertEquals("<p>Large article pages have a <a href=\"/some-context/\">Table Of Contents</a></p>", link);
        } catch (LinkException e) {
            fail("Got unexpected exception "+ e.getMessage());
        }

    }

}
