/**
 * This file Copyright (c) 2009-2010 Magnolia International
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
package info.magnolia.module.delta;

import info.magnolia.cms.filters.ContextFilter;
import junit.framework.TestCase;

import java.util.ArrayList;

import info.magnolia.cms.util.WebXmlUtil;
import info.magnolia.module.InstallContext;
import static org.easymock.EasyMock.*;
/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlConditionsUtilTest extends TestCase {
    public void testWarnsIfIncludeDispatcherIsUsed() {
        doTestFilterDispatchersConditions("web_filterwrongdispatchers.xml", "webxmltest.WithInclude", true, true);
    }

    public void testDoesNotWarnIfErrorDispatcherIsUsed() {
        doTestFilterDispatchersConditions("web_filterwrongdispatchers.xml", "webxmltest.ErrorIsNotMandatory", false, true);
    }

    public void testBlocksIfRequestDispatcherIsMissing() {
        doTestFilterDispatchersConditions("web_filterwrongdispatchers.xml", "webxmltest.WithMissingRequst", false, false);
    }

    private void doTestFilterDispatchersConditions(String webxmlResource, String fakeFilterClass, boolean shouldWarn, boolean expectedResult) {
        final ArrayList conditions = new ArrayList();
        final WebXmlUtil webxml = new WebXmlUtil(WebXmlUtil.class.getResourceAsStream(webxmlResource));
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(webxml, conditions);
        u.filterMustBeRegisteredWithCorrectDispatchers(fakeFilterClass);

        assertEquals(1, conditions.size());
        final Condition condition = (Condition) conditions.get(0);

        final InstallContext installContext = createStrictMock(InstallContext.class);
        if (shouldWarn) {
            installContext.warn(isA(String.class));
        }
        replay(installContext);
        assertEquals(expectedResult, condition.check(installContext));
        verify(installContext);
    }

    public void testFalseConditionWhenContextFilterMissing() {

        assertWebXmlResultsInCondition(
                "web-xml-without-context-filter.xml",
                FalseCondition.class,
                "web.xml updates",
                "Since Magnolia 4.4, the Magnolia context filter " + ContextFilter.class.getName() + " must be mapped in web.xml before MgnlMainFilter with dispatchers REQUEST, FORWARD, INCLUDE and, optionally, ERROR."
                    + " Please add the following to your web.xml file:\n" +
                    " <filter>\n" +
                    "   <display-name>Magnolia context filter</display-name>\n" +
                    "   <filter-name>magnoliaContextFilter</filter-name>\n" +
                    "   <filter-class>info.magnolia.cms.filters.ContextFilter</filter-class>\n" +
                    " </filter>\n" +
                    " <filter-mapping>\n" +
                    "   <filter-name>magnoliaContextFilter</filter-name>\n" +
                    "   <url-pattern>/*</url-pattern>\n" +
                    "   <dispatcher>REQUEST</dispatcher>\n" +
                    "   <dispatcher>FORWARD</dispatcher>\n" +
                    "   <dispatcher>INCLUDE</dispatcher>\n" +
                    "   <dispatcher>ERROR</dispatcher>\n" +
                    " </filter-mapping>");

        assertWebXmlResultsInCondition(
                "web-xml-with-context-filter.xml",
                TrueCondition.class,
                "web.xml updates",
                "Since Magnolia 4.4, the Magnolia context filter " + ContextFilter.class.getName() + " must be mapped in web.xml before MgnlMainFilter with dispatchers REQUEST, FORWARD, INCLUDE and, optionally, ERROR.");

        assertWebXmlResultsInCondition(
                "web-xml-with-context-filter-but-additional-dispatcher.xml",
                WarnCondition.class,
                "web.xml updates",
                "Since Magnolia 4.4, the Magnolia context filter " + ContextFilter.class.getName() + " must be mapped in web.xml before MgnlMainFilter with dispatchers REQUEST, FORWARD, INCLUDE and, optionally, ERROR.");

        assertWebXmlResultsInCondition(
                "web-xml-with-context-filter-but-missing-dispatcher.xml",
                FalseCondition.class,
                "web.xml updates",
                "Since Magnolia 4.4, the Magnolia context filter " + ContextFilter.class.getName() + " must be mapped in web.xml before MgnlMainFilter with dispatchers REQUEST, FORWARD, INCLUDE and, optionally, ERROR."
                + " Please add the following to your web.xml file:\n"
                + " <dispatcher>REQUEST</dispatcher>\n"
                + " <dispatcher>FORWARD</dispatcher>\n"
                + " <dispatcher>INCLUDE</dispatcher>\n"
                + " <dispatcher>ERROR</dispatcher>");
    }

    private void assertWebXmlResultsInCondition(String webxmlfile, Class<? extends Condition> conditionClass, String conditionName, String conditionDescription) {
        final ArrayList<Condition> conditions = new ArrayList<Condition>();
        final WebXmlUtil webxml = new WebXmlUtil(WebXmlUtil.class.getResourceAsStream(webxmlfile));
        final WebXmlConditionsUtil u = new WebXmlConditionsUtil(webxml, conditions);
        u.contextFilterMustBeRegisteredWithCorrectDispatchers(ContextFilter.class.getName());
        assertEquals(1, conditions.size());
        Condition condition = conditions.get(0);
        assertTrue(conditionClass.isAssignableFrom(condition.getClass()));
        assertEquals(conditionName, condition.getName());
        assertEquals(conditionDescription, condition.getDescription());
    }
}
