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

package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ParagraphManager;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.ControlsManager;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.module.RepositoryDefinition;
import info.magnolia.cms.module.ServletDefinition;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.ContentUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation. Imports bootstrap files, registers dialogs ,...
 * @author philipp
 */
public abstract class AbstractModule implements Module {

    /**
     * Logger
     */
    Logger log = LoggerFactory.getLogger(AbstractModule.class);

    /**
     * The configuration passed by the initializer (read from the repository)
     */
    private ModuleConfig config;

    /**
     * The modules definition built by the modules xml file
     */
    private ModuleDefinition definition;

    /**
     * True after a registration.
     */
    private boolean restartNeeded = false;

    /**
     * True after initialization.
     */
    private boolean initialized = false;

    /**
     * Initialize the module. Registers the dialogs, paragraphs and templates of this modules. Calls the abstract onInit
     * method.
     * @throws InitializationException
     */
    public final void init(ModuleConfig moduleConfig) throws InvalidConfigException, InitializationException {
        try {
            Content moduleNode = moduleConfig.getLocalStore().getParent();
            Content node;

            this.setModuleConfig(moduleConfig);

            // register uri mappings
            node = ContentUtil.getCaseInsensitive(moduleNode, "virtualURIMapping");
            if (node != null) {
                VirtualURIManager.getInstance().register(node);
            }

            // register templates
            node = ContentUtil.getCaseInsensitive(moduleNode, "templates");
            if (node != null) {
                TemplateManager.getInstance().register(node);
            }

            // register paragraphs
            node = ContentUtil.getCaseInsensitive(moduleNode, "paragraphs");
            if (node != null) {
                ParagraphManager.getInstance().register(node);
            }

            // register the dialogs
            node = ContentUtil.getCaseInsensitive(moduleNode, "dialogs");
            if (node != null) {
                DialogHandlerManager.getInstance().register(node);
            }

            // register controls
            node = ContentUtil.getCaseInsensitive(moduleNode, "controls");
            if (node != null) {
                ControlsManager.getInstance().register(node);
            }

            // register pages
            node = ContentUtil.getCaseInsensitive(moduleNode, "pages");
            if (node != null) {
                PageHandlerManager.getInstance().register(node);
            }

            // register trees
            node = ContentUtil.getCaseInsensitive(moduleNode, "trees");
            if (node != null) {
                TreeHandlerManager.getInstance().register(node);
            }

            onInit();

            this.setInitialized(true);
        }
        catch (Exception e) {
            throw new InitializationException("can't initialize module [" + moduleConfig.getName() + "]", e);
        }
    }

    /**
     * Calles onRegister if not yet installed after it loaded the bootstrapfiles of this module
     */
    public final void register(ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException {
        this.setDefinition(def);

        if (registerState == REGISTER_STATE_INSTALLATION || registerState == REGISTER_STATE_NEW_VERSION) {
            try {

                final String moduleName = this.getName();

                // bootstrap the module files
                String[] moduleBootstrap = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

                    public boolean accept(String name) {
                        return name.startsWith("/mgnl-bootstrap/config.modules." + moduleName);
                    }
                });

                List bootstrapFiles = new ArrayList(Arrays.asList(moduleBootstrap));

                for (Iterator iter = def.getBootstrapFiles().iterator(); iter.hasNext();) {
                    String additionalBootstrapFile = (String) iter.next();
                    bootstrapFiles.add("/mgnl-bootstrap/" + additionalBootstrapFile);
                }

                ModuleUtil.bootstrap((String[]) bootstrapFiles.toArray(new String[bootstrapFiles.size()]));

                // register servlets
                for (Iterator iter = def.getServlets().iterator(); iter.hasNext();) {
                    ServletDefinition servlet = (ServletDefinition) iter.next();
                    ModuleUtil.registerServlet(servlet);
                    this.setRestartNeeded(true);
                }

                // register repositories
                for (Iterator iter = def.getRepositories().iterator(); iter.hasNext();) {
                    RepositoryDefinition repDef = (RepositoryDefinition) iter.next();
                    ModuleUtil.registerRepository(repDef.getName());
                    this.setRestartNeeded(true);
                }

                // copy the content of mgnl-files to the webapp
                String[] contentFiles = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {

                    public boolean accept(String name) {
                        return name.startsWith("/mgnl-files/templates/" + moduleName)
                            || name.startsWith("/mgnl-files/docroot/" + moduleName);
                    };
                });
                ModuleUtil.installFiles(contentFiles, "mgnl-files");

                // let the module do it's stuff
                onRegister(def, moduleNode, registerState);
            }
            catch (Exception e) {
                throw new RegisterException("can't register module " + this.definition.getName(), e);
            }
        }
    }

    public void destroy() {
    }

    protected void setModuleConfig(ModuleConfig config) {
        this.config = config;
    }

    protected ModuleConfig getModuleConfig() {
        return config;
    }

    /**
     * Template pattern. Implement to performe some module specific stuff
     */
    protected void onRegister(ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException {
    }

    /**
     * Template pattern. Implement to perfome somem module specific stuff
     */
    protected void onInit() throws InitializationException {
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
        return definition.getName();
    }

}
