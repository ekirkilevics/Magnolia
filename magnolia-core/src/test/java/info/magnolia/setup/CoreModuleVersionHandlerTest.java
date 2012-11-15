/**
 * This file Copyright (c) 2009-2012 Magnolia International
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
package info.magnolia.setup;

import static org.junit.Assert.*;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.filters.FilterManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $Id$
 */
public class CoreModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR, "./src/test/resources");
        // We have also have an invalid workspace.xml under .../workspaces/outdated - in order to not run into it, we cheat by setting the repo-home
        // to a value, that will not include it.
        SystemProperty.setProperty(SystemProperty.MAGNOLIA_REPOSITORIES_HOME, "src/test/resources/config/repositories/magnolia/workspaces/current");
        SystemProperty.setProperty("magnolia.author.key.location", "/path/to/the/key");
        System.setProperty("java.security.auth.login.config", "src/test/resources/config/current-jaas.config");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        SystemProperty.clear();
        super.tearDown();
    }

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/core.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new CoreModuleVersionHandler();
    }

    @Test
    public void testPngSwfMimeTypesOnInstall() throws ModuleManagementException, RepositoryException {
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    private String getMimeTypePropertyValue(String typeName) throws RepositoryException {
        return MgnlContext.getHierarchyManager("config").getContent("/server/MIMEMapping/" + typeName)
                .getNodeData("mime-type").getString();
    }

    @Test
    public void testMP4MimeTypesOnInstall() throws ModuleManagementException, RepositoryException {
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // new types
        assertEquals("audio/mp4", getMimeTypePropertyValue("m4a"));
        assertEquals("audio/mp4", getMimeTypePropertyValue("m4b"));
        assertEquals("audio/mp4", getMimeTypePropertyValue("m4r"));
        assertEquals("video/mp4", getMimeTypePropertyValue("m4v"));
        assertEquals("video/mp4", getMimeTypePropertyValue("mp4a"));
        assertEquals("application/mp4", getMimeTypePropertyValue("mp4s"));
        assertEquals("video/mp4", getMimeTypePropertyValue("mp4v"));
        assertEquals("video/mp4", getMimeTypePropertyValue("mpg4"));
        assertEquals("application/x-srt", getMimeTypePropertyValue("srt"));
        // this type used to be application/octet-stream
        assertEquals("video/mp4", getMimeTypePropertyValue("mp4"));
    }

    @Test
    public void test446MimeTypesOnInstall() throws ModuleManagementException, RepositoryException {
        // new Mime types: Eot, Ogg, Otf, Ttf, Webm, Woff
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // new types
        assertEquals("application/vnd.ms-fontobject", getMimeTypePropertyValue("eot"));
        assertEquals("audio/ogg", getMimeTypePropertyValue("oga"));
        assertEquals("video/ogg", getMimeTypePropertyValue("ogg"));
        assertEquals("video/ogg", getMimeTypePropertyValue("ogv"));
        assertEquals("application/x-font-otf", getMimeTypePropertyValue("otf"));
        assertEquals("application/x-font-ttf", getMimeTypePropertyValue("ttf"));
        assertEquals("audio/webm", getMimeTypePropertyValue("weba"));
        assertEquals("video/webm", getMimeTypePropertyValue("webm"));
        assertEquals("application/x-font-woff", getMimeTypePropertyValue("woff"));
    }

    @Test
    public void test45DoesNotInstallWhenFromSmallerThan446() {
        try {
            executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.5"));
        } catch (Throwable t) {
            assertTrue(t instanceof AssertionError);
            assertTrue(t.getMessage().contains("<stoppedConditionsNotMet>"));
        }
    }

    @Test
    public void testUpgradeFrom446() throws Exception{
        // GIVEN
        setupConfigNode("/server/filters/uriSecurity/clientCallback");
        setupConfigNode(FilterManager.SERVER_FILTERS);
        setupConfigNode("/server/security/userManagers/system");
        setupConfigNode("/server/security/userManagers/admin");
        setupConfigNode("/server/filters/servlets");
        setupConfigNode("/server/filters/securityCallback/bypasses");
        setupConfigNode("/server/filters/securityCallback/clientCallbacks/magnolia", ItemType.CONTENTNODE);
        setupConfigNode("/server/filters/securityCallback/clientCallbacks/magnolia/urlPattern");
        setupConfigNode("/server/filters/securityCallback/clientCallbacks/public");
        setupConfigNode("/server/filters/cms/contentSecurity/clientCallback");
        setupConfigProperty("/server/filters/securityCallback/clientCallbacks/magnolia/urlPattern", "patternString", "*");
        setupConfigNode("/server/i18n/system");
        setupConfigNode("/server/i18n/content");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.6"));

        // THEN
        final Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        final String systemUserManagerRealNameValue = configSession.getNode("/server/security/userManagers/system").getProperty("realmName").getString();
        final String adminUserManagerRealNameValue = configSession.getNode("/server/security/userManagers/admin").getProperty("realmName").getString();

        assertEquals("system", systemUserManagerRealNameValue);
        assertEquals("admin", adminUserManagerRealNameValue);

        // check securityCallbacks
        assertFalse(configSession.itemExists("/server/filters/securityCallback/bypasses"));
        assertFalse(configSession.itemExists("/server/filters/securityCallback/clientCallbacks/magnolia/urlPattern"));
        assertFalse(configSession.itemExists("/server/filters/securityCallback/clientCallbacks/magnolia"));
        assertTrue(configSession.itemExists("/server/filters/securityCallback/clientCallbacks/form"));
        assertFalse(configSession.itemExists("/server/filters/cms/contentSecurity/clientCallback"));
    }
}
