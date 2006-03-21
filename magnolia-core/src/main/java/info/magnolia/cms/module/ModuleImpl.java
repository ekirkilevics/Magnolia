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


/**
 * Default implementation. For a more sophisticated version see the AbstractAdminModule from the admin interface
 * sub-project.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class ModuleImpl implements Module {

    /**
     * The modules definition built by the modules xml file
     */
    protected ModuleDefinition definition;

    /**
     * The node of this module
     */
    protected Content moduleNode;

    /**
     * The node containing the configuration for this module
     */
    private Content configNode;

    /**
     * True after a registration.
     */
    private boolean restartNeeded = false;

    /**
     * True after initialization.
     */
    private boolean initialized = false;

    /**
     * @see info.magnolia.cms.module.Module#init(info.magnolia.cms.core.Content)
     */
    public void init(Content configNode) throws InvalidConfigException, InitializationException {
        this.setConfigNode(configNode);
    }

    /**
     * @see info.magnolia.cms.module.Module#register(info.magnolia.cms.module.ModuleDefinition,
     * info.magnolia.cms.core.Content, int)
     */
    public void register(ModuleDefinition def, Content moduleNode, int registerState) throws RegisterException {
        this.setDefinition(def);
        this.setModuleNode(moduleNode);
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
     * @return Returns the config node of the modules node.
     */
    public Content getConfigNode() {
        return this.configNode;
    }

    /**
     * @param configNode The configNode to set.
     */
    protected void setConfigNode(Content configNode) {
        this.configNode = configNode;
    }

    /**
     * @see info.magnolia.cms.module.Module#destroy()
     */
    public void destroy() {
        // nothing todo
    }

}
