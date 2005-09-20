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
package info.magnolia.jaas.sp.jcr;

import org.apache.log4j.Logger;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.beans.config.ContentRepository;

/**
 * This is a default login module for magnolia, it uses initialized repository as
 * defined by the provider interface
 *
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class JCRAuthorizationModule extends JCRLoginModule {

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(JCRAuthorizationModule.class);

    /**
     * checks if the user exist in the repository
     * @return boolean
     */
    public boolean isValidUser() {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.USERS);
        try {
            this.user = hm.getContent(this.name);
            return true;
        } catch (PathNotFoundException pe) {
            pe.printStackTrace();
            log.info("Unable to locate user [" + this.name + "], authentication failed");
        } catch (RepositoryException re) {
            re.printStackTrace();
            log.error("Unable to locate user ["
                + this.name
                + "], authentication failed due to a "
                + re.getClass().getName(), re);
        }
        return false;
    }

}
