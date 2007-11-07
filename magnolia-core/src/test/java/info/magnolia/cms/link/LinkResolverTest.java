/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.BaseLinkTest;
import info.magnolia.cms.util.ContentUtil;

import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision$ ($Author$)
 */
public class LinkResolverTest extends BaseLinkTest {

    private static final String HTML_WITH_ABSOLUTE_LINK = "this is a <a href=\"" + HREF_ABSOLUTE_LINK + "\">test</a>";

    private static final String HTML_WITH_UUIDS = "this is a <a href=\"" + UUID_PATTNER_SIMPLE + "\">test</a>";

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
        doTestParsingLinks(UUID_PATTNER_SIMPLE + "?bar=baz", HREF_ABSOLUTE_LINK + "?bar=baz");
    }

    public void testParsingLinksShouldPreserveAnchors() throws IOException, RepositoryException {
        doTestParsingLinks(UUID_PATTNER_SIMPLE + "#bar", HREF_ABSOLUTE_LINK + "#bar");
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

    public LinkResolver getLinkResolver(){
        return LinkResolver.Factory.getInstance();
    }
}
