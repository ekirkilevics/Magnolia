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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.util.ContentUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation. registers dialogs , paragraphs, ...
 * @author philipp
 * @deprecated see info.magnolia.module
 */
public abstract class AbstractAdminModule extends AbstractModule {

    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Initialize the module. Registers the dialogs, paragraphs and templates of this modules. Calls the abstract onInit
     * method.
     * @throws InitializationException
     */
    public final void init(Content configNode) throws InitializationException {
        try {
            onInit();

            this.setInitialized(true);
        }
        catch (Throwable e) {
            throw new InitializationException("can't initialize module [" + this.getName() + "]", e);
        }
    }

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

}
