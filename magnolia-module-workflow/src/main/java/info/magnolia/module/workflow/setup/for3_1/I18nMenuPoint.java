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
package info.magnolia.module.workflow.setup.for3_1;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.PropertyValuesTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class I18nMenuPoint extends PropertyValuesTask {
    private static final String MENU_PATH = "modules/adminInterface/config/menu/inbox";

    public I18nMenuPoint() {
        super("i18n", "Internationalization of the Inbox menu entry.");
    }

    public void execute(InstallContext ctx) throws TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager("config");
        if (!hm.isExist(MENU_PATH)) {
            ctx.warn("Inbox menu does not exist at " + MENU_PATH);
        }
        try {
            final Content menu = hm.getContentNode(MENU_PATH);
            checkAndModifyPropertyValue(ctx, menu, "label", "Inbox", "menu.inbox");
            newProperty(ctx, menu, "i18nBasename", "info.magnolia.module.workflow.messages");
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Could not execute task: " + e.getMessage(), e);
        }
    }
}
