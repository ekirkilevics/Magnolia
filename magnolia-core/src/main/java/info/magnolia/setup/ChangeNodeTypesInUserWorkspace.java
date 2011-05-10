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
package info.magnolia.setup;

import java.util.Collection;
import java.util.Iterator;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Task used to change <code>nt:folder</code> into <code>mgnl:folder</code> type in users workspace.
 * @author had
 * @version $Id $
 */
public class ChangeNodeTypesInUserWorkspace extends AbstractRepositoryTask {

    /**
     * Content filter accepting only <code>nt:folder</code>.
     */
    private static ContentFilter NT_FOLDER_FILTER = new ContentFilter() {
        @Override
        public boolean accept(Content content) {
            return content.isNodeType("nt:folder");
        }
    };

    public ChangeNodeTypesInUserWorkspace() {
        super("Nodetypes change", "Changes node type of folders in users workspace.");
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager("users");
        try {
            ContentUtil.visit(hm.getRoot(), new TypeChanger(), NT_FOLDER_FILTER);
        } catch (Exception e) {
            throw new TaskExecutionException("Cant' update folder node types in users workspace: " + e.getMessage(), e);
        }
    }

    private final static class TypeChanger implements ContentUtil.PostVisitor {

        @Override
        public void postVisit(Content node) throws Exception {
            Content parent = null;
            if (node.getLevel() == 0) {
                // node is a root node
                return;
            }
            parent = node.getParent();
            String name = node.getName();
            Content tmp = parent.createContent("tmp---tmp---" + name, ItemType.FOLDER);
            Collection children = node.getChildren(ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER);
            for (Iterator iterator = children.iterator(); iterator.hasNext();) {
                Content object = (Content) iterator.next();
                //tmp is not saved yet ... operate in session only
                ContentUtil.moveInSession(object, tmp.getHandle() + "/" + object.getName());
            }
            node.delete();
            //tmp is not saved yet ... operate in session only
            ContentUtil.moveInSession(tmp, StringUtils.removeEnd(parent.getHandle(), "/") + "/" + name);
        }

        @Override
        public void visit(Content node) throws Exception {
            // do nothing
        }
    }
}
