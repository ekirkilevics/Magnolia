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
package info.magnolia.cms.exchange;

import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.Rule;

import javax.jcr.RepositoryException;


/**
 * @author Sameer Charles
 * @version $Revision: 1633 $ ($Author: scharles $)
 */
public interface Syndicator {
    
    /**
     * Set up the syndicator
     * @param user the user to use for the conection to the subscriber
     * @param repositoryName the repository to transmit
     * @param workspaceName the workspace to transmit
     * @param rule the rules defining which nodes to transmit (node types)
     */
    public void init(User user, String repositoryName, String workspaceName, Rule rule);

    /**
     * <p>
     * this will activate specifies page (sub pages) to all configured subscribers
     * </p>
     * @param parent parent under which this page will be activated
     * @param path page to be activated
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void activate(String parent, String path) throws ExchangeException, RepositoryException;

    /**
     * <p>
     * this will activate specifies page (sub pages) to the specified subscribers
     * </p>
     * @param subscriber
     * @param parent parent under which this page will be activated
     * @param path page to be activated
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void activate(Subscriber subscriber, String parent, String path) throws ExchangeException,
        RepositoryException;

    /**
     * @param path , to deactivate
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void deActivate(String path) throws ExchangeException, RepositoryException;

    /**
     * @param path , to deactivate
     * @param subscriber
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void deActivate(Subscriber subscriber, String path) throws ExchangeException, RepositoryException;

}
