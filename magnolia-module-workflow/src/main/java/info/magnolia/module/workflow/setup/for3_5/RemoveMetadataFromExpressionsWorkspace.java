/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow.setup.for3_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 * A {@link info.magnolia.module.delta.Task} to remove MetaData nodes from the Expressions workspace.
 * TODO : depending on how we handle the gui, this might be a non-mandatory step.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RemoveMetadataFromExpressionsWorkspace extends AbstractRepositoryTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RemoveMetadataFromExpressionsWorkspace.class);

    public RemoveMetadataFromExpressionsWorkspace() {
        super("Nodetypes changed", "Removes the metadata nodes from the Expressions workspace");
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager("Expressions");
        final Content root = hm.getRoot();
        final MetadataRemover metadataRemover = new MetadataRemover(ctx);
        try {
            ContentUtil.visit(root, metadataRemover, ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER);
        } catch (Exception e) {
            // TODO : visit() should not throw Exception ...
            throw new TaskExecutionException("Cant' removed MetaData from the Expression workspace: " + e.getMessage(), e);
        }
    }

    private final static class MetadataRemover implements ContentUtil.Visitor {
        public MetadataRemover(InstallContext ctx) {
            this.ctx = ctx;
        }

        private final InstallContext ctx;

        public void visit(Content node) throws Exception {
            final Content metadata = node.getChildByName("MetaData");
            if (metadata != null) {
                log.debug("Will remove MetaData at " + metadata.getHandle());
                metadata.delete();
            }
        }
    }
}
