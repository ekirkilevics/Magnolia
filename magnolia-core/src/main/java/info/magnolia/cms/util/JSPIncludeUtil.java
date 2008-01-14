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
package info.magnolia.cms.util;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.resources.Resources;

import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * This (the WriterResponseWrapper inner class) was copied from the JSTL include tag. *
 *
 * We provide either a Writer or an OutputStream as requested. We actually have a true Writer and an OutputStream
 * backing both, since we don't want to use a character encoding both ways (Writer -> OutputStream -> Writer). So we use
 * no encoding at all (as none is relevant) when the target resource uses a Writer. And we decode the OutputStream's
 * bytes using OUR tag's 'charEncoding' attribute, or ISO-8859-1 as the default. We thus ignore setLocale() and
 * setContentType() in this wrapper. In other words, the target's asserted encoding is used to convert from a Writer to
 * an OutputStream, which is typically the medium through with the target will communicate its ultimate response. Since
 * we short-circuit that mechanism and read the target's characters directly if they're offered as such, we simply
 * ignore the target's encoding assertion.
 * @author philipp
 *
 * @deprecated not used. If you need to include JSPs or other servlet resources, use WebContext.include()
 * @see info.magnolia.context.WebContext#include(String, java.io.Writer)
 * @see info.magnolia.cms.taglibs.Include
 */
public final class JSPIncludeUtil {

    public static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

    /**
     * Don't instantiate.
     */
    private JSPIncludeUtil() {
        // unused
    }

    /** Wraps responses to allow us to retrieve results as Strings. */
    private static class ImportResponseWrapper extends HttpServletResponseWrapper {

        /** The Writer we convey. */
        private StringWriter sw = new StringWriter();

        /** A buffer, alternatively, to accumulate bytes. */
        protected ByteArrayOutputStream bos = new ByteArrayOutputStream();

        /** A ServletOutputStream we convey, tied to this Writer. */
        private ServletOutputStream sos = new ServletOutputStream() {

            public void write(int b) throws IOException {
                bos.write(b);
            }
        };

        /** 'True' if getWriter() was called; false otherwise. */
        private boolean isWriterUsed;

        /** 'True if getOutputStream() was called; false otherwise. */
        private boolean isStreamUsed;

        /** The HTTP status set by the target. */
        private int status = 200;

        /** Constructs a new ImportResponseWrapper. */
        public ImportResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /**
         * Returns a Writer designed to buffer the output.
         */
        public PrintWriter getWriter() {
            if (isStreamUsed) {
                throw new IllegalStateException(Resources.getMessage("IMPORT_ILLEGAL_STREAM")); //$NON-NLS-1$
            }
            isWriterUsed = true;
            return new PrintWriter(sw);
        }

        /**
         * Returns a ServletOutputStream designed to buffer the output.
         */
        public ServletOutputStream getOutputStream() {
            if (isWriterUsed) {
                throw new IllegalStateException(Resources.getMessage("IMPORT_ILLEGAL_WRITER")); //$NON-NLS-1$
            }
            isStreamUsed = true;
            return sos;
        }

        /**
         * Has no effect.
         */
        public void setContentType(String x) {
            // ignore
        }

        /** Has no effect. */
        public void setLocale(Locale x) {
            // ignore
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        /**
         * Retrieves the buffered output, using the containing tag's 'charEncoding' attribute, or the tag's default
         * encoding, <b>if necessary</b>.
         */
        // not simply toString() because we need to throw
        // UnsupportedEncodingException
        public String getString() throws UnsupportedEncodingException {
            if (isWriterUsed) {
                return sw.toString();
            }
            else if (isStreamUsed) {
                return bos.toString(DEFAULT_ENCODING);
            }
            else {
                return StringUtils.EMPTY; // target didn't write anything
            }
        }
    }

    public static String get(String jsp, HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ImportResponseWrapper wrappedResponse = new ImportResponseWrapper(response);
        request.getRequestDispatcher(jsp).include(request, wrappedResponse);
        return wrappedResponse.getString();
    }
}
