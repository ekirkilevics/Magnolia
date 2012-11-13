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
package info.magnolia.cms.security;

import static info.magnolia.cms.security.SecurityConstants.NODE_GROUPS;
import static info.magnolia.cms.security.SecurityConstants.NODE_ROLES;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group manager working directly with JCR API and returning simple groups (no JCR node aware).
 * @author Sameer Charles $Id$
 */
public class MgnlGroupManager extends RepositoryBackedSecurityManager implements GroupManager {
    private static final Logger log = LoggerFactory.getLogger(MgnlGroupManager.class);

    @Override
    public Group createGroup(final String name) throws AccessDeniedException {
        return MgnlContext.doInSystemContext(new SilentSessionOp<MgnlGroup>(getRepositoryName()) {

            @Override
            public MgnlGroup doExec(Session session) throws RepositoryException {
                Node groupNode = session.getNode("/").addNode(name,ItemType.GROUP.getSystemName());
                session.save();
                return new MgnlGroup(groupNode.getIdentifier(), groupNode.getName(), Collections.EMPTY_LIST, Collections.EMPTY_LIST);
            }

            @Override
            public String toString() {
                return "create group " + name;
            }
        });
    }

    @Override
    public Group getGroup(final String name) throws AccessDeniedException {
        return MgnlContext.doInSystemContext(new SilentSessionOp<Group>(getRepositoryName()) {

            @Override
            public Group doExec(Session session) throws RepositoryException {
                if (!session.itemExists("/" + name)){
                    return null;
                }
                Node groupNode = session.getNode("/" + name);
                return newGroupInstance(groupNode);
            }

            @Override
            public String toString() {
                return "get group " + name;
            }
        });
    }

    @Override
    public Collection<Group> getAllGroups() {
        return MgnlContext.doInSystemContext(new SilentSessionOp<Collection<Group>>(getRepositoryName()) {

            @Override
            public Collection<Group> doExec(Session session) throws RepositoryException {
                List<Group> groups = new ArrayList<Group>();
                for (NodeIterator iter = session.getNode("/").getNodes(); iter.hasNext();) {
                    Node node = iter.nextNode();
                    if (!node.isNodeType(ItemType.GROUP.getSystemName())) {
                        continue;
                    }
                    groups.add(newGroupInstance(node));
                }
                return groups;
            }

            @Override
            public String toString() {
                return "get all groups";
            }

        });
    }

    @Override
    public Collection<String> getAllGroups(final String name) {
        return MgnlContext.doInSystemContext(new SilentSessionOp<Collection<String>>(getRepositoryName()) {

            List<String> groups;

            @Override
            public Collection<String> doExec(Session session) throws RepositoryException {
                Group group = getGroup(name);
                if(group == null){
                    return null;
                }
                groups = new ArrayList<String>();
                collectGroups(group);

                return groups;
            }

            private void collectGroups(Group group) throws AccessDeniedException{
                for (Iterator iter = group.getGroups().iterator(); iter.hasNext();){
                    Group subGroup = getGroup((String) iter.next());
                    if(subGroup != null  && !groups.contains(subGroup.getName())){
                        groups.add(subGroup.getName());
                        collectGroups(subGroup);
                    }
                }
            }

            @Override
            public String toString() {
                return "get all groups";
            }
        });
    }

    protected Group newGroupInstance(Node node) throws RepositoryException {
        // remove duplicates
        Collection<String> groups = new HashSet<String>();
        if (node.hasNode(NODE_GROUPS)) {
            for (PropertyIterator iter = new FilteringPropertyIterator(node.getNode(NODE_GROUPS).getProperties(), new JcrAndMgnlPropertyHidingPredicate()); iter.hasNext();) {
                Property subgroup = iter.nextProperty();
                String resources = getResourceName(subgroup.getString());
                if(resources != null){
                    groups.add(resources);
                }
            }
        }
        Collection<String> roles = new HashSet<String>();
        if (node.hasNode(NODE_ROLES)) {
            RoleManager roleMan = SecuritySupport.Factory.getInstance().getRoleManager();
            for (PropertyIterator iter = new FilteringPropertyIterator(node.getNode(NODE_ROLES).getProperties(), new JcrAndMgnlPropertyHidingPredicate()); iter.hasNext();) {
                Property role = iter.nextProperty();
                try {
                    String roleName = roleMan.getRoleNameById(role.getString());
                    if (roleName != null) {
                        roles.add(roleName);
                    }
                } catch (ItemNotFoundException e) {
                    log.warn("assigned role " + role.getString() + " doesn't exist.");
                }
            }
        }
        MgnlGroup group = new MgnlGroup(node.getIdentifier(), node.getName(), groups, roles);
        return group;
    }

    @Override
    protected Node findPrincipalNode(String principalName, Session session) throws RepositoryException {
        final String where = "where name() = '" + principalName + "'";

        final String statement = "select * from [" + ItemType.GROUP + "] " + where;

        Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.JCR_SQL2);
        NodeIterator iter = query.execute().getNodes();
        Node group = null;
        while (iter.hasNext()) {
            Node node = iter.nextNode();
            // unnecessarily redundant since query is already limited no?
            if (node.isNodeType(ItemType.GROUP.getSystemName())) {
                group = node;
                break;
            }
        }
        if (iter.hasNext()) {
            log.error("More than one group found with name \"{}\"", principalName);
        }
        return group;
    }

    @Override
    protected String getRepositoryName() {
        return RepositoryConstants.USER_GROUPS;
    }

    @Override
    public Group addRole(Group group, String roleName) throws AccessDeniedException {
        try {
            add(group.getName(), roleName, NODE_ROLES);
        } catch (PrincipalNotFoundException e) {
            // group doesn't exist in this GM
            return null;
        }
        return getGroup(group.getName());
    }

    @Override
    public Group addGroup(Group group, String groupName) throws AccessDeniedException {
        try {
            add(group.getName(), groupName, NODE_GROUPS);
        } catch (PrincipalNotFoundException e) {
            // group doesn't exist in this GM
            return null;
        }
        return getGroup(groupName);
    }

    /**
     * Predicate hiding properties prefixed with jcr or mgnl.
     */
    private static class JcrAndMgnlPropertyHidingPredicate extends AbstractPredicate<Property> {

        @Override
        public boolean evaluateTyped(Property property) {
            try {
                String name = property.getName();
                return !name.startsWith(MgnlNodeType.JCR_PREFIX) && !name.startsWith(MgnlNodeType.MGNL_PREFIX);
            } catch (RepositoryException e) {
                // either invalid or not accessible to the current user
                return false;
            }
        }
    }
}
