/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.core.search;

import info.magnolia.cms.core.HierarchyManager;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;


/**
 * Wrapping a JCR {@link Query}.
 * Date: Mar 29, 2005 Time: 2:57:55 PM
 * @author Sameer Charles
 */

public class QueryImpl implements Query {

    protected javax.jcr.query.Query query;

    protected HierarchyManager hm;

    protected QueryImpl(javax.jcr.query.Query query, HierarchyManager hm) {
        this.query = query;
        this.hm = hm;
    }

    public QueryResult execute() throws RepositoryException {
        javax.jcr.query.QueryResult result = this.query.execute();
        QueryResultImpl filteredResult = new QueryResultImpl(result, this.hm);
        return filteredResult;
    }

    public String getStatement() {
        return this.query.getStatement();
    }

    public String getLanguage() {
        return this.query.getLanguage();
    }

    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
        return this.query.getStoredQueryPath();
    }

    public Node storeAsNode(String s) throws ItemExistsException, PathNotFoundException, VersionException,
        ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
        return this.query.storeAsNode(s);
    }
}
