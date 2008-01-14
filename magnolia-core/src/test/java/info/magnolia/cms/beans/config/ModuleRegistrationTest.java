/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import info.magnolia.cms.util.FactoryUtil;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class ModuleRegistrationTest extends TestCase {
    private static final File MGNL_CORE_ROOT = new File(ModuleRegistrationTest.class.getResource("/testresource.txt").getFile()).
            getParentFile().getParentFile().getParentFile();

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        FactoryUtil.setDefaultImplementation(ModuleRegistration.class, ModuleRegistration.class.getName());
        super.setUp();
    }

    public void testGetModuleRootJar() {
        URL xmlUrl = getClass().getResource("/testjar.jar");
        File moduleRoot = new ModuleRegistration(). getModuleRoot(xmlUrl);

        assertNotNull("Unable to find jar", moduleRoot);
        assertTrue("Jar " + moduleRoot.getAbsolutePath() + " does not exist", moduleRoot.exists());
        assertEquals(MGNL_CORE_ROOT, moduleRoot);
    }

    public void testGetModuleRootJarWIthDot() {
        URL xmlUrl = getClass().getResource("/.space test/testjar.jar");
        File moduleRoot = new ModuleRegistration().getModuleRoot(xmlUrl);

        assertNotNull("Unable to find jar", moduleRoot);
        assertTrue("Jar " + moduleRoot.getAbsolutePath() + " does not exist", moduleRoot.exists());
        assertEquals(new File(MGNL_CORE_ROOT, "target"), moduleRoot);
    }

    public void testGetModuleRootDirectory() {
        File moduleRoot = new ModuleRegistration().getModuleRoot("/testresource.txt");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());
        assertEquals(MGNL_CORE_ROOT, moduleRoot);
    }

    public void testGetModuleRootDirectoryWithDot() {
        File moduleRoot = new ModuleRegistration().getModuleRoot("/.space test/testresource.txt");

        assertNotNull(moduleRoot);
        assertTrue(moduleRoot.exists());
        assertEquals(new File(MGNL_CORE_ROOT, "target"), moduleRoot);
    }


}
