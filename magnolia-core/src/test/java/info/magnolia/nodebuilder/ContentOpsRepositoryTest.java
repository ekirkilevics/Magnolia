/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.nodebuilder;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.junit.Assert.*;

/**
 * ContentOps tests that require a real repository.
 */
public class ContentOpsRepositoryTest extends RepositoryTestCase {

    private static final String TEMPLATE_NAME = "template";

    private static final String NEW_CONTENT_NAME = "content";

    private static final StrictErrorHandler ERROR_HANDLER = new StrictErrorHandler();

    @Test
    public void testCreatePage() throws RepositoryException {
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Content root = ContentUtil.asContent(session.getRootNode());
        ContentOps.createPage(NEW_CONTENT_NAME, TEMPLATE_NAME).exec(root, ERROR_HANDLER);

        assertTrue(root.hasContent(NEW_CONTENT_NAME));
        Content page = root.getContent(NEW_CONTENT_NAME);
        assertEquals(ItemType.CONTENT, page.getItemType());
        assertEquals(TEMPLATE_NAME, page.getTemplate());
    }

    @Test
    public void testCreateParagraph() throws RepositoryException {
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Content root = ContentUtil.asContent(session.getRootNode());
        ContentOps.createParagraph(NEW_CONTENT_NAME, TEMPLATE_NAME).exec(root, ERROR_HANDLER);

        assertTrue(root.hasContent(NEW_CONTENT_NAME));
        Content paragraph = root.getContent(NEW_CONTENT_NAME);
        assertEquals(ItemType.CONTENTNODE, paragraph.getItemType());
        assertEquals(TEMPLATE_NAME, paragraph.getTemplate());
    }

    @Test
    public void testSetTemplate() throws RepositoryException {
        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        Node contentNode = session.getRootNode().addNode(NEW_CONTENT_NAME, NodeTypes.Content.NAME);
        Content content = ContentUtil.asContent(contentNode);

        ContentOps.setTemplate(TEMPLATE_NAME).exec(content, ERROR_HANDLER);

        assertEquals(TEMPLATE_NAME, content.getTemplate());
    }
}
