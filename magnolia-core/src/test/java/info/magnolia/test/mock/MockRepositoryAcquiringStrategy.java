/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.test.mock;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.RepositoryAcquiringStrategy;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockRepositoryAcquiringStrategy implements RepositoryAcquiringStrategy {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockRepositoryAcquiringStrategy.class);

    private final Map<String, HierarchyManager> hierarchyManagers = new HashMap<String, HierarchyManager>();

    private final Map<String, Session> sessions = new HashMap<String, Session>();

    @Override
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        if(!hierarchyManagers.containsKey(repositoryId)){
            throw new IllegalArgumentException("repository [" + repositoryId + "] not initialized");
        }
        return hierarchyManagers.get(repositoryId);
    }

    public void addHierarchyManager(String repositoryId, HierarchyManager hm){
        hierarchyManagers.put(repositoryId, hm);
    }

    @Override
    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        return null;
    }


    @Override
    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return null;
    }

    @Override
    public void release() {
    }

    @Override
    public Session getSession(String repositoryId, String workspaceId) {
        if(!sessions.containsKey(repositoryId)){
            throw new IllegalArgumentException("session [" + repositoryId + "] not initialized");
        }
        return sessions.get(repositoryId);
    }

    public void addSession(String repositoryId, Session session) {
        sessions.put(repositoryId, session);
    }
}
