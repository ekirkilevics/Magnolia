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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.RepositoryDefinition;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import info.magnolia.module.ui.ModuleManagerUI;
import info.magnolia.module.ui.ModuleManagerNullUI;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TODO where do we setup ModuleRegistry ? TODO where do we setup module configs observation ? TODO : factor out into
 * simpler units
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

    private ModuleRegistry registry = ModuleRegistry.Factory.getInstance();

    public ModuleManagerImpl() {
        this.installContext = new InstallContextImpl();
    }

    public List loadDefinitions() throws ModuleManagementException {
        if (state != null) {
            throw new IllegalStateException("ModuleManager was already initialized !");
        }

        // load all definitions from classpath
        final ModuleDefinitionReader moduleDefinitionReader = new BetwixtModuleDefinitionReader();
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

    public void checkForInstallOrUpdates() throws ModuleManagementException {
        // compare and determine if we need to do anything
        state = new ModuleManagementState();
        final Iterator it = orderedModuleDescriptors.iterator();
        while (it.hasNext()) {
            final ModuleDefinition module = (ModuleDefinition) it.next();
            log.debug("checking for installation or update [{}]", module);
            final ModuleVersionHandler versionHandler = getVersionHandler(module);
            registry.registerModuleVersionHandler(module.getName(), versionHandler);
            installContext.setCurrentModule(module);

            final Version currentVersion = versionHandler.getCurrentlyInstalled(installContext);
            final List deltas = versionHandler.getDeltas(installContext, currentVersion);
            if (deltas.size() > 0) {
                state.addModule(module, currentVersion, deltas);
            }
        }
        // TODO handle modules found in repo but not found on classpath

        // TODO : do in finally{}
        installContext.setCurrentModule(null);

        // if we don't have to perform any update load repositories now
        if (!state.needsUpdateOrInstall()) {
            loadRepositories();
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

    protected ModuleVersionHandler getVersionHandler(ModuleDefinition module) {
        try {
            final Class versionHandlerClass = module.getVersionHandler();
            if (versionHandlerClass != null) {
                return (ModuleVersionHandler) versionHandlerClass.newInstance();
            } else {
                final String moduleClassName = module.getClassName();
                if (moduleClassName != null) {
                    final Class moduleClass = ClassUtil.classForName(moduleClassName);
                    if (ClassUtil.isSubClass(moduleClass, Module.class)) {
                        return new LegacyModuleVersionHandler();
                    }
                }
                return new DefaultModuleVersionHandler();
            }
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // TODO
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public void performInstallOrUpdate() throws ModuleManagementException {
        if (state == null) {
            throw new IllegalStateException("ModuleManager was not initialized !");
        }
        if (!state.needsUpdateOrInstall()) {
            throw new IllegalStateException("ModuleManager has nothing to do !");
        }

        // complete repository loading before install or update
        loadRepositories();

        final Context previousCtx = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        try {
            MgnlContext.setInstance(MgnlContext.getSystemContext());

            final Iterator it = state.getList().iterator();
            while (it.hasNext()) {
                final ModuleAndDeltas moduleAndDeltas = (ModuleAndDeltas) it.next();
                installOrUpdateModule(moduleAndDeltas, installContext);
                it.remove();
            }

        }
        finally {
            MgnlContext.setInstance(previousCtx);
        }
    }

    public InstallContext getInstallContext() {
        return installContext;
    }

    /**
     * Process startup tasks. Tasks retured by <code>ModuleDefinition.getStartupTasks()</code> are always executed and
     * do not require manual intervention.
     */
    private void processStartupTasks() {
        final Context previousCtx = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        try {
            MgnlContext.setInstance(MgnlContext.getSystemContext());
            final Iterator it = orderedModuleDescriptors.iterator();
            boolean success = true;
            while (it.hasNext()) {
                final ModuleDefinition module = (ModuleDefinition) it.next();
                String moduleName = module.getName();
                final ModuleVersionHandler versionHandler = registry.getVersionHandler(moduleName);

                installContext.setCurrentModule(module);

                List tasks = versionHandler.getStartupTasks(installContext);
                if (tasks == null || tasks.isEmpty()) {
                    continue;
                }

                Task task = null;
                try {
                    final Iterator itT = tasks.iterator();
                    while (itT.hasNext()) {
                        task = (Task) itT.next();
                        log.debug("Module {}, executing {}", module, task);
                        task.execute(installContext);
                    }
                }
                catch (TaskExecutionException e) {
                    log.error("Startup task " + task + " for module " + moduleName + " failed: " + e.getMessage() + ".", e);
                    success = false;
                }
                finally {
                    installContext.setCurrentModule(null);
                }

                saveChanges(success);
            }
        }
        finally {
            MgnlContext.setInstance(previousCtx);
        }
    }

    public void startModules() {

        // process startup tasks before actually starting modules
        processStartupTasks();

        try {
            // here we use the implementation, since it has extra methods that should not be exposed to ModuleLifecycle
            // methods.
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

                Content configNode = null;

                if (modulesParentNode.hasContent(moduleName)) {
                    final Content moduleNode = modulesParentNode.getChildByName(moduleName);
                    moduleNodes.add(moduleNode);
                    moduleProperties.put("moduleNode", moduleNode);
                    if (moduleNode.hasContent("config")) {
                        configNode = moduleNode.getContent("config");
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
                    ObservationUtil.registerChangeListener(ContentRepository.CONFIG, "/modules/" + moduleName + "/config", new EventListener() {

                        public void onEvent(EventIterator events) {
                            Object moduleInstance = registry.getModuleInstance(moduleName);
                            ModuleDefinition moduleDefinition = registry.getDefinition(moduleName);

                            // TODO we should keep only one instance of the lifecycle context
                            ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
                            lifecycleContext.setPhase(ModuleLifecycleContext.PHASE_MODULE_RESTART);
                            stopModule(moduleInstance, moduleDefinition, lifecycleContext);

                            populateModuleInstance(moduleInstance, moduleProperties);

                            startModule(moduleInstance, moduleDefinition, lifecycleContext);
                        }
                    });
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
        // TODO
        throw new IllegalStateException("not implemented yet");
    }

    protected void installOrUpdateModule(ModuleAndDeltas moduleAndDeltas, InstallContextImpl ctx) {
        final ModuleDefinition moduleDef = moduleAndDeltas.getModule();
        ctx.setCurrentModule(moduleDef);
        final List deltas = moduleAndDeltas.getDeltas();
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
                }
            }
        }
        catch (TaskExecutionException e) {
            ctx.error("Could not install or update module. Please remove or update faulty jar. (" + e.getMessage() + ")", e);
            success = false;
        }
        finally {
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
                if (persist) {
                    hm.save();
                } else {
                    hm.refresh(false);
                }
            }
            catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }

    /**
     * Perform repository registration tasks (create repositories or workspace, setup nodetypes) that should be done
     * always before starting the new module.
     */
    private void loadRepositories() {

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
     * @param repositoryName
     * @param nodeTypeFile
     * @param workspaces
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
