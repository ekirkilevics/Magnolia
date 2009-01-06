/**
 * This file Copyright (c) 2008-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.SystemProperty;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class PropertiesInitializerTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();
        // shunt log4j
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    }

    public void testSimpleProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("property", SystemProperty.getProperty("test.one"));
    }

    public void testNestedProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("nested property", SystemProperty.getProperty("test.two"));
    }

    public void testNestedPropertyMoreLevels() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("another nested property", SystemProperty.getProperty("test.three"));
    }

    public void testCircularProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("${test.circular2}", SystemProperty.getProperty("test.circular1"));
        assertEquals("${test.circular1}", SystemProperty.getProperty("test.circular2"));
    }

    public void testSelfReferencingProperty() throws Exception {
        PropertiesInitializer pi = PropertiesInitializer.getInstance();

        URL propertyUrl = this.getClass().getResource("/test-magnolia.properties");
        String propertyDir = new File(propertyUrl.toURI().getRawPath()).getParent();

        pi.loadAllProperties("test-magnolia.properties", propertyDir);

        assertEquals("${test.circular3}", SystemProperty.getProperty("test.circular3"));
    }
}
