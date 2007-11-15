/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.IPSecurityManagerImpl;
import info.magnolia.cms.security.Realm;
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckOrCreatePropertyTask;
import info.magnolia.module.delta.CopyOrReplaceNodePropertiesTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.MoveAndRenamePropertyTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RegisterModuleServletsTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;
import info.magnolia.module.delta.WorkspaceXmlConditionsUtil;
import info.magnolia.setup.for3_5.AddURIPermissionsToAllRoles;
import info.magnolia.setup.for3_5.IPConfigRulesUpdate;
import info.magnolia.setup.for3_5.LoginAuthTypePropertyMovedToFilter;
import info.magnolia.setup.for3_5.LoginFormPropertyMovedToFilter;
import info.magnolia.setup.for3_5.MoveMagnoliaUsersToRealmFolder;
import info.magnolia.setup.for3_5.ReconfigureCommands;
import info.magnolia.setup.for3_5.RemoveModuleDescriptorDetailsFromRepo;
import info.magnolia.setup.for3_5.RenamedRenderersToTemplateRenderers;
import info.magnolia.setup.for3_5.UpdateI18nConfiguration;
import info.magnolia.setup.for3_5.UpdateURI2RepositoryMappings;
import info.magnolia.setup.for3_5.UpdateURIMappings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CoreModuleVersionHandler extends AbstractModuleVersionHandler {

    public static final String BOOTSTRAP_AUTHOR_INSTANCE_PROPERTY = "magnolia.bootstrap.authorInstance";

    // tasks which have to be executed wether we're installing or upgrading from 3.0
    private final List genericTasksFor35 = Arrays.asList(new Task[]{
            // TODO : shouldn't this be conditional ? how do we migrate existing filters...
            new BootstrapSingleResource("New filters", "Will override the filter chain configuration with a completely new one.", "/mgnl-bootstrap/core/config.server.filters.xml"),

            new BootstrapConditionally("IPConfig rules changed",
                    "Updates the existing ip access rules to match the new configuration structure or bootstraps the new default configuration.",
                    "/mgnl-bootstrap/core/config.server.IPConfig.xml",
                    new ArrayDelegateTask(null,
                            new NewPropertyTask("IPSecurityManager class property", "IPSecurity is now a component which can be configured through the repository.", "config", "/server/IPConfig", "class", IPSecurityManagerImpl.class.getName()),
                            new IPConfigRulesUpdate()
                    )),

            new UpdateI18nConfiguration(),

            new BootstrapSingleResource("New security configuration", "Install new configuration for security managers.", "/mgnl-bootstrap/core/config.server.security.xml"),
            new BootstrapSingleResource("New rendering strategy for links", "Install new configuration for link resolving.", "/mgnl-bootstrap/core/config.server.rendering.linkResolver.xml"),

            new BootstrapConditionally("MIME mappings", "Adds MIMEMappings to server config, if not already present.", "/mgnl-bootstrap/core/config.server.MIMEMapping.xml"),
            new BootstrapConditionally("URI2Repository mappings", "Installs new configuration of URI2Repository mappings.", "/mgnl-bootstrap/core/config.server.URI2RepositoryMapping.xml", new UpdateURI2RepositoryMappings()),

            // -- /server configuration tasks
            new PropertyExistsDelegateTask("Cleanup", "Config property /server/defaultMailServer was unused.", "config", "/server", "defaultMailServer",
                    new RemovePropertyTask("", "", "config", "/server", "defaultMailServer")),

            // the two following tasks replace the config.server.xml bootstrap file
            new CheckOrCreatePropertyTask("defaultExtension property", "Checks that the defaultExtension property exists in config:/server", "config", "/server", "defaultExtension", "html"),

            new CheckOrCreatePropertyTask("admin property", "Checks that the admin property exists in config:/server", "config", "/server", "admin", StringUtils.defaultIfEmpty(SystemProperty.getProperty(BOOTSTRAP_AUTHOR_INSTANCE_PROPERTY), "true")),
            new MoveAndRenamePropertyTask("basicRealm property", "/server", "basicRealm", "magnolia 3.0", "/server/filters/uriSecurity/clientCallback", "realmName", "Magnolia"),
            new ArrayDelegateTask("defaultBaseUrl property",
                    new NewPropertyTask("defaultBaseUrl property", "Adds the new defaultBaseUrl property with a default value.", "config", "/server", "defaultBaseUrl", "http://localhost:8080/magnolia/"),
                    new WarnTask("defaultBaseUrl property", "Please set the config:/server/defaultBaseUrl property to a full URL to be used when generating absolute URLs for external systems.")
            ),

            // this is only valid when updating - if /server/login exists
            new NodeExistsDelegateTask("Login configuration", "The login configuration was moved to filters configuration", "config", "/server/login",
                    new ArrayDelegateTask("",
                            new LoginAuthTypePropertyMovedToFilter(),
                            new LoginFormPropertyMovedToFilter(),
                            new MoveAndRenamePropertyTask("unsecuredPath is now handled by the bypass mechanism.", "/server/login", "UnsecuredPath", "/server/filters/uriSecurity/bypasses/login", "pattern"),
                            new RemoveNodeTask("Login configuration changed", "Removes /server/login as it is not used anymore.", "config", "/server/login")
                    )),

            new CopyOrReplaceNodePropertiesTask("clientCallback configuration for content security", "The clientCallback configuration needs to be configuration for each security filter. This is copying the one from the URI security filter to the content security filter.",
                    "config", "/server/filters/uriSecurity/clientCallback", "/server/filters/cms/contentSecurity/clientCallback"),

            // --- user/roles repositories related tasks
            new CreateNodeTask("Adds system folder node to users workspace", "Add system realm folder /system to users workspace.", ContentRepository.USERS, "/", Realm.REALM_SYSTEM, ItemType.NT_FOLDER),
            new CreateNodeTask("Adds admin folder node to users workspace", "Add magnolia realm folder /admin to users workspace.", ContentRepository.USERS, "/", Realm.REALM_ADMIN, ItemType.NT_FOLDER),

            new IsAuthorInstanceDelegateTask("URI permissions", "Introduction of URI-based security. All existing roles will have GET/POST permissions on /*.",
                    new AddURIPermissionsToAllRoles(true),
                    new AddURIPermissionsToAllRoles(false)),

            new IsAuthorInstanceDelegateTask("Anonymous role", "Anonymous role must exist",
                new BootstrapConditionally("", "Author permissions", "/mgnl-bootstrap/core/userroles.anonymous.xml"), 
                new BootstrapConditionally("", "Public permissions", "/mgnl-bootstrap/core/public/userroles.anonymous.xml")), 

            new BootstrapConditionally("Anonymous user", "Anonymous user must exist in the system realm: will move the existing one or bootstrap it.",
                    ContentRepository.USERS, "/anonymous", "/mgnl-bootstrap/core/users.system.anonymous.xml",
                    new ArrayDelegateTask("",
                            new MoveNodeTask("", "", ContentRepository.USERS, "/anonymous", "/system/anonymous", false),
                            new NewPropertyTask("Anonymous user", "Anonymous user must have a password.", ContentRepository.USERS, "/system/anonymous", "pswd", new String(Base64.encodeBase64("anonymous".getBytes())))
                    )),

            new BootstrapConditionally("Superuser user", "Superuser user must exist in the system realm: will move the existing one or bootstrap it.",
                    ContentRepository.USERS, "/superuser", "/mgnl-bootstrap/core/users.system.superuser.xml",
                    new MoveNodeTask("", "", ContentRepository.USERS, "/superuser", "/system/superuser", false)),

            new BootstrapConditionally("Superuser role", "Bootstraps the superuser role if needed.", "/mgnl-bootstrap/core/userroles.superuser.xml"),
            // TODO : how about the anonymous role ? it's currently bootstrapped through the webapp module. Has it been modified since 3.0 ?
            //new BootstrapConditionally("Anonymous role", "Bootstraps the anonymous role if needed.", )

            // only relevant if updating, but does not hurt if installing since it checks for mgnl:user nodes
            new MoveMagnoliaUsersToRealmFolder(),

            // --- generic tasks
            new ModuleFilesExtraction(),
            new RegisterModuleServletsTask(),

            // --- system-wide tasks (impact all modules)
            new NodeExistsDelegateTask("Modules node", "Creates the modules node in the config repository if needed.", ContentRepository.CONFIG, "/modules", null,
                    new CreateNodeTask(null, null, ContentRepository.CONFIG, "/", "modules", ItemType.CONTENT.getSystemName())),
            new RenamedRenderersToTemplateRenderers(),
            new ReconfigureCommands(),
            new UpdateURIMappings(),
            // TODO : do we keep this ?
            new RemoveModuleDescriptorDetailsFromRepo(),
    });

    public CoreModuleVersionHandler() {
        super();
    }

    // TODO : review - currently core is always installed since 3.5 is its first version as a module,
    // but we need to behave differently if magnolia was installed previously
    protected List getBasicInstallTasks(InstallContext ctx) {
        return genericTasksFor35;
    }

    protected List getInstallConditions() {
        final ArrayList conditions = new ArrayList();
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(conditions);
        u.servletIsNowWrapped("ActivationHandler");
        u.servletIsNowWrapped("AdminTreeServlet");
        u.servletIsNowWrapped("classpathspool");
        u.servletIsNowWrapped("DialogServlet");
        u.servletIsNowWrapped("PageServlet");
        u.servletIsNowWrapped("log4j");
        u.servletIsNowWrapped("FCKEditorSimpleUploadServlet");
        u.servletIsDeprecated("uuidRequestDispatcher");
        u.filterIsDeprecated("info.magnolia.cms.filters.MagnoliaManagedFilter", "info.magnolia.cms.filters.MgnlMainFilter");
        u.filterMustBeRegisteredWithCorrectDispatchers("info.magnolia.cms.filters.MgnlMainFilter");
        u.listenerMustBeRegistered("info.magnolia.cms.servlets.MgnlServletContextListener");
        u.listenerIsDeprecated("info.magnolia.cms.servlets.PropertyInitializer", "info.magnolia.cms.servlets.MgnlServletContextListener");
        u.listenerIsDeprecated("info.magnolia.cms.beans.config.ShutdownManager", "info.magnolia.cms.servlets.MgnlServletContextListener");
        final WorkspaceXmlConditionsUtil u2 = new WorkspaceXmlConditionsUtil(conditions);
        u2.workspaceHasOldIndexer();
        return conditions;
    }
}
