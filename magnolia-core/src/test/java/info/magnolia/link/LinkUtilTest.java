/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.FactoryUtil;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LinkUtilTest extends BaseLinkTest {

    private static final String HTML_WITH_ABSOLUTE_LINK = "this is a <a href=\"" + HREF_ABSOLUTE_LINK + "\">test</a>";

    private static final String HTML_WITH_UUIDS = "this is a <a href=\"" + UUID_PATTERN_SIMPLE + "\">test</a>";

    public void testParsingLinks() throws IOException, RepositoryException {
        String res = LinkUtil.convertAbsoluteLinksToUUIDs(HTML_WITH_ABSOLUTE_LINK);
        assertEquals(HTML_WITH_UUIDS, res);
    }

    public void testParsingLinksShouldNotTouchNonContentAbsoluteLinks() throws IOException, RepositoryException {
        doTestParsingLinksShouldNotParse("/somthing/else.html");
    }

    public void testParsingLinksDoesNotTryToConvertExternalLinks() {
        doTestParsingLinksShouldNotParse("http://www.magnolia.info");
        doTestParsingLinksShouldNotParse("http://foo.bar.org/File%20Box/Quick%20Reference%20Guides/EMR/upgrade_brief_2007.pdf");
        doTestParsingLinksShouldNotParse("http://foo.bar.org/File Box/Quick Reference Guides/EMR/upgrade_brief_2007.pdf");
    }

    public void testParsingLinksDoesNotTryToConvertPageAnchors() {
        doTestParsingLinksShouldNotParse("#");
        doTestParsingLinksShouldNotParse("#foo");
    }

    public void testParsingLinksShouldPreserverParameters() throws IOException, RepositoryException {
        doTestParsingLinks("http://www.magnolia.info/foo?bar=baz", "http://www.magnolia.info/foo?bar=baz");
        doTestParsingLinks(UUID_PATTERN_SIMPLE + "?bar=baz", HREF_ABSOLUTE_LINK + "?bar=baz");
    }

    public void testParsingLinksShouldPreserveAnchors() throws IOException, RepositoryException {
        doTestParsingLinks(UUID_PATTERN_SIMPLE + "#bar", HREF_ABSOLUTE_LINK + "#bar");
        doTestParsingLinks("http://www.magnolia.info/foo#bar", "http://www.magnolia.info/foo#bar");
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

    public void testUUIDToAbsoluteLinks() throws IOException, RepositoryException {
        String res = LinkUtil.convertToAbsoluteLinks(HTML_WITH_UUIDS, false);
        assertEquals(HTML_WITH_ABSOLUTE_LINK, res);
    }

    public void testUUIDToInternalLinks() throws IOException, RepositoryException {
        String res = LinkUtil.convertToEditorLinks(HTML_WITH_UUIDS);
        assertEquals(HTML_WITH_ABSOLUTE_LINK, res);
    }

    public void testUUIDToRelativeLinks() throws IOException, RepositoryException {
        String res = LinkUtil.convertToRelativeLinks(HTML_WITH_UUIDS, "/parent/sub2");
        assertEquals(StringUtils.replace(HTML_WITH_ABSOLUTE_LINK, "/parent/sub.html", "sub.html"), res);
    }

    public void testUUIDToAbsoluteLinkWithDollar() throws IOException, RepositoryException {
        String htmlAbsoluteWithDollar = "this is a <a href=\"" + HREF_ABSOLUTE_LINK + "?var=${some_var}\">test</a>";       
        String htmlUuidWithDollar = "this is a <a href=\"" + UUID_PATTERN_SIMPLE + "?var=${some_var}\">test</a>";

        String res = LinkUtil.convertToAbsoluteLinks(htmlUuidWithDollar, false);
        assertEquals(htmlAbsoluteWithDollar, res);
    }

    public LinkUtil getLinkUtil(){
        return (LinkUtil) FactoryUtil.getSingleton(info.magnolia.link.LinkUtil.class);
    }

    public void testMakeUUIDFromAbsolutePath() throws IOException, RepositoryException, UUIDLinkException {
        String uuid = LinkUtil.convertAbsolutePathToUUID("/parent/sub", ContentRepository.WEBSITE);
        assertEquals("2", uuid);
    }

    public void testMakingRelativeLinks() {
        assertEquals("d.html", LinkUtil.makePathRelative("/a/b/c.html", "/a/b/d.html"));
        assertEquals("c/e.html", LinkUtil.makePathRelative("/a/b/c.html", "/a/b/c/e.html"));
        assertEquals("../x/y.html", LinkUtil.makePathRelative("/a/b/c.html", "/a/x/y.html"));
        assertEquals("../../z/x/y.html", LinkUtil.makePathRelative("/a/b/c.html", "/z/x/y.html"));
        assertEquals("../../../b.html", LinkUtil.makePathRelative("/a/b/c/d/e.html", "/a/b.html"));
        assertEquals("a/b.html", LinkUtil.makePathRelative("/a.html", "/a/b.html"));
    }

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

    public void testMakeAbsolutePathFromUUID() throws IOException, RepositoryException {
        String absolutePath = LinkUtil.convertUUIDtoHandle("2", ContentRepository.WEBSITE);
        assertEquals("/parent/sub", absolutePath);
    }
}
