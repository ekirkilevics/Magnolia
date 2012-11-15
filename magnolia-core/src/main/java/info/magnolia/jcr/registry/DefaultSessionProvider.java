/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.jcr.registry;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.jcr.RuntimeRepositoryException;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Default-Implementation for a {@link SessionProvider}.
 *
 * @version $Id$
 */
public class DefaultSessionProvider implements SessionProvider {

    private final String logicalWorkspaceName;
    private final Repository repository;
    private final String physicalWorkspaceName;

    public DefaultSessionProvider(String logicalWorkspaceName, Repository repository, String physicalWorkspaceName) {
        this.logicalWorkspaceName = logicalWorkspaceName;
        this.repository = repository;
        this.physicalWorkspaceName = physicalWorkspaceName;
    }

    @Override
    public String getLogicalWorkspaceName() {
        return logicalWorkspaceName;
    }

    @Override
    public Session createSession(Credentials credentials) {
        Session session;
        try {
            session = repository.login(credentials, ContentRepository.getMappedWorkspaceName(physicalWorkspaceName));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return session;
    }
}
