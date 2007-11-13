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
package info.magnolia.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.delta.*;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionComparator;
import info.magnolia.module.model.ModuleDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this and register your deltas in the constructor using the register method.
 *
 * TODO : how do we handle tasks which have to be executed for every update ?
 * (like ModuleFilesExtraction(), since it takes care of not overwriting existing files)
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractModuleVersionHandler implements ModuleVersionHandler {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private final Map allDeltas; // <Version, Delta>

    public AbstractModuleVersionHandler() {
        allDeltas = new TreeMap(new VersionComparator());
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
        delta.getTasks().add(new ModuleVersionUpdateTask(v));
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

    public List getDeltas(InstallContext installContext, Version from) {
        if (from == null) {
            return Collections.singletonList(getInstall(installContext));
        }

        return getUpdateDeltas(from);
    }

    protected List getUpdateDeltas(Version from) {
        final List deltas = new LinkedList();
        final Iterator it = allDeltas.keySet().iterator();
        while (it.hasNext()) {
            final Version v = (Version) it.next();

            if (v.isStrictlyAfter(from)) {
                deltas.add(allDeltas.get(v));
            }
        }
        return deltas;
    }

    // TODO : review / validate
    /**
     * Returns a delta that will execute a common module installation:
     * register repositories, nodetypes and workspaces as stated in the
     * module definition, bootstrap the module's mgnl-bootstrap files and
     * extract the module's mgnl-files files.
     * This method should generally not be overridden.
     *
     * @see #getBasicInstallTasks(InstallContext) override this method if you need a different set of default install tasks.
     * @see #getExtraInstallTasks(InstallContext) override this method if you need extra tasks for install.
     */
    protected Delta getInstall(InstallContext installContext) {
        final List installTasks = new ArrayList();
        installTasks.addAll(getBasicInstallTasks(installContext));
        installTasks.addAll(getExtraInstallTasks(installContext));
        installTasks.add(new ModuleVersionToLatestTask());
        final List conditions = getInstallConditions();
        final Version version = installContext.getCurrentModuleDefinition().getVersionDefinition();
        return DeltaBuilder.install(version, "").addTasks(installTasks).addConditions(conditions);
    }

    protected abstract List getBasicInstallTasks(InstallContext installContext);

    /**
     * Override this method to add specific install tasks to your module.
     * Returns an empty list by default.
     */
    protected List getExtraInstallTasks(InstallContext installContext) {
        return Collections.EMPTY_LIST;
    }

    protected List getInstallConditions() {
        return Collections.EMPTY_LIST;
    }

    public Delta getStartupDelta(InstallContext installContext) {
        final ModuleDefinition moduleDef = installContext.getCurrentModuleDefinition();
        final List tasks = getStartupTasks(installContext);
        return DeltaBuilder.startup(moduleDef, tasks);
    }

    /**
     * Override this method to add specific startup tasks to your module.
     * Returns an empty list by default.
     */
    protected List getStartupTasks(InstallContext installContext) {
        return Collections.EMPTY_LIST;
    }

    // TODO : make this mandatory and "hidden" ?
    public class ModuleVersionToLatestTask extends AbstractRepositoryTask {
        protected ModuleVersionToLatestTask() {
            super("Version number", "Sets installed module version number");
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
            return ctx.getCurrentModuleDefinition().getVersionDefinition();
        }
    }

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
