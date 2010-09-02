/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.security.auth.callback;

import info.magnolia.cms.security.User;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A JAAS {@link CallbackHandler} using plain text passwords.
 * @version $Id$
 */
public class CredentialsCallbackHandler implements CallbackHandler {

    protected static Logger log = LoggerFactory.getLogger(CredentialsCallbackHandler.class);

    protected String name;

    protected char[] pswd;

    /**
     * The realm to which we login.
     */
    protected String realm;

    protected User user;

    /**
     * Default constructor required by java security framework.
     */
    public CredentialsCallbackHandler() {
        // do not instanciate with this constructor
    }

    public CredentialsCallbackHandler(String name, char[] pswd) {
        this.name = name;
        this.pswd = pswd;
    }

    public CredentialsCallbackHandler(String name, char[] pswd, String realm) {
        this(name, pswd);
        this.realm = realm;
    }

    /**
     * Handle name and password callbacks called during the JAAS login processing.
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback) callbacks[i]).setName(this.name);
            }
            else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback) callbacks[i]).setPassword(this.pswd);
            }
            else if (callbacks[i] instanceof RealmCallback) {
                ((RealmCallback) callbacks[i]).setRealm(this.realm);
            }
            else if (callbacks[i] instanceof UserCallback) {
                user = ((UserCallback) callbacks[i]).getUser();
            }
            else if (callbacks[i] instanceof TextOutputCallback) {
                TextOutputCallback outputCallback = (TextOutputCallback) callbacks[i];
                switch (outputCallback.getMessageType()) {
                    case TextOutputCallback.INFORMATION:
                        log.info(outputCallback.getMessage());
                        break;
                    case TextOutputCallback.ERROR:
                        log.error(outputCallback.getMessage());
                        break;
                    case TextOutputCallback.WARNING:
                        log.warn(outputCallback.getMessage());
                        break;
                    default:
                        if (log.isDebugEnabled()) {
                            log.debug("Unsupported message type : {}", Integer
                                .toString(outputCallback.getMessageType()));
                            log.debug("Message : {}", outputCallback.getMessage());
                        }
                }
            }
        }
    }


    public User getUser() {
        return this.user;
    }
}
