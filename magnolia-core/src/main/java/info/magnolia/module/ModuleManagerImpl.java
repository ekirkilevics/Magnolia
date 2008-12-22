/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.cms.util.SystemContentWrapper;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.RepositoryDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.module.ui.ModuleManagerNullUI;
import info.magnolia.module.ui.ModuleManagerUI;
import info.magnolia.module.ui.ModuleManagerWebUI;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TODO where do we setup ModuleRegistry ?
 * TODO : factor out into simpler units
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerImpl implements ModuleManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleManagerImpl.class);

    // TODO : expose a method to retrieve a given module's node ?
    // TODO : see InstallContextImpl.getOrCreateCurrentModuleConfigNode()
    static final String MODULES_NODE = "modules";

    /**
     * List<ModuleDefinition> of modules found to be deployed.
     */
    private List orderedModuleDescriptors;

    private ModuleManagementState state;

    // here we use the implementation, since it has extra methods that should not be exposed to Task methods.
    private final InstallContextImpl installContext;

    private final ModuleRegistry registry;
    private final ModuleDefinitionReader moduleDefinitionReader;

    public ModuleManagerImpl() {
        // load all definitions from classpath
        this(new InstallContextImpl(), new BetwixtModuleDefinitionReader());
    }

    // for tests only
    protected ModuleManagerImpl(InstallContextImpl installContext, ModuleDefinitionReader moduleDefinitionReader) {
        this.installContext = installContext;
        this.moduleDefinitionReader = moduleDefinitionReader;
        this.registry = ModuleRegistry.Factory.getInstance();
    }

    public List loadDefinitions() throws ModuleManagementException {
        if (state != null) {
            throw new IllegalStateException("ModuleManager was already initialized !");
        }

        final Map moduleDefinitions = moduleDefinitionReader.readAll();
        log.debug("loaded definitions: {}", moduleDefinitions);

        final DependencyChecker dependencyChecker = new DependencyChecker();
        dependencyChecker.checkDependencies(moduleDefinitions);
        orderedModuleDescriptors = dependencyChecker.sortByDependencyLevel(moduleDefinitions);
        for (Iterator iter = orderedModuleDescriptors.iterator(); iter.hasNext();) {
            ModuleDefinition moduleDefinition = (ModuleDefinition) iter.next();
            registry.registerModuleDefinition(moduleDefinition.getName(), moduleDefinition);
        }
        return orderedModuleDescriptors;
    }

    /**
     * In difference to the contract specified by interface - checking for install or updates this method also loads repositories when there are no pending install or update tasks !!!
     *
     * @see info.magnolia.module.ModuleManager#checkForInstallOrUpdates()
     */
    public void checkForInstallOrUpdates() {
        // compare and determine if we need to do anything
        state = new ModuleManagementState();
        int taskCount = 0;
        final Iterator it = orderedModuleDescriptors.iterator();
        while (it.hasNext()) {
            final ModuleDefinition module = (ModuleDefinition) it.next();
            log.debug("checking for installation or update [{}]", module);
            final ModuleVersionHandler versionHandler = newVersionHandler(module);
            registry.registerModuleVersionHandler(module.getName(), versionHandler);
            installContext.setCurrentModule(module);

            final Version currentVersion = versionHandler.getCurrentlyInstalled(installContext);
            final List deltas = versionHandler.getDeltas(installContext, currentVersion);
            if (deltas.size() > 0) {
                state.addModule(module, currentVersion, deltas);
                final Iterator itD = deltas.iterator();
                while (itD.hasNext()) {
                    final Delta d = (Delta) itD.next();
                    taskCount += d.getTasks().size();
                }
            }
        }
        // TODO handle modules found in repo but not found on classpath

        installContext.setCurrentModule(null);
        installContext.setTotalTaskCount(taskCount);

        // if we don't have to perform any update load repositories now
        if (!state.needsUpdateOrInstall()) {
            loadModulesRepositories();
        }

        // TODO : check the force bootstrap properties
    }

    public ModuleManagementState getStatus() {
        if (state == null) {
            throw new IllegalStateException("ModuleManager was not initialized !");
        }

        return state;
    }

    public ModuleManagerUI getUI() {
        if (SystemProperty.getBooleanProperty("magnolia.update.auto")) {
            return new ModuleManagerNullUI(this);
        } else {
            return new ModuleManagerWebUI(this);
        }
    }

    protected ModuleVersionHandler newVersionHandler(ModuleDefinition module) {
        try {
            final Class versionHandlerClass = module.getVersionHandler();
            if (versionHandlerClass != null) {
                return (ModuleVersionHandler) versionHandlerClass.newInstance();
            } else {
                return new DefaultModuleVersionHandler();
            }
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public void performInstallOrUpdate() {
        synchronized (installContext) {
            if (state == null) {
                throw new IllegalStateException("ModuleManager was not initialized !");
            }
            if (!state.needsUpdateOrInstall()) {
                throw new IllegalStateException("ModuleManager has nothing to do !");
            }
            if (installContext.getStatus() != null) {
                throw new IllegalStateException("ModuleManager.performInstallOrUpdate() was already started !");
            }
            installContext.setStatus(InstallStatus.inProgress);
        }

        // check all conditions
        boolean conditionsChecked = true;
        final Iterator it = state.getList().iterator();
        while (it.hasNext()) {
            // TODO extract "do for all deltas" logic ?
            final ModuleAndDeltas moduleAndDeltas = (ModuleAndDeltas) it.next();
            installContext.setCurrentModule(moduleAndDeltas.getModule());
            final Iterator itD = moduleAndDeltas.getDeltas().iterator();
            while (itD.hasNext()) {
                final Delta d = (Delta) itD.next();
                final List conditions = d.getConditions();
                final Iterator itC = conditions.iterator();
                while (itC.hasNext()) {
                    final Condition cond = (Condition) itC.next();
                    if (!cond.check(installContext)) {
                        conditionsChecked = false;
                        installContext.info(cond.getDescription());
                    }
                }
            }
        }
        installContext.setCurrentModule(null);
        if (!conditionsChecked) {
            installContext.setStatus(InstallStatus.stoppedConditionsNotMet);
            return;
        }

        loadModulesRepositories();

        MgnlContext.doInSystemContext(new MgnlContext.SystemContextOperation() {
            public void exec() {
                final Iterator it = state.getList().iterator();
                while (it.hasNext()) {
                    final ModuleAndDeltas moduleAndDeltas = (ModuleAndDeltas) it.next();
                    installOrUpdateModule(moduleAndDeltas, installContext);
                    it.remove();
                }
            }
        }, false);

        // TODO : this isn't super clean.
        final InstallStatus status = installContext.isRestartNeeded() ? InstallStatus.installDoneRestartNeeded : InstallStatus.installDone;
        installContext.setStatus(status);
    }

    public InstallContext getInstallContext() {
        return installContext;
    }

    public void startModules() {
        // process startup tasks before actually starting modules
        executeStartupTasks();

        try {
            // here we use the implementation, since it has extra methods that should not be exposed to ModuleLifecycle methods.
            final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
            lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_SYSTEM_STARTUP);
            final HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            final Content modulesParentNode = hm.getContent(MODULES_NODE);
            final Collection moduleNodes = new ArrayList();
            final Iterator it = orderedModuleDescriptors.iterator();

            while (it.hasNext()) {

                ModuleDefinition moduleDefinition = (ModuleDefinition) it.next();
                String moduleClassName = moduleDefinition.getClassName();

                final Map moduleProperties = new HashMap();
                moduleProperties.put("moduleDefinition", moduleDefinition);
                final String moduleName = moduleDefinition.getName();
                moduleProperties.put("name", moduleName);

                if (modulesParentNode.hasContent(moduleName)) {
                    final Content moduleNode = new SystemContentWrapper(modulesParentNode.getChildByName(moduleName));
                    moduleNodes.add(moduleNode);
                    moduleProperties.put("moduleNode", moduleNode);
                    if (moduleNode.hasContent("config")) {
                        final Content configNode = new SystemContentWrapper(moduleNode.getContent("config"));
                        moduleProperties.put("configNode", configNode);
                    }
                }

                Object moduleInstance = registry.getModuleInstance(moduleName);

                if (moduleInstance == null && moduleClassName != null) {
                    moduleInstance = ClassUtil.newInstance(moduleClassName);
                    registry.registerModuleInstance(moduleName, moduleInstance);
                }

                if (moduleInstance != null) {
                    populateModuleInstance(moduleInstance, moduleProperties);

                    startModule(moduleInstance, moduleDefinition, lifecycleContext);

                    // start observation
                    ObservationUtil.registerDeferredChangeListener(ContentRepository.CONFIG, "/modules/" + moduleName + "/config", new EventListener() {

                        public void onEvent(EventIterator events) {
                            final Object moduleInstance = registry.getModuleInstance(moduleName);
                            final ModuleDefinition moduleDefinition = registry.getDefinition(moduleName);

                            // TODO we should keep only one instance of the lifecycle context
                            final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
                            lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_MODULE_RESTART);
                            MgnlContext.doInSystemContext(new MgnlContext.SystemContextOperation(){
                                public void exec() {
                                    stopModule(moduleInstance, moduleDefinition, lifecycleContext);
                                    populateModuleInstance(moduleInstance, moduleProperties);
                                    startModule(moduleInstance, moduleDefinition, lifecycleContext);
                                }
                            }, true);
                        }
                    },5000, 30000);
                }
            }
            lifecycleContext.start(moduleNodes);
        }
        catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // TODO
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * Process startup tasks. Tasks retured by <code>ModuleDefinition.getStartupTasks()</code> are always executed and
     * do not require manual intervention.
     */
    protected void executeStartupTasks() {
        MgnlContext.doInSystemContext(new MgnlContext.SystemContextOperation() {
            public void exec() {
                final Iterator it = orderedModuleDescriptors.iterator();
                while (it.hasNext()) {
                    final ModuleDefinition module = (ModuleDefinition) it.next();
                    final ModuleVersionHandler versionHandler = registry.getVersionHandler(module.getName());
                    installContext.setCurrentModule(module);
                    final Delta startup = versionHandler.getStartupDelta(installContext);
                    applyDeltas(module, Collections.singletonList(startup), installContext);
                }
            }
        }, false);
    }

    protected void startModule(Object moduleInstance, final ModuleDefinition moduleDefinition, final ModuleLifecycleContextImpl lifecycleContext) {
        if (moduleInstance instanceof ModuleLifecycle) {
            lifecycleContext.setCurrentModuleDefinition(moduleDefinition);
            log.info("starting module {}", moduleDefinition.getName());
            ((ModuleLifecycle) moduleInstance).start(lifecycleContext);
        }
    }

    protected void stopModule(Object moduleInstance, final ModuleDefinition moduleDefinition, final ModuleLifecycleContextImpl lifecycleContext) {
        if (moduleInstance instanceof ModuleLifecycle) {
            lifecycleContext.setCurrentModuleDefinition(moduleDefinition);
            log.info("stopping module {}", moduleDefinition.getName());
            ((ModuleLifecycle) moduleInstance).stop(lifecycleContext);
        }
    }

    protected void populateModuleInstance(Object moduleInstance, Map moduleProperties) {
        try {
            BeanUtils.populate(moduleInstance, moduleProperties);
        }
        catch (Exception e) {
            log.error("can't set default properties", e);
        }

        if (moduleProperties.get("configNode") != null) {
            try {
                Content2BeanUtil.setProperties(moduleInstance, (Content) moduleProperties.get("configNode"), true);
            }
            catch (Content2BeanException e) {
                log.error("wasn't able to configure module", e);
            }
        }
    }

    public void stopModules() {
        final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
        lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_SYSTEM_SHUTDOWN);
        if (orderedModuleDescriptors != null) {
            // if module descriptors were read, let's shut down modules in reverse order
            final ArrayList shutdownOrder = new ArrayList(orderedModuleDescriptors);
            Collections.reverse(shutdownOrder);
            for (Iterator iter = shutdownOrder.iterator(); iter.hasNext();) {
                ModuleDefinition md = (ModuleDefinition) iter.next();
                Object module = registry.getModuleInstance(md.getName());
                if (module instanceof ModuleLifecycle) {
                    stopModule(module, md, lifecycleContext);
                }

            }
        }
    }

    protected void installOrUpdateModule(ModuleAndDeltas moduleAndDeltas, InstallContextImpl ctx) {
        final ModuleDefinition moduleDef = moduleAndDeltas.getModule();
        final List deltas = moduleAndDeltas.getDeltas();
        ctx.setCurrentModule(moduleDef);
        log.debug("install/update for {} is starting", moduleDef);
        applyDeltas(moduleDef, deltas, ctx);
        log.debug("install/update for {} has finished", moduleDef);
    }

    /**
     * Applies to given deltas for the given module. It is NOT responsible for setting the given
     * module as being the current module in the given context, but it is responsible for unsetting
     * it when done, and for saving upon success.
     */
    protected void applyDeltas(ModuleDefinition moduleDef, List deltas, InstallContextImpl ctx) {
        boolean success = true;
        try {
            final Iterator it = deltas.iterator();
            while (it.hasNext()) {
                final Delta d = (Delta) it.next();
                final List tasks = d.getTasks();
                final Iterator itT = tasks.iterator();
                while (itT.hasNext()) {
                    final Task task = (Task) itT.next();
                    log.debug("Module {}, executing {}", moduleDef, task);
                    task.execute(ctx);
                    ctx.incExecutedTaskCount();
                }
            }
        } catch (TaskExecutionException e) {
            ctx.error("Could not install or update module. (" + e.getMessage() + ")", e);
            success = false;
        } finally {
            // TODO : ctx.info("Successful installation/update."); after save ?
            ctx.setCurrentModule(null);
        }

        saveChanges(success);
    }

    /**
     * Save changes to jcr, or revert them if something went wrong (set persist=false)
     * @param persist if <code>false</code> changes will be reverted
     */
    private void saveChanges(boolean persist) {
        // save all repositories once a module was properly installed/updated, or rollback changes.
        final Iterator reposIt = ContentRepository.getAllRepositoryNames();
        while (reposIt.hasNext()) {
            final String repoName = (String) reposIt.next();
            log.debug((persist ? "Saving" : "Rolling back") + " repository " + repoName);
            final HierarchyManager hm = MgnlContext.getHierarchyManager(repoName);
            try {
                // don't call save or refresh if useless
                if (hm.getWorkspace().getSession().hasPendingChanges()) {
                    if (persist) {
                        hm.save();
                    }
                    else {
                        hm.refresh(false);
                    }
                }
            }
            catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }

    /**
     * Initializes repositories and workspaces defined by modules.
     * Perform repository registration tasks (create repositories or workspace, setup nodetypes) that should be done
     * always before starting the new module.
     */
    private void loadModulesRepositories() {

        final Iterator it = orderedModuleDescriptors.iterator();

        while (it.hasNext()) {
            final ModuleDefinition def = (ModuleDefinition) it.next();
            // register repositories
            for (Iterator iter = def.getRepositories().iterator(); iter.hasNext();) {
                final RepositoryDefinition repDef = (RepositoryDefinition) iter.next();
                final String repositoryName = repDef.getName();

                final String nodetypeFile = repDef.getNodeTypeFile();

                List wsList = repDef.getWorkspaces();
                String[] workSpaces = (String[]) wsList.toArray(new String[wsList.size()]);

                loadRepository(repositoryName, nodetypeFile, workSpaces);
            }
        }
    }

    /**
     * Loads a single repository plus its workspaces, register nodetypes and grant permissions to superuser
     */
    private void loadRepository(String repositoryName, String nodeTypeFile, String[] workspaces) {

        RepositoryMapping rm = ContentRepository.getRepositoryMapping(repositoryName);

        if (rm == null) {

            RepositoryMapping defaultRepositoryMapping = ContentRepository.getRepositoryMapping("magnolia");
            Map defaultParamenters = defaultRepositoryMapping.getParameters();

            rm = new RepositoryMapping();
            rm.setName(repositoryName);
            rm.addWorkspace(repositoryName);
            rm.setProvider(defaultRepositoryMapping.getProvider());
            rm.setLoadOnStartup(true);

            Map parameters = new HashMap();
            parameters.putAll(defaultParamenters);

            // override changed parameters
            String bindName = repositoryName
                + StringUtils.replace((String) defaultParamenters.get("bindName"), "magnolia", "");
            String repositoryHome = StringUtils.substringBeforeLast((String) defaultParamenters.get("configFile"), "/")
                + "/"
                + repositoryName;

            parameters.put("repositoryHome", repositoryHome);
            parameters.put("bindName", bindName);
            parameters.put("customNodeTypes", nodeTypeFile);

            rm.setParameters(parameters);

            try {
                ContentRepository.loadRepository(rm);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (nodeTypeFile != null) {
            // register nodetypes
            Provider provider = ContentRepository.getRepositoryProvider(repositoryName);
            try {
                provider.registerNodeTypes(nodeTypeFile);
            }
            catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }

        if (workspaces != null) {
            for (int j = 0; j < workspaces.length; j++) {
                String workspace = workspaces[j];

                if (!rm.getWorkspaces().contains(workspace)) {
                    log.debug("Loading new workspace: {}", workspace);

                    try {
                        ContentRepository.loadWorkspace(repositoryName, workspace);
                    }
                    catch (RepositoryException e) {
                        // should never happen, the only exception we can get here is during login
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

    }
}
