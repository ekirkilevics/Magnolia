package info.magnolia.repository;

import java.util.Map;


/**
 * Date: Aug 16, 2004 Time: 4:03:23 PM
 * @author Sameer Charles
 * @version 2.0
 */
public class RepositoryMapping {

    private String name;

    private String ID;

    private String provider;

    private boolean loadOnStartup;

    private Map parameters;

    public RepositoryMapping() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }
}
