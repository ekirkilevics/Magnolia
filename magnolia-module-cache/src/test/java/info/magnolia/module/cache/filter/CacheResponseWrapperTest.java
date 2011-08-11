/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.module.cache.filter;

import static org.junit.Assert.*;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;


/**
 * @version $Id$
 */
public class CacheResponseWrapperTest {

    @Test
    public void testSetHeader() {
        IMocksControl mocksControl = EasyMock.createNiceControl();
        HttpServletResponse mockResponse = mocksControl.createMock(HttpServletResponse.class);

        // set one value - one will be in...
        CacheResponseWrapper wrapper = new CacheResponseWrapper(mockResponse, 0, false);
        String testName = "name";
        wrapper.setHeader(testName, "value");
        Collection names = (Collection) wrapper.getHeaders().get(testName);
        assertEquals(1, names.size());

        // set another value - only this should be in then
        wrapper.setHeader(testName, "anotherValue");
        names = (Collection) wrapper.getHeaders().get(testName);
        assertEquals(1, names.size());
    }

    @Test
    public void testGetLastModified() {
        IMocksControl mocksControl = EasyMock.createNiceControl();
        HttpServletResponse mockResponse = mocksControl.createMock(HttpServletResponse.class);

        CacheResponseWrapper wrapper = new CacheResponseWrapper(mockResponse, 0, false);

        try {
            wrapper.getLastModified();
            fail("No Last-Modified was set yet - expected Exception here!");
        }
        catch (IllegalStateException e) {
            assertTrue(true);
        }

        String timestampString = "Fri, 03 Dec 2010 07:14:01 CET";
        String lastModified = "Last-Modified";
        wrapper.setHeader(lastModified, timestampString);
        assertEquals(1291356841000l, wrapper.getLastModified());

        // force adding second LastModified-value
        wrapper.addHeader(lastModified, timestampString);

        try {
            wrapper.getLastModified();
            fail("Two Last-Modified were set - expected Exception here!");
        }
        catch (IllegalStateException e) {
            assertTrue(true);
        }

        // add long Last-Modified
        wrapper.setDateHeader(lastModified, 1291356841000l);
        assertEquals(1291356841000l, wrapper.getLastModified());

        // add invalid int-type for Last-Modified
        wrapper.setIntHeader(lastModified, 42);
        try {
            wrapper.getLastModified();
            fail("Last-Modified was set as int - expected Exception here!");
        }
        catch (IllegalStateException e) {
            assertTrue(true);
        }
    }
}
