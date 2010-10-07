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
package info.magnolia.module.workflow.commands;

import info.magnolia.cms.util.DateUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowUtil;
import info.magnolia.module.workflow.flows.FlowDefinitionException;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringMapAttribute;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base command class for starting workflows.
 */
public class FlowCommand extends MgnlCommand {
    private static final Logger log = LoggerFactory.getLogger(FlowCommand.class);

    /**
     * The name of the workflow to start.
     */
    private String workflowName = WorkflowConstants.DEFAULT_WORKFLOW;

    /**
     * The dialog used in the inbox.
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
                // transform dates
                if(val instanceof String){
                    try{
                        Date date = DateUtils.parseDate((String)val, new String[]{DateUtil.YYYY_MM_DD_T_HH_MM_SS, DateUtil.YYYY_MM_DD});
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        val = DateFormatUtils.format(cal, WorkflowConstants.OPENWFE_DATE_FORMAT);
                    }
                    catch(ParseException e){
                        // its not a date string
                    }
                }
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

    @Override
    public void release() {
        super.release();
        workflowName = WorkflowConstants.DEFAULT_WORKFLOW;
        dialogName = WorkflowConstants.DEFAULT_EDIT_DIALOG;
    }
}
