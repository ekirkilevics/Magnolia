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
package info.magnolia.cms.security.auth.callback;

import info.magnolia.cms.security.Realm;

import javax.security.auth.callback.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The JCR JAAS module uses this callback to get the realm we login into.
 * @author philipp
 * @version $Id$
 */
public class RealmCallback implements Callback {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(RealmCallback.class);

    private String realm = Realm.DEFAULT_REALM;

    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

}
