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
package info.magnolia.module.exchangesimple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import info.magnolia.cms.security.MgnlKeyPair;

import org.junit.Before;
import org.junit.Test;

/**
 * Module class test.
 * 
 * @version $Id$
 * 
 */
public class ExchangeSimpleModuleTest {

    private ExchangeSimpleModule module;

    @Before
    public void setup() {
        module = new ExchangeSimpleModule();
    }
    @Test
    public void testGetActivationDelayTolerance() {
        // default value
        assertEquals(30000, module.getActivationDelayTolerance());
        module.setActivationDelayTolerance(5000);
        assertEquals(5000, module.getActivationDelayTolerance());
    }

    @Test
    public void testGetActivationKeyLength() {
        // default value
        assertEquals(1024, module.getActivationKeyLength());
        module.setActivationKeyLength(2048);
        assertEquals(2048, module.getActivationKeyLength());
    }

    @Test
    public void testGetTempKeys() {
        //default value
        assertNull( module.getTempKeys());
        module.setTempKeys(new MgnlKeyPair(null, null));
        assertNotNull(module.getTempKeys());
    }

}
