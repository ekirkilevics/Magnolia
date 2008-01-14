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

import junit.framework.TestCase;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.jackrabbit.core.query.xpath.TokenMgrError;
import org.apache.jackrabbit.core.query.xpath.XPathQueryBuilder;
import org.apache.jackrabbit.name.NamespaceResolver;

/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SimpleSearchTagTest extends TestCase {

    /**
     * Test for GenerateXPathQuery(). Uses Jackrabbit internal XPathQueryBuilder in order to validate the query.
     */
    public void testGenerateXPathQuery() {

        SimpleSearchTag tag = new SimpleSearchTag();
        tag.setQuery("AND test query AND path OR and OR join AND AND test AND OR");

        String xpath = tag.generateXPathQuery();

        try {
            NamespaceResolver resolver = null; // session.getNamespaceResolver()
            XPathQueryBuilder.createQuery(xpath, resolver);
        } catch (TokenMgrError e) {
            fail("Invalid query: [" + xpath + "] " + e.getMessage());
        } catch (Throwable e) {
            // ok, we are setting a namespace resolver since we are running "off line", so it's normal to see exceptions
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
                NamespaceResolver resolver = null; // session.getNamespaceResolver()
                XPathQueryBuilder.createQuery(xpath, resolver);
            } catch (TokenMgrError e) {
                fail("Invalid query: [" + xpath + "] " + e.getMessage() + ". Input query was: [" + inputstring.toString() + "]");
            } catch (Throwable e) {
                // ok, we are setting a namespace resolver since we are running "off line", so it's normal to see
                // exceptions
            }
        }
    }

}
