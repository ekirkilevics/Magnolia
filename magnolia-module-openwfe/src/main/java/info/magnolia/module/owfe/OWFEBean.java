package info.magnolia.module.owfe;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Repository;
import javax.servlet.http.HttpServletRequest;

import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.engine.workitem.WorkItem;

import org.apache.commons.lang.StringUtils;

public class OWFEBean {
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(OWFEBean.class.getName());

	static private final String WEBSITE_REPOSITORY = "website";

	JCRWorkItemAPI storage = null;

	public OWFEBean() throws Exception {
		storage = new JCRWorkItemAPI();
	}

	/**
	 * Check if the workitem's parcitipant related with this user
	 * 
	 * @param wi
	 * @param userName
	 * @return
	 */
	public boolean checkPariticipant(InFlowWorkItem wi, String userName) {
		// MgnlUser user =
		// (MgnlUser)UserManagerFactory.getUserManager().getUser(userName);
		MgnlUser user = (MgnlUser) new MgnlUserManager().getUser(userName);

		StringAttribute sa = (StringAttribute) wi.getAttribute("assignTo");
		if (sa != null) {
			String assignTo = sa.toString();
			log.info("this workitem has been assigned to user " + assignTo);
			if (assignTo != null && assignTo.length() > 0) {// have valid
															// assignee
				if (assignTo.endsWith(userName))
					return true;
				else
					return false;
			}
		}
		String pName = wi.getParticipantName();
		log.debug("participant name = " + pName + "(" + pName.substring(5)
				+ ")");
		if (pName.startsWith("command-")) {
			return false;
		} else if (pName.startsWith("user-")) {
			if (userName.equals(pName.substring(5)))
				return true;
			else
				return false;
		} else if (pName.startsWith("role-")) {
			return user.hasRole(pName.substring(5));
		} else if ((pName.startsWith("group-"))) {
			return user.inGroup(pName.substring(5));
		} else
			return false;
	}

	/**
	 * get user name from request
	 * 
	 * @param request
	 * @return
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
		log.debug("enter getWorkItems");
		log.debug("user name = " + userName);
		ArrayList list = new ArrayList();
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		Content root = hm.getRoot();
		// ArrayList ret = new ArrayList();
		try {
			Collection c = root.getChildren(ItemType.WORKITEM);
			int i = 0;
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();
				InFlowWorkItem wi = JCRWorkItemStorage.loadWorkItem(ct);
				if (checkPariticipant(wi, userName)) { // if belong to this
														// user
					list.add(wi);
					log.debug("found one workitem for user" + userName);
				}
			}
			log.debug("enter getWorkItems");
			return list;
		} catch (Exception e) {
			log.error("exception:" + e);
		}
		log.debug("leave getWorkItems");
		return list;

	}

	public int getWorkItemsNumber(HttpServletRequest request) throws Exception {
		return getWorkItems(getUsername(request)).size();
	}

	public WorkItem getWorkItem(HttpServletRequest request, int i)
			throws Exception {
		return (WorkItem) getWorkItems(getUsername(request)).get(i);
	}

	private void removeWorkItem(InFlowWorkItem wi) throws Exception {
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		Content root = hm.getRoot();
		try {
			Content ct = storage.findWorkItem(wi.getLastExpressionId());
				if (ct != null) {
					ct.delete();
					hm.save();
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
			removeWorkItem(if_wi);
			
		}
		removeWorkItem(if_wi);
		OWFEEngine.getEngine().reply(if_wi);
		
	/*	// do activation
		log.debug("attribute=" + if_wi.getAttribute("pathSelected"));
		String path = ((StringAttribute) if_wi.getAttribute("pathSelected"))
				.toString();
		boolean recursive = ((StringAttribute) if_wi.getAttribute("recursive"))
				.equals("true");
		doActivate(request, path, recursive, true);
*/
		
	}

	private Repository getRepository() {
		Repository repo = ContentRepository.getRepository(WEBSITE_REPOSITORY);
		log.info("get repository for " + WEBSITE_REPOSITORY + "=" + repo);
		return repo;
	}

	
	private void doActivate(HttpServletRequest request, String path,
			boolean recursive, boolean includeContentNodes) throws Exception {
		Content c = null;
		HierarchyManager hm = ContentRepository
				.getHierarchyManager(WEBSITE_REPOSITORY);
		if (hm.isPage(path)) {
			c = hm.getContent(path);
		} else {
			c = hm.getContent(path); // ?
		}
		/**
		 * Here rule defines which content types to collect, its a resposibility
		 * of the caller ro set this, it will be different in every hierarchy,
		 * for instance - in website tree recursive activation : rule will allow
		 * mgnl:contentNode, mgnl:content and nt:file - in website tree
		 * non-recursive activation : rule will allow mgnl:contentNode and
		 * nt:file only
		 */
		Rule rule = new Rule();
		rule.addAllowType(ItemType.CONTENTNODE.getSystemName());
		rule.addAllowType(ItemType.NT_FILE);
		if (recursive) {
			rule.addAllowType(ItemType.CONTENT.getSystemName());
		}

		Syndicator syndicator = (Syndicator) FactoryUtil
				.getInstance(Syndicator.class);
		syndicator
				.init(MgnlContext.getUser(), WEBSITE_REPOSITORY,
						ContentRepository
								.getDefaultWorkspace(WEBSITE_REPOSITORY), rule);

		String parentPath = StringUtils.substringBeforeLast(path, "/");
		if (StringUtils.isEmpty(parentPath)) {
			parentPath = "/";
		}
		syndicator.activate(parentPath, path);

		// unlock content
		// c.unlock();

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
			removeWorkItem(if_wi);
		}
		removeWorkItem(if_wi);
		OWFEEngine.getEngine().reply(if_wi);

		log.debug("work item removed.");
		// send mail to developer
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

		return;
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

//		if (userName == null || userName.length() == 0) {
//			log.error("can not assign work item, user name is " + userName);
//			return;
//		}

		if (userName == null)
			userName = "";
		
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
//		if (userName == null || userName.length() == 0) {
//			log.error("can not assign work item, user name is " + userName);
//			return;
//		}
		if (userName == null)
			userName = "";
		
		try {
			wi.addAttribute("assignTo", new StringAttribute(userName));
			storage.storeWorkItem("", wi);
		} catch (Exception e) {
			log.error("assign work item to user " + userName + " failed.)", e);
		}

	}
	
	public void doTest(String s) throws Exception{
		( (JCRPersistedEngine) (OWFEEngine.getEngine()) ).getExpStore().doTest(s);
	}
	


}