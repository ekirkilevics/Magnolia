package info.magnolia.maven.bootstrap.post;


import info.magnolia.maven.bootstrap.PostBootstrapper;


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
