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
package info.magnolia.cms.core;

import info.magnolia.cms.security.AccessManagerImpl;
import java.security.Principal;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.AccessControlPolicyIterator;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlPolicy;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.AMContext;
import org.apache.jackrabbit.core.security.DefaultAccessManager;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jackrabbit.core.security.authorization.Permission;

/**
 * Magnolia's own access manager exposing access to repository to all Magnolia users.
 * @author had
 * @version $Id: $
 * @deprecated this class will change package as part of removal direct JR dependencies.
 */
public class MagnoliaAccessManager extends DefaultAccessManager {

    private static final Logger log = LoggerFactory.getLogger(MagnoliaAccessManager.class);
    private AMContext amctx;
    private final AccessManagerImpl ami = new AccessManagerImpl();

    @Override
    public boolean canAccess(String workspaceName) throws RepositoryException {
        boolean ret = super.canAccess(workspaceName);
        log.debug("canAccess({})?{}", workspaceName, ret);
        if (amctx == null || amctx.getSubject() == null || amctx.getSubject().getPrincipals().size() == 0) {
            log.warn("not logged in for {}, granting ws level access to everyone", workspaceName);
        }
        //TODO: check real perms here .. or rely on super ... double check
        return ret;
    }

    @Override
    public boolean canRead(Path itemPath, ItemId itemId) throws RepositoryException {
        boolean res = super.canRead(itemPath, itemId);
        boolean ourRes = ami.isGranted(null, Permission.READ);
        log.debug("can {} read({}:{},{})?{} or {}", new Object[] {printUserNames(amctx.getSubject().getPrincipals()), amctx.getWorkspaceName(), itemPath, itemId, res, ourRes});
        //TODO: check real perms here .. or rely on super ... double check
        return res;
    }

    private String printUserNames(Set<Principal> principals) {
        StringBuilder sb = new StringBuilder();
        for (Principal p : principals) {
            sb.append(" or " + p.getName());
        }
        sb.delete(0,4);
        return sb.toString();
    }

    @Override
    protected void checkInitialized() {
        log.debug("checkInitialized()");
        super.checkInitialized();
    }

    @Override
    public void checkPermission(ItemId id, int permissions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
        log.debug("checkPermission({},{})", id, permissions + "");
        super.checkPermission(id, permissions);
        //TODO: check real perms here .. or rely on super ... double check
    }

    @Override
    public void checkPermission(Path absPath, int permissions) throws AccessDeniedException, RepositoryException {
        log.debug("checkPermission({}, {})", absPath, permissions + "");
        super.checkPermission(absPath, permissions);
        //TODO: check real perms here .. or rely on super ... double check
    }

    @Override
    protected void checkPermission(String absPath, int permission) throws AccessDeniedException, RepositoryException {
        log.debug("checkPermission({}, {})", absPath, permission + "");
        super.checkPermission(absPath, permission);
    }

    @Override
    protected void checkValidNodePath(String absPath) throws PathNotFoundException, RepositoryException {
        log.debug("checkValidNodePath({})", absPath);
        super.checkValidNodePath(absPath);
    }

    @Override
    public void close() throws Exception {
        log.debug("{}:close()", this);
        super.close();
    }

    @Override
    public JackrabbitAccessControlPolicy[] getApplicablePolicies(Principal principal) throws AccessDeniedException, AccessControlException,
    UnsupportedRepositoryOperationException, RepositoryException {
        log.debug("getApplicablePolicies({})", principal);
        return super.getApplicablePolicies(principal);
    }

    @Override
    public AccessControlPolicyIterator getApplicablePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        log.debug("getApplicablePolicies({})", absPath);
        return super.getApplicablePolicies(absPath);
    }

    @Override
    public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals) throws AccessDeniedException, AccessControlException,
    UnsupportedRepositoryOperationException, RepositoryException {
        log.debug("getEffectivePolicies({})", principals);
        return super.getEffectivePolicies(principals);
    }

    @Override
    public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        log.debug("getEffectivePolicies({})", absPath);
        return super.getEffectivePolicies(absPath);
    }

    @Override
    public JackrabbitAccessControlPolicy[] getPolicies(Principal principal) throws AccessDeniedException, AccessControlException,
    UnsupportedRepositoryOperationException, RepositoryException {
        log.debug("getPolicies({})", principal);
        return super.getPolicies(principal);
    }

    @Override
    public AccessControlPolicy[] getPolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        log.debug("getPolicies({})", absPath);
        return super.getPolicies(absPath);
    }

    @Override
    protected PrivilegeRegistry getPrivilegeRegistry() throws RepositoryException {
        log.debug("getPrivilegeRegistry()");
        return super.getPrivilegeRegistry();
    }

    @Override
    public Privilege[] getPrivileges(String absPath, Set<Principal> principals) throws PathNotFoundException, RepositoryException {
        log.debug("getPrivileges({}, {})", absPath, principals);
        return super.getPrivileges(absPath, principals);
    }

    @Override
    public Privilege[] getPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        log.debug("getPrivileges({})", absPath);
        return super.getPrivileges(absPath);
    }

    @Override
    public boolean hasPrivileges(String absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        log.debug("hasPrivileges({}, {})", absPath, privileges);
        return super.hasPrivileges(absPath, privileges);
    }

    @Override
    public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        log.debug("hasPrivileges({}, {}, {})", new Object[] {absPath, principals, privileges});
        return super.hasPrivileges(absPath, principals, privileges);
    }

    @Override
    public void init(AMContext amContext, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager) throws AccessDeniedException, Exception {
        log.debug("{}:init({}, {}, {})", new Object[] {this, amContext, acProvider, wspAccessManager});
        super.init(amContext, acProvider, wspAccessManager);
        this.amctx = amContext;
    }

    @Override
    public void init(final AMContext amContext) throws AccessDeniedException, Exception {
        super.init(amContext);
        this.amctx = amContext;
        // can get our user from here as we put it in the list of principals
        final String user = this.amctx.getSubject().getPrincipals().iterator().next().getName();
        log.debug("{}:init({})", user, amContext);
    }

    @Override
    public boolean isGranted(ItemId id, int actions) throws ItemNotFoundException, RepositoryException {
        log.debug("isGranted({}, {})", id, actions);
        return super.isGranted(id, actions);
    }

    @Override
    public boolean isGranted(Path absPath, int permissions) throws RepositoryException {
        log.debug("isGranted({}:{}, {})", new Object[] {amctx.getWorkspaceName(), absPath, permissions});
        return super.isGranted(absPath, permissions);
    }

    @Override
    public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
        log.debug("isGranted({}, {}, {})", new Object[] {parentPath, childName, permissions});
        return super.isGranted(parentPath, childName, permissions);
    }

    @Override
    public void removePolicy(String absPath, AccessControlPolicy policy) throws PathNotFoundException, AccessControlException, AccessDeniedException,
    RepositoryException {
        log.debug("removePolicy({}, {})", absPath, policy);
        super.removePolicy(absPath, policy);
    }

    @Override
    public void setPolicy(String absPath, AccessControlPolicy policy) throws PathNotFoundException, AccessControlException, AccessDeniedException,
    RepositoryException {
        log.debug("setPolicy({}, {})", absPath, policy);
        super.setPolicy(absPath, policy);
    }
}
