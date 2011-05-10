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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;

/**
 * Mock implementation of JCR Query Result.
 * @author had
 * @version $Id: $
 */
public class MockQueryResult implements QueryResult {

    private final Session session;
    private String type;
    private String name;

    public MockQueryResult(Session session, String statement, String language) {
        this.session = session;
        type = StringUtils.substringBefore(StringUtils.substringAfter(statement.toLowerCase(), " from "), " where ").trim();

        if ("JCR-SQL2".equals(language)) {
            // strip off square brackets required to encapsulate type for sql2 queries
            type = type.substring(1, type.length() -1 );

            String where = StringUtils.substringAfter(statement, "where").trim();
            if (where.indexOf("name()") != -1) {
                // try to get name we are searching for
                name = StringUtils.substringBefore(StringUtils.substringAfter(StringUtils.substringAfter(where, "name()"), "'"), "'");
            }
        }
    }

    @Override
    public String[] getColumnNames() throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        List<Node> results = new ArrayList<Node>();
        // mocking up real search qould be a pain, so for now we just return all ... could be substituted for op that will return expected results
        addChildren(session.getRootNode().getNodes(), results);
        return new MockJCRNodeIterator(results);
    }

    private void addChildren(NodeIterator nodes, List<Node> results) throws RepositoryException {
        for (NodeIterator iter = nodes; iter.hasNext();) {
            Node node = iter.nextNode();
            if (results.contains(node)) {
                // this will skip shared nodes only as long as they are part of the result set!
                continue;
            }
            if (type.equals(node.getPrimaryNodeType().getName())) {
                if (name == null || name.equals(node.getName())) {
                    results.add(node);
                }
            }
            addChildren(node.getNodes(), results);
        }

    }

    @Override
    public RowIterator getRows() throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getSelectorNames() throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

}
