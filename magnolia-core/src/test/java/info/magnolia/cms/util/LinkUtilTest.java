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

import static org.easymock.classextension.EasyMock.createMock;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.DefaultI18NSupport;
import info.magnolia.cms.i18n.I18NSupport;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class LinkUtilTest extends MgnlTestCase {
    private static final String HTML_WITH_LINK = "this is a <a href=\"/parent/sub.html\">test</a>";

    private static final String HTML_WITH_UUIDS = "this is a <a href=\"${link:{uuid:{2},repository:{website},workspace:{default},path:{/parent/sub}}}\">test</a>";

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
        assertEquals(StringUtils.replace(HTML_WITH_LINK, "/parent/sub.html", "../parent/sub.html"), res);
    }

    public void testUUIDToAbsoluteLinksAfterRenaming() throws IOException, RepositoryException{
        setUpLinkTest();
        ((MockContent)ContentUtil.getContent(ContentRepository.WEBSITE, "/parent/sub")).setName("subRenamed");
        String res = LinkUtil.convertUUIDsToAbsoluteLinks(HTML_WITH_UUIDS);
        assertEquals(StringUtils.replace(HTML_WITH_LINK, "/sub.html", "/subRenamed.html"), res);
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

    private void setUpLinkTest() throws IOException, RepositoryException {
        String website =
            "/parent@uuid=1\n" +
            "/parent/sub@uuid=2\n" +
            "/parent/sub2@uuid=3";

        MockUtil.createAndSetHierarchyManager(ContentRepository.WEBSITE, website);

        URI2RepositoryManager uri2repo = createMock(URI2RepositoryManager.class);
        FactoryUtil.setInstance(URI2RepositoryManager.class, uri2repo);

        FactoryUtil.setInstance(I18NSupport.class, new DefaultI18NSupport());
    }

}
