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
package info.magnolia.test.mock;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

/**
 * Mock Implementation of the JCR Query.
 * @author had
 * @version $Id: $
 */
public class MockQuery implements Query {

    private final String language;
    private final String statement;
    private final Session session;

    public MockQuery(Session session, String statement, String language) {
        this.session = session;
        this.statement = statement;
        this.language = language;

    }

    public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException {
        // TODO Auto-generated method stub

    }

    public QueryResult execute() throws InvalidQueryException, RepositoryException {
        return new MockQueryResult(session, statement, language);
    }

    public String[] getBindVariableNames() throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLanguage() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStatement() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLimit(long limit) {
        // TODO Auto-generated method stub

    }

    public void setOffset(long offset) {
        // TODO Auto-generated method stub

    }

    public Node storeAsNode(String absPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException,
    UnsupportedRepositoryOperationException, RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

}
