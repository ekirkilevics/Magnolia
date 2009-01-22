/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.filters.MgnlMainFilter;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlUtilTest extends TestCase {
    private static final List<String> MANDATORY_DISPATCHERS = Arrays.asList("REQUEST", "FORWARD");
    private static final List<String> OPTIONAL_DISPATCHERS = Arrays.asList("ERROR");

    public void testFilterDispatcherChecksShouldNotFailWithCorrectConfiguration() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterok.xml"));
        assertEquals(1, util.checkFilterDispatchersConfiguration(MgnlMainFilter.class.getName(), MANDATORY_DISPATCHERS, OPTIONAL_DISPATCHERS));
    }

    public void testFilterDispatcherChecksShouldFailIfDispatcherNotSet() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filternodispatcher.xml"));
        assertEquals(-1, util.checkFilterDispatchersConfiguration(MgnlMainFilter.class.getName(), MANDATORY_DISPATCHERS, OPTIONAL_DISPATCHERS));
    }

    public void testFilterDispatcherChecksShouldNotFailIfFilterNotRegistered() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_nofilter.xml"));
        assertEquals(1, util.checkFilterDispatchersConfiguration(MgnlMainFilter.class.getName(), MANDATORY_DISPATCHERS, OPTIONAL_DISPATCHERS));
    }

    public void testFilterDispatcherChecksShouldFailIfMandatoryDispatchersIsNotUsed() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(-1, util.checkFilterDispatchersConfiguration("webxmltest.WithMissingForward", MANDATORY_DISPATCHERS, OPTIONAL_DISPATCHERS));
    }

    public void testFilterDispatcherChecksShouldReturnZeroIfUnsupportedDispatchersAreUsed() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(0, util.checkFilterDispatchersConfiguration("webxmltest.WithInclude", MANDATORY_DISPATCHERS, OPTIONAL_DISPATCHERS));
    }

    public void testFilterDispatcherErrorIsNotMandatory() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(1, util.checkFilterDispatchersConfiguration("webxmltest.ErrorIsNotMandatory", MANDATORY_DISPATCHERS, OPTIONAL_DISPATCHERS));
    }

    public void testFilterDispatcherOrderIsIrrelevant() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(1, util.checkFilterDispatchersConfiguration("webxmltest.OrderIsIrrelevant", MANDATORY_DISPATCHERS, OPTIONAL_DISPATCHERS));
    }

    public void testCanDetectFilterRegistration() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(true, util.isFilterRegistered("webxmltest.OrderIsIrrelevant"));
        assertEquals(false, util.isFilterRegistered("nonregistered.BlehFilter"));
    }

    public void testCanDetectServletRegistration() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterok.xml"));
        assertEquals(true, util.isServletRegistered("test"));
        assertEquals(false, util.isServletRegistered("foobar"));
        assertEquals(true, util.isServletMappingRegistered("test"));
        assertEquals(false, util.isServletMappingRegistered("foobar"));
        assertEquals(true, util.isServletMappingRegistered("test", "/test/*"));
        assertEquals(false, util.isServletMappingRegistered("test", "/bleh/*"));
        assertEquals(false, util.isServletMappingRegistered("foobar", "/bleh/*"));
        assertEquals(true, util.isServletOrMappingRegistered("test"));
        assertEquals(false, util.isServletOrMappingRegistered("foobar"));
    }
}
