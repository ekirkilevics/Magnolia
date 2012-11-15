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

import static org.junit.Assert.*;
import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.test.mock.MockContent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;


public class ContentOpsTest {

    private static final String NODEDATA_VALUE = "value";

    private static final String NODEDATA_NAME = "nodedata";

    private static final String TEMPLATE_NAME = "template";

    private static final String ROOT_NAME = "root";

    private static final String NEW_CONTENT_NAME = "content";

    private static final StrictErrorHandler ERROR_HANDLER = new StrictErrorHandler();

    /**
     * Test method for
     * {@link info.magnolia.nodebuilder.ContentOps#createContent(java.lang.String, info.magnolia.cms.core.ItemType)}.
     */
    @Test
    public void testCreateContent() throws RepositoryException {
        MockContent root = new MockContent(ROOT_NAME);
        ContentOps.createContent(NEW_CONTENT_NAME, ItemType.CONTENTNODE).exec(root, ERROR_HANDLER);
        assertTrue(root.hasContent(NEW_CONTENT_NAME));
        assertEquals(ItemType.CONTENTNODE, root.getContent(NEW_CONTENT_NAME).getItemType());
    }

    /**
     * Test method for
     * {@link info.magnolia.nodebuilder.ContentOps#createPage(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCreatePage() throws RepositoryException {
        MockContent root = new MockContent(ROOT_NAME);
        ContentOps.createPage(NEW_CONTENT_NAME, TEMPLATE_NAME).exec(root, ERROR_HANDLER);

        assertTrue(root.hasContent(NEW_CONTENT_NAME));
        Content page = root.getContent(NEW_CONTENT_NAME);
        assertEquals(ItemType.CONTENT, page.getItemType());
        assertEquals(TEMPLATE_NAME, page.getTemplate());
    }

    /**
     * Test method for
     * {@link info.magnolia.nodebuilder.ContentOps#createCollectionNode(java.lang.String)}.
     */
    @Test
    public void testCreateCollectionNode() throws RepositoryException {
        MockContent root = new MockContent(ROOT_NAME);
        ContentOps.createCollectionNode(NEW_CONTENT_NAME).exec(root, ERROR_HANDLER);

        assertTrue(root.hasContent(NEW_CONTENT_NAME));
        assertEquals(ItemType.CONTENTNODE, root.getContent(NEW_CONTENT_NAME).getItemType());
    }

    /**
     * Test method for
     * {@link info.magnolia.nodebuilder.ContentOps#createParagraph(java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public void testCreateParagraph() throws RepositoryException {
        MockContent root = new MockContent(ROOT_NAME);
        ContentOps.createParagraph(NEW_CONTENT_NAME, TEMPLATE_NAME).exec(root, ERROR_HANDLER);

        assertTrue(root.hasContent(NEW_CONTENT_NAME));
        Content paragraph = root.getContent(NEW_CONTENT_NAME);
        assertEquals(ItemType.CONTENTNODE, paragraph.getItemType());
        assertEquals(TEMPLATE_NAME, paragraph.getTemplate());
    }

    /**
     * Test method for
     * {@link info.magnolia.nodebuilder.ContentOps#setNodeData(java.lang.String, java.lang.Object)}.
     */
    public void testSetNodeData() {
        MockContent content = new MockContent(NEW_CONTENT_NAME);
        ContentOps.setNodeData(NODEDATA_NAME, NODEDATA_VALUE).exec(content, ERROR_HANDLER);

        assertEquals(NODEDATA_VALUE, content.getNodeData(NODEDATA_NAME).getString());
    }

    /**
     * Test method for
     * {@link info.magnolia.nodebuilder.ContentOps#setBinaryNodeData(java.lang.String, java.lang.String, long, java.io.InputStream)}
     * .
     */
    @Test
    public void testSetBinaryNodeData() throws IOException {
        MockContent content = new MockContent(NEW_CONTENT_NAME);
        byte[] bytes = {'C', 'O', 'N', 'T', 'E', 'N', 'T'};

        ContentOps.setBinaryNodeData(NODEDATA_NAME, "test.jpg", bytes.length, new ByteArrayInputStream(bytes)).exec(content, ERROR_HANDLER);

        NodeData nodeData = content.getNodeData(NODEDATA_NAME);

        assertEquals("test", nodeData.getAttribute(FileProperties.PROPERTY_FILENAME));
        assertEquals("jpg", nodeData.getAttribute(FileProperties.PROPERTY_EXTENSION));
        assertEquals(String.valueOf(bytes.length), nodeData.getAttribute(FileProperties.PROPERTY_SIZE));
        InputStream stream = nodeData.getStream();
        assertEquals(new String(bytes), IOUtils.toString(stream));
    }

    /**
     * Test method for {@link info.magnolia.nodebuilder.ContentOps#setTemplate(java.lang.String)}.
     */
    @Test
    public void testSetTemplate() {
        MockContent content = new MockContent(NEW_CONTENT_NAME);
        ContentOps.setTemplate(TEMPLATE_NAME).exec(content, ERROR_HANDLER);

        assertEquals(TEMPLATE_NAME, content.getTemplate());
    }
}
