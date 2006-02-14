package info.magnolia.module.owfe;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.simple.SimpleSyndicator;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.MgnlUserManager;
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

import com.ns.log.Log;

public class OWFEBean {
	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OWFEBean.class.getName());
	
	
	static private final String WEBSITE_REPOSITORY = "website";
	JCRWorkItemAPI storage = null;
	
	public OWFEBean() throws Exception{
		storage = new JCRWorkItemAPI();		
	}
	
	/**
	 * Check if the workitem's parcitipant related with this user
	 * @param wi
	 * @param userName
	 * @return
	 */
	public boolean checkPariticipant(InFlowWorkItem wi,String userName){
		//MgnlUser user = (MgnlUser)UserManagerFactory.getUserManager().getUser(userName);
		MgnlUser user = (MgnlUser)new MgnlUserManager().getUser(userName);
		
		String pName = wi.getParticipantName();
		Log.trace("owfe", "participant name = " + pName + "(" + pName.substring(5) + ")");
		if (pName.startsWith("command-")){
			return false;
		}else		if (pName.startsWith("user-")){
			if (userName.equals(pName.substring(5)))
				return true;
			else
				return false;
		}else if (pName.startsWith("role-")){
			return user.hasRole(pName.substring(5));
		}else if ((pName.startsWith("group-"))){
			return user.inGroup(pName.substring(5));
		}else
			return false;		
	}
	
	/**
	 * get user name from request
	 * @param request
	 * @return
	 */
	public String getUsername(HttpServletRequest request){
		//User user = SessionAccessControl.getUser(request);
		//User user = SessionAccessControl.getUser();
		//return user.getName(); 
		return MgnlContext.getUser().getName();
		//return "superuser";
	}
	/**
	 * get all work items for the user
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public List getWorkItems(String userName) throws Exception{
		Log.trace("owfe", "enter getWorkItems");
		Log.trace("owfe", "user name = " + userName);
		ArrayList list = new ArrayList();
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		Content root = hm.getRoot();
		//ArrayList ret = new ArrayList();
		try {		
			Collection c = root.getChildren(ItemType.WORKITEM);
			int i = 0;
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();				
				InFlowWorkItem wi = JCRWorkItemStorage.loadWorkItem(ct);
				if (checkPariticipant(wi, userName)){	// if belong to this user
					list.add(wi);
					Log.trace("found one workitem for user" + userName);
				}
			}
			Log.trace("owfe", "enter getWorkItems");
			return list;
		} catch (Exception e) {			
			log.error("exception:" + e);
			Log.error("owfe", e);
		}
		Log.trace("owfe", "leave getWorkItems");
		return list;
		
	}
	
	
	public int getWorkItemsNumber(HttpServletRequest request) throws Exception {		
		return getWorkItems(getUsername(request)).size();
	}

	public WorkItem getWorkItem(HttpServletRequest request, int i) throws Exception {
		return	(WorkItem)getWorkItems(getUsername(request)).get(i);
	}

	private void removeWorkItem(InFlowWorkItem wi)
			throws Exception {	
		HierarchyManager hm = OWFEEngine.getOWFEHierarchyManager("Store");
		Content root = hm.getRoot();
		ArrayList ret = new ArrayList();
		try {		
			Collection c = root.getChildren(ItemType.WORKITEM);
			int i = 0;
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();				
				InFlowWorkItem _wi = JCRWorkItemStorage.loadWorkItem(ct);
				Log.trace("_eid =" + wi.getLastExpressionId());
				if (_wi.getLastExpressionId().equals(wi.getLastExpressionId())) {
					ct.delete();
					hm.save();
					Log.trace("owfe", "work item removed");
				}
			}
		} catch (Exception e) {			
			log.error("exception:" + e);	
			Log.error("owfe", e);
		}
	}
	

	public void approveActivation(String expressionId, HttpServletRequest request)
			throws Exception {
		InFlowWorkItem if_wi = storage.retrieveWorkItem("", FlowExpressionId.fromParseableString(expressionId));
		if (if_wi == null)
			throw new Exception(
					"cant not get the work iem by this expression id (" + expressionId
							+ ")");
		
		if_wi.touch();
		if_wi.setAttribute("OK", new StringAttribute("true"));
		try{
			OWFEEngine.getEngine().reply(if_wi);
		}catch(Exception e){
			Log.error("owfe", e);
			removeWorkItem(if_wi);
		}
		removeWorkItem(if_wi);
		
		// do activation
		Log.trace("owfe", "attribute=" + if_wi.getAttribute("pathSelected"));
		String path = ((StringAttribute) if_wi.getAttribute("pathSelected"))
				.toString();
		boolean recursive = ((StringAttribute) if_wi
				.getAttribute("recursive")).equals("true");
		doActivate(request, path, recursive, true);

		// send mail to developer
	}

//	private HierarchyManager getHierarchyManager() {
//
//		HierarchyManager hm = ContentRepository
//				.getHierarchyManager(WEBSITE_REPOSITORY);
//		Log.trace("get HierarchyManager for " + WEBSITE_REPOSITORY + "=" + hm);
//		return hm;
//	}

	private Repository getRepository() {
		Repository repo = ContentRepository.getRepository(WEBSITE_REPOSITORY);
		Log.trace("get repository for " + WEBSITE_REPOSITORY + "=" + repo);
		return repo;
	}

	private void doActivate(HttpServletRequest request, String path,
			boolean recursive, boolean includeContentNodes) throws Exception {
		Content c = null;
		HierarchyManager hm = ContentRepository.getHierarchyManager(WEBSITE_REPOSITORY);
		if (hm.isPage(path)) {
			c = hm.getContent(path);
		} else {
			c = hm.getContent(path); //?
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
		SimpleSyndicator syndicator = new SimpleSyndicator(request,
				WEBSITE_REPOSITORY, ContentRepository
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
		
		InFlowWorkItem if_wi = storage.retrieveWorkItem("", FlowExpressionId.fromParseableString(expressionId));
		if (if_wi == null)
			throw new Exception(
					"cant not get the work iem by this expression id (" + expressionId
							+ ")");
		if_wi.touch();
		if_wi.setAttribute("OK", new StringAttribute("false"));
		
		try{
			OWFEEngine.getEngine().reply(if_wi);
		}catch(Exception e){
			Log.error("owfe", e);
			removeWorkItem(if_wi);
		}
		removeWorkItem(if_wi);
		
		
		Log.trace("owfe", "work item removed.");
		// send mail to developer
	}
//
//	public void cancelActivation(String id) throws Exception {
//
//
//		InFlowWorkItem if_wi = storage.retrieveWorkItem("", FlowExpressionId.fromParseableString(expressionId));
//		if (if_wi == null)
//			throw new Exception(
//					"cant not get the work iem by this expression id (" + id
//							+ ")");
//		if_wi.touch();
//		
//		removeWorkItem(if_wi);
//		
//		CancelItem ci = new CancelItem(
//				FlowExpressionId.fromParseableString(id), "project-lead");
//		wl_pl.dispatch(ci);
//	}

/**
 * update the attributes of the work item
 */
	public void updateWorkItem(String expressionId, String[] names, String values[])
			throws Exception {
	
		InFlowWorkItem ifwi = storage.retrieveWorkItem("", FlowExpressionId.fromParseableString(expressionId));
		if (ifwi == null)
			throw new Exception(
					"cant not get the work iem by this expression id (" + expressionId
							+ ")");
		
		for (int i = 0; i < names.length; i++) {
			ifwi.setAttribute(names[i], new StringAttribute(values[i]));
		}

		storage.storeWorkItem("", ifwi);

		return;
	}
}
