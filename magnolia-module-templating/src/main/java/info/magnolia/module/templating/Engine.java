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
package info.magnolia.module.templating;

import info.magnolia.cms.beans.config.TemplateManager;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.module.admininterface.AbstractAdminModule;
import info.magnolia.module.templating.renderers.JspTemplateRenderer;


/**
 * Module "templating" main class.
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class Engine extends AbstractAdminModule {

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onInit()
     */
    public void onInit() {
        // set local store to be accessed via admin interface classes or JSP
        Store.getInstance().setStore(this.getConfigNode());

        // temporary hardcoded renderers
        TemplateManager.getInstance().registerTemplateRenderer("jsp", new JspTemplateRenderer());
        TemplateManager.getInstance().registerTemplateRenderer("servlet", new JspTemplateRenderer());
    }

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onRegister(int)
     */
    protected void onRegister(int registerState) throws RegisterException {
        // nothing todo
    }
}