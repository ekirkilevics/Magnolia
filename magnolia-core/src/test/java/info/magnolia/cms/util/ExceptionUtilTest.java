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

import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.exception.MgnlException;
import info.magnolia.test.hamcrest.UtilMatchers;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.junit.Test;


import static info.magnolia.cms.util.ExceptionUtil.wasCausedBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ExceptionUtilTest {
    @Test
    public void testUnwrapIfShouldThrowWrappedExceptionIfItMatchesThe2ndParameter() {
        doTestUnwrapIf(new FileNotFoundException("HOP"), IOException.class);
    }

    @Test
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

    @Test
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


    @Test
    public void testUnwrapIfWithCauseBeingNull() {
        final RuntimeException runtimeWrapping = new RuntimeException((Throwable) null);

        try {
            ExceptionUtil.unwrapIf(runtimeWrapping, IOException.class);
            fail();
        } catch (Throwable t) {
            assertSame(runtimeWrapping, t);
            assertNull(t.getCause());
        }
    }

    @Test
    public void testUnwrapIfWithUnwrapIfBeingNull() {
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

    @Test
    public void canSneakilyRethrowGivenExceptions() {
        final IOException e = new IOException("The exception we'll throw without catching explicitly");
        try {
            someMethodThatWillCatchThrowableAndSneakilyRethrow(e, IOException.class, Error.class);
            fail("Should not be here");
        } catch (Throwable caught) {
            assertSame(e, caught);
        }
    }

    @Test
    public void willThrowGivenRuntimeEvenIfNotExplicitlyAllowed() {
        final IndexOutOfBoundsException e = new IndexOutOfBoundsException("The exception we'll throw without catching explicitly");
        try {
            someMethodThatWillCatchThrowableAndSneakilyRethrow(e, IOException.class, Error.class);
            fail("Should not be here");
        } catch (Throwable caught) {
            assertSame(e, caught);
        }
    }

    @Test
    public void whatHappensWithACheckedExceptionWeDontExplicitlyAllow() {
        final ClassNotFoundException e = new ClassNotFoundException("What happens to this exception ?");
        try {
            someMethodThatWillCatchThrowableAndSneakilyRethrow(e, IOException.class, Error.class);
            fail("Should not be here");
        } catch (Throwable caught) {
            assertThat(caught, UtilMatchers.isExceptionWithMessage(Error.class, "Caught the following exception, which was not allowed: "));
            assertSame(e, caught.getCause());
        }
    }

    @Test
    public void exampleOfAbuse() {
        try {
        ExceptionUtil.rethrow(new IOException(), IOException.class);
            fail("should have thrown an undeclared IOException");
        } catch (Throwable e) {
            if (e.getClass().equals(IOException.class)) {
            // well ok then ...
            } else {
                fail("should have thrown an undeclared IOException");
            }
        }
    }

    protected void someMethodThatWillCatchThrowableAndSneakilyRethrow(Throwable e, Class<? extends Throwable>... allowedExceptions) {
        try {
            throw e;
        } catch (Throwable t) {
            ExceptionUtil.rethrow(t, allowedExceptions);
        }
    }

    @Test
    public void translatesSimpleExceptionNameProperly() {
        assertEquals("Path not found", ExceptionUtil.classNameToWords(new PathNotFoundException()));
    }

    @Test
    public void translatesSimpleExceptionWithMessage() {
        assertEquals("Path not found: /foo/bar", ExceptionUtil.exceptionToWords(new PathNotFoundException("/foo/bar")));
    }

    @Test
    public void ignoresExceptionSuffixIfNotPresent() {
        assertEquals("Dummy problem: lol", ExceptionUtil.exceptionToWords(new DummyProblem("lol")));
    }

    @Test
    public void wasCausedByReturnsTrueIfGivenExceptionMatches() {
        assertTrue(wasCausedBy(new MgnlException(), MgnlException.class));
    }

    @Test
    public void wasCausedByReturnsTrueIfCauseExceptionMatches() {
        assertTrue(wasCausedBy(new RuntimeException(new MgnlException()), MgnlException.class));
    }

    @Test
    public void wasCausedByReturnsTrueIfDeeperExceptionMatches() {
        assertTrue(wasCausedBy(new IOException(new RuntimeException(new UnsupportedOperationException(new MgnlException()))), MgnlException.class));
    }

    @Test
    public void wasCausedByReturnsFalseIfNoCauseInGivenException() {
        assertFalse(wasCausedBy(new IOException("no cause here"), MgnlException.class));
    }

    @Test
    public void wasCausedByReturnsFalseIfNoCauseMatches() {
        assertFalse(wasCausedBy(new IOException(new RuntimeException(new UnsupportedOperationException("no cause here"))), MgnlException.class));
    }

    @Test
    public void wasCausedByReturnsTrueIfMatchIsASubClass() {
        assertTrue("preventive check - if this fails, the test has become invalid. The rest of this test class relies on the fact that Content2BeanException is a subclass of MgnlException", MgnlException.class.isAssignableFrom(Content2BeanException.class));
        assertTrue(wasCausedBy(new IOException(new Content2BeanException("this is the cause")), MgnlException.class));
    }

    @Test
    public void wasCausedByReturnsFalseIfMatchIsAParentClass() {
        assertFalse(wasCausedBy(new IOException(new MgnlException("this is the cause")), Content2BeanException.class));
    }

    private static class DummyProblem extends Exception {
        public DummyProblem(String message) {
            super(message);
        }
    }
}
