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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;

/**
 * Implementation of Binary for mocking purposes.
 */
public class MockBinary implements Binary {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private byte[] buffer = EMPTY_BYTE_ARRAY;

    public MockBinary(InputStream in) throws IOException {
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();
    }

    public MockBinary(byte[] buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must be non-null");
        }
        this.buffer = buffer;
    }

    @Override
    public InputStream getStream() throws RepositoryException {
        return new ByteArrayInputStream(buffer);
    }

    @Override
    public int read(byte[] b, long position) throws IOException, RepositoryException {
        // this instance is backed by an in-memory buffer
        int length = Math.min(b.length, buffer.length - (int) position);
        if (length > 0) {
            System.arraycopy(buffer, (int) position, b, 0, length);
            return length;
        } else {
            return -1;
        }
    }

    @Override
    public long getSize() {
        return buffer.length;
    }

    @Override
    public void dispose() {
        buffer = EMPTY_BYTE_ARRAY;
    }
}