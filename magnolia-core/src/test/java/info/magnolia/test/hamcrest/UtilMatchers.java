/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.test.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A minimal set of useful Matchers to test various objects (regular expressions, exceptions, ....
 * TODO: use hamcrest-generator to generate this class out of non-inner Matcher classes ?
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UtilMatchers {

    // TODO if we name this method "matches()", we conflict with org.mockito.Matchers.matches, which o.m.Mockito extends (i.e we have it in our test's scope since we static import Mockito.*)
    public static Matcher<String> regexMatch(final String regex) {
        return new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String item) {
                return item.matches(regex);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a string matching the ").appendText(regex).appendText(" regular expression");
            }
        };
    }

    public static <T extends Throwable> Matcher<T> isExceptionWithMessage(final Class<? extends Throwable> t, final String expectedMessage) {
        return new TypeSafeMatcher<T>() {
            @Override
            protected boolean matchesSafely(Throwable item) {
                return item.getClass().isAssignableFrom(t) && expectedMessage.equals(item.getMessage());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a " + t.getSimpleName() + " with message [" + expectedMessage + "]");
            }
        };
    }

    public static Matcher<? extends Throwable> isExceptionWithMatchingMessage(final Class<? extends Throwable> t, final String expectedMessageRegex) {
        return new TypeSafeMatcher<Throwable>() {
            @Override
            protected boolean matchesSafely(Throwable item) {
                return item.getClass().isAssignableFrom(t) && item.getMessage().matches(expectedMessageRegex);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a " + t.getSimpleName() + " with a message matching the regular expression /" + expectedMessageRegex + "/");
            }
        };
    }

}
