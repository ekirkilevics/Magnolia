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

import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.auth.PrincipalCollection;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlPolicy;

import org.apache.jackrabbit.core.ItemImpl;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.AbstractCompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.AccessControlEditor;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.combined.CombinedProvider;
import org.apache.jackrabbit.spi.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Magnolia specific ACL provider. This code depends on JR specific API rather then on JCR API
 * @author had
 * @version $Id: $
 * @deprecated this class will change package as part of removal direct JR dependencies.
 */
// TODO: should extend just abstract control provider!!!
public class MagnoliaAccessProvider extends CombinedProvider {

    /**
     * Compiled user permissions denying access to all resources.
     * @author had
     * @version $Id: $
     */
    public class DenyAllPermissions extends AbstractCompiledPermissions {

        public boolean canRead(Path itemPath, ItemId itemId) throws RepositoryException {
            return false;
        }

        @Override
        protected Result buildResult(Path absPath) throws RepositoryException {
            return null;
        }

    }

    /**
     * Permission based on user ACL for given workspace.
     * @author had
     * @version $Id: $
     */
    public class ACLBasedPermissions extends AbstractCompiledPermissions {

        private final PrincipalCollection acls;

        public ACLBasedPermissions(PrincipalCollection acls) {
            this.acls = acls;
        }

        public boolean canRead(Path itemPath, ItemId itemId) throws RepositoryException {
            log.error("IMPLEMENT CHECK FOR " + itemPath + " :: " + itemId);
            return false;
        }

        @Override
        protected Result buildResult(Path absPath) throws RepositoryException {
            log.error("BUILD RESULT FOR " + absPath);
            return null;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(MagnoliaAccessProvider.class);

    @Override
    public boolean canAccessRoot(Set<Principal> principals) throws RepositoryException {
        try {
            boolean ret = super.canAccessRoot(principals);
            log.debug("canAccessRoot({})=>{}", principals, ret);
            return ret;
        } catch (AccessControlException e) {
            // thrown when there no known principal ... e.g. when there is no authenticated user
            return false;
        }
    }

    @Override
    public void close() {
        log.debug("close()");
        super.close();
    }

    @Override
    public CompiledPermissions compilePermissions(Set<Principal> principals) throws RepositoryException {
        log.debug("compile permissions for {} at {}", printUserNames(principals), session == null ? null : session.getWorkspace().getName());
        checkInitialized();
        // superuser is also admin user!
        if (isAdminOrSystem(principals)) {
            // TODO: retrieve admin user name in configurable manner.
            if ("admin".equals(session.getUserID()) && "uri".equals(session.getWorkspace().getName())) {
                // special admin user has meaning only within the system. Do not allow such user access over uri.
                return new DenyAllPermissions();
            }
            return getAdminPermissions();
        }

        boolean hasSomePermissions = false;
        for  (Iterator<Principal> iter = principals.iterator(); iter.hasNext(); ) {
            Principal princ = iter.next();
            if (princ instanceof PrincipalCollection) {
                log.debug("found mgnl principal " + princ);
                PrincipalCollection collection = (PrincipalCollection) princ;
                if (collection.iterator().hasNext() && collection.iterator().next() instanceof ACL) {
                    String name = super.session.getWorkspace().getName();
                    //TODO: filter out only those permissions relevant to the given workspace!
                    return getUserPermissions(collection);
                }
            }
        }
        // it should never come to this, but if it does ... sorry ... no access
        return new DenyAllPermissions();
    }

    private CompiledPermissions getUserPermissions(PrincipalCollection collection) {
        return new ACLBasedPermissions(collection);
    }

    @Override
    public AccessControlEditor getEditor(Session editingSession) {
        log.debug("getEditor({})", editingSession);
        return super.getEditor(editingSession);
    }

    @Override
    public AccessControlPolicy[] getEffectivePolicies(Path absPath, CompiledPermissions permissions) throws ItemNotFoundException, RepositoryException {
        log.debug("getEffectivePolicies({}, {})", absPath, permissions);
        return super.getEffectivePolicies(absPath, permissions);
    }

    @Override
    public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals, CompiledPermissions permissions) throws RepositoryException {
        log.debug("getEffectivePolicies({}, {})", principals, permissions);
        return super.getEffectivePolicies(principals, permissions);
    }

    @Override
    public void init(Session systemSession, Map configuration) throws RepositoryException {
        log.debug("init({}, {})", systemSession, configuration);
        super.init(systemSession, configuration);
    }

    @Override
    public boolean isAcItem(ItemImpl item) throws RepositoryException {
        log.debug("isAcItem({})", item);
        return super.isAcItem(item);
    }

    @Override
    public boolean isAcItem(Path absPath) throws RepositoryException {
        log.debug("isAcItem({})", absPath);
        return super.isAcItem(absPath);
    }

    private String printUserNames(Set<Principal> principals) {
        StringBuilder sb = new StringBuilder();
        for (Principal p : principals) {
            sb.append(" or ").append(p.getName()).append("[").append(p.getClass().getName()).append("]");
        }
        sb.delete(0,4);
        return sb.toString();
    }

}
