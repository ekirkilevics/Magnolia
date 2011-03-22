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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;


/**
 * Group manager working directly with JCR API and returning simple groups (no JCR node aware).
 * @author Sameer Charles $Id$
 */
public class MgnlGroupManager extends RepositoryBackedSecurityManager implements GroupManager {
    private static final Logger log = LoggerFactory.getLogger(MgnlGroupManager.class);

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

    public Group getGroup(final String name) throws AccessDeniedException {
        return MgnlContext.doInSystemContext(new SilentSessionOp<Group>(getRepositoryName()) {

            @Override
            public Group doExec(Session session) throws RepositoryException {
                Node groupNode = session.getNode("/" + name);
                return newGroupInstance(groupNode);
            }

            @Override
            public String toString() {
                return "get group " + name;
            }
        });
    }

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

    protected Group newGroupInstance(Node node) throws RepositoryException {
        List<String> groups = new ArrayList<String>();
        for (NodeIterator iter = node.getNode("groups").getNodes();iter.hasNext();) {
            Node subgroup = iter.nextNode();
            groups.add(subgroup.getName());
        }
        List<String> roles = new ArrayList<String>();
        for (NodeIterator iter = node.getNode("roles").getNodes();iter.hasNext();) {
            Node role = iter.nextNode();
            roles.add(role.getName());
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
            if (node.isNodeType(ItemType.USER.getSystemName())) {
                group = node;
                break;
            }
        }
        if (iter.hasNext()) {
            log.error("More than one group found with name [{}] in realm [{}]");
        }
        return group;
    }

    @Override
    protected String getRepositoryName() {
        return ContentRepository.USER_GROUPS;
    }
}
