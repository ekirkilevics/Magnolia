/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.cms.taglibs.util;

import javax.jcr.NamespaceException;
import javax.jcr.query.InvalidQueryException;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.jackrabbit.core.query.DefaultQueryNodeFactory;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.query.xpath.XPathQueryBuilder;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.conversion.IllegalNameException;
import org.apache.jackrabbit.spi.commons.conversion.NameResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;

/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SimpleSearchTagTest extends TestCase {

    /**
     *
     */
    private static final NameResolver DUMMY_NAME_RESOLVER = new NameResolver(){
                public String getJCRName(Name name) throws NamespaceException {
                    return "jcr:" + name.getLocalName();
                }
                public Name getQName(final String name) throws IllegalNameException, NamespaceException {
                    return NameFactoryImpl.getInstance().create(Name.NS_JCR_URI, StringUtils.substringAfter(name, ":"));
                }
            };
    /**
     *
     */
    private static final DefaultQueryNodeFactory DUMMY_QUERY_NODE_FACTORY = new DefaultQueryNodeFactory(SearchIndex.VALID_SYSTEM_INDEX_NODE_TYPE_NAMES);

    /**
     * Test for GenerateXPathQuery(). Uses Jackrabbit internal XPathQueryBuilder in order to validate the query.
     * @throws InvalidQueryException
     */
    public void testGenerateXPathQuery(){

        SimpleSearchTag tag = new SimpleSearchTag();
        tag.setQuery("AND test query AND path OR and OR join AND AND test AND OR");

        String xpath = tag.generateXPathQuery();

        try {
            XPathQueryBuilder.createQuery(xpath, DUMMY_NAME_RESOLVER, DUMMY_QUERY_NODE_FACTORY);
        }
        catch (InvalidQueryException e) {
            fail("Invalid query: [" + xpath + "] " + e.getMessage());
        }
    }

    /**
     * Test for GenerateXPathQuery(). Uses Jackrabbit internal XPathQueryBuilder in order to validate the query.
     */
    public void testGenerateXPathQuerySmokeTest() {

        SimpleSearchTag tag = new SimpleSearchTag();

        for (int j = 0; j < 100; j++) {

            StringBuffer inputstring = new StringBuffer(100);
            for (int u = 0; u < 10; u++) {
                inputstring.append(RandomStringUtils.random(RandomUtils.nextInt(10)));
                inputstring.append(" ");
                inputstring.append(RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(10)));
                inputstring.append(" ");
            }
            tag.setQuery(inputstring.toString());
            String xpath = tag.generateXPathQuery();

            try {
                XPathQueryBuilder.createQuery(xpath, DUMMY_NAME_RESOLVER, DUMMY_QUERY_NODE_FACTORY);
            } catch (InvalidQueryException e) {
                fail("Invalid query: [" + xpath + "] " + e.getMessage() + ". Input query was: [" + inputstring.toString() + "]");
            }
        }
    }

}
