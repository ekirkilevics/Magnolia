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
package info.magnolia.cms.link;

import info.magnolia.cms.util.BaseLinkTest;

import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public class LinkResolverTest extends BaseLinkTest {

    private static final String HTML_WITH_ABSOLUTE_LINK = "this is a <a href=\"" + HREF_ABSOLUTE_LINK + "\">test</a>";

    private static final String HTML_WITH_UUIDS = "this is a <a href=\"" + UUID_PATTERN_SIMPLE + "\">test</a>";

    public void testParsingLinks() throws IOException, RepositoryException {
        String res = getLinkResolver().parseLinks(HTML_WITH_ABSOLUTE_LINK);
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
        final String res = getLinkResolver().parseLinks(originalHtml);
        assertEquals(expectedHtml, res);
    }

    public void testUUIDToAbsoluteLinks() throws IOException, RepositoryException {
        String res = getLinkResolver().convertToAbsoluteLinks(HTML_WITH_UUIDS, false);
        assertEquals(HTML_WITH_ABSOLUTE_LINK, res);
    }

    public void testUUIDToInternalLinks() throws IOException, RepositoryException {
        String res = getLinkResolver().convertToEditorLinks(HTML_WITH_UUIDS);
        assertEquals(HTML_WITH_ABSOLUTE_LINK, res);
    }

    public void testUUIDToRelativeLinks() throws IOException, RepositoryException {
        String res = getLinkResolver().convertToRelativeLinks(HTML_WITH_UUIDS, "/parent/sub2");
        assertEquals(StringUtils.replace(HTML_WITH_ABSOLUTE_LINK, "/parent/sub.html", "sub.html"), res);
    }

    public void testUUIDToAbsoluteLinkWithDollar() throws IOException, RepositoryException {
        String htmlAbsoluteWithDollar = "this is a <a href=\"" + HREF_ABSOLUTE_LINK + "?var=${some_var}\">test</a>";       
        String htmlUuidWithDollar = "this is a <a href=\"" + UUID_PATTERN_SIMPLE + "?var=${some_var}\">test</a>";

        String res = getLinkResolver().convertToAbsoluteLinks(htmlUuidWithDollar, false);
        assertEquals(htmlAbsoluteWithDollar, res);
    }

    public LinkResolver getLinkResolver(){
        return LinkResolver.Factory.getInstance();
    }
}
