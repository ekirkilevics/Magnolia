/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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

    private Map repositories = new LinkedHashMap();

    private RepositoryConfiguration defaultRepositoryConfiguration;

    private Map systemRepositories = new LinkedHashMap();

    public RepositoryConfiguration getDefaultRepositoryConfiguration() {
        return this.defaultRepositoryConfiguration;
    }

    public void setDefaultRepositoryConfiguration(RepositoryConfiguration defaultRepositoryConfiguration) {
        this.defaultRepositoryConfiguration = defaultRepositoryConfiguration;
    }

    public Map getRepositories() {
        return this.repositories;
    }

    public void setRepositories(Map repositories) {
        this.repositories = repositories;
    }

    public void addRepository(String name, RepositoryConfiguration repositoryConfiguration) {
        this.repositories.put(name, repositoryConfiguration);
    }

    public Map getSystemRepositories() {
        return this.systemRepositories;
    }

    public void setSystemRepositories(Map systemRepositories) {
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

        public void addAclType(AclTypeConfiguration type) {
            this.repositoryConfiguration.addAclType(type);
        }

        public void addPermission(PermissionConfiguration permission) {
            this.repositoryConfiguration.addPermission(permission);
        }

        public List getAclTypes() {
            return this.repositoryConfiguration.getAclTypes();
        }

        public List getPermissions() {
            return this.repositoryConfiguration.getPermissions();
        }

        public boolean isChooseButton() {
            return this.repositoryConfiguration.isChooseButton();
        }

        public void setAclTypes(List patternTypes) {
            this.repositoryConfiguration.setAclTypes(patternTypes);
        }

        public void setChooseButton(boolean chooseButton) {
            this.repositoryConfiguration.setChooseButton(chooseButton);
        }

        public void setPermissions(List permissions) {
            this.repositoryConfiguration.setPermissions(permissions);
        }

        public String toViewPattern(String path) {
            return this.repositoryConfiguration.toViewPattern(path);
        }
    }

}
