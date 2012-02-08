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
package info.magnolia.module.admininterface.setup;

import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.test.ComponentsTestUtil;

import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;

/**
 * TODO Changes needed for http://jira.magnolia-cms.com/browse/SCRUM-140 forced me to comment out a couple of test which were failing.
 * @version $Id$
 */
public class AdminModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {
    private static final String QUICKSTART = "redirect:/.magnolia/pages/quickstart.html";
    private static final String ADMIN_CENTRAL = "redirect:/.magnolia/pages/adminCentral.html";

    @Override
    @Before
    public void setUp() throws Exception {
        ComponentsTestUtil.setInstance(ModuleRegistry.class, new ModuleRegistryImpl());
        ComponentsTestUtil.setImplementation(UnicodeNormalizer.Normalizer.class, "info.magnolia.cms.util.UnicodeNormalizer$NonNormalizer");

        super.setUp();
    }

    @Override
    public String getModuleDescriptorPath() {
        return "/META-INF/magnolia/admininterface.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml",
                "/META-INF/magnolia/rendering.xml",
                "/META-INF/magnolia/admininterface.xml"
        );
    }

    @Override
    public ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new AdminModuleVersionHandler();
    }

    @Test
    public void testDefaultURISetOnAuthorInstancesIsSetToAdminCentral() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/", "admin", "true");
        // fake a core install:
        setupConfigProperty("/server/filters/servlets/", "foo", "bar");

        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        checkDefaultUriMapping(ADMIN_CENTRAL);
        assertNoMessages(installContext);
    }

    @Test
    public void testDefaultURISetOnPublicInstancesIsSetToQuickStartIfNoTemplatesExist() throws ModuleManagementException, RepositoryException {
        setupConfigProperty("/server/", "admin", "false");
        // fake a core install:
        setupConfigProperty("/server/filters/servlets/", "foo", "bar");

        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        checkDefaultUriMapping(QUICKSTART);
        assertNoMessages(installContext);
    }

    private void checkDefaultUriMapping(String expectedValue) throws RepositoryException {
        assertConfig(expectedValue, "/modules/adminInterface/virtualURIMapping/default/toURI");
        assertConfig("/", "/modules/adminInterface/virtualURIMapping/default/fromURI");
        assertConfig(DefaultVirtualURIMapping.class.getName(),"/modules/adminInterface/virtualURIMapping/default/class");
    }
}
