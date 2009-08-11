/**
 * This file Copyright (c) 2009 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.logging.AuditLoggingManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.InstallStatus;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleManagerImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.test.RepositoryTestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CoreModuleVersionHandlerTest extends RepositoryTestCase {
    protected void setUp() throws Exception {
        super.setUp();
        // this should disable observation for audit logging (mgnl-beans.properties registers a path for this componenent)
        FactoryUtil.setInstance(AuditLoggingManager.class, new AuditLoggingManager());
    }

    public void testPngSwfMimeTypesOnInstall() throws ModuleManagementException, RepositoryException {
        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(null);

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom35x() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        // prior to 3.6.4, the mime types for png and swf were incorrect
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "application/octet-stream");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "application/octet-stream");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("3.5"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom35xWithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a 3pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        // prior to 3.6.4, the mime types for png and swf were incorrect - but values have been customized on this instance
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("3.5"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom364() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "image/png;");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("3.6.4"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 3.6.7+, since we have corrected values
     * for the mimetypes; when updating from 3.6.4 in an instance where the user
     * customized the mime types values, or updating from 3.6.7, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    public void testPngSwfMimeTypesOnUpdateFrom364WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("3.6.4"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom401() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "image/png;");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("4.0.1"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 4.0.2+, since we have corrected values
     * for the mimetypes; when updating from 4.0.1 in an instance where the user
     * customized the mime types values, or updating from 4.0.2, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    public void testPngSwfMimeTypesOnUpdateFrom401WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("4.0.1"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    public void testPngSwfMimeTypesOnUpdateFrom410() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end)
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "image/png;");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "application/x-shockwave-flash;");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("4.1"));

        assertEquals("image/png", getMimeTypePropertyValue("png"));
        assertEquals("application/x-shockwave-flash", getMimeTypePropertyValue("swf"));
    }

    /**
     * This is essentially the same case as testing the update from 4.1.1+, since we have corrected values
     * for the mimetypes; when updating from 4.1.0 in an instance where the user
     * customized the mime types values, or updating from 4.1.1, where the values would have been fixed
     * by update tasks, is equivalent.
     */
    public void testPngSwfMimeTypesOnUpdateFrom410WithUserFixedValues() throws ModuleManagementException, RepositoryException {
        // fake a pre-install:
        setupProperty(ContentRepository.CONFIG, "/server/filters/servlets/log4j/mappings/--magnolia-log4j-", "pattern", "/.magnolia/log4j*");
        setupProperty(ContentRepository.CONFIG, "server/rendering/linkResolver", "foo", "bar");
        setupProperty(ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*");

        // prior to 3.6.4, the mime types for flv and svg did not exit
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/flv", "mime-type", "video/x-flv");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/svg", "mime-type", "image/svg+xml");
        // at 3.6.4, the mime types for png and swf were updated, but still incorrectly (; at the end) - values have been customized on this instance
        // this was only fixed for 3.6.7, 4.0.2 and 4.1.1
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "custom-type-for-png");
        setupProperty(ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "custom-type-for-swf");

        doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(Version.parseVersion("4.1"));

        assertEquals("custom-type-for-png", getMimeTypePropertyValue("png"));
        assertEquals("custom-type-for-swf", getMimeTypePropertyValue("swf"));
    }

    private void setupProperty(final String workspace, String path, String propertyName, String value) throws RepositoryException {
        final HierarchyManager hm = MgnlContext.getHierarchyManager(workspace);
        final Content content = ContentUtil.createPath(hm, path);
        NodeDataUtil.getOrCreateAndSet(content, propertyName, value);
        hm.save();
    }

    private String getMimeTypePropertyValue(String typeName) throws RepositoryException {
        return MgnlContext.getHierarchyManager("config").getContent("/server/MIMEMapping/" + typeName).getNodeData("mime-type").getString();
    }

    private void doUpdatesOnInstallAsIfTheCurrentlyInstalledVersionOfCoreWas(final Version currentVersion) throws ModuleManagementException {
        final BetwixtModuleDefinitionReader reader = new BetwixtModuleDefinitionReader();
        final ModuleDefinition core = reader.readFromResource("/META-INF/magnolia/core.xml");

        final ModuleDefinitionReader readerMock = createStrictMock(ModuleDefinitionReader.class);
        expect(readerMock.readAll()).andReturn(Collections.singletonMap("core", core));
        replay(readerMock);

        final InstallContextImpl ctx = new InstallContextImpl();
        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        final ModuleManagerImpl mm = new ModuleManagerImpl(ctx, readerMock, moduleRegistry) {
            protected ModuleVersionHandler newVersionHandler(ModuleDefinition module) {
                assertEquals("this test doesn't behave as expected", core, module);
                return new CoreModuleVersionHandler() {
                    public Version getCurrentlyInstalled(InstallContext ctx) {
                        return currentVersion;
                    }

                    // cheat - one of the conditions needs web.xml. Can't be bothered to fake that here
                    protected List getInstallConditions() {
                        return Collections.emptyList();
                    }
                };
            }
        };

        final List<ModuleDefinition> defs = mm.loadDefinitions();
        assertEquals(1, defs.size());
        assertEquals(core, defs.get(0));

        mm.checkForInstallOrUpdates();
        assertTrue(mm.getStatus().needsUpdateOrInstall());

        mm.performInstallOrUpdate();
        assertEquals(InstallStatus.installDone, mm.getInstallContext().getStatus());

        verify(readerMock);
    }
}
