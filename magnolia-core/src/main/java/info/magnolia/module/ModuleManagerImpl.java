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
import info.magnolia.cms.module.Module;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaType;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.module.model.reader.DependencyChecker;
import info.magnolia.module.model.reader.ModuleDefinitionReader;
import org.apache.commons.beanutils.BeanUtils;

import javax.jcr.RepositoryException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TODO where do we setup ModuleRegistry ?
 *
 * TODO where do we setup module configs observation ?
 *
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

    public ModuleManagementState checkForInstallOrUpdates() throws ModuleManagementException {
        // compare and determine if we need to do anything
        state = new ModuleManagementState();
        final Iterator it = orderedModuleDescriptors.iterator();
        while (it.hasNext()) {
            final ModuleDefinition module = (ModuleDefinition) it.next();
            final ModuleVersionHandler versionHandler = getVersionHandler(module);
            registry.registerModuleVersionHandler(module.getName(), versionHandler);
            installContext.setCurrentModule(module);

            final Version currentVersion = versionHandler.getCurrentlyInstalled(installContext);
            final List deltas = versionHandler.getDeltas(installContext, currentVersion);
            if (deltas.size() > 0) {
                // TODO : add the correct DeltaType (install if version is null)
                state.addModule(DeltaType.update, module, currentVersion, deltas);
            }
        }
        // TODO handle modules found in repo but not found on classpath

        // TODO : do in finally{}
        installContext.setCurrentModule(null);

        // TODO : check the force bootstrap properties

        return state;
    }

    public ModuleManagementState getStatus() {
        if (state == null) {
            throw new IllegalStateException("ModuleManager was not initialized !");
        }

        return state;
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
                        return new LegacyModuleVersionHandler(module.getVersionDefinition());
                    }
                }
                return new DefaultModuleVersionHandler();
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // TODO
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public void performInstallOrUpdate() throws RepositoryException {
        if (state == null) {
            throw new IllegalStateException("ModuleManager was not initialized !");
        }
        if (!state.needsUpdateOrInstall()) {
            throw new IllegalStateException("ModuleManager has nothing to do !");
        }

        final Context previousCtx = MgnlContext.hasInstance() ? MgnlContext.getInstance() : null;
        try {
            MgnlContext.setInstance(MgnlContext.getSystemContext());

            final Iterator it = state.getList().iterator();
            while (it.hasNext()) {
                final ModuleAndDeltas moduleAndDeltas = (ModuleAndDeltas) it.next();
                installOrUpdateModule(moduleAndDeltas, installContext);
                it.remove();
            }

        } finally {
            MgnlContext.setInstance(previousCtx);
        }
    }

    public InstallContext getInstallContext() {
        return installContext;
    }

    public void startModules() {
        try {
            // here we use the implementation, since it has extra methods that should not be exposed to ModuleLifecycle methods.
            final ModuleLifecycleContextImpl lifecycleContext = new ModuleLifecycleContextImpl();
            final HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            final Content modulesParentNode = hm.getContent(MODULES_NODE);
            final Collection moduleNodes = new ArrayList();
            final Iterator it = orderedModuleDescriptors.iterator();

            while (it.hasNext()) {

                final ModuleDefinition moduleDefinition = (ModuleDefinition) it.next();
                lifecycleContext.setCurrentModuleDefinition(moduleDefinition);
                final String moduleClassName = moduleDefinition.getClassName();

                Map moduleProperties = new HashMap();
                moduleProperties.put("moduleDefinition", moduleDefinition);
                String moduleName = moduleDefinition.getName();
                moduleProperties.put("name", moduleName);

                if (modulesParentNode.hasContent(moduleName)) {
                    final Content moduleNode = modulesParentNode.getChildByName(moduleName);
                    moduleNodes.add(moduleNode);
                    moduleProperties.put("moduleNode", moduleNode);

                    if (moduleNode.hasContent("config")) {
                        moduleProperties.put("configNode", moduleNode.getContent("config"));
                    }
                }

                Object moduleInstance = registry.getModuleInstance(moduleName);

                if (moduleInstance == null && moduleClassName != null) {
                    moduleInstance = ClassUtil.newInstance(moduleClassName);
                    registry.registerModuleInstance(moduleName, moduleInstance);
                }

                if (moduleInstance != null) {
                    try {
                        BeanUtils.populate(moduleInstance, moduleProperties);
                    }
                    catch (InvocationTargetException e) {
                        log.error("can't set default properties", e);
                    }

                    if (moduleProperties.get("configNode") != null) {
                        try {
                            Content2BeanUtil.setProperties(moduleInstance, (Content) moduleProperties.get("configNode"));
                        }
                        catch (Content2BeanException e) {
                            log.error("wasn't able to configure module", e);
                        }
                    }

                    if (moduleInstance instanceof ModuleLifecycle) {
                        log.debug("starting module {}", moduleName);
                        ((ModuleLifecycle) moduleInstance).start(lifecycleContext);
                    }
                }
            }
            lifecycleContext.start(moduleNodes);
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // TODO
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
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
        } catch (TaskExecutionException e) {
            ctx.error("Could not install or update module. Please remove or update faulty jar. (" + e.getMessage() + ")", e);
            success = false;
        } finally {
            // TODO : ctx.info("Successful installation/update."); after save ?
            ctx.setCurrentModule(null);
        }

        // save all repositories once a module was properly installed/updated, or rollback changes.
        final Iterator reposIt = ContentRepository.getAllRepositoryNames();
        while (reposIt.hasNext()) {
            final String repoName = (String) reposIt.next();
            log.debug((success ? "Saving" : "Rolling back") + " repository " + repoName);
            final HierarchyManager hm = MgnlContext.getHierarchyManager(repoName);
            try {
                if (success) {
                    hm.save();
                } else {
                    hm.refresh(false);
                }
            } catch (RepositoryException e) {
                throw new RuntimeException(e); // TODO
            }
        }

    }
}
