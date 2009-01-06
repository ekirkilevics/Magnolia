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
