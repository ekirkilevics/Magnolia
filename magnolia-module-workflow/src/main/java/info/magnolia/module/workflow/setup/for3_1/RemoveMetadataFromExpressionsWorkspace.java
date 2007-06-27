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
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;

import javax.jcr.RepositoryException;

/**
 * TODO : depending on how we handle the gui, this might be a non-mandatory step.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RemoveMetadataFromExpressionsWorkspace implements Delta {

    public void apply(InstallContext ctx) throws RepositoryException {
        final HierarchyManager hm = ctx.getHierarchyManager("Expressions");
        final Content root = hm.getRoot();
        final MetadataRemover metadataRemover = new MetadataRemover(ctx);
        try {
            ContentUtil.visit(root, metadataRemover, ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER);
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO
        }
    }

    private final static class MetadataRemover implements ContentUtil.Visitor {
        public MetadataRemover(InstallContext ctx) {
            this.ctx = ctx;
        }

        private final InstallContext ctx;

        public void visit(Content node) throws Exception {
            final Content metadata = node.getChildByName("MetaData");
            ctx.debug("Will remove MetaData at " + metadata.getHandle());
            metadata.delete();
        }
    }
}
