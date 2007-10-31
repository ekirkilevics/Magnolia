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
 * @version $Revision: $ ($Author: $)
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
