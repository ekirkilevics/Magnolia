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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.WorkflowConstants;
import openwfe.org.engine.workitem.LaunchItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * The activation command which will launch a flow to do scheduled activation by "sleep" functionality of owfe
 * @author jackie
 */
public class TimeBasedFlowActivationCommand extends FlowCommand {

    private static final String WEB_SCHEDULED_ACTIVATION = "scheduledActivation";

    private static Logger log = LoggerFactory.getLogger(TimeBasedFlowActivationCommand.class);

    public TimeBasedFlowActivationCommand() {
        // set default value
        setWorkflowName(WEB_SCHEDULED_ACTIVATION);
    }

    /**
     * Set the start and end date for this page
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem) {
        super.prepareLaunchItem(context, launchItem);
        
        SimpleDateFormat sdf = new SimpleDateFormat(WorkflowConstants.OPENWFE_DATE_FORMAT);

        // add start date and end date
        String repository = (String) context.get(Context.ATTRIBUTE_REPOSITORY);
        String path = (String) context.get(Context.ATTRIBUTE_PATH);

        HierarchyManager hm = ContentRepository.getHierarchyManager(repository);

        Content node = null;
        try {
            node = hm.getContent(path);
        }
        catch (RepositoryException e) {
            log.error("can't find node for path [" + path + "]", e);
            return;
        }

        Calendar cd = null;


        // get start time
        try {
            if(node.hasNodeData(WorkflowConstants.ATTRIBUTE_START_DATE))
                cd = node.getNodeData(WorkflowConstants.ATTRIBUTE_START_DATE).getDate();
        }
        catch (Exception e) {
            log.warn("cannot get start time for node " + path, e);
        }

        if (cd != null) {
            String date1 = sdf.format(new Date(cd.getTimeInMillis()));
            if(log.isDebugEnabled())
            log.debug("start date = " + date1);
            launchItem.getAttributes().puts(WorkflowConstants.ATTRIBUTE_START_DATE, date1);
        }

        Calendar ce = null;

        // get end time
        try {
            if(node.hasNodeData(WorkflowConstants.ATTRIBUTE_END_DATE))
                ce = node.getNodeData(WorkflowConstants.ATTRIBUTE_END_DATE).getDate();
        }
        catch (Exception e) {
            log.warn("cannot get end time for node " + path, e);
        }

        if (ce != null) {
            String date2 = sdf.format(new Date(ce.getTimeInMillis()));
            if(log.isDebugEnabled())
                log.debug("end date = " + date2);
            launchItem.getAttributes().puts(WorkflowConstants.ATTRIBUTE_END_DATE, date2);
        }
    }

}
