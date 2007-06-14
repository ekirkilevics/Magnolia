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
package info.magnolia.module.workflow.commands;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowUtil;
import info.magnolia.module.workflow.flows.FlowDefinitionException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringMapAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FlowCommand extends MgnlCommand {

    private static Logger log = LoggerFactory.getLogger(FlowCommand.class);

    /**
     * The name of the workflow to start
     */
    private String workflowName = WorkflowConstants.DEFAULT_WORKFLOW;

    /**
     * The dialog used in the inbox
     */
    private String dialogName = WorkflowConstants.DEFAULT_EDIT_DIALOG;

    public boolean execute(Context ctx) throws FlowDefinitionException {
        // Get the references
        LaunchItem li = new LaunchItem();
        prepareLaunchItem(ctx, li);
        WorkflowUtil.launchFlow(li, getWorkflowName());
        return true;
    }

    /**
     * The default implementation puts all the contexts attributes which are in the request scope into the work item.
     * @param context
     * @param launchItem
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem) {
        Map map = context.getAttributes(Context.LOCAL_SCOPE);
        // create map for workflowItem with all serializable entries from the context
        Map serializableMap = new HashMap();
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            Object val = map.get(key);
            if (val instanceof Serializable) {
                serializableMap.put(key, val);
            }
        }
        serializableMap.put(WorkflowConstants.ATTRIBUTE_USERNAME, context.getUser().getName());
        StringMapAttribute attrs = AttributeUtils.java2attributes(serializableMap);
        launchItem.setAttributes(attrs);
        // set the dialog to use in the inbox
        launchItem.getAttributes().sput(WorkflowConstants.ATTRIBUTE_EDIT_DIALOG, getDialogName());
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String flowName) {
        this.workflowName = flowName;
    }


    public String getDialogName() {
        return this.dialogName;
    }


    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }
}
