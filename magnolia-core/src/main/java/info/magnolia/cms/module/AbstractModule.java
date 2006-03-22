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

import java.io.IOException;
import java.util.Iterator;

import org.jdom.JDOMException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClasspathResourcesUtil;


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
     * Calles onRegister if not yet installed after it loaded the bootstrapfiles of this module
     */
    public final void register(ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException {
        this.setDefinition(def);
        this.setModuleNode(moduleNode);

        if (registerState == REGISTER_STATE_INSTALLATION || registerState == REGISTER_STATE_NEW_VERSION) {
            try {

                final String moduleName = this.getName();

                bootstrap(moduleName);

                registerServlets(def);

                registerRepositories(def);

                installFiles(moduleName);

                // let the module do it's stuff
                onRegister(registerState);
            }
            catch (Exception e) {
                throw new RegisterException("can't register module " + this.definition.getName(), e);
            }
        }
    }

    /**
     * Template pattern. Implement to performe some module specific stuff
     * @param registerState
     */
    protected abstract void onRegister(int registerState) throws RegisterException;

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
        return definition.getName();
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
        // nothing todo
    }

    /**
     * Copy the files from /mgnl-files/templates /mgnl-files/docroot and /mgnl-files/admintemplates to the filesystem.
     * @param moduleName
     * @throws Exception
     */
    protected void installFiles(final String moduleName) throws Exception {
        // copy the content of mgnl-files to the webapp
        String[] moduleFiles = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

            public boolean accept(String name) {
                return name.startsWith("/mgnl-files/templates/" + moduleName)
                    || name.startsWith("/mgnl-files/docroot/" + moduleName)
                    || name.startsWith("/mgnl-files/admintemplates/" + moduleName);
            };
        });

        ModuleUtil.installFiles(moduleFiles, "/mgnl-files/");
    }

    /**
     * Register the repositories defined in the descriptor.
     * @param def
     */
    protected void registerRepositories(ModuleDefinition def) {
        // register repositories
        for (Iterator iter = def.getRepositories().iterator(); iter.hasNext();) {
            RepositoryDefinition repDef = (RepositoryDefinition) iter.next();
            ModuleUtil.registerRepository(repDef.getName());
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
        // register servlets
        for (Iterator iter = def.getServlets().iterator(); iter.hasNext();) {
            ServletDefinition servlet = (ServletDefinition) iter.next();
            ModuleUtil.registerServlet(servlet);
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

}
