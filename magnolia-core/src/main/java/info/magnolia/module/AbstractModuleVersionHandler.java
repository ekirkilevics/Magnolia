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
package info.magnolia.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.ModuleFilesExtraction;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Extend this and register your deltas in the constructor using the register method.
 * Add your own install tasks by overriding the getExtraInstallTasks() method.
 * In most cases, modules won't need to override any other method.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractModuleVersionHandler implements ModuleVersionHandler {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private final Map<Version, Delta> allDeltas;

    public AbstractModuleVersionHandler() {
        allDeltas = new TreeMap<Version, Delta>(new VersionComparator());
    }

    /**
     * Registers the delta needed to update to version v from the previous one.
     * Adds a Task to update the module version in the repository.
     */
    protected void register(Delta delta) {
        final Version v = delta.getVersion();
        if (allDeltas.containsKey(v)) {
            throw new IllegalStateException("Version " + v + " was already registered in this ModuleVersionHandler.");
        }
        delta.getTasks().addAll(getDefaultUpdateTasks(v));
        delta.getConditions().addAll(getDefaultUpdateConditions(v));
        allDeltas.put(v, delta);
    }

    public Version getCurrentlyInstalled(InstallContext ctx) {
        try {
            log.debug("checking currently installed version of module [{}]", ctx.getCurrentModuleDefinition());

            // check if this module was ever installed
            if (!ctx.hasModulesNode()) {
                return null;
            }
            final Content moduleNode = ctx.getOrCreateCurrentModuleNode();
            final NodeData versionProp = moduleNode.getNodeData("version");
            if (!versionProp.isExist()) {
                return null;
            }

            return Version.parseVersion(versionProp.getString());
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public List<Delta> getDeltas(InstallContext installContext, Version from) {
        if (from == null) {
            return Collections.singletonList(getInstall(installContext));
        }

        return getUpdateDeltas(installContext, from);
    }

    protected List<Delta> getUpdateDeltas(InstallContext installContext, Version from) {
        final List<Delta> deltas = new LinkedList<Delta>();
        for (Version v : allDeltas.keySet()) {
            if (v.isStrictlyAfter(from)) {
                deltas.add(allDeltas.get(v));
            }
        }

        // if there was no delta for the version being installed, we still need to add the default update tasks
        final Version toVersion = installContext.getCurrentModuleDefinition().getVersion();
        if (toVersion.isStrictlyAfter(from) && !allDeltas.containsKey(toVersion)) {
            deltas.add(getDefaultUpdate(installContext));
        }
        return deltas;
    }

    /**
     * @deprecated since 4.2, renamed to getDefaultUpdate(InstallContext installContext)
     */
    protected Delta getUpdate(InstallContext installContext) {
        return getDefaultUpdate(installContext);
    }

    /**
     * The minimal delta to be applied for each update, even if no delta was specifically registered
     * for the version being installed.
     */
    protected Delta getDefaultUpdate(InstallContext installContext) {
        final Version toVersion = installContext.getCurrentModuleDefinition().getVersion();
        final List<Task> defaultUpdateTasks = getDefaultUpdateTasks(toVersion);
        final List<Condition> defaultUpdateConditions = getDefaultUpdateConditions(toVersion);
        return DeltaBuilder.update(toVersion, "").addTasks(defaultUpdateTasks).addConditions(defaultUpdateConditions);
    }

    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        final List<Task> defaultUpdates = new ArrayList<Task>(2);
        defaultUpdates.add(new ModuleFilesExtraction());
        defaultUpdates.add(new ModuleVersionUpdateTask(forVersion));
        return defaultUpdates;
    }

    protected List<Condition> getDefaultUpdateConditions(Version forVersion) {
        return Collections.emptyList();
    }

    /**
     *
     * @see #getBasicInstallTasks(InstallContext) override this method if you need a different set of default install tasks.
     * @see #getExtraInstallTasks(InstallContext) override this method if you need extra tasks for install.
     */
    protected Delta getInstall(InstallContext installContext) {
        final List<Task> installTasks = new ArrayList<Task>();
        installTasks.addAll(getBasicInstallTasks(installContext));
        installTasks.addAll(getExtraInstallTasks(installContext));
        installTasks.add(new ModuleVersionToLatestTask());
        final List<Condition> conditions = getInstallConditions();
        final Version version = installContext.getCurrentModuleDefinition().getVersion();
        return DeltaBuilder.install(version, "").addTasks(installTasks).addConditions(conditions);
    }

    protected abstract List<Task> getBasicInstallTasks(InstallContext installContext);

    /**
     * Override this method to add specific install tasks to your module.
     * Returns an empty list by default.
     */
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Collections.emptyList();
    }

    protected List<Condition> getInstallConditions() {
        return Collections.emptyList();
    }

    public Delta getStartupDelta(InstallContext installContext) {
        final ModuleDefinition moduleDef = installContext.getCurrentModuleDefinition();
        final List<Task> tasks = getStartupTasks(installContext);
        return DeltaBuilder.startup(moduleDef, tasks);
    }

    /**
     * Override this method to add specific startup tasks to your module.
     * Returns an empty list by default.
     */
    protected List<Task> getStartupTasks(InstallContext installContext) {
        return Collections.emptyList();
    }

    /**
     * The task which modifies the "version" property to the version being <strong>installed</strong>.
     * (from module descriptor)
     * TODO : make this mandatory and "hidden" ?
     */
    public class ModuleVersionToLatestTask extends AbstractRepositoryTask {
        protected ModuleVersionToLatestTask() {
            super("Version number", "Sets installed module version number.");
        }

        protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
            // make sure we have the /modules node
            if (!ctx.hasModulesNode()) {
                final HierarchyManager hm = ctx.getConfigHierarchyManager();
                hm.createContent("/", ModuleManagerImpl.MODULES_NODE, ItemType.CONTENT.getSystemName());
            }

            final Content moduleNode = ctx.getOrCreateCurrentModuleNode();
            final NodeData nodeData = NodeDataUtil.getOrCreate(moduleNode, "version");
            nodeData.setValue(getVersion(ctx).toString());
        }

        protected Version getVersion(InstallContext ctx) {
            return ctx.getCurrentModuleDefinition().getVersion();
        }
    }

    /**
     * The task which modifies the "version" property to the version we're <strong>updating</strong> to.
     */
    public class ModuleVersionUpdateTask extends ModuleVersionToLatestTask {
        private final Version toVersion;

        protected ModuleVersionUpdateTask(Version toVersion) {
            super();
            this.toVersion = toVersion;
        }

        protected Version getVersion(InstallContext ctx) {
            return toVersion;
        }
    }

}
