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
package info.magnolia.cms.security;

import static info.magnolia.cms.security.SecurityConstants.NODE_ROLES;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.iterator.FilteringNodeIterator;
import org.apache.jackrabbit.commons.predicate.NodeTypePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common parent class for repo based security managers.
 * @author had
 * @version $Id: $
 */
public abstract class RepositoryBackedSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(RepositoryBackedSecurityManager.class);

    public boolean hasAny(final String principalName, final String resourceName, final String resourceTypeName) {
        long start = System.currentTimeMillis();
        try {
            String sessionName;
            if (StringUtils.equalsIgnoreCase(resourceTypeName, NODE_ROLES)) {
                sessionName = RepositoryConstants.USER_ROLES;
            } else {
                sessionName = RepositoryConstants.USER_GROUPS;
            }

            // this is an original code from old ***Managers.
            // TODO: If you ever need to speed it up, turn it around - retrieve group or role by its name and read its ID, then loop through IDs this user has assigned to find out if he has that one or not.
            final Collection<String> groupsOrRoles = MgnlContext.doInSystemContext(new JCRSessionOp<Collection<String>>(getRepositoryName()) {

                @Override
                public Collection<String> exec(Session session) throws RepositoryException {
                    List<String> list = new ArrayList<String>();
                    Node principal = findPrincipalNode(principalName, session);
                    if(principal == null) {
                        log.debug("No User '"+principalName+"' found in repository");
                        return list;
                    }
                    Node groupsOrRoles = principal.getNode(resourceTypeName);

                    for (PropertyIterator props = groupsOrRoles.getProperties(); props.hasNext();) {
                        Property property = props.nextProperty();
                        try {
                            // just get all the IDs of given type assigned to the principal
                            list.add(property.getString());
                        } catch (ItemNotFoundException e) {
                            log.debug("Role [{}] does not exist in the {} repository", resourceName, resourceTypeName);
                        } catch (IllegalArgumentException e) {
                            log.debug("{} has invalid value", property.getPath());
                        } catch (ValueFormatException e) {
                            log.debug("{} has invalid value", property.getPath());
                        }
                    }
                    return list;
                }
            });


            // check if any of the assigned IDs match the requested name
            return MgnlContext.doInSystemContext(new JCRSessionOp<Boolean>(sessionName) {

                @Override
                public Boolean exec(Session session) throws RepositoryException {
                    for (String groupOrRole : groupsOrRoles) {
                        // check for the existence of this ID
                        try {
                            if (session.getNodeByIdentifier(groupOrRole).getName().equalsIgnoreCase(resourceName)) {
                                return true;
                            }
                        } catch (RepositoryException e) {
                            log.debug("Role [{}] does not exist in the ROLES repository", resourceName);
                        }
                    }
                    return false;
                }});

        } catch (RepositoryException e) {
            // Item not found or access denied ...
            log.debug(e.getMessage(), e);
        } finally {
            log.debug("checked {} for {} in {}ms.", new Object[] {resourceName, resourceTypeName, (System.currentTimeMillis() - start)});
        }
        return false;
    }

    /**
     * Adds link to a resource (group or role) to the principal (user or group).
     * This call is lenient and will not throw exception in case principal doesn't exist! Instead it will simply return without making any change.
     * @param principalName name of the user or group to be updated
     * @param resourceName name of the group or role to be added
     * @param resourceTypeName type of the added resource (group or role) {@link #NODE_ROLES}, {@link #NODE_GROUPS}
     * @throws PrincipalNotFoundException
     */
    protected void add(final String principalName, final String resourceName, final String resourceTypeName) throws PrincipalNotFoundException {
        try {
            final String nodeID = getLinkedResourceId(resourceName, resourceTypeName);

            if (!hasAny(principalName, resourceName, resourceTypeName)) {
                Session session = MgnlContext.getJCRSession(getRepositoryName());
                Node principalNode = findPrincipalNode(principalName, session);
                if (principalNode == null) {
                    throw new PrincipalNotFoundException("Principal " + principalName + " of type " + resourceTypeName + " was not found.");
                }
                if (!principalNode.hasNode(resourceTypeName)) {
                    principalNode.addNode(resourceTypeName, ItemType.CONTENTNODE.getSystemName());
                }
                Node node = principalNode.getNode(resourceTypeName);
                // add corresponding ID
                // used only to get the unique label
                String newName = Path.getUniqueLabel(session, node.getPath(), "0");
                node.setProperty(newName, nodeID);
                session.save();
            }
        }
        catch (RepositoryException e) {
            log.error("failed to add " + resourceTypeName + " "+ resourceName + " to  [" + principalName + "]", e);
        }
    }

    private String getLinkedResourceId(final String resourceName, final String resourceTypeName) throws AccessDeniedException {
        final String nodeID;
        if (StringUtils.equalsIgnoreCase(resourceTypeName, NODE_ROLES)) {
            Role role = SecuritySupport.Factory.getInstance().getRoleManager().getRole(resourceName);
            if (role == null) {
                log.warn("Invalid role requested: {}", resourceName);
                nodeID = null;
            }
            else {
                nodeID = role.getId();
            }
        }
        else {
            nodeID = SecuritySupport.Factory.getInstance().getGroupManager().getGroup(resourceName).getId();
        }
        return nodeID;
    }

    protected String getResourceName(final String resourceId) {
        try {
            return MgnlContext.getJCRSession(getRepositoryName()).getNodeByIdentifier(resourceId).getName();
        } catch (ItemNotFoundException e) {
            // referenced node doesn't exist
            return null;
        } 
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * This call is lenient and will not throw exception in case principal doesn't exist! Instead it will simply return without making any change.
     * 
     * @param principalName
     *            name of the user or group to be updated
     * @param resourceName
     *            name of the group or role to be added
     * @param resourceTypeName
     *            type of the added resource (group or role) {@link #NODE_ROLES}, {@link #NODE_GROUPS}
     * @throws PrincipalNotFoundException
     */
    protected void remove(final String principalName, final String resourceName, final String resourceTypeName) throws PrincipalNotFoundException {
        try {
            final String nodeID = getLinkedResourceId(resourceName, resourceTypeName);

            if (hasAny(principalName, resourceName, resourceTypeName)) {
                Session session = MgnlContext.getJCRSession(getRepositoryName());
                Node principalNode = findPrincipalNode(principalName, session);
                if (!principalNode.hasNode(resourceTypeName)) {
                    throw new PrincipalNotFoundException("Principal " + principalName + " of type " + resourceTypeName + " was not found.");
                }
                Node node = principalNode.getNode(resourceTypeName);
                for (PropertyIterator iter = node.getProperties(); iter.hasNext();) {
                    Property nodeData = iter.nextProperty();
                    // check for the existence of this ID
                    try {
                        if (nodeData.getString().equals(nodeID)) {
                            nodeData.remove();
                            session.save();
                            // do not break here ... if resource was ever added multiple times remove all occurrences
                        }
                    } catch (IllegalArgumentException e) {
                        log.debug("{} has invalid value", nodeData.getPath());
                    } catch (ValueFormatException e) {
                        log.debug("{} has invalid value", nodeData.getPath());
                    }
                }
            }
        }
        catch (RepositoryException e) {
            log.error("failed to remove " + resourceTypeName + " "+ resourceName + " from [" + principalName + "]", e);
        }
    }

    protected abstract String getRepositoryName();

    protected abstract Node findPrincipalNode(String principalName, Session session) throws RepositoryException;

    public Map<String, ACL> getACLs(final String principalName) {
        return MgnlContext.doInSystemContext(new SilentSessionOp<Map<String,ACL>>(getRepositoryName()) {
            @Override
            public Map<String, ACL> doExec(Session session) throws Throwable {
                Node node = findPrincipalNode(principalName, session);
                if(node == null){
                    return Collections.emptyMap();
                }
                return getACLs(node);
            }});
    }

    protected Map<String, ACL> getACLs(Node node) throws RepositoryException, ValueFormatException, PathNotFoundException {
        Map<String, ACL> principalList = new HashMap<String, ACL>();
        NodeIterator it = new FilteringNodeIterator(node.getNodes(), new NodeTypePredicate(ItemType.CONTENTNODE.getSystemName(), true));
        while (it.hasNext()) {
            Node aclEntry = it.nextNode();
            if (!aclEntry.getName().startsWith("acl")) {
                continue;
            }
            String name = StringUtils.substringAfter(aclEntry.getName(), "acl_");

            List<Permission> permissionList = new ArrayList<Permission>();
            // add acl
            NodeIterator permissionIterator = new FilteringNodeIterator(aclEntry.getNodes(), new NodeTypePredicate(ItemType.CONTENTNODE.getSystemName(), true));
            while (permissionIterator.hasNext()) {
                Node map = permissionIterator.nextNode();
                String path = map.getProperty("path").getString();
                UrlPattern p = new SimpleUrlPattern(path);
                Permission permission = new PermissionImpl();
                permission.setPattern(p);
                permission.setPermissions(map.getProperty("permissions").getLong());
                permissionList.add(permission);
            }

            ACL acl;
            // get the existing acl object if created before with some
            // other role
            if (principalList.containsKey(name)) {
                acl = principalList.get(name);
                permissionList.addAll(acl.getList());
            }
            acl = new ACLImpl(name, permissionList);
            principalList.put(name, acl);

        }
        return principalList;
    }

}
