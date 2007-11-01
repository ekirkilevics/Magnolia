/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.delta;

import info.magnolia.cms.util.WebXmlUtil;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/**
 * A utility class for web.xml related conditions, which will add
 * conditions to a given list of tasks based on some conditions.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebXmlConditionsUtil {
    private final WebXmlUtil webXmlUtil = new WebXmlUtil();
    private final List conditions;

    public WebXmlConditionsUtil(List conditions) {
        this.conditions = conditions;
    }

    public void servletIsNowWrapped(final String servletName) {
        if (webXmlUtil.isServletOrMappingRegistered(servletName)) {
            conditions.add(new FalseCondition("web.xml updates", "Since Magnolia 3.1, servlets are wrapped in ServletDispatchingFilter; please remove the <servlet> and <servlet-mapping> elements for " + servletName + " from your web.xml file."));
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

    public void filterMustBeRegisteredWithCorrectDispatchers(final String filterClass) {
        if (!webXmlUtil.isFilterRegistered(filterClass) || !webXmlUtil.areFilterDispatchersConfiguredProperly(filterClass, Arrays.asList(new String[]{"REQUEST", "FORWARD"}), Collections.singletonList("ERROR"))) {
            conditions.add(new FalseCondition("web.xml updates",
                    "Since Magnolia 3.1, the main Magnolia filter is " + filterClass + ", and it must be mapped with dispatchers REQUEST, FORWARD and, optionally, ERROR. "
                        + " Please add <dispatcher>REQUEST</dispatcher>"
                        + " <dispatcher>FORWARD</dispatcher>"
                        + " <dispatcher>ERROR</dispatcher> to the filter-mapping element in your web.xml file."));
        }
    }

    public void filterIsDeprecated(final String deprecatedFilterClass, final String replacementFilterClass) {
        if (webXmlUtil.isFilterRegistered(deprecatedFilterClass)) {
            conditions.add(new FalseCondition("web.xml updates", "The " + deprecatedFilterClass + " filter class is now deprecated. Please replace it with " + replacementFilterClass + ": please update the corresponding <filter-class> element in your web.xml file."));
        }
    }

}
