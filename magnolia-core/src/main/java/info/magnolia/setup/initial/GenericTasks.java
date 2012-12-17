/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.setup.initial;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.Realm;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckOrCreatePropertyTask;
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
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.setup.CoreModuleVersionHandler;

import java.util.Arrays;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 * Groups tasks which are need to do a fresh install of magnolia 4.5 (or higher).
 *
 * @see info.magnolia.setup.CoreModuleVersionHandler
 */
public class GenericTasks {
    /**
     * @return tasks which have to be executed upon new installation (not update)
     */
    public static List<Task> genericTasksForNewInstallation() {
        final String areWeBootstrappingAuthorInstance = StringUtils.defaultIfEmpty(SystemProperty.getProperty(CoreModuleVersionHandler.BOOTSTRAP_AUTHOR_INSTANCE_PROPERTY), "true");
        return Arrays.asList(
                // - install server node
                new NodeExistsDelegateTask("Server node", "Creates the server node in the config repository if needed.", RepositoryConstants.CONFIG, "/server", null,
                        new CreateNodeTask(null, null, RepositoryConstants.CONFIG, "/", "server", NodeTypes.Content.NAME)),

                // - install or update modules node
                new NodeExistsDelegateTask("Modules node", "Creates the modules node in the config repository if needed.", RepositoryConstants.CONFIG, "/modules", null,
                        new CreateNodeTask(null, null, RepositoryConstants.CONFIG, "/", "modules", NodeTypes.Content.NAME)),

                new BootstrapSingleResource("Bootstrap", "Bootstraps the new filter configuration", "/mgnl-bootstrap/core/config.server.filters.xml", ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW),

                new BootstrapSingleResource("IPConfig rules changed",
                        "Updates the existing ip access rules to match the new configuration structure or bootstraps the new default configuration.",
                        "/mgnl-bootstrap/core/config.server.IPConfig.xml"),

                new BootstrapSingleModuleResource("i18n content", "bootstrap the config", "config.server.i18n.content.xml"),
                new BootstrapSingleModuleResource("i18n system", "bootstrap the config", "config.server.i18n.system.xml"),


                new BootstrapSingleResource("New security configuration", "Install new configuration for security managers.", "/mgnl-bootstrap/core/config.server.security.xml"),
                new BootstrapSingleResource("New rendering strategy for links", "Install new configuration for link resolving.", "/mgnl-bootstrap/core/config.server.rendering.linkManagement.xml"),

                new BootstrapConditionally("MIME mappings", "Adds MIMEMappings to server config, if not already present.", "/mgnl-bootstrap/core/config.server.MIMEMapping.xml"),
                new BootstrapConditionally("URI2Repository mappings", "Installs new configuration of URI2Repository mappings.", "/mgnl-bootstrap/core/config.server.URI2RepositoryMapping.xml", new UpdateURI2RepositoryMappings()),

                // -- /server configuration tasks
                new PropertyExistsDelegateTask("Cleanup", "Config property /server/defaultMailServer was unused.", "config", "/server", "defaultMailServer",
                        new RemovePropertyTask("", "", "config", "/server", "defaultMailServer")),

                // the two following tasks replace the config.server.xml bootstrap file
                new CheckOrCreatePropertyTask("defaultExtension property", "Checks that the defaultExtension property exists in config:/server", "config", "/server", "defaultExtension", "html"),

                new CheckOrCreatePropertyTask("admin property", "Checks that the admin property exists in config:/server", "config", "/server", "admin", areWeBootstrappingAuthorInstance),

                new ArrayDelegateTask("defaultBaseUrl property",
                        new NewPropertyTask("defaultBaseUrl property", "Adds the new defaultBaseUrl property with a default value.", "config", "/server", "defaultBaseUrl", "http://localhost:8080/magnolia/"),
                        new WarnTask("defaultBaseUrl property", "Please set the config:/server/defaultBaseUrl property to a full URL to be used when generating absolute URLs for external systems.")
                ),

                // this is only valid when updating - if /server/login exists
                new NodeExistsDelegateTask("Login configuration", "The login configuration was moved to filters configuration.", "config", "/server/login",
                        new ArrayDelegateTask("",
                                new LoginAuthTypePropertyMovedToFilter(),
                                new LoginFormPropertyMovedToFilter(),
                                new MoveAndRenamePropertyTask("unsecuredPath is now handled by the bypass mechanism.", "/server/login", "UnsecuredPath", "/server/filters/uriSecurity/bypasses/login", "pattern"),
                                new RemoveNodeTask("Login configuration changed", "Removes /server/login as it is not used anymore.", "config", "/server/login")
                        )),

                // --- user/roles repositories related tasks
                new CreateNodeTask("Adds system folder node to users workspace", "Add system realm folder /system to users workspace.", RepositoryConstants.USERS, "/", Realm.REALM_SYSTEM.getName(), NodeTypes.Folder.NAME),
                new CreateNodeTask("Adds admin folder node to users workspace", "Add magnolia realm folder /admin to users workspace.", RepositoryConstants.USERS, "/", Realm.REALM_ADMIN.getName(), NodeTypes.Folder.NAME),

                new IsAuthorInstanceDelegateTask("URI permissions", "Introduction of URI-based security. All existing roles will have GET/POST permissions on /*.",
                        new AddURIPermissionsToAllRoles(true),
                        new AddURIPermissionsToAllRoles(false)),

                new IsAuthorInstanceDelegateTask("Anonymous role", "Anonymous role must exist.",
                        new BootstrapConditionally("", "Author permissions", "/info/magnolia/setup/author/userroles.anonymous.xml"),
                        new BootstrapConditionally("", "Public permissions", "/info/magnolia/setup/public/userroles.anonymous.xml")),

                new BootstrapConditionally("Superuser role", "Bootstraps the superuser role if needed.", "/mgnl-bootstrap/core/userroles.superuser.xml"),

                new BootstrapConditionally("Anonymous user", "Anonymous user must exist in the system realm: will move the existing one or bootstrap it.",
                        RepositoryConstants.USERS, "/anonymous", "/mgnl-bootstrap/core/users.system.anonymous.xml",
                        new ArrayDelegateTask("",
                                new MoveNodeTask("", "", RepositoryConstants.USERS, "/anonymous", "/system/anonymous", false),
                                new NewPropertyTask("Anonymous user", "Anonymous user must have a password.", RepositoryConstants.USERS, "/system/anonymous", "pswd", new String(Base64.encodeBase64("anonymous".getBytes())))
                        )),

                new BootstrapConditionally("Superuser user", "Superuser user must exist in the system realm: will move the existing one or bootstrap it.",
                        RepositoryConstants.USERS, "/superuser", "/mgnl-bootstrap/core/users.system.superuser.xml",
                        new MoveNodeTask("", "", RepositoryConstants.USERS, "/superuser", "/system/superuser", false)),

                // --- generic tasks
                new ModuleFilesExtraction(),
                new RegisterModuleServletsTask(),

                // --- system-wide tasks (impact all modules)
                new WarnIgnoredModuleFilters(),
                new UpdateURIMappings()
        );
    }

}
