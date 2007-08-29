package info.magnolia.setup.for3_1;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MoveMagnoliaUsersToRealmFolders extends AbstractRepositoryTask {

    public MoveMagnoliaUsersToRealmFolders() {
        super("Update Magnolia users repository structure", "Moves magnolia admin users into /" + Realm.REALM_ADMIN + " folder and system users into /" + Realm.REALM_SYSTEM);
    }

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MoveMagnoliaUsersToRealmFolders.class);

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        // move existing users there
        final HierarchyManager usersHm = installContext.getHierarchyManager(ContentRepository.USERS);

        Collection users = usersHm.getRoot().getChildren(ItemType.USER);

        Iterator iter = users.iterator();
        while(iter.hasNext()) {
            Content node = (Content) iter.next();
            usersHm.getWorkspace().getSession().move(node.getHandle(), getAdminRealmFolder() + "/" + node.getName());
            log.info("Moved user " + node.getName() + " to " + getAdminRealmFolder() + "/" + node.getName());
        }
    }

    protected String getAdminRealmFolder() {
        return "/" + Realm.REALM_ADMIN;
    }
}
