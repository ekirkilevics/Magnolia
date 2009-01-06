/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow;

import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.flows.FlowDefinitionException;
import info.magnolia.module.workflow.flows.FlowDefinitionManager;
import info.magnolia.module.workflow.jcr.JCRPersistedEngine;
import info.magnolia.module.workflow.jcr.JCRWorkItemStore;
import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowItem;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.worklist.store.StoreException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Util to use the mangoila workflow module. Methods to launch and proceed.
 * @author jackie
 */
public class WorkflowUtil {

    final static public StringAttribute ATTRIBUTE_TRUE = new StringAttribute("true");

    final static public StringAttribute ATTRIBUTE_FALSE = new StringAttribute("false");

    private final static Logger log = LoggerFactory.getLogger(WorkflowUtil.class.getName());

    /**
     * Util: don't instantiate
     */
    private WorkflowUtil() {
    }

    public static JCRWorkItemStore getWorkItemStore(){
        return WorkflowModule.getWorkItemStore();
    }

    public static void launchFlow(LaunchItem li) {
        try {
            JCRPersistedEngine engine = WorkflowModule.getEngine();
            // Launch the item
            engine.launch(li, true);
        }
        catch (Exception e) {
            log.error("Launching flow failed", e);
        }
    }

    /**
     * Simply launch a flow for the specified node
     */
    public static void launchFlow(String repository, String path, String flowName) throws Exception {
        try {
            // Get the references
            LaunchItem li = new LaunchItem();

            // start activation
            if (repository != null) {
                li.addAttribute(Context.ATTRIBUTE_REPOSITORY, new StringAttribute(repository));
            }
            if (path != null) {
                li.addAttribute(Context.ATTRIBUTE_PATH, new StringAttribute(path));
            }
            launchFlow(li, flowName);
            // Launch the item
        }
        catch (Exception e) {
            log.error("Launching flow " + flowName + " failed", e);
        }
    }

    /**
     * Start a flow
     * @param li the prepared lunchItem
     * @param flowName the flow to start
     * @throws FlowDefinitionException
     */
    public static void launchFlow(LaunchItem li, String flowName) throws FlowDefinitionException {
        FlowDefinitionManager configurator = WorkflowModule.getFlowDefinitionManager();

        configurator.configure(li, flowName);

        launchFlow(li);
    }

    /**
     * @param id
     */
    public static void proceed(String id) {
        proceed(id, WorkflowConstants.ACTION_PROCEED);
    }

    public static void proceed(String id, String action) {
        proceed(id, action, null);
    }

    public static void proceed(String id, String action, String comment) {
        InFlowWorkItem wi = getWorkItem(id);
        if (wi == null) {
            log.error("can't proceed workitem [{}]", id);
            return;
        }
        wi.touch();
        // remove old exception
        if(wi.containsAttribute(Context.ATTRIBUTE_EXCEPTION)){
            wi.removeAttribute(Context.ATTRIBUTE_EXCEPTION);
        }
        if(wi.containsAttribute(Context.ATTRIBUTE_MESSAGE)){
            wi.removeAttribute(Context.ATTRIBUTE_MESSAGE);
        }
        wi.getAttributes().puts(WorkflowConstants.ATTRIBUTE_ACTION, action);
        wi.getAttributes().puts(WorkflowConstants.ATTRIBUTE_USERNAME, MgnlContext.getUser().getName());

        if (StringUtils.isNotEmpty(comment)) {
            wi.getAttributes().puts(Context.ATTRIBUTE_COMMENT, comment);
        }
        proceed(wi);
    }

    public static void reject(String id, String comment) {
        proceed(id, WorkflowConstants.ACTION_REJECT, comment);
    }

    public static void cancel(String id, String comment) {
        proceed(id, WorkflowConstants.ACTION_CANCEL, comment);
    }

    /**
     * Proceed this item
     * @param wi
     */
    public static void proceed(InFlowWorkItem wi) {
        try {
            WorkflowModule.getEngine().reply(wi);
        }
        catch (Exception e) {
            log.error("Error while accessing the workflow engine", e);
        }
        finally {
            removeWorkItem(wi);
        }
    }

    /**
     * @param id identifier for the workitem as stored in the engine
     * @return <code>InFlowWorkItem</code> corresponding to the workitem
     */
    public static InFlowWorkItem getWorkItem(String id) {
        InFlowWorkItem wi = null;
        try {
            wi = getWorkItemStore().retrieveWorkItem(StringUtils.EMPTY, FlowExpressionId.fromParseableString(id));
        }
        catch (StoreException e) {
            log.error("can't get the workitem by expression [" + id + "]", e);
        }
        return wi;
    }

    public static String getPath(String id){
        return getWorkItemStore().createPathFromId(FlowExpressionId.fromParseableString(id));
    }

    /**
     * get all work items for the user
     */
    public static List getWorkItems(String userName) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("enter getWorkItems");
            log.debug("user name = " + userName);
        }

        long start = System.currentTimeMillis();

        User user = Security.getUserManager().getUser(userName);
        Collection groups = user.getGroups();
        Collection roles = user.getRoles();

        StringBuffer queryString = new StringBuffer();
        queryString.append("//*[(@assignTo=\"");
        queryString.append(userName);
        queryString.append("\") or (@participant=\""+WorkflowConstants.PARTICIPANT_PREFIX_USER);
        queryString.append(userName);
        queryString.append("\" and not(@assignTo))");
        for (Iterator iter = groups.iterator(); iter.hasNext();) {
            Object group = iter.next();
            queryString.append(" or (@participant=\""+WorkflowConstants.PARTICIPANT_PREFIX_GROUP);
            queryString.append(group);
            // FIXME
            // queryString.append("\" and @assignTo!=\"");
            // queryString.append(userName);
            queryString.append("\") ");
        }
        for (Iterator iter = roles.iterator(); iter.hasNext();) {
            Object role = iter.next();
            // FIXME
            queryString.append(" or (@participant=\""+WorkflowConstants.PARTICIPANT_PREFIX_ROLE);
            queryString.append(role);
            // queryString.append("\" and @assignTo!=\"");
            // queryString.append(userName);
            queryString.append("\") ");
        }
        queryString.append("]");

        if (log.isDebugEnabled()) {
            log.info("xpath query string = " + queryString);
        }

        final List doQuery = getWorkItemStore().doQuery(queryString.toString());
        long end = System.currentTimeMillis();
        log.debug("Retrieving workitems done. (Took " + (end - start) + " ms)");
        return doQuery;
    }

    public static String getId(InFlowItem wi) {
        return wi.getId().toParseableString();
    }

    /**
     * assign work item to a user, if userName = "", then assignment for the workItem will be deleted
     */
    public static void assignWorkItemToUser(String id, String userName) {
        if (id == null || id.length() == 0) {
            log.error("can not assign work item, invalid express id " + id);
            return;
        }

        if (userName == null) {
            log.info("User name was null");
            return;
        }

        InFlowWorkItem wi = getWorkItem(id);
        if (wi == null) {
            log.error("can not assign work item, can not retrieve work tiem by  express id " + id);
            return;
        }
        assignWorkItemToUser(wi, userName);
    }

    /**
     * assign work item to a user, if userName = "", then assignment for the workItem will be deleted
     */
    public static void assignWorkItemToUser(InFlowWorkItem wi, String userName) {
        if (userName == null) {
            log.info("User name was null");
            return;
        }

        try {
            wi.addAttribute(WorkflowConstants.ATTRIBUTE_ASSIGN_TO, new StringAttribute(userName));
            getWorkItemStore().storeWorkItem(StringUtils.EMPTY, wi);
        }
        catch (Exception e) {
            log.error("assign work item to user " + userName + " failed.)", e);
        }

    }

    /**
     * return a list of workItem for one usre
     */
    public static List getUserInbox(String userName) throws Exception {
        return getWorkItems(userName);
    }

    /**
     * return a list of workItem for one group
     */
    public static List getGroupInbox(String GroupName) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("enter getGroupInbox");
            log.debug("GroupName = " + GroupName);
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append("//*[@participant=\""+WorkflowConstants.PARTICIPANT_PREFIX_GROUP);
        queryString.append(GroupName);
        queryString.append("\"]");

        if (log.isDebugEnabled()) {
            log.debug("xpath query string = " + queryString);
        }
        return getWorkItemStore().doQuery(queryString.toString());

    }

    /**
     * return a list of workItem for one role
     */
    public static List getRoleInbox(String roleName) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("enter getGroupInbox");
            log.debug("roleName = " + roleName);
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append("//*[@participant=\""+WorkflowConstants.PARTICIPANT_PREFIX_GROUP);
        queryString.append(roleName);
        queryString.append("\"]");

        if (log.isDebugEnabled()) {
            log.debug("xpath query string = " + queryString);
        }
        return getWorkItemStore().doQuery(queryString.toString());
    }

    /**
     * remove one work item by id
     */
    private static void removeWorkItem(InFlowWorkItem wi) {
        try {
            getWorkItemStore().removeWorkItem(wi.getId());
        }
        catch (StoreException e) {
            log.error("can't remove workitem", e);
        }
    }

}
