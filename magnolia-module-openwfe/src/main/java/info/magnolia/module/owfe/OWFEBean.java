package info.magnolia.module.owfe;

import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.jaas.sp.jcr.JCRUserMgr;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.engine.workitem.WorkItem;

public class OWFEBean implements WorkflowAPI {
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(OWFEBean.class.getName());

	JCRWorkItemAPI storage = null;

	public OWFEBean() throws Exception {
		storage = new JCRWorkItemAPI();
	}

	// /**
	// * Check if the workitem's parcitipant related with this user
	// *
	// * @param wi
	// * @param userName
	// * @return true if the item is assifned to the user
	// */
	// public boolean checkParticipant(InFlowWorkItem wi, String userName) {
	// // MgnlUser user =
	// // (MgnlUser)UserManagerFactory.getUserManager().getUser(userName);
	// MgnlUser user = (MgnlUser) new MgnlUserManager().getUser(userName);
	//
	// StringAttribute sa = (StringAttribute) wi.getAttribute("assignTo");
	// if (sa != null) {
	// String assignTo = sa.toString();
	// log.info("this workitem has been assigned to user " + assignTo);
	// if (assignTo != null && assignTo.length() > 0) {// have valid
	// // assignee
	// return assignTo.endsWith(userName);
	// }
	// }
	// String pName = wi.getParticipantName();
	// if (log.isDebugEnabled())
	// log.debug("participant name = " + pName + "(" + pName.substring(5) +
	// ")");
	// if (pName.startsWith("command-")) {
	// return false;
	// } else if (pName.startsWith("user-")) {
	// return userName.equals(pName.substring(5));
	// } else if (pName.startsWith("role-")) {
	// return user.hasRole(pName.substring(5));
	// } else if ((pName.startsWith("group-"))) {
	// return user.inGroup(pName.substring(5));
	// } else
	// return false;
	// }

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

	public int getWorkItemsNumber(HttpServletRequest request) throws Exception {
		return getWorkItems(getUsername(request)).size();
	}

	public List getWorkItems(HttpServletRequest request) throws Exception {
		return getWorkItems(getUsername(request));
	}

	public WorkItem getWorkItem(HttpServletRequest request, int i)
			throws Exception {
		return (WorkItem) getWorkItems(getUsername(request)).get(i);
	}

	private void removeWorkItem(InFlowWorkItem wi) throws Exception {
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		try {
			Content ct = storage.getWorkItemById(wi.getLastExpressionId());
			if (ct != null) {
				ct.delete();
				hm.save();
				if (log.isDebugEnabled())
					log.debug("work item removed");
			}

		} catch (Exception e) {
			log.error("exception:" + e);
		}
	}

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
		if_wi.setAttribute("OK", new StringAttribute("true"));
		try {
			OWFEEngine.getEngine().reply(if_wi);
		} catch (Exception e) {
			log.error("reply to engine failed", e);

		} finally {
			removeWorkItem(if_wi);
		}
		
		log.info("approve ok");

	}

	public void rejectActivation(String expressionId) throws Exception {

		InFlowWorkItem if_wi = storage.retrieveWorkItem("", FlowExpressionId
				.fromParseableString(expressionId));
		if (if_wi == null)
			throw new Exception(
					"cant not get the work iem by this expression id ("
							+ expressionId + ")");
		if_wi.touch();
		if_wi.setAttribute("OK", new StringAttribute("false"));

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

	//
	// public void cancelActivation(String id) throws Exception {
	//
	//
	// InFlowWorkItem if_wi = storage.retrieveWorkItem("",
	// FlowExpressionId.fromParseableString(expressionId));
	// if (if_wi == null)
	// throw new Exception(
	// "cant not get the work iem by this expression id (" + id
	// + ")");
	// if_wi.touch();
	//
	// removeWorkItem(if_wi);
	//
	// CancelItem ci = new CancelItem(
	// FlowExpressionId.fromParseableString(id), "project-lead");
	// wl_pl.dispatch(ci);
	// }

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

	//
	// public void assignWorkItemToUser(String expressionId, HttpServletRequest
	// reuqest){
	// assignWorkItemToUser(expressionId, this.getUsername(reuqest));
	// }

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

	public void doTest(String s) throws Exception {
		OWFEEngine.getEngine().getExpStore().doTest(s);
	}

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

	public List getUserInbox(String userName) throws Exception {
		return this.getWorkItems(userName);
	}

}