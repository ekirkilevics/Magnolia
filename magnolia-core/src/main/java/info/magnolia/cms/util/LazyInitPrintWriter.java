/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.cms.util;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Writer that only opens the underlying writer when there is something needed to be written. This allows us to pass
 * around a Writer instance without actually having acquired it from the servlet response.
 */
public class LazyInitPrintWriter extends PrintWriter {

    public LazyInitPrintWriter(final ServletResponse response) {
        super(new Writer() {

            private Writer writer;

            public void write(char[] cbuf, int off, int len) throws IOException {
                getTargetWriter().write(cbuf, off, len);
            }

            public void flush() throws IOException {
                if (writer != null)
                    writer.flush();
            }

            public void close() throws IOException {
                getTargetWriter().close();
            }

            private Writer getTargetWriter() throws IOException {
                if (writer != null)
                    return writer;
                writer = response.getWriter();
                return writer;
            }
        });
    }
}
