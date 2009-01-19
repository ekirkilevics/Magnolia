/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
import info.magnolia.module.AbstractModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.WebXmlConditionsUtil;
import info.magnolia.module.delta.WorkspaceXmlConditionsUtil;
import info.magnolia.setup.for3_5.GenericTasks;
import info.magnolia.setup.for3_6.CheckMagnoliaDevelopProperty;
import info.magnolia.setup.for3_6.CheckNodeTypesDefinition;
import info.magnolia.setup.for3_6.CheckNodesForMixVersionable;
import info.magnolia.setup.for3_6_2.UpdateGroups;
import info.magnolia.setup.for3_6_2.UpdateRoles;
import info.magnolia.setup.for3_6_2.UpdateUsers;

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

    private final Task updateLinkResolverClass = new CheckAndModifyPropertyValueTask("Link rendering", "Update link rendering implementation class",ContentRepository.CONFIG, "/server/rendering/linkResolver","class", "info.magnolia.cms.link.LinkResolverImpl","info.magnolia.link.LinkTransformerManager");
    private final Task renameLinkResolver = new MoveNodeTask("Link management configuration", "Updates rendering configuration to new link management.", ContentRepository.CONFIG, "/server/rendering/linkResolver", "/server/rendering/linkManagement", true);

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

        register(DeltaBuilder.update("4.0", "")
                .addTask(auditTrailManagerTask)
                .addTask(bootstrapFreemarker)
                .addTask(updateLinkResolverClass )
                .addTask(renameLinkResolver )
        );
    }

    protected List getBasicInstallTasks(InstallContext ctx) {
        List l = new ArrayList();
        l.addAll(GenericTasks.genericTasksFor35());
        l.add(new CheckMagnoliaDevelopProperty());
        l.add(new CheckNodesForMixVersionable());
        l.add(auditTrailManagerTask);
        l.add(bootstrapFreemarker);
        return l;
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
        u.listenerIsDeprecated("info.magnolia.cms.servlets.PropertyInitializer", "info.magnolia.cms.servlets.MgnlServletContextListener");
        u.listenerIsDeprecated("info.magnolia.cms.beans.config.ShutdownManager", "info.magnolia.cms.servlets.MgnlServletContextListener");
        final WorkspaceXmlConditionsUtil u2 = new WorkspaceXmlConditionsUtil(conditions);
        u2.workspaceHasOldIndexer();

        conditions.add(new CheckNodeTypesDefinition());
        return conditions;
    }
}
