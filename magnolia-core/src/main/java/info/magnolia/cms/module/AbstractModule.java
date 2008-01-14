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
package info.magnolia.cms.module;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.files.BasicFileExtractor;
import info.magnolia.module.files.ModuleFileExtractorTransformer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation. Imports bootstrap files, registers servlets, registers repositories and extracts files from
 * the jar. For a more sophisticated version see the AbstractAdminModule from the admin interface sub-project.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 * @deprecated see info.magnolia.module
 */
public abstract class AbstractModule implements Module, ModuleLifecycle {

    /**
     * The modules definition built by the modules xml file
     */
    protected ModuleDefinition definition;

    /**
     * The node of this module
     */
    protected Content moduleNode;

    /**
     * The config node of the module
     */
    protected Content configNode;

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

    public AbstractModule() {
        log.warn("This module extends the deprecated AbstractModule class [{}]", this.getClass());
    }

    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        try {
            register(this.getModuleDefinition(), this.getModuleNode(), Module.REGISTER_STATE_NONE);
        }
        catch (RegisterException e) {
            log.error("error during starting module", e);
        }
        try {
            init(this.getConfigNode());
        }
        catch (InvalidConfigException e) {
            log.error("error during starting module", e);
        }
        catch (InitializationException e) {
            log.error("error during starting module", e);
        }
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        destroy();
    }

    /**
     * Calles onRegister if not yet installed after it loaded the bootstrapfiles of this module
     */
    public final void register(ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException {
        setDefinition(def);
        // will be reset by the ModuleManager before the init method is called
        setModuleNode(moduleNode);

        if (registerState == REGISTER_STATE_INSTALLATION || registerState == REGISTER_STATE_NEW_VERSION) {
            try {
                // let the module do its stuff
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
     * Set by the ModuleManager
     */
    public void setModuleDefinition(ModuleDefinition md) {
        setDefinition(md);
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
    public void setModuleNode(Content moduleNode) {
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
     * @deprecated should not be used directly. Use FileExtractor instead
     */
    protected void installFiles(final String moduleName, File moduleRoot) throws Exception {
        log.info("installing files for module {}", moduleName);
        final ModuleFileExtractorTransformer fileExtractorTransformer = new ModuleFileExtractorTransformer(moduleName);
        new BasicFileExtractor().extractFiles(fileExtractorTransformer);
    }

    /**
     * Register the repositories defined in the descriptor.
     * @param def
     * @throws RegisterException
     * @deprecated see info.magnolia.module.delta.RegisterRepositoriesAndWorkspacesTask
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
     * @deprecated see info.magnolia.module.delta.RegisterModuleServletsTask
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
     * @deprecated see info.magnolia.module.delta.BootstrapResourcesTask
     */
    protected void bootstrap(final String moduleName) throws IOException, RegisterException {
        // bootstrap the module files
        String[] moduleBootstrap = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

            public boolean accept(String name) {
                return name.startsWith("/mgnl-bootstrap/" + moduleName + "/") && name.endsWith(".xml");
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

    /**
     * @return the configNode
     */
    public Content getConfigNode() {
        if (this.configNode == null && getModuleNode() != null) {
            try {
                if (getModuleNode().hasContent("config")) {
                    this.configNode = getModuleNode().getContent("config");
                }
            }
            catch (RepositoryException e) {
                // ignored
            }
        }
        return this.configNode;
    }

    /**
     * @param configNode the configNode to set
     */
    public void setConfigNode(Content configNode) {
        this.configNode = configNode;
    }

}
