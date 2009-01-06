/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.delta;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content.ContentFilter;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AllChildrenNodesOperation extends AbstractRepositoryTask {
    private final String repositoryName;
    private final String parentNodePath;
    private final ContentFilter filter;

    public AllChildrenNodesOperation(String name, String description, String repositoryName, String parentNodePath) {
        this(name, description, repositoryName, parentNodePath, ContentUtil.EXCLUDE_META_DATA_CONTENT_FILTER);
    }

    public AllChildrenNodesOperation(String name, String description, String repositoryName, String parentNodePath, ContentFilter filter) {
        super(name, description);
        this.repositoryName = repositoryName;
        this.parentNodePath = parentNodePath;
        this.filter = filter;
    }

    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Content parentNode = getParentNode(ctx);
        final Collection childNodes = parentNode.getChildren(filter);
        final Iterator it = childNodes.iterator();
        while (it.hasNext()) {
            final Content node = (Content) it.next();
            operateOnChildNode(node, ctx);
        }
    }

    protected Content getParentNode(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = ctx.getHierarchyManager(repositoryName);
        return hm.getContent(parentNodePath);
    }

    protected abstract void operateOnChildNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException;
}
