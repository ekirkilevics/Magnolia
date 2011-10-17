/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.test.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessDeniedException;
import junit.framework.TestCase;


public class BinaryMockNodeDataTest extends TestCase {

    private static final byte[] BYTES = {'C', 'O', 'N', 'T', 'E', 'N', 'T'};


    /**
     * MAGNOLIA-3777: mock content: write binary attributes to the underlying binary node if existing
     */
    public void testThatAttributesAreSetOnTheWrappedNode() throws AccessDeniedException, UnsupportedOperationException, RepositoryException{
        // GIVEN a binary node data which wraps a resource node
        MockContent parent = new MockContent("parent");
        final String binaryNodeAndAttributeName = "file";
        BinaryMockNodeData binaryNodeData = new BinaryMockNodeData(parent, binaryNodeAndAttributeName);

        // WHEN setting an attribute
        binaryNodeData.setAttribute("attribute", "value");

        Content wrappedBinaryNode = parent.getContent(binaryNodeAndAttributeName);

        // THEN the value should be stored in the node
        assertEquals(wrappedBinaryNode.getNodeData("attribute").getString(), "value");
    }

    public void testThatTheBinaryContentCanBeReadMultipleTimes() throws Exception{
        MockContent parent = new MockContent("parent");
        BinaryMockNodeData binaryNodeData = new BinaryMockNodeData(parent, "file");
        binaryNodeData.setValue(new ByteArrayInputStream(BYTES));

        // THEN we can read the content twice
        byte[] streamByteArray;
        InputStream stream;
        for (int i = 0; i < 2; i++) {
            stream = binaryNodeData.getStream();
            streamByteArray = IOUtils.toByteArray(stream);
            assertTrue(ArrayUtils.isEquals(BYTES, streamByteArray));
        }
    }


    public void testThatStreamIsSetOnTheWrappedNode() throws AccessDeniedException, UnsupportedOperationException, RepositoryException, IOException{
        // GIVEN a binary node data which wraps a resource node
        MockContent parent = new MockContent("parent");
        final String binaryNodeAndAttributeName = "file";
        BinaryMockNodeData binaryNodeData = new BinaryMockNodeData(parent, binaryNodeAndAttributeName);

        // WHEN setting the stream
        binaryNodeData.setValue(new ByteArrayInputStream(BYTES));

        Content resourceNode = parent.getContent(binaryNodeAndAttributeName);

        // THEN the stream can be read directly (following the real node structure)
        assertTrue(ArrayUtils.isEquals(BYTES, IOUtils.toByteArray(resourceNode.getNodeData(ItemType.JCR_DATA).getStream())));
        // or via the node data
        assertTrue(ArrayUtils.isEquals(BYTES, IOUtils.toByteArray(binaryNodeData.getStream())));

    }


}
