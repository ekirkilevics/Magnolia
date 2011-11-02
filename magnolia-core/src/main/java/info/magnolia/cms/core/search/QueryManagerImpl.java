/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;


/**
 * Wrapping a JCR {@link javax.jcr.query.QueryManager}.
 *
 * @version $Id$
 */

public class QueryManagerImpl implements QueryManager {

    protected javax.jcr.query.QueryManager queryManager;

    protected HierarchyManager hm;

    public QueryManagerImpl(javax.jcr.query.QueryManager queryManager, HierarchyManager hm) {
        this.queryManager = queryManager;
        this.hm = hm;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hm == null) ? 0 : hm.hashCode());
        result = prime * result + ((queryManager == null) ? 0 : queryManager.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QueryManagerImpl other = (QueryManagerImpl) obj;
        if (hm == null) {
            if (other.hm != null)
                return false;
        } else if (!hm.equals(other.hm))
            return false;
        if (queryManager == null) {
            if (other.queryManager != null)
                return false;
        } else if (!queryManager.equals(other.queryManager))
            return false;
        return true;
    }

    @Override
    public Query createQuery(String s, String s1) throws InvalidQueryException, RepositoryException {
        javax.jcr.query.Query query = this.queryManager.createQuery(s, s1);
        return (new QueryImpl(query, this.hm));
    }

    @Override
    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
        javax.jcr.query.Query query = this.queryManager.getQuery(node);
        return (new QueryImpl(query, this.hm));
    }

    @Override
    public String[] getSupportedQueryLanguages() throws RepositoryException {
        return this.queryManager.getSupportedQueryLanguages();
    }

 }
