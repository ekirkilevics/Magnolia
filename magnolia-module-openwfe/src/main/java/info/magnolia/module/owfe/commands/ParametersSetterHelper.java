package info.magnolia.module.owfe.commands;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.module.owfe.MgnlConstants;
import info.magnolia.module.owfe.OWFEEngine;
import openwfe.org.engine.workitem.Attribute;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.commons.chain.Context;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * This class to convert the parameters depending on the context.
 * A new context would need a new way to handle parameters but this way we avoid creating too many classes.
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class ParametersSetterHelper {
	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(ParametersSetterHelper.class);
	
    public HashMap translateParam(MgnlCommand command, Context context)throws Exception {
        String[] expected = command.getExpectedParameters();
        //String[] accepted = command.getAcceptedParameters();

        HashMap params = new HashMap();

        WorkItem workItem = (WorkItem) context.get(MgnlConstants.INFLOW_PARAM);
        if (workItem != null) {
            for (int i = 0; i < expected.length; i++){
            	Attribute attr = workItem.getAttribute(expected[i]);
            	if (attr != null)            		
            	{
            		params.put(expected[i], attr.toString());
            		log.info("parameter[" + i + "] added:" + expected[i] + "=" + attr.toString());
            	}
            	
            	else{
            		log.error("cannot find parameter "+ expected[i] + " for command " + command);
            		throw new Exception("cannot find parameter "+ expected[i] + " for command " + command);
            	}
            }
        } else {
            HttpServletRequest request = (HttpServletRequest) context.get(MgnlConstants.P_REQUEST);
            for (int i = 0; i < expected.length; i++) {
                params.put(expected[i], request.getParameter(expected[i]));
            }
            
            HashMap map = (HashMap)context.get(MgnlConstants.INTREE_PARAM);
            for (int i = 0; i < expected.length; i++) {
                params.put(expected[i], map.get(expected[i]));
            }
            
            // check
            for (int i = 0; i < expected.length; i++){
            	if (params.get(expected[i]) == null)
            		throw new Exception("cannot find parameter "+ expected[i] + " for command " + command);
            }

        }
        context.put(MgnlCommand.PARAMS, params);
        return params;
    }

}
