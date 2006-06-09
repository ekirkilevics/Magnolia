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
package info.magnolia.module.workflow;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.jcr.JCRFlowDefinition;
import info.magnolia.module.workflow.jcr.JCRPersistedEngine;
import info.magnolia.module.workflow.jcr.JCRWorkItemAPI;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowItem;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.worklist.store.StoreException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Util to use the mangoila workflow module. Methods to launch and proceed.
 * @author jackie
 */
public class WorkflowUtil {

    final static public StringAttribute ATTRIBUTE_TRUE = new StringAttribute("true");

    final static public StringAttribute ATTRIBUTE_FALSE = new StringAttribute("false");

    private final static Logger log = LoggerFactory.getLogger(WorkflowUtil.class.getName());

    /**
     * Where the work items are stored
     */
    private static JCRWorkItemAPI storage;

    static {
        try {
            storage = new JCRWorkItemAPI();
        }
        catch (Exception e) {
            log.error("can't initialize the workflow util", e);
        }
    }

    /**
     * Util: don't instantiate
     */
    private WorkflowUtil() {
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
     */
    public static void launchFlow(LaunchItem li, String flowName) {
        li.setWorkflowDefinitionUrl(WorkflowConstants.ATTRIBUTE_WORKFLOW_DEFINITION_URL);

        // Retrieve and add the flow definition to the LaunchItem
        String flowDef = new JCRFlowDefinition().getflowDefAsString(flowName);
        li.getAttributes().puts(WorkflowConstants.ATTRIBUTE_DEFINITION, flowDef);
        JCRPersistedEngine engine = WorkflowModule.getEngine();

        try {
            // Launch the item
            engine.launch(li, true);
        }
        catch (Exception e) {
            log.error("Launching flow " + flowName + " failed", e);
        }
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

        wi.getAttributes().puts(WorkflowConstants.ATTRIBUTE_ACTION, action);
        if (StringUtils.isNotEmpty(comment)) {
            wi.getAttributes().puts(Context.ATTRIBUTE_COMMENT, comment);
        }
        proceed(wi);
    }

    public static void reject(String id, String comment) {
        proceed(id,WorkflowConstants.ACTION_REJECT,comment);
    }

    public static void cancel(String id, String comment) {
        proceed(id,WorkflowConstants.ACTION_CANCEL,comment);
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
            wi = storage.retrieveWorkItem(StringUtils.EMPTY, FlowExpressionId.fromParseableString(id));
        }
        catch (StoreException e) {
            log.error("can't get the workitem by expression [" + id + "]", e);
        }
        return wi;
    }

    /**
     * get all work items for the user
     * @param userName
     * @return
     * @throws Exception
     */
    public static List getWorkItems(String userName) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("enter getWorkItems");
            log.debug("user name = " + userName);
        }

        long start = System.currentTimeMillis();

        MgnlUser user = (MgnlUser) MgnlContext.getUser();
        Collection groups = user.getGroups();
        Collection roles = user.getRoles();
        StringBuffer queryString = new StringBuffer();
        queryString.append("//*[(@assignTo=\"");
        queryString.append(userName);
        queryString.append("\") or (@participant=\"user-");
        queryString.append(userName);
        queryString.append("\" and not(@assignTo))");
        for (Iterator iter = groups.iterator(); iter.hasNext();) {
            Object group = iter.next();
            queryString.append(" or (@participant=\"group-");
            queryString.append(group);
            // FIXME
            //queryString.append("\" and @assignTo!=\"");
            //queryString.append(userName);
            queryString.append("\") ");
        }
        for (Iterator iter = roles.iterator(); iter.hasNext();) {
            Object role = iter.next();
            //FIXME
            queryString.append(" or (@participant=\"role-");
            queryString.append(role);
            //queryString.append("\" and @assignTo!=\"");
            //queryString.append(userName);
            queryString.append("\") ");
        }
        queryString.append("]");

        if (log.isDebugEnabled())
            log.info("xpath query string = " + queryString);

        final List doQuery = storage.doQuery(queryString.toString());
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
            storage.storeWorkItem(StringUtils.EMPTY, wi);
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
        queryString.append("//*[@participant=\"group-");
        queryString.append(GroupName);
        queryString.append("\"]");

        if (log.isDebugEnabled())
            log.debug("xpath query string = " + queryString);
        return storage.doQuery(queryString.toString());

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
        queryString.append("//*[@participant=\"group-");
        queryString.append(roleName);
        queryString.append("\"]");

        if (log.isDebugEnabled())
            log.debug("xpath query string = " + queryString);
        return storage.doQuery(queryString.toString());
    }

    /**
     * remove one work item by id
     */
    private static void removeWorkItem(InFlowWorkItem wi) {
        try {
            storage.removeWorkItem(wi.getId());
        }
        catch (StoreException e) {
            log.error("can't remove workitem", e);
        }
    }

}