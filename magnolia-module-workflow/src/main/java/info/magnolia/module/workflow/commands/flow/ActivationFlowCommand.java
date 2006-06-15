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
import info.magnolia.cms.util.DateUtil;
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
public class ActivationFlowCommand extends FlowCommand {

    private static final String WEB_SCHEDULED_ACTIVATION = "activation";

    private static final Logger log = LoggerFactory.getLogger(ActivationFlowCommand.class);
    
    public ActivationFlowCommand() {
        setWorkflowName(WEB_SCHEDULED_ACTIVATION);
    }
    
    /**
     * Set the start and end date for this page
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem) {
        super.prepareLaunchItem(context, launchItem);

        String repository = (String) context.get(Context.ATTRIBUTE_REPOSITORY);
        String path = (String) context.get(Context.ATTRIBUTE_PATH);

        try {
            Content node = ContentRepository.getHierarchyManager(repository).getContent(path);
            updateDateAttribute(node, launchItem,WorkflowConstants.ATTRIBUTE_START_DATE);
            updateDateAttribute(node, launchItem,WorkflowConstants.ATTRIBUTE_END_DATE);
        }
        catch (RepositoryException e) {
            log.error("can't find node for path [" + path + "]", e);
        }

    }

    /**
     * Set a date stored in the repository into the list of attributes of the launch item.
     *
     * <ul>
     * <li>get utc calendar from repository</li>
     * <li>convert utc to local calendar</li>
     * <li>get string time for open wfe from local calendar</li>
     * <li>set string attribute of the launch item</li>
     * </ul>
     */
    private void updateDateAttribute(Content node, LaunchItem launchItem,String attributeName)  {
        final SimpleDateFormat sdf = new SimpleDateFormat(WorkflowConstants.OPENWFE_DATE_FORMAT);
        try {
            if(node.hasNodeData(attributeName)) {
                Calendar cd = node.getNodeData(attributeName).getDate();
                cd = DateUtil.getLocalCalendarFromUTC(cd);
                String date = sdf.format(new Date(cd.getTimeInMillis()));
                launchItem.getAttributes().puts(attributeName, date);
            }
        } catch (Exception e) {
            log.warn("cannot set date:"+attributeName+" for node" + node.getHandle(), e);
        }
    }

}
