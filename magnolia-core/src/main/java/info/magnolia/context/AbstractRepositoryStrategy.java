/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.context;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.util.WorkspaceAccessUtil;
import info.magnolia.stats.JCRStats;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.lang.UnhandledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class AbstractRepositoryStrategy implements RepositoryAcquiringStrategy {

    private static Logger log = LoggerFactory.getLogger(AbstractRepositoryStrategy.class);

    private Map jcrSessions = new HashMap();

    private Map hierarchyManagers = new HashMap();

    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        final String hmAttrName = repositoryId + "_" + workspaceId;
        HierarchyManager hm = (HierarchyManager) hierarchyManagers.get(hmAttrName);

        if (hm == null) {
            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            try {
                hm = util.createHierarchyManager(getUserId(),
                        getRepositorySession(repositoryId, workspaceId),
                        getAccessManager(repositoryId, workspaceId));
                hierarchyManagers.put(hmAttrName, hm);
            }
            catch (RepositoryException e) {
                throw new UnhandledException(e);
            }
        }

        return hm;
    }

    abstract protected String getUserId();

    public QueryManager getQueryManager(String repositoryId, String workspaceId) {
        return this.getHierarchyManager(repositoryId, workspaceId).getQueryManager();
    }

    protected Session getRepositorySession(String repositoryName, String workspaceName) throws LoginException, RepositoryException {
        final String repoSessAttrName = repositoryName + "_" + workspaceName;

        Session jcrSession = (Session) jcrSessions.get(repoSessAttrName);

        if (jcrSession == null) {
            log.debug("creating jcr session {} by thread {}", repositoryName, Thread.currentThread().getName());

            WorkspaceAccessUtil util = WorkspaceAccessUtil.getInstance();
            jcrSession = util.createRepositorySession(util.getDefaultCredentials(), repositoryName, workspaceName);
            jcrSessions.put(repoSessAttrName, jcrSession);
            JCRStats.getInstance().incSessionCount();
        }

        return jcrSession;
    }

    protected void release(boolean checkObservation) {
        log.debug("releasing jcr sessions");
        for (Iterator iter = jcrSessions.values().iterator(); iter.hasNext();) {
            Session session = (Session) iter.next();
            if(session != null && session.isLive()){
                try {
                    final ObservationManager observationManager = session.getWorkspace().getObservationManager();
                    final EventListenerIterator listeners = observationManager.getRegisteredEventListeners();
                    if(!checkObservation || !listeners.hasNext()){
                        session.logout();
                        log.debug("logged out jcr session: {} by thread {}", session, Thread.currentThread().getName());

                        JCRStats.getInstance().decSessionCount();
                    }
                    else{
                        log.warn("won't close session because of registered observation listener {}", session.getWorkspace().getName());
                        if(log.isDebugEnabled()){
                            while(listeners.hasNext()) {
                                EventListener listener = listeners.nextEventListener();
                                log.debug("registered listener {}", listener);
                            }
                        }
                    }
                }
                catch (RepositoryException e) {
                    log.error("can't check if event listeners are registered", e);
                }
            }
            else{
                log.warn("session has been already closed {}", session.getWorkspace().getName());
            }
        }
        hierarchyManagers.clear();
        jcrSessions.clear();
    }

}
