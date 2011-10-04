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
import info.magnolia.repository.Provider;
import info.magnolia.repository.RepositoryNameMap;

import java.util.HashMap;
import java.util.Iterator;
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

    /**
     * RepositoryMapping's as defined in repository.xml.
     */
    private final Map<String, RepositoryNameMap> logical2PhysicalWorkspaceMapping = new HashMap<String, RepositoryNameMap>();

    /**
     * JCR providers as mapped in repositories.xml.
     */
    private final Map<String, Provider> repository2ProviderMapping = new HashMap<String, Provider>();

    protected Map<String, Provider> getProviders() {
        return repository2ProviderMapping;
    }

    public void register(String repositoryName, Provider provider) {
        synchronized (repository2ProviderMapping) {
            repository2ProviderMapping.put(repositoryName, provider);
        }
    }

    public void unregisterProvider(String repositoryName) {
        synchronized (repository2ProviderMapping) {
            repository2ProviderMapping.remove(repositoryName);
        }
    }

    public Provider getProvider(String repositoryName) throws RegistrationException {
        Provider provider = repository2ProviderMapping.get(repositoryName);
        if (provider == null) {
            final String mappedRepositoryName = getRepositoryNameFor(repositoryName);
            if (mappedRepositoryName != null) {
                provider = repository2ProviderMapping.get(mappedRepositoryName);
            }
            if (provider == null) {
                final String s = "Failed to retrieve repository provider '" + repositoryName + "' (mapped as '" + mappedRepositoryName + "'). Your Magnolia instance might not have been initialized properly.";
                throw new RegistrationException(s);
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
        repository2ProviderMapping.clear();
    }

    public void shutdown() {
        log.info("Shutting down JCR");
        final Iterator<Provider> providers = repository2ProviderMapping.values().iterator();
        while (providers.hasNext()) {
            final Provider provider = providers.next();
            provider.shutdownRepository();
        }
        repository2ProviderMapping.clear();
    }
}
