/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.adminInterface;

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
