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

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.auth.ACL;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlPolicy;

import org.apache.jackrabbit.core.ItemImpl;
import org.apache.jackrabbit.core.cache.GrowingLRUMap;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.security.authorization.AbstractCompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.AccessControlEditor;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.combined.CombinedProvider;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.CachingPathResolver;
import org.apache.jackrabbit.spi.commons.conversion.IllegalNameException;
import org.apache.jackrabbit.spi.commons.conversion.NameResolver;
import org.apache.jackrabbit.spi.commons.conversion.ParsingPathResolver;
import org.apache.jackrabbit.spi.commons.conversion.PathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Magnolia specific ACL provider. This code depends on JR specific API rather then on JCR API. That's why it'll be moved out from core (SCRUM-636).
 *
 * @version $Id$
 */
// TODO: should extend just abstract control provider!!!
public class MagnoliaAccessProvider extends CombinedProvider {

    /**
     * Permission based on user ACL for given workspace. Caches the result of resolving paths from ids, the caching
     * implementation based {@link org.apache.jackrabbit.core.security.authorization.principalbased.ACLProvider.CompiledPermissionImpl}.
     *
     * @version $Id$
     */
    public class ACLBasedPermissions extends AbstractCompiledPermissions {

        private final AccessManager ami;
        @SuppressWarnings("unchecked")
        private final Map<ItemId, Boolean> readCache = new GrowingLRUMap(1024, 5000);
        private final Object monitor = new Object();

        public ACLBasedPermissions(List<Permission> permissions) {
            // TODO: use provider instead of fixed impl
            ami = new AccessManagerImpl();
            ami.setPermissionList(permissions);
        }

        @Override
        public boolean canRead(Path itemPath, ItemId itemId) throws RepositoryException {

            if (itemPath == null) {

                // we deal only with permissions on nodes
                if (!itemId.denotesNode()) {
                    itemId = ((PropertyId)itemId).getParentId();
                }

                synchronized (monitor) {

                    if (readCache.containsKey(itemId)) {
                        return readCache.get(itemId);
                    }

                    itemPath = session.getHierarchyManager().getPath(itemId);
                    boolean canRead = canRead(itemPath, itemId);
                    readCache.put(itemId, canRead);
                    return canRead;
                }
            }

            String path = pathResolver.getJCRPath(itemPath);
            log.debug("Read request for " + path + " :: " + itemId);
            return ami.isGranted(path, Permission.READ);
        }

        @Override
        protected Result buildResult(Path absPath) throws RepositoryException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Result getResult(Path absPath) throws RepositoryException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean grants(Path absPath, int permissions) throws RepositoryException {
            long magnoliaPermissions = convertJackrabbitPermissionsToMagnoliaPermissions(permissions);
            return ami.isGranted(pathResolver.getJCRPath(absPath), magnoliaPermissions);
        }

        @Override
        public int getPrivileges(Path absPath) throws RepositoryException {
            throw new UnsupportedOperationException();
        }

    }

    private static final Logger log = LoggerFactory.getLogger(MagnoliaAccessProvider.class);

    /**
     * Used to convert a jackrabbit Path abstraction into a path string with slashes and no namespaces.
     */
    private final PathResolver pathResolver = new CachingPathResolver(new ParsingPathResolver(null, new NameResolver() {

        @Override
        public Name getQName(String name) throws IllegalNameException, NamespaceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getJCRName(Name name) throws NamespaceException {
            return name.getLocalName();
        }
    }));

    private static final long permissionMapping[][] = {
        {org.apache.jackrabbit.core.security.authorization.Permission.READ, Permission.READ},
        {org.apache.jackrabbit.core.security.authorization.Permission.SET_PROPERTY, Permission.SET},
        {org.apache.jackrabbit.core.security.authorization.Permission.ADD_NODE, Permission.ADD},
        {org.apache.jackrabbit.core.security.authorization.Permission.REMOVE_NODE, Permission.REMOVE},
        {org.apache.jackrabbit.core.security.authorization.Permission.REMOVE_PROPERTY, Permission.REMOVE},
        {org.apache.jackrabbit.core.security.authorization.Permission.READ_AC, Permission.EXECUTE},
        {org.apache.jackrabbit.core.security.authorization.Permission.MODIFY_AC, Permission.EXECUTE},
        {org.apache.jackrabbit.core.security.authorization.Permission.NODE_TYPE_MNGMT, Permission.ADD},
        {org.apache.jackrabbit.core.security.authorization.Permission.VERSION_MNGMT, Permission.EXECUTE},
        {org.apache.jackrabbit.core.security.authorization.Permission.LOCK_MNGMT, Permission.EXECUTE},
        {org.apache.jackrabbit.core.security.authorization.Permission.LIFECYCLE_MNGMT, Permission.EXECUTE},
        {org.apache.jackrabbit.core.security.authorization.Permission.RETENTION_MNGMT, Permission.EXECUTE},
    };

    private long convertJackrabbitPermissionsToMagnoliaPermissions(long jackRabbitPermissions) {
        long magnoliaPermissions = 0;
        for (long[] mapping : permissionMapping) {
            long jackrabbitPermission = mapping[0];
            long magnoliaPermission = mapping[1];
            if ((jackRabbitPermissions & jackrabbitPermission) != 0) {
                magnoliaPermissions = magnoliaPermissions | magnoliaPermission;
            }
        }
        return magnoliaPermissions;
    }

    @Override
    public boolean canAccessRoot(Set<Principal> principals) throws RepositoryException {
        checkInitialized();

        // old Magnolia security allowed access to root to every user
        return true;
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
            return getAdminPermissions();
        }

        final String workspaceName = super.session.getWorkspace().getName();

        ACL acl = PrincipalUtil.findAccessControlList(principals, workspaceName);
        if (acl != null) {
            return getUserPermissions(acl.getList());
        }

        return CompiledPermissions.NO_PERMISSION;
    }

    private CompiledPermissions getUserPermissions(List<Permission> permissions) {
        return new ACLBasedPermissions(permissions);
    }

    @Override
    public AccessControlEditor getEditor(Session editingSession) {
        log.debug("getEditor({})", editingSession);
        return new MagnoliaACLEditor(super.getEditor(editingSession));
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
