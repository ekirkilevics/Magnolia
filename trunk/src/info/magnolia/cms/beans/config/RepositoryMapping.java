package info.magnolia.cms.beans.config;


/**
 * Date: Aug 16, 2004
 * Time: 4:03:23 PM
 *
 * @author Sameer Charles
 * @version 2.0
 */



public class RepositoryMapping {


    private String name;
    private String ID;
    private boolean loadOnStartup;



    RepositoryMapping() {}


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

    public boolean isLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }


}


