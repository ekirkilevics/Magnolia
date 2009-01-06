/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.exchange;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.Rule;

import javax.jcr.RepositoryException;
import java.util.List;


/**
 * @author Sameer Charles
 * $Id: Syndicator.java 6443 2006-09-21 10:14:34Z scharles $
 */
public interface Syndicator {

    /**
     * Sets up the syndicator.
     *
     * @param user the user to use for the conection to the subscriber
     * @param repositoryName the repository to transmit
     * @param workspaceName the workspace to transmit
     * @param rule the rules defining which nodes to transmit (node types)
     */
    public void init(User user, String repositoryName, String workspaceName, Rule rule);

    /**
     * This will activate the specified node to all configured subscribers.
     *
     * @param parent parent under which this page will be activated
     * @param content to be activated
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void activate(String parent, Content content)
            throws ExchangeException, RepositoryException;

    /**
     * This will activate the specified node to all configured subscribers.
     *
     * @param parent parent under which this page will be activated
     * @param content to be activated
     * @param orderBefore List of UUID to be used by the implementation to order this node after activation
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void activate(String parent, Content content, List orderBefore)
            throws ExchangeException, RepositoryException;

    /**
     * This will activate the specified node to the specified subscriber.
     *
     * @param subscriber
     * @param parent parent under which this page will be activated
     * @param content to be activated
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void activate(Subscriber subscriber, String parent, Content content)
            throws ExchangeException, RepositoryException;

    /**
     * This will activate the specified node to the specified subscriber.
     *
     * @param subscriber
     * @param parent parent under which this page will be activated
     * @param content to be activated
     * @param orderBefore List of UUID to be used by the subscriber to order this node after activation
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void activate(Subscriber subscriber, String parent, Content content, List orderBefore)
            throws ExchangeException, RepositoryException;

    /**
     * @param node to deactivate
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void deactivate(Content node)
            throws ExchangeException, RepositoryException;

    /**
     * @param node to deactivate
     * @param subscriber
     * @throws RepositoryException
     * @throws ExchangeException
     */
    public void deactivate(Subscriber subscriber, Content node)
            throws ExchangeException, RepositoryException;


}
