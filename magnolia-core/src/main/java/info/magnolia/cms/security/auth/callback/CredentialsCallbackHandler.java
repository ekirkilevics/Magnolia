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
 * Plain text callback handler
 * @author Sameer Charles
 * @version $Id$
 */
public class CredentialsCallbackHandler implements CallbackHandler {

    /**
     * Logger
     */
    protected static Logger log = LoggerFactory.getLogger(CredentialsCallbackHandler.class);

    /**
     * user id
     */
    protected String name;

    /**
     * password
     */
    protected char[] pswd;

    /**
     * The realm to which we login
     */
    protected String realm;

    protected User user;

    /**
     * default constructor required by java security framework
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
     * handle name and password callback which must be set while constructing this handler
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
