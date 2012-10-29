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
package info.magnolia.module.admininterface.config;

import info.magnolia.cms.beans.config.ContentRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 */
public class SecurityConfiguration {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    private Map<String, RepositoryConfiguration> repositories = new LinkedHashMap<String, RepositoryConfiguration>();

    private RepositoryConfiguration defaultRepositoryConfiguration;

    private Map<String, RepositoryConfiguration> systemRepositories = new LinkedHashMap<String, RepositoryConfiguration>();

    public RepositoryConfiguration getDefaultRepositoryConfiguration() {
        return this.defaultRepositoryConfiguration;
    }

    public void setDefaultRepositoryConfiguration(RepositoryConfiguration defaultRepositoryConfiguration) {
        this.defaultRepositoryConfiguration = defaultRepositoryConfiguration;
    }

    public Map<String, RepositoryConfiguration> getRepositories() {
        return this.repositories;
    }

    public void setRepositories(Map<String, RepositoryConfiguration> repositories) {
        this.repositories = repositories;
    }

    public void addRepository(String name, RepositoryConfiguration repositoryConfiguration) {
        this.repositories.put(name, repositoryConfiguration);
    }

    public Map<String, RepositoryConfiguration> getSystemRepositories() {
        return this.systemRepositories;
    }

    public void setSystemRepositories(Map<String, RepositoryConfiguration> systemRepositories) {
        this.systemRepositories = systemRepositories;
    }

    public void addSystemRepository(String name, RepositoryConfiguration repository){
        this.systemRepositories.put(name, repository);
    }

    public List getVisibleRepositories(){
        List visibleRepositories = new ArrayList();
        // add all configured repositories
        visibleRepositories.addAll(this.getRepositories().values());

        for (Iterator iter = ContentRepository.getAllRepositoryNames(); iter.hasNext();) {
            String name = (String) iter.next();
            if(!systemRepositories.containsKey(name)){
                if(!this.getRepositories().containsKey(name)){
                    DefaultRepositoryConfigurationWrapper repositoryConfiguration = new DefaultRepositoryConfigurationWrapper(this.getDefaultRepositoryConfiguration());
                    repositoryConfiguration.setName(name);
                    visibleRepositories.add(repositoryConfiguration);
                }
            }
        }
        Collections.sort(visibleRepositories);
        return visibleRepositories;
    }

    /**
     * Delegate everything exept name and label.
     * @author philipp
     */
    static class DefaultRepositoryConfigurationWrapper extends RepositoryConfiguration{
        RepositoryConfiguration repositoryConfiguration;

        public DefaultRepositoryConfigurationWrapper(RepositoryConfiguration repositoryConfiguration) {
            this.repositoryConfiguration = repositoryConfiguration;
        }

        @Override
        public void addAclType(AclTypeConfiguration type) {
            this.repositoryConfiguration.addAclType(type);
        }

        @Override
        public void addPermission(PermissionConfiguration permission) {
            this.repositoryConfiguration.addPermission(permission);
        }

        @Override
        public List getAclTypes() {
            return this.repositoryConfiguration.getAclTypes();
        }

        @Override
        public List getPermissions() {
            return this.repositoryConfiguration.getPermissions();
        }

        @Override
        public boolean isChooseButton() {
            return this.repositoryConfiguration.isChooseButton();
        }

        @Override
        public void setAclTypes(List patternTypes) {
            this.repositoryConfiguration.setAclTypes(patternTypes);
        }

        @Override
        public void setChooseButton(boolean chooseButton) {
            this.repositoryConfiguration.setChooseButton(chooseButton);
        }

        @Override
        public void setPermissions(List permissions) {
            this.repositoryConfiguration.setPermissions(permissions);
        }

        @Override
        public String toViewPattern(String path) {
            return this.repositoryConfiguration.toViewPattern(path);
        }
    }

}
