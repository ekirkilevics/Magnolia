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
package info.magnolia.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.BasicDelta;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.RepositoryException;

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
    private final Map allDeltas; // <Version, Delta>

    public AbstractModuleVersionHandler() {
        allDeltas = new TreeMap(new VersionComparator());
    }

    /**
     * Registers the delta needed to update to version v from the previous one.
     * Adds a Task to update the module version in the repository.
     */
    protected void register(Version v, Delta delta) {
        if (allDeltas.containsKey(v)) {
            throw new IllegalStateException("Version " + v + " was already registered in this ModuleVersionHandler.");
        }
        delta.getTasks().add(new ModuleVersionUpdateTask(v));
        allDeltas.put(v, delta);
    }

    /**
     * A convenience method to register version deltas by version String.
     * @see #register(info.magnolia.module.model.Version,info.magnolia.module.delta.Delta)
     */
    protected void register(String versionStr, Delta delta) {
        register(new Version(versionStr), delta);
    }

    public Version getCurrentlyInstalled(InstallContext ctx) {
        try {
            // check if this module was ever installed
            if (!ctx.hasModulesNode()) {
                return null;
            }
            final Content moduleNode = ctx.getOrCreateCurrentModuleNode();
            final NodeData versionProp = moduleNode.getNodeData("version");
            if (!versionProp.isExist()) {
                return null;
            }

            return new Version(versionProp.getString());
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public List getDeltas(InstallContext installContext, Version from) {
        if (from == null) {
            return Collections.singletonList(getInstall(installContext));
        }

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
     * @see #getBasicInstallTasks() override this method if you need a different set of default install tasks.
     * @see #getExtraInstallTasks() override this method if you need extra tasks for install.
     */
    protected Delta getInstall(InstallContext installContext) {
        final List basicInstallTasks = new ArrayList();
        basicInstallTasks.addAll(getBasicInstallTasks(installContext));
        basicInstallTasks.addAll(getExtraInstallTasks(installContext));
        basicInstallTasks.add(new ModuleVersionToLatestTask());

        return new BasicDelta("Installation", "", basicInstallTasks);
    }

    protected abstract List getBasicInstallTasks(InstallContext installContext);

    /**
     * Override this method to add specific install tasks to your module.
     * Returns an empty list by default.
     */
    protected List getExtraInstallTasks(InstallContext installContext) {
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
