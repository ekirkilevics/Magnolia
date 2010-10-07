/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.context;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.SystemUserManager;
import info.magnolia.cms.util.UrlPattern;
import info.magnolia.cms.util.WorkspaceAccessUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a single full access AccessManager. JCR sessions are only released if no event listener were registered.
 */
public class SystemRepositoryStrategy extends AbstractRepositoryStrategy {

    private static final Logger log = LoggerFactory.getLogger(SystemRepositoryStrategy.class);

    private AccessManager accessManager;

    private Map<String, EventListener> observedHMs = new HashMap<String, EventListener>();

    public SystemRepositoryStrategy(SystemContext context) {
    }

    public AccessManager getAccessManager(String repositoryId, String workspaceId) {
        if (accessManager == null) {
            accessManager = WorkspaceAccessUtil.getInstance().createAccessManager(getSystemPermissions(), repositoryId, workspaceId);
        }

        return accessManager;
    }

    protected List<Permission> getSystemPermissions() {
        List<Permission> acl = new ArrayList<Permission>();
        UrlPattern p = UrlPattern.MATCH_ALL;
        Permission permission = new PermissionImpl();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL);
        acl.add(permission);
        return acl;
    }

    protected String getUserId() {
        return SystemUserManager.SYSTEM_USER;
    }

    public void release() {
        if (!observedHMs.isEmpty()) {
            for (Map.Entry<String, EventListener> entry : observedHMs.entrySet()) {
                final String[] key = entry.getKey().split("_");
                final HierarchyManager hm = super.getHierarchyManager(key[0], key[1]);
                try {
                    hm.getWorkspace().getObservationManager().removeEventListener(entry.getValue());
                } catch (UnsupportedRepositoryOperationException e) {
                    log.error("Failed to remove listener from short living session. Session doesn't support listener removal.");
                } catch (RepositoryException e) {
                    log.error("Failed to remove listener from short living session. Will not be able to release this session.");
                }
            }
            // release reference to listener so it can be GCed
            observedHMs.clear();
        }
        super.release(true);
    }

    @Override
    public HierarchyManager getHierarchyManager(String repositoryId, String workspaceId) {
        final HierarchyManager hm = super.getHierarchyManager(repositoryId, workspaceId);
        final String key = repositoryId + "_" + workspaceId;
        if (!observedHMs.keySet().contains(key)) {
            final EventListener listener = new EventListener() {

                public void onEvent(EventIterator iterator) {
                    // reload everything
                    try {
                        hm.refresh(true);
                    } catch (RepositoryException e) {
                        log.error("Failed to refresh short living session after update. Session will not be able to see content changes if repository uses update-on-read strategy.");
                    }
                }
            };
            try {
                hm.getWorkspace().getObservationManager().addEventListener(listener, Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED, "/", true, null, null, false);
                observedHMs.put(key, listener);
            } catch (UnsupportedRepositoryOperationException e) {
                log.warn("Repository doesn't support observation. Observers will not be notified of changes in repository.");
            } catch (RepositoryException e) {
                log.error("Failed to register observer for repository updates.");
            }
        }
        return hm;
    }
}
