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
package info.magnolia.context;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * Wraps an HttpServletResponse and redirects both outputs (getWriter() and getOutputStream())
 * to the given Writer. (As per the ServletResponse javadoc, only one of those methods can be used)
 *
 * This might cause encoding issues, so use carefully. (not sure it makes any sense to allow the
 * outputStream usage in our case, but oh well, ...)
 * See the original JSPIncludeUtil for more discussion about encoding.
 */
class WriterResponseWrapper extends HttpServletResponseWrapper {
    private final Writer out;
    private final PrintWriter pw;
    private boolean writerWasUsed = false;
    private boolean streamWasUsed = false;
    private final ServletOutputStream sos = new ServletOutputStream() {
        public void write(int b) throws IOException {
            out.write(b);
        }
    };
    private int status = 200;

    WriterResponseWrapper(HttpServletResponse response, Writer out) {
        super(response);
        this.out = out;
        this.pw = new PrintWriter(out);
    }

    public PrintWriter getWriter() {
        writerWasUsed = checkOutputState(streamWasUsed);
        return pw;
    }

    public ServletOutputStream getOutputStream() {
        streamWasUsed = checkOutputState(writerWasUsed);
        return sos;
    }

    private boolean checkOutputState(boolean forbidden) {
        if (forbidden) {
            throw new IllegalStateException("According to the ServletResponse javadoc, either getWriter or getOutputStream may be called to write the body, not both.");
        }
        return true;
    }

    public void setContentType(String x) {
        // ignore
    }

    public void setLocale(Locale x) {
        // ignore
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
