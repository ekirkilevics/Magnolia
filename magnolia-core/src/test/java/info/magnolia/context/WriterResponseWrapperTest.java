/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.context;

import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

/**
 * @version $Id$
 */
public class WriterResponseWrapperTest {

    @Test
    public void testCantUseWriterAfterOutputStream() throws IOException {
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final StringWriter out = new StringWriter();
        final WriterResponseWrapper wrw = new WriterResponseWrapper(response, out);
        final ServletOutputStream os = wrw.getOutputStream();
        try {
            wrw.getWriter();
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("According to the ServletResponse javadoc, either getWriter or getOutputStream may be called to write the body, not both.", e.getMessage());
        }
        os.print("boo");
        assertEquals("boo", out.toString());
    }

    @Test
    public void testCantUseOutputStreamAfterWriter() {
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final StringWriter out = new StringWriter();
        final WriterResponseWrapper wrw = new WriterResponseWrapper(response, out);
        final PrintWriter w = wrw.getWriter();
        try {
            wrw.getOutputStream();
            fail("should have failed");
        } catch (IllegalStateException e) {
            assertEquals("According to the ServletResponse javadoc, either getWriter or getOutputStream may be called to write the body, not both.", e.getMessage());
        }
        w.print("boo");
        assertEquals("boo", out.toString());
    }
}
