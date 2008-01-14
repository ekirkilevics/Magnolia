/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.cache;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * A simple ServletOutputStream implementation that duplicates any output to two different output stream. Very similar
 * to TeeOutputStream from commons-io.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class MultiplexServletOutputStream extends ServletOutputStream {

    private final OutputStream stream1;

    private final OutputStream stream2;

    public MultiplexServletOutputStream(OutputStream stream1, OutputStream stream2) {
        this.stream1 = stream1;
        this.stream2 = stream2;
    }

    public void write(int value) throws IOException {
        stream1.write(value);
        stream2.write(value);
    }

    public void write(byte[] value) throws IOException {
        stream1.write(value);
        stream2.write(value);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        stream1.write(b, off, len);
        stream2.write(b, off, len);
    }

    public void flush() throws IOException {
        stream1.flush();
        stream2.flush();
    }

    public void close() throws IOException {
        try {
            stream1.close();
        }
        finally {
            stream2.close();
        }
    }
}
