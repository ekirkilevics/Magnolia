/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs.util;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.jackrabbit.core.NamespaceResolver;
import org.apache.jackrabbit.core.query.xpath.TokenMgrError;
import org.apache.jackrabbit.core.query.xpath.XPathQueryBuilder;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class SimpleSearchTagTest extends TestCase {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SimpleSearchTagTest.class);

    /**
     * Test for GenerateXPathQuery(). Uses Jackrabbit internal XPathQueryBuilder in order to validate the query.
     */
    public void testGenerateXPathQuery() {

        SimpleSearchTag tag = new SimpleSearchTag();
        tag.setQuery("AND test query AND path OR and OR join AND AND test AND OR");

        String xpath = tag.generateXPathQuery();
        log.debug(xpath);

        try {
            NamespaceResolver resolver = null; // session.getNamespaceResolver()
            XPathQueryBuilder.createQuery(xpath, resolver);
        }
        catch (TokenMgrError e) {
            fail("Invalid query: [" + xpath + "] " + e.getMessage());
        }
        catch (Throwable e) {
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
            log.debug(xpath);

            try {
                NamespaceResolver resolver = null; // session.getNamespaceResolver()
                XPathQueryBuilder.createQuery(xpath, resolver);
            }
            catch (TokenMgrError e) {
                fail("Invalid query: ["
                    + xpath
                    + "] "
                    + e.getMessage()
                    + ". Input query was: ["
                    + inputstring.toString()
                    + "]");
            }
            catch (Throwable e) {
                // ok, we are setting a namespace resolver since we are running "off line", so it's normal to see
                // exceptions
            }
        }
    }

}
