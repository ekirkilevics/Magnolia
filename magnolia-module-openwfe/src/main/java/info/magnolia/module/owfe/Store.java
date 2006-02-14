package info.magnolia.module.owfe;

import info.magnolia.cms.core.Content;


/**
 * Date: Oct 14, 2004 Time: 3:23:06 PM
 * @author Sameer Charles
 * @version 2.0
 */
public class Store {
 
    private static Store store = new Store();

    private Content localStore;

    protected Store() {
    }

    public static Store getInstance() {
        return Store.store;
    }

    public void setStore(Content localStore) {
        this.localStore = localStore;
    }

    public Content getStore() {
        return this.localStore;
    }
}
