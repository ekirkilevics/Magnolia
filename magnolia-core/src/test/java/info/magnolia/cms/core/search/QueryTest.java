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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.test.RepositoryTestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;


/**
 * Imports testdata into the repository during {@link #setUp()} and runs various queries against it
 * using the Magnolia Query API.
 * @author jfrantzius
 *
 */
public class QueryTest extends RepositoryTestCase {


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("info/magnolia/cms/core/search/website.level1.xml");
        DataTransporter.importXmlStream(
            xmlStream,
            "website",
            "/",
            "name matters only when importing a file that needs XSL transformation",
            false,
            ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
            true,
            true);
    }

    /**
     * Simple query for the single content object with template "home"
     */
    public void testSimpleQuery() throws InvalidQueryException, RepositoryException, IOException {
        String path = "/level1/level2";
        String templateName = "home";

        String sql = createQueryString(path, templateName);

        QueryManager qm = MgnlContext.getQueryManager("website");
        Query query = qm.createQuery(sql, Query.SQL);
        QueryResult magnoliaQueryResult = query.execute();
        Collection<Content> foundContent = magnoliaQueryResult.getContent();
        assertFalse(foundContent.isEmpty());
        assertTrue(foundContent.size() == 1);
        assertTrue(foundContent.iterator().next().getName().equals("homepage"));
    }

    public void testMaxResultSize() throws InvalidQueryException, RepositoryException {
        String path = "/level1/level2";
        String templateName = "teaserPage1";

        String sql = createQueryString(path, templateName);

        // first query the full result
        QueryManager qm = MgnlContext.getQueryManager("website");
        Query query = qm.createQuery(sql, Query.SQL);
        QueryResult fullQueryResult = query.execute();
        Collection<Content> fullResult = fullQueryResult.getContent();
        assertTrue(fullResult.size() == 5);
        // do same using QueryUtil (
        fullResult = QueryUtil.query("website", sql, Query.SQL, ItemType.CONTENT.getSystemName());
        assertTrue(fullResult.size() == 5);

        // now restrict to maxResultSize==3
        query.setLimit(3);
        QueryResult restrictedQueryResult = query.execute();
        Collection<Content> restrictedResult = restrictedQueryResult.getContent();
        verifyRestrictedResult(fullResult, restrictedResult, 3);
        // do same using QueryUtil
        restrictedResult = QueryUtil.query("website", sql, Query.SQL, ItemType.CONTENT.getSystemName(), 3);
        verifyRestrictedResult(fullResult, restrictedResult, 3);
    }

    private void verifyRestrictedResult(Collection<Content> full, Collection<Content> restricted, int size) {
        assertTrue(restricted.size() == size);

        // make sure restricted is a prefix of full
        Iterator<Content> iterator = full.iterator();
        for (Content content : restricted) {
            assertEquals(content.getHandle(), iterator.next().getHandle());
        }
    }

    private String createQueryString(String path, String templateName) {
        String sql = "select * from nt:base where jcr:path like '" + path + "/%' and mgnl:template = '" + templateName
            + "'";
        return sql;
    }
}
