/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.cms.security;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;
import info.magnolia.context.MgnlContext.Op;

/**
 * Operation requiring session access. Session is available and guaranteed valid throughout the whole op execution time.
 * @author had
 * @version $Id: $
 * @param <R>
 */
public abstract class SessionOp<R> implements Op<R, RepositoryException> {

    protected static final Logger log = LoggerFactory.getLogger(SessionOp.class);

    private final String repository;

    public SessionOp(String repository) {
        this.repository = repository;
    }

    public R exec() throws RepositoryException {
        Session session = null;
        try {
            // if you need to access repo before all is initialized use OnInitSessionOp instead
            session = MgnlContext.getJCRSession(repository);
        } catch (RepositoryException e) {
            log.error("failed to retrieve repository " + repository + " with " + e.getMessage(), e);
        }
        return exec(session);
    }

    public abstract R exec(Session session) throws RepositoryException;
}
