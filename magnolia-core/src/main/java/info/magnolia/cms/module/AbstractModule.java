/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClasspathResourcesUtil;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation. Imports bootstrap files, registers servlets, registers repositories and extracts files from
 * the jar. For a more sophisticated version see the AbstractAdminModule from the admin interface sub-project.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractModule implements Module {

    /**
     * The modules definition built by the modules xml file
     */
    protected ModuleDefinition definition;

    /**
     * The node of this module
     */
    protected Content moduleNode;

    /**
     * True after a registration.
     */
    private boolean restartNeeded = false;

    /**
     * True after initialization.
     */
    private boolean initialized = false;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AbstractModule.class);

    /**
     * Calles onRegister if not yet installed after it loaded the bootstrapfiles of this module
     */
    public final void register(ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException {
        this.setDefinition(def);
        this.setModuleNode(moduleNode);

        if (registerState == REGISTER_STATE_INSTALLATION || registerState == REGISTER_STATE_NEW_VERSION) {
            try {
            	
                final String moduleName = this.getName();

                registerServlets(def);

                registerRepositories(def);

                bootstrap(moduleName);
                
                installFiles(moduleName, def.getModuleRoot());

                // let the module do it's stuff
                onRegister(registerState);
            }
            catch (Exception e) {
                throw new RegisterException("can't register module " + this.definition.getName(), e);
            }
        }
    }

	/**
     * @see info.magnolia.cms.module.Module#unregister(info.magnolia.cms.module.ModuleDefinition,
     * info.magnolia.cms.core.Content)
     */
    public void unregister(ModuleDefinition def, Content moduleNode) {
        // TODO implement the unrigister
        onUnRegister();
        // now delete the jar if existing
        // set the restart needed
    }

    /**
     * Template pattern. Implement to performe some module specific stuff
     * @param registerState
     */
    protected void onRegister(int registerState) throws RegisterException {

    }

    /**
     * Template pattern. Implement to perform some module specific stuff.
     */
    protected void onUnRegister() {
    }

    /**
     * @return Returns the restartNeeded.
     */
    public boolean isRestartNeeded() {
        return this.restartNeeded;
    }

    /**
     * @param restartNeeded The restartNeeded to set.
     */
    protected void setRestartNeeded(boolean restartNeeded) {
        this.restartNeeded = restartNeeded;
    }

    /**
     * @return Returns the definition.
     */
    public ModuleDefinition getModuleDefinition() {
        return this.definition;
    }

    /**
     * @param definition The definition to set.
     */
    protected void setDefinition(ModuleDefinition definition) {
        this.definition = definition;
    }

    /**
     * @return Returns the initialized.
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * @param initialized The initialized to set.
     */
    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Delegate to the modules definition getName() method
     * @return
     */
    public String getName() {
        if (definition != null) {
            return definition.getName();
        }
        return null;
    }

    /**
     * @return Returns the moduleNode.
     */
    public Content getModuleNode() {
        return this.moduleNode;
    }

    /**
     * @param moduleNode The moduleNode to set.
     */
    protected void setModuleNode(Content moduleNode) {
        this.moduleNode = moduleNode;
    }

    /**
     * @see info.magnolia.cms.module.Module#destroy()
     */
    public void destroy() {

    }

    /**
     * Copy the files from /mgnl-files/* to the filesystem.
     * @param moduleName
     * @param moduleRoot module root dir or jar file.
     * @throws Exception
     */
    protected void installFiles(final String moduleName, File moduleRoot) throws Exception {
        // copy the content of mgnl-files to the webapp
        String[] moduleFiles = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

            public boolean accept(String name) {
                return name.startsWith("/mgnl-files/") && StringUtils.contains(name, "/" + moduleName + "/");
            }
        });

        log.info("installing files for module {}", moduleName);

        ModuleUtil.installFiles(moduleFiles, "/mgnl-files/");
    }

    /**
     * Register the repositories defined in the descriptor.
     * @param def
     * @throws RegisterException
     * @throws IOException
     * @throws ConfigurationExceptionJDOMException
     */
    protected void registerRepositories(ModuleDefinition def) throws RegisterException {
        boolean restartNeeded = false;

        // register repositories
        for (Iterator iter = def.getRepositories().iterator(); iter.hasNext();) {
            RepositoryDefinition repDef = (RepositoryDefinition) iter.next();
            String repository = repDef.getName();

            String nodetypeFile = repDef.getNodeTypeFile();
            boolean repositoryAdded = ModuleUtil.registerRepository(repository, nodetypeFile);
            if (repositoryAdded) {
                restartNeeded = true;
            }

            for (Iterator iterator = repDef.getWorkspaces().iterator(); iterator.hasNext();) {
                String workspace = (String) iterator.next();

                if (ModuleUtil.registerWorkspace(repository, workspace)) {
                    restartNeeded = true;
                }
            }
        }

        if (restartNeeded) {
            this.setRestartNeeded(true);
        }
    }

    /**
     * Register the servlets defined in the descriptor.
     * @param def
     * @throws JDOMException
     * @throws IOException
     */
    protected void registerServlets(ModuleDefinition def) throws JDOMException, IOException {
        boolean restartNeeded = false;

        // register servlets
        for (Iterator iter = def.getServlets().iterator(); iter.hasNext();) {
            ServletDefinition servlet = (ServletDefinition) iter.next();
            restartNeeded = restartNeeded | ModuleUtil.registerServlet(servlet);
        }

        if (restartNeeded) {
            this.setRestartNeeded(true);
        }
    }
    
    

    /**
     * Bootsrap the files in mgnl-bootsrap/modulename directory
     * @param moduleName
     * @throws IOException
     * @throws RegisterException
     */
    protected void bootstrap(final String moduleName) throws IOException, RegisterException {
        // bootstrap the module files
        String[] moduleBootstrap = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

            public boolean accept(String name) {
                return name.startsWith("/mgnl-bootstrap/" + moduleName) && name.endsWith(".xml");
            }
        });

        ModuleUtil.bootstrap(moduleBootstrap);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getName() + " (" + getName() + " module)";
    }

}
