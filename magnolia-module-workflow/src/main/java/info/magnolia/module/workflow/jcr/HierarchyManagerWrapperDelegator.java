/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.workflow.jcr;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;

import javax.jcr.RepositoryException;

/**
 * A basic HierarchyManagerWrapper that just delegates all calls to the given HierarchyManager.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class HierarchyManagerWrapperDelegator implements HierarchyManagerWrapper {
    private final HierarchyManager hierarchyManager;
    private final String workspaceName;

    public HierarchyManagerWrapperDelegator(HierarchyManager hierarchyManager) {
        this.hierarchyManager = hierarchyManager;
        this.workspaceName = getHierarchyManager().getWorkspace().getName();
    }

    public void save() throws RepositoryException {
        hierarchyManager.save();
    }

    public boolean isExist(String path) {
        return hierarchyManager.isExist(path);
    }

    public Content getContent(String path) throws RepositoryException {
        return hierarchyManager.getContent(path);
    }

    public Content createPath(String path, ItemType itemType) throws RepositoryException, AccessDeniedException {
        return ContentUtil.createPath(hierarchyManager, path, ItemType.EXPRESSION);
    }

    protected HierarchyManager getHierarchyManager() {
        return hierarchyManager;
    }

    protected String getWorkspaceName() {
        return workspaceName;
    }
}
