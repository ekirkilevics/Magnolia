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
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;

import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringMapAttribute;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activation command which will launch a flow to do scheduled activation by "sleep" functionality of owfe
 * @author jackie
 */
public class ActivationFlowCommand extends MgnlCommand  {

    private static final Logger log = LoggerFactory.getLogger(ActivationFlowCommand.class);

    public boolean execute(Context ctx) {
        try {
            // Get the references
            LaunchItem li = new LaunchItem();
            prepareLaunchItem(ctx, li);
            WorkflowUtil.launchFlow(li);
        }
        catch (Exception e) {
            log.error("Launching failed", e);
            return false;
        }
        return true;
    }
    
    /**
     * Set the start and end date for this page
     */
    private void prepareLaunchItem(Context context, LaunchItem launchItem) {
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
        
        String repository = (String) context.get(Context.ATTRIBUTE_REPOSITORY);
        String path = (String) context.get(Context.ATTRIBUTE_PATH);

        try {
            Content node = ContentRepository.getHierarchyManager(repository).getContent(path);
            updateDateAttribute(node, launchItem, WorkflowConstants.ATTRIBUTE_START_DATE);
            updateDateAttribute(node, launchItem, WorkflowConstants.ATTRIBUTE_END_DATE);
        }
        catch (RepositoryException e) {
            log.error("can't find node for path [" + path + "]", e);
        }
        
        // set flow
        launchItem.setWorkflowDefinitionUrl(WorkflowConstants.ATTRIBUTE_WORKFLOW_DEFINITION_URL);
        InputStream stream = null;
        try {
            stream = ClasspathResourcesUtil.getStream("info/magnolia/module/workflow/flow.xml");
            String flowDef = IOUtils.toString(stream);
            launchItem.getAttributes().puts(WorkflowConstants.ATTRIBUTE_DEFINITION, flowDef);
        }
        catch (IOException e) {
            log.error("can't read flow definition", e);
        }
        finally{
            IOUtils.closeQuietly(stream);
        }

    }

    /**
     * Set a date stored in the repository into the list of attributes of the launch item. Ignore past activation dates
     * <ul>
     * <li>get utc calendar from repository</li>
     * <li>convert utc to local calendar</li>
     * <li>get string time for open wfe from local calendar</li>
     * <li>set string attribute of the launch item</li>
     * </ul>
     */
    private void updateDateAttribute(Content node, LaunchItem launchItem, String attributeName) {
        final SimpleDateFormat sdf = new SimpleDateFormat(WorkflowConstants.OPENWFE_DATE_FORMAT);
        try {
            if (node.hasNodeData(attributeName)) {
                Calendar cd = node.getNodeData(attributeName).getDate(); // utc calendar from repository
                Calendar now = DateUtil.getCurrentUTCCalendar();
                if (cd.before(now) && isActivationDate(attributeName)) {
                    log.info("Ignoring past activation date:" + attributeName + " from node:" + node.getHandle());
                }
                else {
                    String date = sdf.format(new Date(DateUtil.getLocalCalendarFromUTC(cd).getTimeInMillis()));
                    launchItem.getAttributes().puts(attributeName, date);
                }
            }
        }
        catch (Exception e) {
            log.warn("cannot set date:" + attributeName + " for node" + node.getHandle(), e);
        }
    }

    private boolean isActivationDate(String attributeName) {
        return ((attributeName.equals(WorkflowConstants.ATTRIBUTE_START_DATE)) || (attributeName
            .equals(WorkflowConstants.ATTRIBUTE_END_DATE)));
    }

}
