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
package info.magnolia.setup.for3_1;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RenamedRenderersToTemplateRenderers extends AllModulesNodeOperation {
    private static final String OLDNAME = "renderers";
    private static final String NEWNAME = "template-renderers";

    public RenamedRenderersToTemplateRenderers() {
        super("Template renderers", "Modules' renderers nodes were renamed to template-renderers");
    }

    protected void operateOnModuleNode(Content parent, HierarchyManager hm, InstallContext ctx) throws TaskExecutionException {
        final String moduleNodePath = parent.getHandle();
        final String oldPath = moduleNodePath + "/" + OLDNAME;
        final String newPath = moduleNodePath + "/" + NEWNAME;
        if (hm.isExist(oldPath)) {
            ctx.debug("Will move " + oldPath + " to " + newPath);
            try {
                hm.moveTo(oldPath, newPath);
            } catch (RepositoryException e) {
                ctx.error("Could not rename " + oldPath + " to " + newPath, e);
            }
        }
    }

}
