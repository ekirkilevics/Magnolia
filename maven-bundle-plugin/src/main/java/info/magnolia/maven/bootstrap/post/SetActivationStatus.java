/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.maven.bootstrap.post;


import info.magnolia.maven.bootstrap.PostBootstrapper;

/**
 * Used to set the activation status on repositories. We use the util to avoid dependencies
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public class SetActivationStatus implements PostBootstrapper{
    
    /**
     * List of repositories
     */
    private String[] repositories;
    
    private String path = "/";
    
    private boolean activated = true;
    
    public void execute(String webappDir) throws Exception {
        for (int i = 0; i < repositories.length; i++) {
            ActivationStatusUtil.setStatus(repositories[i], path, activated);
        }
    }
    
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    
    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * @return the activated
     */
    public boolean isActivated() {
        return activated;
    }

    
    /**
     * @param activated the activated to set
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    
    /**
     * @return the repositories
     */
    public String[] getRepositories() {
        return repositories;
    }

    
    /**
     * @param repositories the repositories to set
     */
    public void setRepositories(String[] repositories) {
        this.repositories = repositories;
    }
}
