/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.filters.ContextFilter;
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddMimeMappingTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.FilterOrderingTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.PropertyValueDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.delta.WebXmlConditionsUtil;
import info.magnolia.module.delta.WorkspaceXmlConditionsUtil;
import info.magnolia.setup.for3_5.GenericTasks;
import info.magnolia.setup.for3_6.CheckMagnoliaDevelopProperty;
import info.magnolia.setup.for3_6.CheckNodeTypesDefinition;
import info.magnolia.setup.for3_6.CheckNodesForMixVersionable;
import info.magnolia.setup.for3_6_2.UpdateGroups;
import info.magnolia.setup.for3_6_2.UpdateRoles;
import info.magnolia.setup.for3_6_2.UpdateUsers;
import info.magnolia.setup.for4_3.UpdateUserPermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * 3.5 being the first version of core as a module, it is always "installed",
 * but we need it to behave differently if magnolia was installed previously
 * (ie. updating from 3.0), which is why there are so many "conditional
 * tasks". Once 3.5 is out the door, this will need to be revised
 * completely.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CoreModuleVersionHandler extends AbstractModuleVersionHandler {
    public static final String BOOTSTRAP_AUTHOR_INSTANCE_PROPERTY = "magnolia.bootstrap.authorInstance";

    // TODO : why is this a BootstrapConditionally and not a BootstrapSingleResource ?
    private final BootstrapConditionally auditTrailManagerTask = new BootstrapConditionally("New auditory log configuration", "Install new configuration for auditory log manager.", "/mgnl-bootstrap/core/config.server.auditLogging.xml");
    private final BootstrapSingleResource bootstrapFreemarker = new BootstrapSingleResource("Freemarker configuration", "Freemarker template loaders can now be configured in Magnolia. Adds default configuration", "/mgnl-bootstrap/core/config.server.rendering.freemarker.xml");
    private final CreateNodeTask addFreemarkerSharedVariables = new CreateNodeTask("Freemarker configuration", "Adds sharedVariables node to the Freemarker configuration",
            ContentRepository.CONFIG, "/server/rendering/freemarker", "sharedVariables", ItemType.CONTENTNODE.getSystemName());

    public CoreModuleVersionHandler() {
        super();

        register(DeltaBuilder.update("3.5", "")
                .addTasks(GenericTasks.genericTasksFor35())
        );

        register(DeltaBuilder.update("3.6", "")
                .addCondition(new CheckNodeTypesDefinition())
                .addTask(new CheckMagnoliaDevelopProperty())
                .addTask(new CheckNodesForMixVersionable())
        );

        final CheckAndModifyPropertyValueTask log4jServletMapping = new CheckAndModifyPropertyValueTask("Mapping for log4j configuration servlet", "Fixes the mapping for the log4j configuration servlet, making it specification compliant.",
                ContentRepository.CONFIG,
                "/server/filters/servlets/log4j/mappings/--magnolia-log4j-",
                "pattern", "/.magnolia/log4j*", "/.magnolia/log4j"
        );

        register(DeltaBuilder.update("3.6.2", "")
                .addTask(new UpdateUsers())
                .addTask(new UpdateRoles())
                .addTask(new UpdateGroups())
                .addTask(log4jServletMapping)
        );

        register(DeltaBuilder.update("3.6.4", "")
                .addTask(new AddMimeMappingTask("flv", "video/x-flv", "/.resources/file-icons/flv.png"))
                .addTask(new AddMimeMappingTask("svg", "image/svg+xml", "/.resources/file-icons/svg.png"))
                .addTask(new CheckAndModifyPropertyValueTask("PNG MIME mapping", "Checks and updates PNG MIME mapping if not correct.", ContentRepository.CONFIG, "/server/MIMEMapping/png", "mime-type", "application/octet-stream", "image/png"))
                .addTask(new CheckAndModifyPropertyValueTask("SWF MIME mapping", "Checks and updates SWF MIME mapping if not correct.", ContentRepository.CONFIG, "/server/MIMEMapping/swf", "mime-type", "application/octet-stream", "application/x-shockwave-flash"))
        );

        register(DeltaBuilder.update("3.6.7", "")
                // since these mimetypes were correct with the 3.6.4 release, but with wrong values, and this
                // has only been recognized after 4.0.1 and 4.1 were released, we need to apply the same
                // fix tasks for 3.6.7, 4.0.2 and 4.1.1
                .addTask(fixMimetype("png", "image/png;", "image/png"))
                .addTask(fixMimetype("swf", "application/x-shockwave-flash;", "application/x-shockwave-flash"))
        );

        final Task updateLinkResolverClass = new CheckAndModifyPropertyValueTask("Link rendering", "Updates the link rendering implementation class.", ContentRepository.CONFIG, "/server/rendering/linkResolver", "class", "info.magnolia.cms.link.LinkResolverImpl", "info.magnolia.link.LinkTransformerManager");
        final Task renameLinkResolver = new MoveNodeTask("Link management configuration", "Updates the link management configuration.", ContentRepository.CONFIG, "/server/rendering/linkResolver", "/server/rendering/linkManagement", true);
        register(DeltaBuilder.update("4.0", "")
                .addTask(auditTrailManagerTask)
                .addTask(bootstrapFreemarker)
                .addTask(updateLinkResolverClass)
                .addTask(renameLinkResolver)
                .addTask(new ChangeNodeTypesInUserWorkspace())
        );

        register(DeltaBuilder.update("4.0.2", "")
                // the two tasks below - updateUsers and CAMPVT(Fix anonymous permissions) are also executed for 3.6.6 set on 3.6 branch. This is due to fact that tasks needed to be applied on 3.6 branch after 4.0 release already. Tasks are safe to be executed multiple times.
                .addTask(new UpdateUsers())
                .addTask(new CheckAndModifyPropertyValueTask("Fix for anonymous user permissions", "Fix previously incorrect path for anonymous user permissions.", ContentRepository.USERS, "/system/anonymous/acl_users/0", "path", "/anonymous/*", "/system/anonymous/*"))

                // warning - if adding update tasks for the 4.0.2 release below this point,
                // make sure they'd also be applied correctly for the 4.1 branch in the various possible update scenarios
                // since 4.1 will be released before 4.0.2 (4.0.2 -> 4.1 -> 4.1.1; 4.0.1 -> 4.1 -> 4.1.1)
        );

        register(DeltaBuilder.update("4.0.3", "")

                // since these mimetypes were correct with the 3.6.4 release, but with wrong values, and this
                // has only been recognized after 4.0.1 and 4.1 were released, we need to apply the same
                // fix tasks for 3.6.7, 4.0.3 and 4.1.1
                .addTask(fixMimetype("png", "image/png;", "image/png"))
                .addTask(fixMimetype("swf", "application/x-shockwave-flash;", "application/x-shockwave-flash"))
        );

        register(DeltaBuilder.update("4.1.1", "")
                // since these mimetypes were correct with the 3.6.4 release, but with wrong values, and this
                // has only been recognized after 4.0.1 and 4.1 were released, we need to apply the same
                // fix tasks for 3.6.7, 4.0.2 and 4.1.1
                .addTask(fixMimetype("png", "image/png;", "image/png"))
                .addTask(fixMimetype("swf", "application/x-shockwave-flash;", "application/x-shockwave-flash"))
        );

        register(DeltaBuilder.update("4.3", "")
                .addTask(addFreemarkerSharedVariables)
                .addTask(
                new ArrayDelegateTask("New unicode normalization filter", "Add the new unicode normalization filter.",
                        new BootstrapSingleResource("Unicode Normalization filter ", "Add new Unicode Normalization filter.", "/mgnl-bootstrap/core/config.server.filters.unicodeNormalization.xml"),
                        new FilterOrderingTask("multipartRequest", "New filter ordering : context, contentType, multipart, unicodeNormalization, login.", new String[]{"contentType"}),
                        new FilterOrderingTask("unicodeNormalization", "New filter ordering : context, contentType, multipart, unicodeNormalization, login.", new String[]{"multipartRequest"})
                ))
                .addTask(new UpdateUserPermissions())
        );
        
        register(DeltaBuilder.update("4.3.6", "")
                .addTask(new NodeExistsDelegateTask("TemplateExceptionHandler", "Checks if templateExceptionHandler node exists", ContentRepository.CONFIG, "/server/rendering/freemarker/templateExceptionHandler", new WarnTask("TemplateExceptionHandler", "Unable to set node templateExceptionHandler because it already exists"), new CreateNodeTask("TemplateExceptionHandler", "Creates node templateExceptionHandler", ContentRepository.CONFIG, "/server/rendering/freemarker", "templateExceptionHandler", ItemType.CONTENTNODE.getSystemName())))
                .addTask(new PropertyExistsDelegateTask("Class", "Checks if class property exists", ContentRepository.CONFIG, "/server/rendering/freemarker/templateExceptionHandler", "class", new WarnTask("class","Unable to set property class because it already exists"),  new NewPropertyTask("Class", "Creates property class and sets it to class path", ContentRepository.CONFIG, "/server/rendering/freemarker/templateExceptionHandler", "class", "info.magnolia.freemarker.ModeDependentTemplateExceptionHandler"))));

        register(DeltaBuilder.update("4.4", "")
                .addTask(new RemoveNodeTask("Remove context filter", "Removes the context filter from filter chain.", ContentRepository.CONFIG, "/server/filters/context")));
    }

    private PropertyValueDelegateTask fixMimetype(String mimeType, final String previouslyWrongValue, final String fixedValue) {
        final String workspaceName = ContentRepository.CONFIG;
        final String nodePath = "/server/MIMEMapping/" + mimeType;
        final CheckAndModifyPropertyValueTask fixTask = new CheckAndModifyPropertyValueTask(null, null, workspaceName, nodePath,
                "mime-type", previouslyWrongValue, fixedValue);

        return new PropertyValueDelegateTask(mimeType.toUpperCase() + " MIME mapping",
                "Checks and updates " + mimeType.toUpperCase() + "MIME mapping if not correct.",
                workspaceName, nodePath, "mime-type", previouslyWrongValue, false, fixTask);
    }

    protected List<Task> getBasicInstallTasks(InstallContext ctx) {
        final List<Task> l = new ArrayList<Task>();
        l.addAll(GenericTasks.genericTasksFor35());
        l.add(new CheckMagnoliaDevelopProperty());
        l.add(new CheckNodesForMixVersionable());
        l.add(auditTrailManagerTask);
        l.add(bootstrapFreemarker);
        l.add(addFreemarkerSharedVariables);
        return l;
    }

    protected List<Condition> getInstallConditions() {
        final ArrayList<Condition> conditions = new ArrayList<Condition>();
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
        u.listenerIsDeprecated("info.magnolia.cms.servlets.PropertyInitializer", "info.magnolia.cms.servlets.MgnlServletContextListener");
        u.listenerIsDeprecated("info.magnolia.cms.beans.config.ShutdownManager", "info.magnolia.cms.servlets.MgnlServletContextListener");
        u.contextFilterMustBeRegisteredWithCorrectDispatchers(ContextFilter.class.getName());
        final WorkspaceXmlConditionsUtil u2 = new WorkspaceXmlConditionsUtil(conditions);
        u2.workspaceHasOldIndexer();

        conditions.add(new CheckNodeTypesDefinition());
        return conditions;
    }
}
