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
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddNodeTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckOrCreatePropertyTask;
import info.magnolia.module.delta.CopyOrReplaceNodePropertiesTask;
import info.magnolia.module.delta.ModuleBootstrapTask;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.MoveAndRenamePropertyTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.setup.for3_1.LoginAuthTypePropertyMovedToFilter;
import info.magnolia.setup.for3_1.LoginFormPropertyMovedToFilter;
import info.magnolia.setup.for3_1.MoveMagnoliaUsersToRealmFolders;
import info.magnolia.setup.for3_1.ReconfigureCommands;
import info.magnolia.setup.for3_1.RemoveModuleDescriptorDetailsFromRepo;
import info.magnolia.setup.for3_1.RenamedRenderersToTemplateRenderers;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CoreModuleVersionHandler extends AbstractModuleVersionHandler {
    private final List tasks31 = Arrays.asList(new Task[]{
            new WarnTask("web.xml updates", "CacheGeneratorServlet has been removed in Magnolia 3.1: please remove the corresponding <servlet> and <servlet-mapping> elements in your web.xml file."),
            new WarnTask("web.xml updates", "MagnoliaManagedFilter was renamed to MagnoliaMainFilter: please update the corresponding <filter-class> element in your web.xml file."),

            new AddNodeTask("Adds system folder node to users workspace", "Add system realm folder /system to users workspace", ContentRepository.USERS, "/", Realm.REALM_SYSTEM, ItemType.NT_FOLDER),
            new AddNodeTask("Adds admin folder node to users workspace", "Add magnolia realm folder /admin to users workspace", ContentRepository.USERS, "/", Realm.REALM_ADMIN, ItemType.NT_FOLDER),

            new ModuleBootstrapTask(), // TODO we should prbly avoid this since we don't know if we're installing or updating
            new ModuleFilesExtraction(),

            // TODO : this should be conditional ? how do we migrate existing filters...
            new BootstrapSingleResource("New filters", "Will override the filter chain configuration with a completely new one.", "/mgnl-bootstrap/core/config.server.filters.xml"),

            new ReconfigureCommands(),

            new PropertyExistsDelegateTask("Cleanup", "Config property /server/defaultMailServer was unused",
                            "config", "/server", "defaultMailServer",
                    new RemovePropertyTask("Cleanup", "Config property /server/defaultMailServer was unused",
                            "config", "/server", "defaultMailServer")),

            // the two following tasks replace the config.server.xml bootstrap file
            new CheckOrCreatePropertyTask("defaultExtension property", "Checks that the defaultExtension property exists in config:/server", "config", "/server", "defaultExtension", "html"),
            new CheckOrCreatePropertyTask("admin property", "Checks that the admin property exists in config:/server", "config", "/server", "admin", "true"),
            new MoveAndRenamePropertyTask("basicRealm property",
                    "/server", "basicRealm", "magnolia 3.0",
                    "/server/filters/uriSecurity/clientCallback", "realmName", "Magnolia"),
            new ArrayDelegateTask("defaultBaseUrl property",
                    new NewPropertyTask("defaultBaseUrl property", "Adds the new defaultBaseUrl property with a default value.",
                            "config", "/server", "defaultBaseUrl", "http://localhost:8080/magnolia/"),
                    new WarnTask("defaultBaseUrl property", "Please set the config:/server/defaultBaseUrl property to a full URL to be used when generating absolute URLs for external systems.")
            ),

            // this is only valid when updating - if /server/login exists
            new NodeExistsDelegateTask("Login configuration", "The login configuration was moved to filters configuration",
                    "config", "/server/login", new ArrayDelegateTask("",
                    new LoginAuthTypePropertyMovedToFilter(),
                    new LoginFormPropertyMovedToFilter(),
                    new MoveAndRenamePropertyTask("unsecuredPath is now handled by the bypass mechanism",
                            "/server/login", "UnsecuredPath",
                            "/server/filters/uriSecurity/bypasses/login", "pattern"),
                    new RemoveNodeTask("Login configuration changed", "Removes /server/login as it is not used anymore", "config", "/server/login")
            )),

            new CopyOrReplaceNodePropertiesTask("clientCallback configuration for content security", "The clientCallback configuration needs to be configuration for each security filter. This is copying the one from the URI security filter to the content security filter.",
                    "config", "/server/filters/uriSecurity/clientCallback", "/server/filters/cms/contentSecurity/clientCallback"),

            new RenamedRenderersToTemplateRenderers(),

            // TODO : do we keep this ?
            new RemoveModuleDescriptorDetailsFromRepo(),

            new NodeExistsDelegateTask(
                "Moves anonymous user",
                "Anonymous user must exist in the system realm",
                ContentRepository.USERS,
                "/" + MgnlUserManager.ANONYMOUS_USER,
                new MoveNodeTask("", "", ContentRepository.USERS, "/" + MgnlUserManager.ANONYMOUS_USER, "/"
                    + Realm.REALM_SYSTEM
                    + "/"
                    + MgnlUserManager.ANONYMOUS_USER, true)),

            new NodeExistsDelegateTask(
                "Moves superuser user",
                "Superuser user must exist in the system realm",
                ContentRepository.USERS,
                "/" + MgnlUserManager.SYSTEM_USER,
                new MoveNodeTask("", "", ContentRepository.USERS, "/" + MgnlUserManager.SYSTEM_USER, "/"
                    + Realm.REALM_SYSTEM
                    + "/"
                    + MgnlUserManager.SYSTEM_USER, true)),

            // other users are moved to the admin realm
            new MoveMagnoliaUsersToRealmFolders(),


            // new BootstrapSingleResource("new i18n", /*TODO*/"blah blah", "/mgnl-bootstrap/core/config.server.i18n.content.xml"),
            // new BootstrapSingleResource("superuser role", /*TODO*/"blah blah", "/mgnl-bootstrap/core/userroles.superuser.xml"),
    });

    public CoreModuleVersionHandler() {
        super();
    }

    // TODO : review - currently core is always installed since 3.1 is its first version as a module,
    // but we need to behave differently if magnolia was installed previously
    protected List getBasicInstallTasks(InstallContext installContext) {
        return tasks31;
    }
}
