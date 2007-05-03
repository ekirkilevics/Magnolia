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
package info.magnolia.jaas.sp.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.auth.Entity;
import info.magnolia.jaas.principal.EntityImpl;
import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.api.HierarchyManager;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles $Id$
 */
public class JCRAuthenticationModule extends AbstractLoginModule {

    private static Logger log = LoggerFactory.getLogger(JCRAuthenticationModule.class);

    protected Content user;

    /**
     * Releases all associated memory
     */
    public boolean release() {
        return true;
    }

    /**
     * checks is the credentials exist in the repository
     * @return boolean
     */
    public boolean validateUser() throws FailedLoginException ,LoginException {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            String serverPassword = this.user.getNodeData("pswd").getString().trim();
            // we do not allow users with no password
            if (StringUtils.isEmpty(serverPassword)) return false;
            // plain text server password
            serverPassword = new String(Base64.decodeBase64(serverPassword.getBytes()));
            return StringUtils.equals(serverPassword, new String(this.pswd));
        }
        catch (PathNotFoundException pe) {
            log.debug("Unable to locate user [{}], authentication failed", this.name);
            throw new FailedLoginException(pe.getMessage());
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw new LoginException(re.getMessage());
        }
    }

    /**
     * set user details
     */
    public void setEntity() {
        EntityImpl user = new EntityImpl();
        String language = this.user.getNodeData("language").getString();
        user.addProperty(Entity.LANGUAGE, language);
        user.addProperty(Entity.NAME, this.user.getName());
        user.addProperty(Entity.FULL_NAME, this.user.getTitle());
        user.addProperty(Entity.PASSWORD, new String(this.pswd));
        this.subject.getPrincipals().add(user);
    }

    /**
     * set access control list from the user, roles and groups
     */
    public void setACL() {
    }

}
