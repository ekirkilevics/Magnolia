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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.DateUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.WorkflowConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.RepositoryException;
import openwfe.org.engine.workitem.LaunchItem;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The activation command which will launch a flow to do scheduled activation by "sleep" functionality of owfe
 * @author jackie
 */
public class ActivationFlowCommand extends PathMappedFlowCommand {

    private static final Logger log = LoggerFactory.getLogger(ActivationFlowCommand.class);

    private boolean recursive;

    /**
     * Set the start and end date for this page
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem) {
        super.prepareLaunchItem(context, launchItem);

        try {
            Content node = MgnlContext.getSystemContext().getHierarchyManager(getRepository()).getContent(getPath());
            updateDateAttribute(node, launchItem, WorkflowConstants.ATTRIBUTE_START_DATE);
            updateDateAttribute(node, launchItem, WorkflowConstants.ATTRIBUTE_END_DATE);
        }
        catch (RepositoryException e) {
            log.error("can't find node for path [" + getRepository() + ":" + getPath() + "]", e);
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
        return ((attributeName.equals(WorkflowConstants.ATTRIBUTE_START_DATE)) || (attributeName.equals(WorkflowConstants.ATTRIBUTE_END_DATE)));
    }

    public String getDialogName() {
        String dialogName = super.getDialogName();
        if(StringUtils.isEmpty(WorkflowConstants.DEFAULT_EDIT_DIALOG)){
            return WorkflowConstants.DEFAULT_ACTIVATION_EDIT_DIALOG;
        }
        return dialogName;
    }

    public boolean isRecursive() {
        return this.recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public String getWorkflowName() {
        String workflowName = super.getWorkflowName();
        if(super.getWorkflowName().equals(WorkflowConstants.DEFAULT_WORKFLOW)){
            workflowName = WorkflowConstants.DEFAULT_ACTIVATION_WORKFLOW;
        }
        return workflowName;
    }

    @Override
    public void release() {
        super.release();
        this.recursive = false;
    }
}
