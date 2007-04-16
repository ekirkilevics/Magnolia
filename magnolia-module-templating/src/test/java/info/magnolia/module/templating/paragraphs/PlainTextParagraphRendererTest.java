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
package info.magnolia.module.templating.paragraphs;

import info.magnolia.cms.core.Content;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PlainTextParagraphRendererTest extends TestCase {
    public void testJustWorks() throws IOException, RepositoryException {
        final PlainTextParagraphRenderer r = new PlainTextParagraphRenderer();
        final Content content = getNode(CONTENTNODE1, "/foo/bar/MyPage");
        final StringWriter out = new StringWriter();
        r.render(content, null, out);
        assertEquals("In a crooked little town, they were lost and never found", out.toString());
    }

    private static final String CONTENTNODE1 = "" +
            "foo.bar.MyPage.text=In a crooked little town, they were lost and never found";

    private Content getNode(String configNode, String path) throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(configNode).getContent(path);
    }
}
