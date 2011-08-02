/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.test;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.junit.Ignore;

/**
 * Dummy test access manager that at the moment does nothing and grants all rights to everyone.
 * 
 * @author had
 * 
 */
@Ignore
public class TestAccessManager implements AccessManager {

    @Override
    public void init(AMContext context) throws AccessDeniedException, Exception {
    }

    @Override
    public void init(AMContext context, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessMgr) throws AccessDeniedException, Exception {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void checkPermission(ItemId id, int permissions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
    }

    @Override
    public void checkPermission(Path absPath, int permissions) throws AccessDeniedException, RepositoryException {
    }

    @Override
    public boolean isGranted(ItemId id, int permissions) throws ItemNotFoundException, RepositoryException {
        return true;
    }

    @Override
    public boolean isGranted(Path absPath, int permissions) throws RepositoryException {
        return true;
    }

    @Override
    public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
        return true;
    }

    @Override
    public boolean canRead(Path itemPath, ItemId itemId) throws RepositoryException {
        return true;
    }

    @Override
    public boolean canAccess(String workspaceName) throws RepositoryException {
        return true;
    }

}
