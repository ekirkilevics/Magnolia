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
