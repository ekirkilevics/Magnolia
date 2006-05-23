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
package info.magnolia.module.owfe;

import info.magnolia.cms.core.HierarchyManager;

import java.util.List;

/**
 * the interface for work flow in magnolia
 * @author jackie
 */
interface WorkflowInterface {

    /**
     * get all work items for the user
     * @param userName
     * @return
     * @throws Exception
     */
    public List getUserInbox(String userName) throws Exception;

    /**
     * get all work items for the user
     * @param groupName
     * @return
     * @throws Exception
     */
    public List getGroupInbox(String groupName) throws Exception;

    /**
     * get all work items for the role
     * @param roleName
     * @return
     * @throws Exception
     */
    public List getRoleInbox(String roleName) throws Exception;

    /**
     * get work items of the current user
     */
    public List getWorkItems() throws Exception;

    /**
     * approve acitvation
     * @param expressionId
     * @throws Exception
     */
    public void approveActivation(String expressionId) throws Exception;

    /**
     * reject activation
     * @param expressionId
     * @throws Exception
     */
    public void rejectActivation(String expressionId, String comment) throws Exception;

    /**
     * update the attributes of the work item
     */
    public void updateWorkItem(String expressionId, String[] names, String values[]) throws Exception;

    /**
     * assign work item to another user. After assignment, all other won't see the work item. If assign to "" or null,
     * all the users saw the work item before will see it again.
     * @param expressionId
     * @param userName
     */
    public void assignWorkItemToUser(String expressionId, String userName);

    /**
     * simply launch a flow
     * @param hm hierarchy manager
     * @param path the path of selected node
     * @param flowName name of flow
     * @throws Exception
     */
    public void launchFlow(HierarchyManager hm, String path, String flowName) throws Exception;

}
