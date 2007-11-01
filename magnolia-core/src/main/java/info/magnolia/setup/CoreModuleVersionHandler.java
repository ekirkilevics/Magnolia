/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.setup;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
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
import info.magnolia.setup.for3_1.AddURIPermissionsToAllRoles;
import info.magnolia.setup.for3_1.IPConfigRulesUpdate;
import info.magnolia.setup.for3_1.LoginAuthTypePropertyMovedToFilter;
import info.magnolia.setup.for3_1.LoginFormPropertyMovedToFilter;
import info.magnolia.setup.for3_1.MoveMagnoliaUsersToRealmFolder;
import info.magnolia.setup.for3_1.ReconfigureCommands;
import info.magnolia.setup.for3_1.RemoveModuleDescriptorDetailsFromRepo;
import info.magnolia.setup.for3_1.RenamedRenderersToTemplateRenderers;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CoreModuleVersionHandler extends AbstractModuleVersionHandler {
    // tasks which have to be executed wether we're installing or upgrading from 3.0
    private final List genericTasksFor31 = Arrays.asList(new Task[]{
            // TODO : shouldn't this be conditional ? how do we migrate existing filters...
            new BootstrapSingleResource("New filters", "Will override the filter chain configuration with a completely new one.", "/mgnl-bootstrap/core/config.server.filters.xml"),

            new BootstrapConditionally("IPConfig rules changed",
                    "Updates the existing ip access rules to match the new configuration structure or bootstraps the new default configuration",
                    "/mgnl-bootstrap/core/config.server.IPConfig.xml",
                    new ArrayDelegateTask(null,
                            new NewPropertyTask("IPSecurityManager class property", "IPSecurity is now a component which can be configured through the repository", "config", "/server/IPConfig", "class", IPSecurityManagerImpl.class.getName()),
                            new IPConfigRulesUpdate()
                    )),

            // TODO add conditions for all these bootstrap since with this version we don't know if we're installing or updating !
            new BootstrapSingleResource("new i18n", /*TODO*/"blah blah", "/mgnl-bootstrap/core/config.server.i18n.content.xml"),
            new BootstrapSingleResource("new i18n", /*TODO*/"blah blah", "/mgnl-bootstrap/core/config.server.i18n.system.xml"),
            new BootstrapSingleResource(/*TODO*/"", /*TODO*/"", "/mgnl-bootstrap/core/config.server.MIMEMapping.xml"),
            new BootstrapSingleResource(/*TODO*/"", /*TODO*/"", "/mgnl-bootstrap/core/config.server.security.xml"),
            new BootstrapSingleResource(/*TODO*/"", /*TODO*/"", "/mgnl-bootstrap/core/config.server.URI2RepositoryMapping.xml"),
            new BootstrapSingleResource(/*TODO*/"", /*TODO*/"", "/mgnl-bootstrap/core/config.modules.adminInterface.virtualURIMapping.default.xml"),

            // -- /server configuration tasks
            new PropertyExistsDelegateTask("Cleanup", "Config property /server/defaultMailServer was unused", "config", "/server", "defaultMailServer",
                    new RemovePropertyTask("", "", "config", "/server", "defaultMailServer")),

            // the two following tasks replace the config.server.xml bootstrap file
            new CheckOrCreatePropertyTask("defaultExtension property", "Checks that the defaultExtension property exists in config:/server", "config", "/server", "defaultExtension", "html"),
            new CheckOrCreatePropertyTask("admin property", "Checks that the admin property exists in config:/server", "config", "/server", "admin", "true"),
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
                            new MoveAndRenamePropertyTask("unsecuredPath is now handled by the bypass mechanism", "/server/login", "UnsecuredPath", "/server/filters/uriSecurity/bypasses/login", "pattern"),
                            new RemoveNodeTask("Login configuration changed", "Removes /server/login as it is not used anymore", "config", "/server/login")
                    )),

            new CopyOrReplaceNodePropertiesTask("clientCallback configuration for content security", "The clientCallback configuration needs to be configuration for each security filter. This is copying the one from the URI security filter to the content security filter.",
                    "config", "/server/filters/uriSecurity/clientCallback", "/server/filters/cms/contentSecurity/clientCallback"),

            // --- user/roles repositories related tasks
            new CreateNodeTask("Adds system folder node to users workspace", "Add system realm folder /system to users workspace", ContentRepository.USERS, "/", Realm.REALM_SYSTEM, ItemType.NT_FOLDER),
            new CreateNodeTask("Adds admin folder node to users workspace", "Add magnolia realm folder /admin to users workspace", ContentRepository.USERS, "/", Realm.REALM_ADMIN, ItemType.NT_FOLDER),

            new IsAuthorInstanceDelegateTask("URI permissions", "Introduction of URI-based security. All existing roles will have GET/POST permissions on /*.",
                    new AddURIPermissionsToAllRoles(true),
                    new AddURIPermissionsToAllRoles(false)),

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
            new RenamedRenderersToTemplateRenderers(),
            new ReconfigureCommands(),
            // TODO : do we keep this ?
            new RemoveModuleDescriptorDetailsFromRepo(),
    });

    public CoreModuleVersionHandler() {
        super();
    }

    // TODO : review - currently core is always installed since 3.1 is its first version as a module,
    // but we need to behave differently if magnolia was installed previously
    protected List getBasicInstallTasks(InstallContext ctx) {
        final ArrayList tasks = new ArrayList(genericTasksFor31);
        tasks.add(new WarnTask("web.xml updates", "MagnoliaManagedFilter was renamed to MagnoliaMainFilter: please update the corresponding <filter-class> element in your web.xml file."));
        return tasks;
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
        u.filterMappedWithDispatcher("info.magnolia.cms.filters.MgnlMainFilter");
        return conditions;
    }
}
