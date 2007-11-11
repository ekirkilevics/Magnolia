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
package info.magnolia.cms.security;

import java.util.Collection;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class AbstractUser implements User {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(AbstractUser.class);
    
    protected Subject subject;

    
    public Subject getSubject() {
        return this.subject;
    }

    
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

}
