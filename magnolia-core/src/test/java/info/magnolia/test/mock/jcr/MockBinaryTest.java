/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.test.mock.jcr;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Tests.
 */
public class MockBinaryTest {

    private static final String TEST_CONTENT = "Hi there";

    @Test
    public void testConstructionFromByteArray() throws Exception {
        // GIVEN

        // WHEN
        final MockBinary binary = new MockBinary(TEST_CONTENT.getBytes("UTF-8"));

        // THEN
        final InputStream resultingStream = binary.getStream();

        byte[] buffer = new byte[resultingStream.available()];
        resultingStream.read(buffer);
        resultingStream.close();

        assertEquals(TEST_CONTENT, new String(buffer));
    }

    @Test
    public void testConstructionFromInputStream() throws Exception {
        // GIVEN
        final ByteArrayInputStream stream = new ByteArrayInputStream(TEST_CONTENT.getBytes("UTF-8"));

        // WHEN
        final MockBinary binary = new MockBinary(stream);

        // THEN
        final InputStream resultingStream = binary.getStream();

        byte[] buffer = new byte[resultingStream.available()];
        resultingStream.read(buffer);

        assertEquals(TEST_CONTENT, new String(buffer));
        resultingStream.close();
    }

    @Test
    public void testGetSize() throws Exception {
        // GIVEN
        final MockBinary binary = new MockBinary(TEST_CONTENT.getBytes("UTF-8"));

        // WHEN
        final long size = binary.getSize();

        // THEN
        assertEquals(8l, size);
    }

    @Test
    public void testDisposeResetsToEmptyBuffer() throws Exception {
        // GIVEN
        final MockBinary binary = new MockBinary(TEST_CONTENT.getBytes("UTF-8"));

        // WHEN
        binary.dispose();

        // THEN
        assertEquals(0l, binary.getSize());
    }

    @Test
    public void testReadFullBuffer() throws Exception {
        // GIVEN
        final MockBinary binary = new MockBinary(TEST_CONTENT.getBytes("UTF-8"));
        final byte[] extract = new byte[8];

        // WHEN
        binary.read(extract, 0);

        // THEN
        assertEquals(TEST_CONTENT, new String(extract));
    }

    @Test
    public void testReadPartsOfBuffer() throws Exception {
        // GIVEN
        final MockBinary binary = new MockBinary(TEST_CONTENT.getBytes("UTF-8"));
        final byte[] extract = new byte[3];

        // WHEN
        final int result = binary.read(extract, 3);

        // THEN
        assertEquals("the", new String(extract));
        assertEquals(result, 3);
    }

    @Test
    public void testReadWithNonExistingStartPosition() throws Exception {
        // GIVEN
        final MockBinary binary = new MockBinary(TEST_CONTENT.getBytes("UTF-8"));
        final byte[] extract = new byte[4];

        // WHEN
        final int result = binary.read(extract, 10);

        // THEN
        assertEquals(new String(new byte[4]), new String(extract));
        assertEquals(-1, result);
    }
}