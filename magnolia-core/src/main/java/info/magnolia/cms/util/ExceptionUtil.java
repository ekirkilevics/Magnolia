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

import org.apache.commons.lang.StringUtils;

/**
 * Util to handle exceptions.
 *
 * @version $Revision: $ ($Author: $)
 */
public class ExceptionUtil {

    /**
     * Given a RuntimeException, this method will:
     * - throw its cause exception, if the cause exception is an instance of the type of the unwrapIf parameter
     * - throw its cause exception, if the cause exception is a RuntimeException
     * - throw the given RuntimeException otherwise.
     */
    public static <E extends Throwable> void unwrapIf(RuntimeException e, Class<E> unwrapIf) throws E {
        final Throwable wrapped = e.getCause();
        if (unwrapIf != null && unwrapIf.isInstance(wrapped)) {
            throw (E) wrapped;
        } else if (wrapped != null && wrapped instanceof RuntimeException) {
            throw (RuntimeException) wrapped;
        } else {
            throw e;
        }
    }

    /**
     * This method helps palliating the absence of multi-catch (introduced in Java 7) - catch the lower common
     * denominator exception and let this method do the rest - <strong>Use with great care!</strong>.
     * <strong>Warning:</strong> this method can be abused, and would let one throw undeclared exceptions, which would
     * in turn produce unexpected and undesirable effects on calling code. Just resist the urge to use this outside
     * "multi-catch" scenarios.
     */
    @SuppressWarnings({"unchecked", "varargs"})
    public static void rethrow(Throwable e, Class<? extends Throwable>... allowedExceptions) {
        if (RuntimeException.class.isInstance(e)) {
            throw (RuntimeException) e;
        }
        for (Class<? extends Throwable> allowedException : allowedExceptions) {
            if (allowedException.isInstance(e)) {
                sneakyThrow(e);
            }
        }
        throw new Error("Caught the following exception, which was not allowed: ", e);
    }

    /**
     * Highly inspired by Lombok and the JavaPuzzlers, this method will let you throw a checked exception without declaring it.
     * Use with GREAT care.
     */
    private static void sneakyThrow(Throwable t) {
        ExceptionUtil.<RuntimeException>sneakyThrow_(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow_(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Returns true if the given exception or any of the nested cause exceptions is an instance of the <tt>suspectedCause</tt> exception argument, or a subclass thereof.
     * This is equivalent to ExceptionUtils.indexOfThrowable(e, javax.jcr.AccessDeniedException.class) >= 0, only more readable, and possibly more performant.
     */
    public static boolean wasCausedBy(Throwable e, Class<? extends Throwable> suspectedCause) {
        if (e != null && suspectedCause.isAssignableFrom(e.getClass())) {
            return true;
        } else if (e == null) {
            return false;
        } else {
            return wasCausedBy(e.getCause(), suspectedCause);
        }
    }

    /**
     * Translates an exception class name to an english-readable idiom. Example: an instance of AccessDeniedException will be returned as "Access denied".
     */
    public static String classNameToWords(Exception e) {
        return StringUtils.capitalize(StringUtils.removeEnd(e.getClass().getSimpleName(), "Exception").replaceAll("[A-Z]", " $0").toLowerCase().trim());
    }

    /**
     * Translates an exception class name to an english-readable idiom, along with the exception's message.
     * Example: an instance of AccessDeniedException("/foo/bar") will be returned as "Access denied: /foo/bar".
     */
    public static String exceptionToWords(Exception e) {
        if (e.getMessage() != null) {
            return classNameToWords(e) + ": " + e.getMessage();
        } else {
            return classNameToWords(e);
        }
    }
}
