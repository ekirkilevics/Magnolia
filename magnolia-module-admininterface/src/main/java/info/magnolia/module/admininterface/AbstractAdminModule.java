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

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.beans.config.ParagraphManager;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.beans.config.TemplateRendererManager;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.ControlsManager;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.CommandsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation. registers dialogs , paragraphs, ...
 * @author philipp
 */
public abstract class AbstractAdminModule extends AbstractModule {

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The node containing the configuration for this module
     */
    private Content configNode;

    /**
     * Initialize the module. Registers the dialogs, paragraphs and templates of this modules. Calls the abstract onInit
     * method.
     * @throws InitializationException
     */
    public final void init(Content configNode) throws InitializationException {
        this.setConfigNode(configNode);
        try {

            initEntry("virtualURIMapping", VirtualURIManager.getInstance());
            initEntry("templates", TemplateManager.getInstance());
            initEntry("renderers", TemplateRendererManager.getInstance());
            initEntry("paragraphs", ParagraphManager.getInstance());
            initEntry("dialogs", DialogHandlerManager.getInstance());
            initEntry("controls", ControlsManager.getInstance());
            initEntry("pages", PageHandlerManager.getInstance());
            initEntry("trees", TreeHandlerManager.getInstance());
            initEntry("commands", CommandsManager.getInstance());
            initEntry("shutdown", ShutdownManager.getInstance());

            onInit();

            this.setInitialized(true);
        }
        catch (Exception e) {
            throw new InitializationException("can't initialize module [" + this.getName() + "]", e);
        }
    }

    /**
     * @param nodeName
     * @param manager
     */
    private void initEntry(String nodeName, ObservedManager manager) {
        Content node = ContentUtil.getCaseInsensitive(moduleNode, nodeName);
        if (node != null) {
            manager.register(node);
        }
    }

    /**
     * Template pattern. Implement to perfome somem module specific stuff
     */
    protected abstract void onInit() throws InitializationException;

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

}
