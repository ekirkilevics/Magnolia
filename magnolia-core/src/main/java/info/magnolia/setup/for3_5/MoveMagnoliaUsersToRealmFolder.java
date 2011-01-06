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
package info.magnolia.setup.for3_5;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Realm;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Iterator;

/**
 * A task which moves existing users to the /admin realm folder.
 *
 * @author philipp
 * @version $Revision$ ($Author$)
 */
public class MoveMagnoliaUsersToRealmFolder extends AbstractRepositoryTask {
    private static final Logger log = LoggerFactory.getLogger(MoveMagnoliaUsersToRealmFolder.class);

    public MoveMagnoliaUsersToRealmFolder() {
        super("Update Magnolia users repository structure", "Moves Magnolia admin users into /" + Realm.REALM_ADMIN + " folder.");
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        // move existing users there
        final HierarchyManager usersHm = installContext.getHierarchyManager(ContentRepository.USERS);

        Collection users = usersHm.getRoot().getChildren(ItemType.USER);

        Iterator iter = users.iterator();
        while (iter.hasNext()) {
            Content node = (Content) iter.next();
            usersHm.getWorkspace().getSession().move(node.getHandle(), getAdminRealmFolder() + "/" + node.getName());
            log.info("Moved user " + node.getName() + " to " + getAdminRealmFolder() + "/" + node.getName());
        }
    }

    protected String getAdminRealmFolder() {
        return "/" + Realm.REALM_ADMIN;
    }
}
