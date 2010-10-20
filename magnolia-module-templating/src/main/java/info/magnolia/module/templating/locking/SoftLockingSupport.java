/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.templating.locking;


import java.util.List;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.objectfactory.Components;

/**
 * Support for <em>"soft"</em> locking in Magnolia. Soft locking differs from JCR <em>"hard"</em> locking in that it does not really locks the node being edited, rather
 * it just stores information about who is locking a certain content, so that this information can be shown as a warning, e.g. to users concurrently editing the same page.
 * @author fgrilli
 * @see LockManager
 *
 */
public interface SoftLockingSupport {
    static final String CONCURRENT_EDITING_USERS_LIST_ATTRIBUTE =  "concurrentEditingUsersListAttribute";
    /**
     * @param content the {@link Content} to be locked.
     */
    void lock(Content content);

    /**
     * @param content the {@link Content} to be unlocked.
     */
    void unlock(Content content);

    /**
     * @return the users who are locking the content, or an empty <code>List</code> if content is not locked.
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    List<String> lockedBy(Content content) throws AccessDeniedException, RepositoryException;

    /**
     * @return <code>true</code> if content is locked, <code>false</code> otherwise.
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    boolean isLocked(Content content) throws AccessDeniedException, RepositoryException;

    /**
     * @param maxTime the max time expressed in <strong>minutes</strong> after which the current user lock on content is timed out.
     * @return <code>true</code> if the current user lock on this content has timed out, <code>false</code> otherwise.
     * @throws RepositoryException
     * @throws AccessDeniedException
     */
    boolean isTimeout(Content content, Long maxTime) throws AccessDeniedException, RepositoryException;

    /**
     * Used to obtain a singleton of this object.
     * @author fgrilli
     *
     */
    static class Factory{
        public static SoftLockingSupport getInstance(){
            return Components.getSingleton(SoftLockingSupport.class);
        }
    }
}
