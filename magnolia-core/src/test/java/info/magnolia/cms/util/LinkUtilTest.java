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
package info.magnolia.cms.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LinkUtilTest extends BaseLinkTest {

    private static final String HTML_WITH_LINK = "this is a <a href=\"" + HREF_SIMPLE + "\">test</a>";

    private static final String HTML_WITH_UUIDS = "this is a <a href=\"" + UUID_PATTNER_SIMPLE + "\">test</a>";

    public void testLinkToUUID() throws IOException, RepositoryException {
        String res = LinkUtil.convertAbsoluteLinksToUUIDs(HTML_WITH_LINK);
        assertEquals(HTML_WITH_UUIDS, res);
    }

    public void testConvertAbsoluteLinksToUUIDsDoesNotTryToConvertExternalLinks() {
        doTestConvertAbsoluteLinksToUUIDsShouldNotConvert("http://www.magnolia.info");
        doTestConvertAbsoluteLinksToUUIDsShouldNotConvert("http://foo.bar.org/File%20Box/Quick%20Reference%20Guides/EMR/upgrade_brief_2007.pdf");
        doTestConvertAbsoluteLinksToUUIDsShouldNotConvert("http://foo.bar.org/File Box/Quick Reference Guides/EMR/upgrade_brief_2007.pdf");
    }

    public void testConvertAbsoluteLinksToUUIDsDoesNotTryToConvertPageAnchors() {
        doTestConvertAbsoluteLinksToUUIDsShouldNotConvert("#");
        doTestConvertAbsoluteLinksToUUIDsShouldNotConvert("#foo");
    }

    public void testConvertLinksToUUIDShouldPreserverParameters() throws IOException, RepositoryException {
        doTestConvertAbsoluteLinksToUUIDs("http://www.magnolia.info/foo?bar=baz", "http://www.magnolia.info/foo?bar=baz");
        doTestConvertAbsoluteLinksToUUIDs(UUID_PATTNER_SIMPLE + "?bar=baz", HREF_SIMPLE + "?bar=baz");
    }

    public void testConvertLinksToUUIDShouldPreserveAnchors() throws IOException, RepositoryException {
        doTestConvertAbsoluteLinksToUUIDs(UUID_PATTNER_SIMPLE + "#bar", HREF_SIMPLE + "#bar");
        doTestConvertAbsoluteLinksToUUIDs("http://www.magnolia.info/foo#bar", "http://www.magnolia.info/foo#bar");
    }

    private void doTestConvertAbsoluteLinksToUUIDsShouldNotConvert(String href) {
        doTestConvertAbsoluteLinksToUUIDs(href, href);
    }

    private void doTestConvertAbsoluteLinksToUUIDs(String expectedHref, String originalHref) {
        final String originalHtml = "this is a <a href=\"" + originalHref + "\">test</a>, yo.";
        final String expectedHtml = "this is a <a href=\"" + expectedHref + "\">test</a>, yo.";
        final String res = LinkUtil.convertAbsoluteLinksToUUIDs(originalHtml);
        assertEquals(expectedHtml, res);
    }

    public void testUUIDToAbsoluteLinks() throws IOException, RepositoryException {
        String res = LinkUtil.convertUUIDsToAbsoluteLinks(HTML_WITH_UUIDS);
        assertEquals(HTML_WITH_LINK, res);
    }

    public void testUUIDToRelativeLinks() throws IOException, RepositoryException {
        Content sub2 = ContentUtil.getContent(ContentRepository.WEBSITE, "/parent/sub2");
        String res = LinkUtil.convertUUIDsToRelativeLinks(HTML_WITH_UUIDS, sub2);
        assertEquals(StringUtils.replace(HTML_WITH_LINK, "/parent/sub.html", "sub.html"), res);
    }

    public void testMakeAbsolutePathFromUUID() throws IOException, RepositoryException {
        String absolutePath = LinkUtil.makeAbsolutePathFromUUID("2", ContentRepository.WEBSITE);
        assertEquals("/parent/sub", absolutePath);
    }

    public void testMakeUUIDFromAbsolutePath() throws IOException, RepositoryException {
        String uuid = LinkUtil.makeUUIDFromAbsolutePath("/parent/sub", ContentRepository.WEBSITE);
        assertEquals("2", uuid);
    }
}
