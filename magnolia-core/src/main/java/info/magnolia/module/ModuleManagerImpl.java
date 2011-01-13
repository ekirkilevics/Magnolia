/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
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
import info.magnolia.module.model.reader.DependencyCheckerImpl;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.module.ui.ModuleManagerNullUI;
import info.magnolia.module.ui.ModuleManagerUI;
import info.magnolia.module.ui.ModuleManagerWebUI;
import info.magnolia.objectfactory.ClassFactory;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.MgnlInstantiationException;
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryMapping;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

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
 * TODO : factor out into simpler units.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleManagerImpl implements ModuleManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleManagerImpl.class);

    private static final int DEFAULT_MODULE_OBSERVATION_DELAY = 5000;
    private static final int DEFAULT_MODULE_OBSERVATION_MAX_DELAY = 30000;

    // TODO : expose a method to retrieve a given module's node ?
    // TODO : see InstallContextImpl.getOrCreateCurrentModuleConfigNode()
    static final String MODULES_NODE = "modules";

    /**
     * List<ModuleDefinition> of modules found to be deployed.
     */
    private List<ModuleDefinition> orderedModuleDescriptors;

    private ModuleManagementState state;

    // here we use the implementation, since it has extra methods that should not be exposed to Task methods.
    private final InstallContextImpl installContext;

    private final ModuleRegistry registry;
    private final ModuleDefinitionReader moduleDefinitionReader;
    private final DependencyChecker dependencyChecker;

    /**
     * @deprecated since 5.0 - use IoC - temporarily kept for tests ?
     */
    protected ModuleManagerImpl() {
        // load all definitions from classpath
        this(new InstallContextImpl(), new BetwixtModuleDefinitionReader());
    }

    /**
     * @deprecated since 5.0 - use IoC - temporarily kept for tests ?
     */
    protected ModuleManagerImpl(InstallContextImpl installContext, ModuleDefinitionReader moduleDefinitionReader) {
        this(installContext, moduleDefinitionReader, ModuleRegistry.Factory.getInstance(), new DependencyCheckerImpl());
    }

    public ModuleManagerImpl(InstallContextImpl installContext, ModuleDefinitionReader moduleDefinitionReader, ModuleRegistry moduleRegistry, DependencyChecker dependencyChecker) {
        this.installContext = installContext;
        this.moduleDefinitionReader = moduleDefinitionReader;
        this.registry = moduleRegistry;
        this.dependencyChecker = dependencyChecker;
    }

    public List<ModuleDefinition> loadDefinitions() throws ModuleManagementException {
        if (state != null) {
            throw new IllegalStateException("ModuleManager was already initialized !");
        }

        final Map<String, ModuleDefinition> moduleDefinitions = moduleDefinitionReader.readAll();
        if (moduleDefinitions.isEmpty()) {
            throw new ModuleManagementException("No module definition was found.");
        }
        log.debug("Loaded definitions: {}", moduleDefinitions);

        dependencyChecker.checkDependencies(moduleDefinitions);
        orderedModuleDescriptors = dependencyChecker.sortByDependencyLevel(moduleDefinitions);
        for (ModuleDefinition moduleDefinition : orderedModuleDescriptors) {
            registry.registerModuleDefinition(moduleDefinition.getName(), moduleDefinition);
        }
        return orderedModuleDescriptors;
    }

    /**
     * In addition to checking for install or updates, this method also loads
     * repositories when there are no pending install or update tasks.
     *
     * @see info.magnolia.module.ModuleManager#checkForInstallOrUpdates()
     */
    public void checkForInstallOrUpdates() {
        // compare and determine if we need to do anything
        state = new ModuleManagementState();
        int taskCount = 0;
        for (ModuleDefinition module : orderedModuleDescriptors) {
            installContext.setCurrentModule(module);
            log.debug("Checking for installation or update [{}]", module);
            final ModuleVersionHandler versionHandler = newVersionHandler(module);
            registry.registerModuleVersionHandler(module.getName(), versionHandler);

            final Version currentVersion = versionHandler.getCurrentlyInstalled(installContext);
            final List<Delta> deltas = versionHandler.getDeltas(installContext, currentVersion);
            if (deltas.size() > 0) {
                state.addModule(module, currentVersion, deltas);
                for (Delta delta : deltas) {
                    taskCount += delta.getTasks().size();
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
            final Class<? extends ModuleVersionHandler> versionHandlerClass = module.getVersionHandler();
            if (versionHandlerClass != null) {
                return Classes.getClassFactory().newInstance(versionHandlerClass);
            } else {
                return new DefaultModuleVersionHandler();
            }
        } catch (MgnlInstantiationException e) {
            throw e; // TODO
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
        for (ModuleAndDeltas moduleAndDeltas : state.getList()) {
            // TODO extract "do for all deltas" logic ?
            installContext.setCurrentModule(moduleAndDeltas.getModule());
            for (Delta delta : moduleAndDeltas.getDeltas()) {
                final List<Condition> conditions = delta.getConditions();
                for (Condition cond : conditions) {
                    if (!cond.check(installContext)) {
                        conditionsChecked = false;
                        installContext.warn(cond.getDescription());
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

        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            public void doExec() {
                final Iterator<ModuleAndDeltas> it = state.getList().iterator();
                while (it.hasNext()) {
                    final ModuleAndDeltas moduleAndDeltas = it.next();
                    installOrUpdateModule(moduleAndDeltas, installContext);
                    it.remove();
                }
            }
        }, true);

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

        // here we use the implementation, since it has extra methods that should not be exposed to ModuleLifecycle methods.
        final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
        lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_SYSTEM_STARTUP);
        final HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);
        Content modulesParentNode;
        try {
            modulesParentNode = hm.getContent(MODULES_NODE);
        }
        catch (RepositoryException e) {
            throw new RuntimeException("Can't start module due to failing to load the /modules node.",e);
        }
        final Collection<Content> moduleNodes = new ArrayList<Content>();

        for (ModuleDefinition moduleDefinition : orderedModuleDescriptors) {
            final String moduleClassName = moduleDefinition.getClassName();
            final String moduleName = moduleDefinition.getName();
            log.info("Initializing module {}", moduleName);

            try {
                // TODO : why would this return anything else than null ?
                Object moduleInstance = registry.getModuleInstance(moduleName);

                if (moduleInstance == null && moduleClassName != null) {
                    try {
                        final ClassFactory classFactory = Classes.getClassFactory();
                        final Class<?> moduleClass = classFactory.forName(moduleClassName);
                        moduleInstance = classFactory.newInstance(moduleClass);
// TODO ? -- the module should be in a container  moduleInstance = Components.getSingleton(moduleClass);
                    } catch (Throwable t) {
                        log.error("Can't instantiate " + moduleClassName + " for module " + moduleName + " : " + t.getClass() + " : " + t.getMessage(), t);
                        continue;
                    }
                    registry.registerModuleInstance(moduleName, moduleInstance);
                }

                // Prepare properties for module instances; if the bean has "moduleDefinition",
                // "name", "moduleNode" or "configNode" properties, they will be populated accordingly.
                final Map<String, Object> moduleProperties = new HashMap<String, Object>();
                moduleProperties.put("moduleDefinition", moduleDefinition);
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
                            MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                                public void doExec() {
                                    stopModule(moduleInstance, moduleDefinition, lifecycleContext);
                                    populateModuleInstance(moduleInstance, moduleProperties);
                                    startModule(moduleInstance, moduleDefinition, lifecycleContext);
                                }
                            }, true);
                        }
                    }, DEFAULT_MODULE_OBSERVATION_DELAY, DEFAULT_MODULE_OBSERVATION_MAX_DELAY);
                }
            }
            catch (Throwable th) {
                log.error("Can't start module " + moduleName, th);
            }
        }
        
        lifecycleContext.start(moduleNodes);
    }

    /**
     * Process startup tasks. Tasks retured by <code>ModuleDefinition.getStartupTasks()</code> are always executed and
     * do not require manual intervention.
     */
    protected void executeStartupTasks() {
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
            public void doExec() {
                for (ModuleDefinition module : orderedModuleDescriptors) {
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
            log.info("Starting module {}", moduleDefinition.getName());
            ((ModuleLifecycle) moduleInstance).start(lifecycleContext);
        }
    }

    protected void stopModule(Object moduleInstance, final ModuleDefinition moduleDefinition, final ModuleLifecycleContextImpl lifecycleContext) {
        if (moduleInstance instanceof ModuleLifecycle) {
            lifecycleContext.setCurrentModuleDefinition(moduleDefinition);
            log.info("Stopping module {}", moduleDefinition.getName());
            ((ModuleLifecycle) moduleInstance).stop(lifecycleContext);
        }
    }

    protected void populateModuleInstance(Object moduleInstance, Map<String, Object> moduleProperties) {
        try {
            BeanUtils.populate(moduleInstance, moduleProperties);
        }
        catch (Throwable e) {
            log.error("Can't initialize module " + moduleInstance + ": " + e.getMessage(), e);
        }

        if (moduleProperties.get("configNode") != null) {
            try {
                Content2BeanUtil.setProperties(moduleInstance, (Content) moduleProperties.get("configNode"), true);
            }
            catch (Content2BeanException e) {
                log.error("Wasn't able to configure module " + moduleInstance + ": " + e.getMessage(), e);
            }
        }
    }

    public void stopModules() {
        final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
        lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_SYSTEM_SHUTDOWN);
        if (orderedModuleDescriptors != null) {
            // if module descriptors were read, let's shut down modules in reverse order
            final ArrayList<ModuleDefinition> shutdownOrder = new ArrayList<ModuleDefinition>(orderedModuleDescriptors);
            Collections.reverse(shutdownOrder);
            for (ModuleDefinition md : shutdownOrder) {
                Object module = registry.getModuleInstance(md.getName());
                if (module instanceof ModuleLifecycle) {
                    stopModule(module, md, lifecycleContext);
                }

            }
        }
    }

    protected void installOrUpdateModule(ModuleAndDeltas moduleAndDeltas, InstallContextImpl ctx) {
        final ModuleDefinition moduleDef = moduleAndDeltas.getModule();
        final List<Delta> deltas = moduleAndDeltas.getDeltas();
        ctx.setCurrentModule(moduleDef);
        log.debug("Install/update for {} is starting: {}", moduleDef, moduleAndDeltas);
        applyDeltas(moduleDef, deltas, ctx);
        log.debug("Install/update for {} has finished", moduleDef, moduleAndDeltas);
    }

    /**
     * Applies to given deltas for the given module. It is NOT responsible for setting the given
     * module as being the current module in the given context, but it is responsible for unsetting
     * it when done, and for saving upon success.
     */
    protected void applyDeltas(ModuleDefinition moduleDef, List<Delta> deltas, InstallContextImpl ctx) {
        boolean success = true;
        Task currentTask = null;
        try {
            for (Delta delta : deltas) {
                final List<Task> tasks = delta.getTasks();
                for (Task task : tasks) {
                    currentTask = task;
                    log.debug("Module {}, executing {}", moduleDef, currentTask);
                    task.execute(ctx);
                    ctx.incExecutedTaskCount();
                }
            }
        } catch (TaskExecutionException e) {
            ctx.error("Could not install or update " + moduleDef.getName() + " module. Task '" + currentTask.getName() + "' failed. (" + ExceptionUtils.getRootCauseMessage(e) + ")", e);
            success = false;
        } catch (RuntimeException e) {
            ctx.error("Error while installing or updating " + moduleDef.getName() + " module. Task '" + currentTask.getName() + "' failed. (" + ExceptionUtils.getRootCauseMessage(e) + ")", e);
            throw e;
        } finally {
            // TODO : ctx.info("Successful installation/update."); after save ?
            ctx.setCurrentModule(null);
        }

        saveChanges(success);
    }

    /**
     * Save changes to jcr, or revert them if something went wrong.
     * @param persist if <code>true</code>, all workspaces are save; if <code>false</code> changes will be reverted.
     */
    private void saveChanges(boolean persist) {
        // save all repositories once a module was properly installed/updated, or rollback changes.
        final Iterator<String> reposIt = ContentRepository.getAllRepositoryNames();
        while (reposIt.hasNext()) {
            final String repoName = reposIt.next();
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
        for (ModuleDefinition def : orderedModuleDescriptors) {
            // register repositories
            for (final RepositoryDefinition repDef : def.getRepositories()) {
                final String repositoryName = repDef.getName();

                final String nodetypeFile = repDef.getNodeTypeFile();

                final List<String> wsList = repDef.getWorkspaces();
                String[] workSpaces = wsList.toArray(new String[wsList.size()]);

                loadRepository(repositoryName, nodetypeFile, workSpaces);
            }
        }
    }

    /**
     * Loads a single repository plus its workspaces, register nodetypes and grant permissions to superuser.
     */
    private void loadRepository(String repositoryNameFromModuleDescriptor, String nodeTypeFile, String[] workspaces) {

        if (workspaces == null || workspaces.length == 0)
        {
            log.error("Trying to register the repository {} without any workspace.", repositoryNameFromModuleDescriptor);
            return;
        }

        final String DEFAULT_REPOSITORY_NAME = "magnolia";
        String repositoryName = repositoryNameFromModuleDescriptor;

        if (workspaces.length > 0) {
            // get the repository name from the mapping, users may want to manually add it here if needed
            RepositoryMapping repositoryMapping = ContentRepository.getRepositoryMapping(workspaces[0]);
            if (repositoryMapping != null) {
                repositoryName = repositoryMapping.getName();
            }
        }

        RepositoryMapping rm = ContentRepository.getRepositoryMapping(repositoryName);

        if (rm == null) {

            final RepositoryMapping defaultRepositoryMapping = ContentRepository.getRepositoryMapping(DEFAULT_REPOSITORY_NAME);
            final Map<String, String> defaultParameters = defaultRepositoryMapping.getParameters();

            rm = new RepositoryMapping();
            rm.setName(repositoryName);
            rm.setProvider(defaultRepositoryMapping.getProvider());
            rm.setLoadOnStartup(true);

            final Map<String, String> parameters = new HashMap<String, String>();
            parameters.putAll(defaultParameters);

            // override changed parameters
            final String bindName = repositoryName + StringUtils.replace(defaultParameters.get("bindName"), "magnolia", "");
            final String repositoryHome = StringUtils.substringBeforeLast(defaultParameters.get("configFile"), "/")
                + "/"
                + repositoryName;

            parameters.put("repositoryHome", repositoryHome);
            parameters.put("bindName", bindName);
            parameters.put("customNodeTypes", nodeTypeFile);

            rm.setParameters(parameters);

            try {
                ContentRepository.loadRepository(rm);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (nodeTypeFile != null) {
            // register nodetypes
            registerNodeTypeFile(repositoryName, nodeTypeFile);
            // if this repo is not the default one, register nodetypes on default repo (MAGNOLIA-3189)
            if (!DEFAULT_REPOSITORY_NAME.equals(repositoryName)) {
                registerNodeTypeFile(DEFAULT_REPOSITORY_NAME, nodeTypeFile);
            }
        }

        if (workspaces != null) {
            for (String workspace : workspaces) {
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
    
    /**
     * Register nodeType file in repository.
     * @param repositoryName repository name
     * @param nodeTypeFile nodeType file
     */
    private void registerNodeTypeFile(String repositoryName, String nodeTypeFile) {
        Provider provider = ContentRepository.getRepositoryProvider(repositoryName);
        try {
            provider.registerNodeTypes(nodeTypeFile);
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
    }
}
