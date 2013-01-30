/**
 * This file Copyright (c) 2011-2012 Magnolia International
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

import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.MgnlInstantiationException;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlPolicy;

import org.apache.jackrabbit.core.ItemImpl;
import org.apache.jackrabbit.core.security.authorization.AccessControlEditor;
import org.apache.jackrabbit.core.security.authorization.CompiledPermissions;
import org.apache.jackrabbit.core.security.authorization.combined.CombinedProvider;
import org.apache.jackrabbit.spi.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Magnolia specific ACL provider. This code depends on JR specific API rather then on JCR API. That's why it'll be moved out from core (SCRUM-636).
 * 
 * @version $Id$
 */
// TODO: should extend just abstract control provider!!!
public class MagnoliaAccessProvider extends CombinedProvider {

    private static final Logger log = LoggerFactory.getLogger(MagnoliaAccessProvider.class);

    private CompiledPermissions RootOnlyPermission;

    private Map<?, ?> configuration;

    private final Class<? extends DefaultACLBasedPermissions> defaultPermissionsClass = DefaultACLBasedPermissions.class;

    private Class<? extends DefaultACLBasedPermissions> permissionsClass;

    private final String compiledPermissionsClass = null;

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
            return getUserPermissions(addJcrSystemReadPermissions(acl.getList()));
        }

        return RootOnlyPermission;
    }

    private CompiledPermissions getUserPermissions(List<Permission> permissions) {
        return Classes.getClassFactory().newInstance(permissionsClass, permissions, session, configuration);
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

    private final String warnMessage = "Check settings of 'permissionsClass' parameter under Workspace>WorkspaceSecurity>AccessControlProvider>. " +
            "Using default " + defaultPermissionsClass + " instead. Only classes extended from this default class can be used.";

    @Override
    public void init(Session systemSession, Map configuration) throws RepositoryException {

        log.debug("init({}, {})", systemSession, configuration);
        super.init(systemSession, configuration);
        RootOnlyPermission = new RootOnlyPermissions(session);
        this.configuration = configuration;

        Object compiledPermissionsClass = configuration.get("permissionsClass");
        if (compiledPermissionsClass == null) {
            permissionsClass = defaultPermissionsClass;
            return;
        }
        try {
            permissionsClass = Classes.getClassFactory().forName((String) compiledPermissionsClass);
            if (!DefaultACLBasedPermissions.class.isAssignableFrom(permissionsClass)) {
                log.warn("The '{}' cannot be used as permissionClass. " + warnMessage, permissionsClass, defaultPermissionsClass);
                permissionsClass = defaultPermissionsClass;
            } else { // try to instantiate
                Classes.getClassFactory().newInstance(permissionsClass, new LinkedList<Permission>(), session, configuration);
                log.info("Using {} for resolving permissions.", permissionsClass);
            }
        } catch (ClassNotFoundException e) {
            log.warn("The class '{}' doesn't exist. " + warnMessage, compiledPermissionsClass, defaultPermissionsClass);
            permissionsClass = defaultPermissionsClass;

        } catch (MgnlInstantiationException e) {
            log.warn("Cannot instantiate '{}'. The permissionClass must have constructor with exact same arguments like '{}'. Using the default permission class '{}' instead.", permissionsClass, defaultPermissionsClass);
            permissionsClass = defaultPermissionsClass;

        } catch (Exception e) { // use default permission class if any exception occurs
            log.warn("Cannot instantiate permissionsClass '{}'. " + warnMessage, permissionsClass, e);
            permissionsClass = defaultPermissionsClass;
        }
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
        sb.delete(0, 4);
        return sb.toString();
    }

    private List<Permission> addJcrSystemReadPermissions(List<Permission> permissions) {
        Permission permission = new PermissionImpl();
        permission.setPattern(new SimpleUrlPattern("/jcr:system"));
        permission.setPermissions(Permission.READ);
        permissions.add(permission);
        permission = new PermissionImpl();
        permission.setPattern(new SimpleUrlPattern("/jcr:system/*"));
        permission.setPermissions(Permission.READ);
        permissions.add(permission);
        return permissions;
    }
}
