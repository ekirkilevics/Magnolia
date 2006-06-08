/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.workflow.commands.flow;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowUtil;
import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringMapAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FlowCommand extends MgnlCommand {
	/**
	 * The name of the workflow to start
	 */
	private String workflowName; 

    private static Logger log = LoggerFactory.getLogger(FlowCommand.class);
    
    public boolean execute(Context ctx) {

        try {
            // Get the references
            LaunchItem li = new LaunchItem();
            
            prepareLaunchItem(ctx, li);
            
            li.setWorkflowDefinitionUrl(WorkflowConstants.ATTRIBUTE_WORKFLOW_DEFINITION_URL);

            WorkflowUtil.launchFlow(li, getWorkflowName());

        }
        catch (Exception e) {
            log.error("Launching failed", e);
            return true;
        }
        return false;
    }

    /**
     * The default implementation puts all the contexts attributes which are in the request scope into the work item.
     * @param context
     * @param launchItem
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem){
        Map map = context.getAttributes(Context.LOCALE_SCOPE);
        // remove not serializable objects
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Object val = map.get(key);
            if(!(val instanceof Serializable)){
                map.remove(key);
            }
        }
        StringMapAttribute attrs = AttributeUtils.java2attributes(map);
        launchItem.setAttributes(attrs);
    }

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String flowName) {
		this.workflowName = flowName;
	}
}
