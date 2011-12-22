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
package info.magnolia.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

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
        SystemProperty.setProperty("magnolia.author.key.location", "/path/to/the/key");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        System.clearProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR);
        super.tearDown();
    }

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/core.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new CoreModuleVersionHandler() {
            // cheat - one of the conditions needs web.xml. Can't be bothered to fake that here
            @Override
            protected List<Condition> getInstallConditions() {
                return Collections.emptyList();
            }
        };
    }

    @Test
    public void testPngSwfMimeTypesOnInstall() throws ModuleManagementException, RepositoryException {
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    @Test
    public void testPngSwfMimeTypesOnUpdateFrom35x() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        // prior to 3.6.4, the mime types for png and swf were incorrect
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "application/octet-stream");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/octet-stream");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.5"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    @Test
    public void testPngSwfMimeTypesOnUpdateFrom35xWithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a 3pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        // prior to 3.6.4, the mime types for png and swf were incorrect - but values have been customized on this instance
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.5"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    @Test
    public void testPngSwfMimeTypesOnUpdateFrom364() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "image/png;");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.6.4"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 3.6.7+, since we have corrected values
     * for the mimetypes; when updating from 3.6.4 in an instance where the user
     * customized the mime types values, or updating from 3.6.7, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    @Test
    public void testPngSwfMimeTypesOnUpdateFrom364WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("3.6.4"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    @Test
    public void testPngSwfMimeTypesOnUpdateFrom401() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "image/png;");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.1"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 4.0.3+, since we have corrected values
     * for the mimetypes; when updating from 4.0.1 in an instance where the user
     * customized the mime types values, or updating from 4.0.3, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    @Test
    public void testPngSwfMimeTypesOnUpdateFrom401WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.0.1"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    @Test
    public void testPngSwfMimeTypesOnUpdateFrom410() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "image/png;");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 4.1.1+, since we have corrected values
     * for the mimetypes; when updating from 4.1.0 in an instance where the user
     * customized the mime types values, or updating from 4.1.1, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    @Test
    public void testPngSwfMimeTypesOnUpdateFrom410WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupConfigProperty("/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupConfigProperty("/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupConfigProperty("/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupConfigProperty("/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    private String getMimeTypePropertyValue(String typeName) throws RepositoryException {
        return MgnlContext.getHierarchyManager("config").getContent("/server/MIMEMapping/" + typeName).getNodeData("mime-type").getString();
    }

    private String getMimePropertyValue(String typeName, String propertyName) throws RepositoryException {
        return MgnlContext.getHierarchyManager(RepositoryConstants.CONFIG).getContent("/server/MIMEMapping/" + typeName).getNodeData(propertyName).getString();
    }

    /**
     * test unicode normalization filter and the new filter ordering (update version to 4.3).
     */
    @Test
    public void testUnicodeNormalizerAndFilterOrderOnUpdateFrom410() throws ModuleManagementException, RepositoryException {
        setupConfigNode("/server/filters/");
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute", ItemType.CONTENTNODE);
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigProperty("/server/filters/context", "enable", "true");
        setupConfigProperty("/server/filters/contentType", "enable", "true");
        setupConfigProperty("/server/filters/login", "enable", "true");
        setupConfigProperty("/server/filters/logout", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/activation", "enable", "true");
        // needed for 4.4.5 - configuration of mov MIME type
        setupConfigNode("/server/MIMEMapping/mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");
        // let's make sure we've set up this test with filters in their pre-4.3 order
        final Iterator<Content> filters = MgnlContext.getHierarchyManager("config").getContent("/server/filters/").getChildren().iterator();
        assertEquals("context", filters.next().getName());
        assertEquals("contentType", filters.next().getName());
        assertEquals("login", filters.next().getName());
        assertEquals("logout", filters.next().getName());
        assertEquals("uriSecurity", filters.next().getName());
        assertEquals("multipartRequest", filters.next().getName());
        assertEquals("activation", filters.next().getName());
        assertFalse(filters.hasNext());



        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.1"));

        final Iterator<Content> updatedFilters = MgnlContext.getHierarchyManager("config").getContent("/server/filters/").getChildren().iterator();
        assertEquals("context", updatedFilters.next().getName());
        assertEquals("contentType", updatedFilters.next().getName());
        assertEquals("multipartRequest", updatedFilters.next().getName());
        assertEquals("unicodeNormalization", updatedFilters.next().getName());
        assertEquals("login", updatedFilters.next().getName());
        assertEquals("logout", updatedFilters.next().getName());
        assertEquals("securityCallback", updatedFilters.next().getName());
        assertEquals("uriSecurity", updatedFilters.next().getName());
        assertEquals("activation", updatedFilters.next().getName());
        assertEquals("range", updatedFilters.next().getName());
        assertFalse(updatedFilters.hasNext());
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
    public void testMP4MimeTypesOnUpdateTo445() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigNode("/server/MIMEMapping/mov");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // other mime types didn't exist before 4.4.5
        setupConfigProperty("/server/MIMEMapping/mp4", "mime-type", "application/octet-stream");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.4"));

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
        // new properties for mov
        assertEquals("/.resources/file-icons/moov.png", getMimePropertyValue("mov", "icon"));
        assertEquals("mov", getMimePropertyValue("mov", "extension"));
    }

    @Test
    public void testMP4MimeTypesOnUpdateTo445WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigNode("/server/MIMEMapping/mov");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // if custom mime types have been set up already, we don't want to overwrite them
        setupConfigProperty("/server/MIMEMapping/m4a", "mime-type", "custom-type-for-m4a");
        setupConfigProperty("/server/MIMEMapping/m4b", "mime-type", "custom-type-for-m4b");
        setupConfigProperty("/server/MIMEMapping/m4r", "mime-type", "custom-type-for-m4r");
        setupConfigProperty("/server/MIMEMapping/m4v", "mime-type", "custom-type-for-m4v");
        setupConfigProperty("/server/MIMEMapping/mp4a", "mime-type", "custom-type-for-mp4a");
        setupConfigProperty("/server/MIMEMapping/mp4s", "mime-type", "custom-type-for-mp4s");
        setupConfigProperty("/server/MIMEMapping/mp4v", "mime-type", "custom-type-for-mp4v");
        setupConfigProperty("/server/MIMEMapping/mpg4", "mime-type", "custom-type-for-mpg4");
        setupConfigProperty("/server/MIMEMapping/srt", "mime-type", "custom-type-for-srt");
        setupConfigProperty("/server/MIMEMapping/mp4", "mime-type", "custom-type-for-mp4");
        // same if mov extension and icon have been set manually
        setupConfigProperty("/server/MIMEMapping/mov", "extension", "custom-extension-for-mov");
        setupConfigProperty("/server/MIMEMapping/mov", "icon", "custom-icon-for-mov");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.4"));

        assertEquals("custom-type-for-m4a", getMimeTypePropertyValue("m4a"));
        assertEquals("custom-type-for-m4b", getMimeTypePropertyValue("m4b"));
        assertEquals("custom-type-for-m4r", getMimeTypePropertyValue("m4r"));
        assertEquals("custom-type-for-m4v", getMimeTypePropertyValue("m4v"));
        assertEquals("custom-type-for-mp4a", getMimeTypePropertyValue("mp4a"));
        assertEquals("custom-type-for-mp4s", getMimeTypePropertyValue("mp4s"));
        assertEquals("custom-type-for-mp4v", getMimeTypePropertyValue("mp4v"));
        assertEquals("custom-type-for-mpg4", getMimeTypePropertyValue("mpg4"));
        assertEquals("custom-type-for-srt", getMimeTypePropertyValue("srt"));
        assertEquals("custom-type-for-mp4", getMimeTypePropertyValue("mp4"));

        assertEquals("custom-extension-for-mov", getMimePropertyValue("mov", "extension"));
        assertEquals("custom-icon-for-mov", getMimePropertyValue("mov", "icon"));
    }

    @Test
    public void testDefaultMP4MimeTypeOnUpdateTo445WithUserFixedValue() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigNode("/server/MIMEMapping/mov");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // already changed value differing from original installation
        setupConfigProperty("/server/MIMEMapping/mp4", "mime-type", "application/octet-stream");
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.4"));

        // don't overwrite user settings
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
    public void test446MimeTypesOnUpdateTo446() throws ModuleManagementException, RepositoryException {
        // new Mime types: Eot, Ogg, Otf, Ttf, Webm, Woff
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigNode("/server/MIMEMapping/mov");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);
        
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.5"));

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
    public void test446MimeTypesOnUpdateTo446WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // new Mime types: Eot, Ogg, Otf, Ttf, Webm, Woff
        // fake a pre-install:
        setupConfigProperty("/server/filters/multipartRequest", "enable", "true");
        setupConfigProperty("/server/filters/uriSecurity/clientCallback", "foo", "bar");
        setupConfigProperty("/server/rendering/freemarker", "foo", "bar"); // this was bootstrapped starting from 4.0
        setupConfigNode("/server/filters/bypasses/dontDispatchOnForwardAttribute");
        setupConfigNode("/server/MIMEMapping/mov");
        setupConfigProperty("/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupConfigProperty("server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl");
        setupProperty(RepositoryConstants.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", null);

        // if custom mime types have been set up already, we don't want to overwrite them
        setupConfigProperty("/server/MIMEMapping/eot", "mime-type", "custom-type-for-eot");
        setupConfigProperty("/server/MIMEMapping/oga", "mime-type", "custom-type-for-oga");
        setupConfigProperty("/server/MIMEMapping/ogg", "mime-type", "custom-type-for-ogg");
        setupConfigProperty("/server/MIMEMapping/ogv", "mime-type", "custom-type-for-ogv");
        setupConfigProperty("/server/MIMEMapping/otf", "mime-type", "custom-type-for-otf");
        setupConfigProperty("/server/MIMEMapping/ttf", "mime-type", "custom-type-for-ttf");
        setupConfigProperty("/server/MIMEMapping/weba", "mime-type", "custom-type-for-weba");
        setupConfigProperty("/server/MIMEMapping/webm", "mime-type", "custom-type-for-webm");
        setupConfigProperty("/server/MIMEMapping/woff", "mime-type", "custom-type-for-woff");
        
        // needed for 4.5 - UpdateUserManagers task
        setupConfigNode("/server/security/userManagers");

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("4.4.5"));

        assertEquals("custom-type-for-eot", getMimeTypePropertyValue("eot"));
        assertEquals("custom-type-for-oga", getMimeTypePropertyValue("oga"));
        assertEquals("custom-type-for-ogg", getMimeTypePropertyValue("ogg"));
        assertEquals("custom-type-for-ogv", getMimeTypePropertyValue("ogv"));
        assertEquals("custom-type-for-otf", getMimeTypePropertyValue("otf"));
        assertEquals("custom-type-for-ttf", getMimeTypePropertyValue("ttf"));
        assertEquals("custom-type-for-weba", getMimeTypePropertyValue("weba"));
        assertEquals("custom-type-for-webm", getMimeTypePropertyValue("webm"));
        assertEquals("custom-type-for-woff", getMimeTypePropertyValue("woff"));
    }
}
