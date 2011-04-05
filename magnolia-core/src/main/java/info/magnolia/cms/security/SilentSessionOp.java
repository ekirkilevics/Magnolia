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

import info.magnolia.context.MgnlContext;
import info.magnolia.context.MgnlContext.Op;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session operation that just logs all exceptions instead of re-throwing them.
 * @author had
 * @version $Id: $
 * @param <R>
 */
public abstract class SilentSessionOp<R> implements Op<R, RuntimeException> {

    protected static final Logger log = LoggerFactory.getLogger(JCRSessionOp.class);

    private final String repository;

    public SilentSessionOp(String repository) {
        this.repository = repository;
    }

    public R exec() {
        Session session = null;
        try {
            // can't do ... need a session before repo is setup and pico is initialized ...
            session = MgnlContext.getJCRSession(repository);
        } catch (RepositoryException e) {
            log.error("failed to retrieve repository " + repository + " with " + e.getMessage(), e);
        }
        try {
            return doExec(session);
        } catch (Throwable t) {
            log.error("Failed to execute " + toString() + " session operation with " + t.getMessage(), t);
            return null;
        }
    }

    public abstract R doExec(Session session) throws Throwable;
}
