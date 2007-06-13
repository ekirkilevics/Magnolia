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
import info.magnolia.cms.link.LinkHelper;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LinkUtilTest extends BaseLinkTest {

    private static final String HTML_WITH_LINK = "this is a <a href=\"" + HREF_SIMPLE + "\">test</a>";

    private static final String HTML_WITH_UUIDS = "this is a <a href=\"" + UUID_PATTNER_SIMPLE + "\">test</a>";

    public void testLinkToUUID() throws IOException, RepositoryException{
        setUpLinkTest();
        String res = LinkUtil.convertAbsoluteLinksToUUIDs(HTML_WITH_LINK);
        assertEquals(HTML_WITH_UUIDS, res);
    }

    public void testUUIDToAbsoluteLinks() throws IOException, RepositoryException{
        setUpLinkTest();
        String res = LinkUtil.convertUUIDsToAbsoluteLinks(HTML_WITH_UUIDS);
        assertEquals(HTML_WITH_LINK, res);
    }

    public void testUUIDToRelativeLinks() throws IOException, RepositoryException{
        setUpLinkTest();
        Content sub2 = ContentUtil.getContent(ContentRepository.WEBSITE, "/parent/sub2");
        String res = LinkUtil.convertUUIDsToRelativeLinks(HTML_WITH_UUIDS, sub2);
        assertEquals(StringUtils.replace(HTML_WITH_LINK, "/parent/sub.html", "sub.html"), res);
    }

    public void testMakeAbsolutePathFromUUID() throws IOException, RepositoryException{
        setUpLinkTest();
        String absolutePath = LinkUtil.makeAbsolutePathFromUUID("2", ContentRepository.WEBSITE);
        assertEquals("/parent/sub", absolutePath);
    }

    public void testMakeUUIDFromAbsolutePath() throws IOException, RepositoryException{
        setUpLinkTest();
        String uuid = LinkUtil.makeUUIDFromAbsolutePath("/parent/sub", ContentRepository.WEBSITE);
        assertEquals("2", uuid);
    }


    public void testInternalRelativeLinksAreProperlyDetermined() {
        assertTrue(LinkHelper.isInternalRelativeLink("foo"));
        assertTrue(LinkHelper.isInternalRelativeLink("foo/bar"));
        assertTrue(LinkHelper.isInternalRelativeLink("foo/bar.gif"));

        assertFalse(LinkHelper.isInternalRelativeLink("/foo"));
        assertFalse(LinkHelper.isInternalRelativeLink("/foo/bar"));
        assertFalse(LinkHelper.isInternalRelativeLink("/foo/bar.gif"));

        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar.gif"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar/baz.gif"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar/"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/bar"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com/"));
        assertFalse(LinkHelper.isInternalRelativeLink("http://foo.com"));
        assertFalse(LinkHelper.isInternalRelativeLink("https://foo.com"));
        assertFalse(LinkHelper.isInternalRelativeLink("https://foo.com/bar"));
        assertFalse(LinkHelper.isInternalRelativeLink("ftp://user:pass@server.com/foo/bar"));

        assertFalse(LinkHelper.isInternalRelativeLink("mailto:murdock@a-team.org"));

        assertFalse(LinkHelper.isInternalRelativeLink("#anchor"));
        assertFalse(LinkHelper.isInternalRelativeLink("#another-anchor"));

        assertFalse(LinkHelper.isInternalRelativeLink("javascript:void(window.open('http://www.google.com','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
        assertFalse(LinkHelper.isInternalRelativeLink("javascript:void(window.open('/foo/bar','','resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,fullscreen=no,dependent=no,width=200,height=200'))"));
    }

}
