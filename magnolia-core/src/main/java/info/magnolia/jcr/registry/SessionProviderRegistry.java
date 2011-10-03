/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.jcr.registry;

import info.magnolia.registry.RegistrationException;
import info.magnolia.repository.RepositoryNameMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central registry of all SessionsProviders.
 *
 * @version $Id$
 */
@Singleton
public class SessionProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(SessionProviderRegistry.class);

    private final Map<String, RepositoryNameMap> logical2PhysicalWorkspaceMapping = new HashMap<String, RepositoryNameMap>();
    private final Map<String, SessionProvider> repository2SessionProviderMapping = new HashMap<String, SessionProvider>();

    protected Map<String, SessionProvider> getSessionProviders() {
        return repository2SessionProviderMapping;
    }

    public void register(SessionProvider provider) {
        synchronized (repository2SessionProviderMapping) {
            repository2SessionProviderMapping.put(provider.getLogicalWorkspaceName(), provider);
        }
    }

    public void unregisterSessionProvider(String repositoryName) {
        synchronized (repository2SessionProviderMapping) {
            repository2SessionProviderMapping.remove(repositoryName);
        }
    }

    public SessionProvider getSessionProvider(String repositoryName) throws RegistrationException {
        SessionProvider provider;
        synchronized (repository2SessionProviderMapping) {
            provider = repository2SessionProviderMapping.get(repositoryName);
            if (provider == null) {
                List<String> types = new ArrayList<String>(repository2SessionProviderMapping.keySet());
                Collections.sort(types);
                throw new RegistrationException("Can't find a registration for logical workspaceName [" + repositoryName + "]. Registered workspaces are " + types);
            }
        }
        return provider;
    }

    /**
     * Add a mapping from a logicalWorkspaceName to a physical repo/workspaceName.
     */
    public void addLogical2PhysicalWorkspaceMapping(String logicalWorkspaceName, String repositoryName, String physicalWorkspaceName) {
        String workspaceName =  StringUtils.isEmpty(physicalWorkspaceName) ? logicalWorkspaceName : physicalWorkspaceName;
        RepositoryNameMap nameMap = new RepositoryNameMap(repositoryName, workspaceName);
        synchronized (logical2PhysicalWorkspaceMapping) {
            logical2PhysicalWorkspaceMapping.put(logicalWorkspaceName, nameMap);
        }
    }

    public Iterator<String> getAllLogicalWorkspaceNames() {
        return logical2PhysicalWorkspaceMapping.keySet().iterator();
    }

    public boolean hasMappingFor(String logicalWorkspaceName) {
        return logical2PhysicalWorkspaceMapping.containsKey(logicalWorkspaceName);
    }

    public String getRepositoryNameFor(String logicalWorkspaceName) {
        RepositoryNameMap nameMap = logical2PhysicalWorkspaceMapping.get(logicalWorkspaceName);
        return (nameMap==null) ? logicalWorkspaceName : nameMap.getRepositoryName();
    }

    public String getWorkspaceNameFor(String logicalWorkspaceName) {
        RepositoryNameMap nameMap = logical2PhysicalWorkspaceMapping.get(logicalWorkspaceName);
        return (nameMap == null) ? logicalWorkspaceName : nameMap.getWorkspaceName();
    }

    public String getLogicalWorkspaceNameFor(String physicalWorkspaceName) {
        Iterator<String> keys = logical2PhysicalWorkspaceMapping.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            RepositoryNameMap nameMap = logical2PhysicalWorkspaceMapping.get(key);
            if (nameMap.getWorkspaceName().equalsIgnoreCase(physicalWorkspaceName)) {
                return key;
            }
        }
        // TODO dlipp: logging an error & continue with a "guessed" value doesn't see right.
        log.error("No Repository/Workspace name mapping defined for "+physicalWorkspaceName);
        return physicalWorkspaceName;
    }

    public void clear() {
        logical2PhysicalWorkspaceMapping.clear();
        repository2SessionProviderMapping.clear();
    }
}
