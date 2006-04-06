package info.magnolia.module.owfe;

import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.jaas.sp.jcr.JCRUserMgr;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

/**
 * the class implements all the interface of work flow API
 * @author jackie
 *
 */
public class OWFEBean implements WorkflowAPI {
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(OWFEBean.class.getName());

	JCRWorkItemAPI storage = null;

	public OWFEBean() throws Exception {
		// for testing purpose 
		MgnlContext.setInstance(MgnlContext.getSystemContext()); 
		storage = new JCRWorkItemAPI();
	}


	/**
	 * get user name from request
	 * 
	 * @param request
	 * @return get the user name from the request
	 */
	public String getUsername(HttpServletRequest request) {
		
		return MgnlContext.getUser().getName();
	}

	/**
	 * get all work items for the user
	 * 
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public List getWorkItems(String userName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("enter getWorkItems");
			log.debug("user name = " + userName);
		}
		ArrayList list = new ArrayList();
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		Content root = hm.getRoot();
		MgnlUser user = (MgnlUser) new MgnlUserManager().getUser(userName);
		List groups = new JCRUserMgr().getGroupsForUser(userName);
		List roles = new JCRUserMgr().getRolesForUser(userName);
		StringBuffer queryString = new StringBuffer();
		queryString.append("//*[(@assignTo=\"" + userName
				+ "\") or (@participant=\"user-" + userName
				+ "\" and not(@assignTo))");
		for (int i = 0; i < groups.size(); i++) {
			queryString.append(" or (@participant=\"group-" + groups.get(i)
					+ "\" and @assignTo!=\"" + userName + "\") ");
		}
		for (int i = 0; i < roles.size(); i++) {
			queryString.append(" or (@participant=\"role-" + roles.get(i)
					+ "\" and @assignTo!=\"" + userName + "\") ");
		}
		queryString.append("]");
		log.info("xpath query string = " + queryString);
		return storage.doQuery(queryString.toString());
	}

	/**
	 * get number of work items for current user
	 */
	public int getWorkItemsNumber(HttpServletRequest request) throws Exception {
		return getWorkItems(getUsername(request)).size();
	}

	/**
	 * return a list of work item for current user
	 */
	public List getWorkItems(HttpServletRequest request) throws Exception {
		return getWorkItems(getUsername(request));
	}

//	/**
//	 * get the work item by index for 
//	 * @param request
//	 * @param i
//	 * @return
//	 * @throws Exception
//	 */
//	public WorkItem getWorkItem(HttpServletRequest request, int i)
//			throws Exception {
//		return (WorkItem) getWorkItems(getUsername(request)).get(i);
//	}

	/**
	 * remove one work item by id
	 */
	public void removeWorkItem(InFlowWorkItem wi) throws Exception {
//		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
//		try {
//			Content ct = storage.getWorkItemById(wi.getLastExpressionId());
//			if (ct != null) {
//				ct.delete();
//				hm.save();
//				if (log.isDebugEnabled())
//					log.debug("work item removed");
//			}
//
//		} catch (Exception e) {
//			log.error("exception:" + e);
//		}
		storage.removeWorkItem(wi.getId());
	}

	/**
	 * approve activiation
	 */
	public void approveActivation(String expressionId,
			HttpServletRequest request) throws Exception {

		// get workitem
		InFlowWorkItem if_wi = storage.retrieveWorkItem("", FlowExpressionId
				.fromParseableString(expressionId));
		if (if_wi == null)
			throw new Exception(
					"cant not get the work iem by this expression id ("
							+ expressionId + ")");

		if_wi.touch();
	//	if_wi.setAttribute("OK", new StringAttribute("true"));
		try {
			OWFEEngine.getEngine().reply(if_wi);
		} catch (Exception e) {
			log.error("reply to engine failed", e);

		} finally {
			removeWorkItem(if_wi);
		}
		
		log.info("approve ok");

	}

	/**
	 * reject the activation request, the work item will be removed
	 */
	public void rejectActivation(String expressionId) throws Exception {

		InFlowWorkItem if_wi = storage.retrieveWorkItem("", FlowExpressionId
				.fromParseableString(expressionId));
		if (if_wi == null)
			throw new Exception(
					"cant not get the work iem by this expression id ("
							+ expressionId + ")");
		if_wi.touch();
	//	if_wi.setAttribute("OK", new StringAttribute("false"));

		try {
			OWFEEngine.getEngine().reply(if_wi);
		} catch (Exception e) {
			log.error("Error while accessing the workflow engine", e);
			
		}
		 finally {
				removeWorkItem(if_wi);
			}
		
		if (log.isDebugEnabled())
			log.debug("work item removed.");

		log.info("reject ok");
	}


	/**
	 * update the attributes of the work item
	 */
	public void updateWorkItem(String expressionId, String[] names,
			String values[]) throws Exception {

		InFlowWorkItem ifwi = storage.retrieveWorkItem("", FlowExpressionId
				.fromParseableString(expressionId));
		if (ifwi == null)
			throw new Exception(
					"cant not get the work iem by this expression id ("
							+ expressionId + ")");
		for (int i = 0; i < names.length; i++) {
			ifwi.setAttribute(names[i], new StringAttribute(values[i]));
		}
		storage.storeWorkItem("", ifwi);
	}



	/**
	 * assign work item to a user, if userName = "", then assignment for the workItem will be deleted
	 */
	public void assignWorkItemToUser(String expressionId, String userName) {
		if (expressionId == null || expressionId.length() == 0) {
			log.error("can not assign work item, invalid express id "
					+ expressionId);
			return;
		}

		if (userName == null) {
			log.info("User name was null");
			return;
		}

		FlowExpressionId eid = FlowExpressionId
				.fromParseableString(expressionId);
		if (eid == null) {
			log
					.error("can not assign work item, can not parse invalid express id "
							+ expressionId);
			return;
		}

		InFlowWorkItem if_wi = null;
		try {
			if_wi = storage.retrieveWorkItem("", eid);
		} catch (Exception e) {
			log.error("retrieve work item failed", e);
		}
		if (if_wi == null) {

			log
					.error("can not assign work item, can not retrieve work tiem by  express id "
							+ expressionId);
			return;
		}
		assignWorkItemToUser(if_wi, userName);

	}

	/**
	 * assign work item to a user, if userName = "", then assignment for the workItem will be deleted
	 */
	public void assignWorkItemToUser(InFlowWorkItem wi, String userName) {
		if (userName == null) {
			log.info("User name was null");
			return;
		}

		try {
			wi.addAttribute("assignTo", new StringAttribute(userName));
			storage.storeWorkItem("", wi);
		} catch (Exception e) {
			log.error("assign work item to user " + userName + " failed.)", e);
		}

	}

	/**
	 * for testing purpose
	 * @param s
	 * @throws Exception
	 */
	public void doTest(String s) throws Exception {
		OWFEEngine.getEngine().getExpStore().doTest(s);
	}

	/**
	 * return a list of workItem for one group
	 */
	public List getGroupInbox(String GroupName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("enter getGroupInbox");
			log.debug("GroupName = " + GroupName);
		}
		ArrayList list = new ArrayList();
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		Content root = hm.getRoot();

		StringBuffer queryString = new StringBuffer();
		queryString.append("//*[@participant=\"group-" + GroupName + "\"]");

		log.info("xpath query string = " + queryString);
		return storage.doQuery(queryString.toString());

	}

	/**
	 * return a list of workItem for one role
	 */
	public List getRoleInbox(String roleName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("enter getGroupInbox");
			log.debug("roleName = " + roleName);
		}
		ArrayList list = new ArrayList();
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		Content root = hm.getRoot();

		StringBuffer queryString = new StringBuffer();
		queryString.append("//*[@participant=\"group-" + roleName + "\"]");

		log.info("xpath query string = " + queryString);
		return storage.doQuery(queryString.toString());
	}

	/**
	 * return a list of workItem for one usre
	 */
	public List getUserInbox(String userName) throws Exception {
		return this.getWorkItems(userName);
	}
	
	/**
	 * Simply launch a flow
	 */
	 public void LaunchFlow(HierarchyManager hm, String path, String flowName) throws Exception{
         log.debug("- Lauch flow -" + this.getClass().toString() + "- Start");
         if (flowName == null || flowName.length() == 0)
        	 throw new IllegalArgumentException("flowName is null or empty string");
         try {
             // Get the references
             LaunchItem li = new LaunchItem();
             li.addAttribute(MgnlConstants.P_ACTION, new StringAttribute(this.getClass().getName()));
             li.setWorkflowDefinitionUrl(MgnlConstants.P_WORKFLOW_DEFINITION_URL);

             // Retrieve and add the flow definition to the LaunchItem
             String flowDef = new JCRFlowDefinition().getflowDefAsString(flowName);
             li.getAttributes().puts(MgnlConstants.P_DEFINITION, flowDef);
             JCRPersistedEngine engine = OWFEEngine.getEngine();

             // start activation
             if (hm != null)
            	 li.addAttribute(MgnlConstants.P_HM,  AttributeUtils.java2owfe(hm));
             if (path != null )
            	 li.addAttribute(MgnlConstants.P_PATH, new StringAttribute(path));
           

             // Launch the item
             engine.launch(li, true);

         } catch (Exception e) {
             log.error("Launching flow " + flowName + " failed", e);
         }

         // End execution
         //if (log.isDebugEnabled())
             log.debug("- Lauch flow -" + this.getClass().toString() + "- End");
       
	 }

}