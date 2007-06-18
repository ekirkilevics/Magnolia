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
