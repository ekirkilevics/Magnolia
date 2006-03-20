package info.magnolia.module.owfe;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import openwfe.org.engine.workitem.WorkItem;

public interface WorkflowAPI {
	/**
	 * get all work items for the user
	 * 
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public List getWorkItems(String userName) throws Exception;

	/**
	 * get the number of work items in user's inbox
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public int getWorkItemsNumber(HttpServletRequest request) throws Exception;

	/**
	 * get work itemby the index in the lis return by getWorkItems(String userName) 
	 * @param request
	 * @param i
	 * @return
	 */
	public WorkItem getWorkItem(HttpServletRequest request, int i);

	/**
	 * approve acitvation
	 * @param expressionId
	 * @param request
	 * @throws Exception
	 */
	public void approveActivation(String expressionId,
			HttpServletRequest request) throws Exception;

	/**
	 * reject activation
	 * @param expressionId
	 * @throws Exception
	 */
	public void rejectActivation(String expressionId) throws Exception;

	/**
	 * update the attributes of the work item
	 */
	public void updateWorkItem(String expressionId, String[] names,
			String values[]) throws Exception;

	/**
	 * assign work item to another user. After assignment, all other won't see the work item.
	 * If assign to "" or null, all the users saw the work item before will see it again.
	 * @param expressionId
	 * @param userName
	 */
	public void assignWorkItemToUser(String expressionId, String userName);

}
