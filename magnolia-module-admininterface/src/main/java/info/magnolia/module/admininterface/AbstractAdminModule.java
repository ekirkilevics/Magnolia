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

import info.magnolia.cms.beans.config.*;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.ControlsManager;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.util.ContentUtil;
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
            Content node;

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

            // register renderers
            node = ContentUtil.getCaseInsensitive(moduleNode, "renderers");
            if (node != null) {
                TemplateRendererManager.getInstance().register(node);
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

            // register shutdown tasks
            node = ContentUtil.getCaseInsensitive(moduleNode,"shutdown");
            if(node!=null) {
                ShutdownManager.getInstance().register(node);
            }

            onInit();

            this.setInitialized(true);
        }
        catch (Exception e) {
            throw new InitializationException("can't initialize module [" + this.getName() + "]", e);
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
