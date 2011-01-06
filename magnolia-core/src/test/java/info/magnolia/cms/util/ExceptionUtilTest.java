/**
 * This file Copyright (c) 2009-2011 Magnolia International
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

import junit.framework.TestCase;

import javax.jcr.RepositoryException;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ExceptionUtilTest extends TestCase {
    public void testUnwrapIfShouldThrowWrappedExceptionIfItMatchesThe2ndParameter() {
        doTestUnwrapIf(new FileNotFoundException("HOP"), IOException.class);
    }

    public void testUnwrapIfShouldThrowWrappedExceptionIfItIsARuntimeExceptionEvenIfItDoesntMatch2ndParameter() {
        doTestUnwrapIf(new ArrayIndexOutOfBoundsException("HELLO"), IOException.class);
    }

    private void doTestUnwrapIf(Throwable wrapped, final Class<? extends Exception> unwrapIf) {
        // wrapping the test exception - as client code would do
        final RuntimeException runtimeWrapping = new RuntimeException(wrapped);
        try {
            ExceptionUtil.unwrapIf(runtimeWrapping, unwrapIf);
            fail();
        } catch (Throwable t) {
            assertSame(wrapped, t);
            assertNull(t.getCause());
        }
    }

    public void testUnwrapIfShouldThrowPassedExceptionIfItDoesNotMatchAndIsntARuntimeException() {
        // wrapping the test exception - as client code would do
        final IOException originalException = new IOException("AIE");
        final RuntimeException runtimeWrapping = new RuntimeException(originalException);

        try {
            ExceptionUtil.unwrapIf(runtimeWrapping, RepositoryException.class);
            fail();
        } catch (Throwable t) {
            assertSame(runtimeWrapping, t);
            assertSame(originalException, t.getCause());
            assertNull(t.getCause().getCause());
        }
    }

    public void testUnwrapIfWithCauseBeeingNull() {
        final RuntimeException runtimeWrapping = new RuntimeException((Throwable)null);

        try {
            ExceptionUtil.unwrapIf(runtimeWrapping, IOException.class);
            fail();
        } catch (Throwable t) {
            assertSame(runtimeWrapping, t);
            assertNull(t.getCause());
        }
    }

    public void testUnwrapIfWithUnwrapIfBeeingNull() {
        final IOException originalException = new IOException("AIE");
        final RuntimeException runtimeWrapping = new RuntimeException(originalException);
        try {
            ExceptionUtil.unwrapIf(runtimeWrapping, null);
            fail();
        } catch (Throwable t) {
            assertSame(runtimeWrapping, t);
            assertSame(originalException, t.getCause());
        }
    }

}
