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
package info.magnolia.module.delta;

import info.magnolia.cms.util.WebXmlUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A utility class for web.xml related conditions, which will add
 * conditions to a given list of tasks based on some conditions.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlConditionsUtil {
    private final WebXmlUtil webXmlUtil;
    private final List<Condition> conditions;

    public WebXmlConditionsUtil(List<Condition> conditions) {
        this(new WebXmlUtil(), conditions);
    }

    WebXmlConditionsUtil(WebXmlUtil webXmlUtil, List<Condition> conditions) {
        this.webXmlUtil = webXmlUtil;
        this.conditions = conditions;
    }

    public void servletIsNowWrapped(final String servletName) {
        if (webXmlUtil.isServletOrMappingRegistered(servletName)) {
            conditions.add(new FalseCondition("web.xml updates", "Since Magnolia 3.5, servlets are wrapped in ServletDispatchingFilter; please remove the <servlet> and <servlet-mapping> elements for " + servletName + " from your web.xml file."));
        }
    }

    public void servletIsDeprecated(final String servletName) {
        if (webXmlUtil.isServletOrMappingRegistered(servletName)) {
            conditions.add(new FalseCondition("web.xml updates", "The " + servletName + " servlet is deprecated and not in use; please remove the corresponding <servlet> and <servlet-mapping> elements from your web.xml file."));
        }
    }

    public void servletIsRemoved(final String servletName) {
        if (webXmlUtil.isServletOrMappingRegistered(servletName)) {
            conditions.add(new FalseCondition("web.xml updates", "The " + servletName + " servlet does not exist anymore; please remove the corresponding <servlet> and <servlet-mapping> elements from your web.xml file."));
        }
    }
    
    public void servletIsPresent(String servletName) {
        if (!webXmlUtil.isServletRegistered(servletName)) {
            conditions.add(new FalseCondition("web.xml updates", "The " + servletName + " servlet is not installed. Please configure a <servlet> and an appropriate mapping with name " + servletName + " in your web.xml file."));
        }
    }

    public void filterMustBeRegisteredWithCorrectDispatchers(final String filterClass) {
        final String conditionName = "web.xml updates";
        final String message = "Since Magnolia 3.5, the main Magnolia filter is " + filterClass + ", and it must be mapped with dispatchers REQUEST, FORWARD and, optionally, ERROR. The INCLUDE dispatcher is not supported.";
        final String additionalMessage = " Please add \n"
                + " <dispatcher>REQUEST</dispatcher>\n"
                + " <dispatcher>FORWARD</dispatcher>\n"
                + " <dispatcher>ERROR</dispatcher>\n"
                + " to the filter-mapping element in your web.xml file.";

        if (!webXmlUtil.isFilterRegistered(filterClass)) {
            conditions.add(new FalseCondition(conditionName, message));
        } else {
            final int result = webXmlUtil.checkFilterDispatchersConfiguration(filterClass, Arrays.asList(new String[]{"REQUEST", "FORWARD"}), Collections.singletonList("ERROR"));
            if (result > 0) {
                conditions.add(new TrueCondition(conditionName, message));
            } else if (result == 0) {
                conditions.add(new WarnCondition(conditionName, message));
            } else if (result < 0) {
                conditions.add(new FalseCondition(conditionName + additionalMessage, message));
            }
        }
    }

    public void filterIsDeprecated(final String deprecatedFilterClass, final String replacementFilterClass) {
        if (webXmlUtil.isFilterRegistered(deprecatedFilterClass)) {
            conditions.add(new FalseCondition("web.xml updates", "The " + deprecatedFilterClass + " filter class is now deprecated. Please replace it with " + replacementFilterClass + ": please update the corresponding <filter-class> element in your web.xml file."));
        }
    }

    public void listenerIsDeprecated(final String deprecatedListenerClass, final String replacementListenerClass) {
        if (webXmlUtil.isListenerRegistered(deprecatedListenerClass)) {
            conditions.add(new FalseCondition("web.xml updates", "The " + deprecatedListenerClass + " listener class is now deprecated. Please replace it with " + replacementListenerClass + ": please update the corresponding <listener> element in your web.xml file."));
        }
    }

    public void listenerMustBeRegistered(String listenerClass) {
        if (!webXmlUtil.isListenerRegistered(listenerClass)) {
            conditions.add(new FalseCondition("web.xml updates", "The " + listenerClass + " listener is not installed. Please configure a <listener> with class " + listenerClass + " in your web.xml file."));
        }
    }
}
