/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.jcr.predicate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.test.mock.jcr.MockProperty;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * Test case for {@link StringPropertyValueFilterPredicate}.
 */
public class StringPropertyValueFilteringPredicateTest {

    @Test
    public void testEvaluate() throws Exception {
        // GIVEN
        String value = "value";
        // WHEN
        StringPropertyValueFilterPredicate predicate = new StringPropertyValueFilterPredicate(value);
        // THEN
        assertTrue(predicate.evaluate(new MockProperty("someProperty", "value", null)));
        assertTrue(predicate.evaluate(new MockProperty("mgnl:someProperty", "value", null)));
        assertFalse(predicate.evaluate(new MockProperty("jcr:someProperty", "values ", null)));
        assertFalse(predicate.evaluate(new MockProperty("mgnl:someProperty", new Boolean(false), null)));
    }

    @Test
    public void testEvaluateType() throws Exception {
        // GIVEN
        String value = "1";
        // WHEN
        StringPropertyValueFilterPredicate predicate = new StringPropertyValueFilterPredicate(value);
        // THEN
        assertTrue(predicate.evaluate(new MockProperty("jcr:someProperty", "1", null)));
        assertFalse(predicate.evaluate(new MockProperty("jcr:someProperty", new Integer(1), null)));
    }

    @Test
    public void testEvaluateNull() throws Exception {
        // GIVEN
        String value = null;
        // WHEN
        StringPropertyValueFilterPredicate predicate = new StringPropertyValueFilterPredicate(value);
        // THEN
        assertFalse(predicate.evaluate(new MockProperty("jcr:someProperty", "a", null)));
        assertTrue(predicate.evaluate(new MockProperty("jcr:someProperty", null, null)));
    }

    @Test
    public void testReturnsFalseOnException() {
        JCRMgnlPropertyHidingPredicate predicate = new JCRMgnlPropertyHidingPredicate();
        Property property = mock(Property.class);
        try {
            when(property.getName()).thenThrow(new RepositoryException());
        } catch (RepositoryException e) {
            fail();
        }
        predicate.evaluate(property);
    }
}
