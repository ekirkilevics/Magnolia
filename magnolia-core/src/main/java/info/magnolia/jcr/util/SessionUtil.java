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
package info.magnolia.jcr.util;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.wrapper.DelegateSessionWrapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session-related utility methods.
 *
 * @version $Id$
 */
public class SessionUtil {

    private final static Logger log = LoggerFactory.getLogger(SessionUtil.class);

    public static boolean hasSameUnderlyingSession(Session first, Session second) {
        return unwrap(first).equals(unwrap(second));
    }

    public static Session unwrap(Session session) {
        return session instanceof DelegateSessionWrapper ? ((DelegateSessionWrapper) session).unwrap() : session;
    }

    /**
     * Return the Node for the Given Path from the given repository. In case of Exception, return null.
     */
    public static Node getNode(String repository, String path) {
        Node res = null;
        Session session;
        if (StringUtils.isBlank(repository) || StringUtils.isBlank(path)) {
            log.debug("getNode returns null because either nodePath: '" + path + "' or repository: '" + repository
                    + "' is empty");
            return res;
        }
        try {
            session = MgnlContext.getJCRSession(repository);
            if (session != null) {
                res = session.getNode(path);
            }
        } catch (RepositoryException e) {
            log.error("Exeption during node Search for nodePath: '" + path + "' in repository: '" + repository + "'", e);
        }
        return res;
    }

    /**
     * Return the Node by the given identifier from the given repository. In case of Exception, return null.
     */
    public static Node getNodeByIdentifier(String repository, String id) {
        Node res = null;
        Session session;
        if (StringUtils.isBlank(repository) || StringUtils.isBlank(id)) {
            log.debug("getNode returns null because either identifier: '" + id + "' or repository: '" + repository
                    + "' is empty");
            return res;
        }
        try {
            session = MgnlContext.getJCRSession(repository);
            if (session != null) {
                res = session.getNodeByIdentifier(id);
            }
        } catch (RepositoryException e) {
            log.error("Exeption during node Search by identifier: '" + id + "' in repository: '" + repository + "'", e);
        }
        return res;
    }
}
