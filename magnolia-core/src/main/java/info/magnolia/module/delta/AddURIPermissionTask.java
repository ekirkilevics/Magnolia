/**
 * This file Copyright (c) 2007-2009 Magnolia International
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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class AddURIPermissionTask extends AbstractRepositoryTask {
    public final static int DENY = 0;
    public final static int GET = 8;
    public final static int GET_POST = 63;

    private final String roleName;
    private final String uri;
    private final int permission;

    public AddURIPermissionTask(String name, String description, String roleName, String uri, int permission) {
        super(name, description);
        this.roleName = roleName;
        this.uri = uri;
        this.permission = permission;
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = installContext.getHierarchyManager(ContentRepository.USER_ROLES);
        final Content roleNode = hm.getContent("/" + roleName);
        final Content uriPermissionsNode = ContentUtil.getOrCreateContent(roleNode, "acl_uri", ItemType.CONTENTNODE);
        
        final Content permNode = uriPermissionsNode.createContent("0", ItemType.CONTENTNODE);
        permNode.createNodeData("path", uri);
        permNode.createNodeData("permissions", new Long(permission));
    }
}
