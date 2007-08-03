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

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RemoveModuleDescriptorDetailsFromRepo extends AllModulesNodeOperation {

    public RemoveModuleDescriptorDetailsFromRepo() {
        super("Cleanup modules node", "Removes the name, displayName and class properties from the modules nodes, as these are not used anymore.");
    }

    protected void operateOnModuleNode(Content node, HierarchyManager hm, InstallContext ctx) {
        deleteNodeDataIfExists(node, "name", ctx);
        deleteNodeDataIfExists(node, "displayName", ctx);
        deleteNodeDataIfExists(node, "class", ctx);
    }

    private void deleteNodeDataIfExists(Content node, String name, InstallContext ctx) {
        try {
            if (node.hasNodeData(name)) {
                node.deleteNodeData(name);
            }
        } catch (RepositoryException e) {
            ctx.warn("Could not delete property " + name + " from node " + node.getHandle() + ".");
        }
    }
}
