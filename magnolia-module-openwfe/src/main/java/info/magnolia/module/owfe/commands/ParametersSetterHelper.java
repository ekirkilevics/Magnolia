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
package info.magnolia.module.owfe.commands;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.module.owfe.MgnlConstants;
import openwfe.org.engine.workitem.Attribute;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.commons.chain.Context;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


/**
 * This class to convert the parameters depending on the context. A new context would need a new way to handle
 * parameters but this way we avoid creating too many classes.
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class ParametersSetterHelper {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(ParametersSetterHelper.class);

    public HashMap translateParam(MgnlCommand command, Context context) throws Exception {
        String[] expected = command.getExpectedParameters();
        // String[] accepted = command.getAcceptedParameters();

        HashMap params = new HashMap();

        WorkItem workItem = (WorkItem) context.get(MgnlConstants.INFLOW_PARAM);
        if (workItem != null) {
            for (int i = 0; i < expected.length; i++) {
                Attribute attr = workItem.getAttribute(expected[i]);
                if (attr != null) {
                    params.put(expected[i], attr.toString());
                    log.info("parameter[" + i + "] added:" + expected[i] + "=" + attr.toString());
                }

                else {
                    log.error("cannot find parameter " + expected[i] + " for command " + command);
                    throw new Exception("cannot find parameter " + expected[i] + " for command " + command);
                }
            }
        }
        else {
            HttpServletRequest request = (HttpServletRequest) context.get(MgnlConstants.P_REQUEST);
            for (int i = 0; i < expected.length; i++) {
                params.put(expected[i], request.getParameter(expected[i]));
            }

            HashMap map = (HashMap) context.get(MgnlConstants.INTREE_PARAM);
            for (int i = 0; i < expected.length; i++) {
                params.put(expected[i], map.get(expected[i]));
                log.info("translate: " + expected[i] + "=" + map.get(expected[i]));
            }

            // check
            for (int i = 0; i < expected.length; i++) {
                if (params.get(expected[i]) == null)
                    throw new Exception("cannot find parameter " + expected[i] + " for command " + command);
            }

        }
        context.put(MgnlCommand.PARAMS, params);
        return params;
    }

}
