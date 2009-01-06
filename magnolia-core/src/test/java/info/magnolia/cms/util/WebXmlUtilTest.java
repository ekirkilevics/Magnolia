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

/**
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlUtilTest extends TestCase {

    public void testFilterDispatcherChecksShouldNotFailWithCorrectConfiguration() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterok.xml"));
        assertTrue(util.areFilterDispatchersConfiguredProperly(MgnlMainFilter.class.getName(), Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherChecksShouldFailIfDispatcherNotSet() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filternodispatcher.xml"));
        assertFalse(util.areFilterDispatchersConfiguredProperly(MgnlMainFilter.class.getName(), Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherChecksShouldNotFailIfFilterNotRegistered() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_nofilter.xml"));
        assertTrue(util.areFilterDispatchersConfiguredProperly(MgnlMainFilter.class.getName(), Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherChecksShouldFailIfWrongDispatchersAreUsed() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(false, util.areFilterDispatchersConfiguredProperly("webxmltest.WithMissingForward", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
        assertEquals(false, util.areFilterDispatchersConfiguredProperly("webxmltest.WithInclude", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherErrorIsNotMandatory() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(true, util.areFilterDispatchersConfiguredProperly("webxmltest.ErrorIsNotMandatory", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
    }

    public void testFilterDispatcherOrderIsIrrelevant() {
        WebXmlUtil util = new WebXmlUtil(getClass().getResourceAsStream("web_filterwrongdispatchers.xml"));
        assertEquals(true, util.areFilterDispatchersConfiguredProperly("webxmltest.OrderIsIrrelevant", Arrays.asList("REQUEST", "FORWARD"), Arrays.asList("ERROR")));
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
