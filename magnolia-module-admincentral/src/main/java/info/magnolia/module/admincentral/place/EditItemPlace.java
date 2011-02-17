package info.magnolia.module.admincentral.place;

import info.magnolia.module.vaadin.place.Place;


public class EditItemPlace extends Place {

    private String workspace;

    private String path;

    public EditItemPlace(String workspace, String path) {
        this.workspace = workspace;
        this.path = path;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getPath() {
        return path;
    }

}
