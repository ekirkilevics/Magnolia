package info.magnolia.module.owfe.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.worklist.store.StoreException;
import openwfe.org.xml.XmlCoder;
import openwfe.org.xml.XmlUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class JCRWorkItemAPI {

	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(JCRWorkItemAPI.class.getName());

	//
	// CONSTANTS & co
	public final static String REPO_OWFE = "owfe";

	public final static String WORKSPACEID = "Store";

	public final static String WORKITEM_NODENAME = "workItem";

	//
	// FIELDS
	// Repository repository = null;
	HierarchyManager hm = null;

	//
	// CONSTRUCTORS
	public JCRWorkItemAPI() throws Exception {
		hm = ContentRepository.getHierarchyManager(REPO_OWFE, WORKSPACEID);
		if (hm == null) {
			throw new Exception(
					"Can't get HierarchyManager Object for workitems repository");
		}

		//
		// done

	}

	//
	// METHODS

	/**
	 * remove one workItem by its ID
	 */
	public void removeWorkItem(FlowExpressionId fei)
			throws StoreException {
		try {
			Content ct = getWorkItemById(fei);
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

	/**
	 * retrieve work item by 
	 * @param storeName
	 * @param fei
	 * @return
	 * @throws StoreException
	 */
	public InFlowWorkItem retrieveWorkItem(final String storeName,
			final FlowExpressionId fei) throws StoreException {
		if (log.isDebugEnabled()) {
			log.debug("starting retrieve work item. this = " + this);
			log.debug("retrieve work item for ID = " + fei.toParseableString());
		}
		// String fileName = determineFileName(storeName, fei, false);
		//
		// fileName = Utils.getCanonicalPath
		// (getContext().getApplicationDirectory(), fileName);
		Content ct = getWorkItemById(fei);

		if (ct == null)
			throw new StoreException("cannot find workitem " + fei);

		try {
			return loadWorkItem(ct);
		} catch (Exception e) {
			throw new StoreException("load work item form xml failed", e);
		}
	}

	/** 
	 * load a work item from a JCR content
	 * @param ct the content node
	 * @return
	 * @throws Exception
	 */
	public static InFlowWorkItem loadWorkItem(Content ct) throws Exception {
		InFlowWorkItem wi;
		InputStream s = ct.getNodeData("value").getStream();
		if (log.isDebugEnabled())
			log.debug("retrieve work item: value = " + s.toString());
		final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
		Document doc = builder.build(s);
		wi = (InFlowWorkItem) XmlCoder.decode(doc);

		Iterator itt = wi.getAttributes().alphaStringIterator();
		while (itt.hasNext()) {
			Object o = itt.next();
			String name1 = (String) o;
			if (log.isDebugEnabled())
				log.debug(name1 + "=" + wi.getAttribute(name1).toString());
		}
		return wi;
	}



	/**
	 * retrieve a work item by participant name
	 * @param participant	the full participant name (for example, user-superuser)
	 */
	public Content getWorkItemByParticipant(String participant) {
		String queryString = "//*[@participant=\"" + participant + "\"]";
		log.info("xpath query string = " + queryString);
		List list = doQuery(queryString);
		if (list != null && list.size() > 0)
			return (Content) list.get(0);
		else
			return null;
	}

	/**
	 * get work item by id
	 * 
	 * @param fei
	 */
	public Content getWorkItemById(FlowExpressionId fei) {
		String path = createPathFromId(fei);

		log.debug("path = " + path);
		
		try {
            return hm.getContent(path, false, ItemType.WORKITEM);
		} catch (Exception e) {
			log.error("get work item by id failed, path = " + path, e);
		}		
		return null;
	}
	
	/**
	 * check whether the specified work item exists
	 * @param fei	expression id of work item
	 * @return	true if exist, false if not
	 */
	public boolean hasWorkItem(FlowExpressionId fei) throws AccessDeniedException,
    RepositoryException {
		String path = createPathFromId(fei);
		log.debug("path = " + path);
		Content c;
		try {
			c = hm.getContent(path, false, ItemType.WORKITEM);							
		} catch (PathNotFoundException e) {
			return false;
		}
		
		return (c!=null);
	}

	/**
	 * check if the content contains the right work Item  with same id
	 * @param ct JCR content
	 * @param eid	id of work item
	 */
	public boolean checkContentWithEID(Content ct, FlowExpressionId eid) {
		String cid = ct.getNodeData("ID").getString();
		if (log.isDebugEnabled())
			log.debug("checkContentWithEID: ID = " + cid);
		FlowExpressionId id = FlowExpressionId.fromParseableString(cid);
		return id.equals(eid);
	}

	/**
	 * convert the name to valid path
	 * @param id
	 */
	private String convertPath(String id) {
		return StringUtils.replace(StringUtils.replace(id, "|", ""), ":", ".");
	}

	/**
	 * create the jcr node path for work Item by its id
	 * @param eid
	 */
	private String createPathFromId(FlowExpressionId eid) {
		String ret;
		// FlowExpressionId eid = wi.getId();
		String wlInstId = eid.getWorkflowInstanceId();
		int groupNumber = Integer.valueOf(wlInstId.substring(wlInstId.length()-3)).intValue()%100;
		ret = eid.getWorkflowDefinitionName() + "/"
				+ eid.getWorkflowDefinitionRevision() + "/" + groupNumber +"/"
				+ eid.getWorkflowInstanceId() + "/" + eid.getExpressionName()
				+ "/" + eid.getExpressionId();

		return convertPath(ret);
	}

	/**
	 * store work Item
	 * @param arg0
	 * @param wi	the work item intends to be stored
	 * @throws StoreException
	 */
	public void storeWorkItem(String arg0, InFlowWorkItem wi)
			throws StoreException {
		try {

			// delete it if already exist
			if (hasWorkItem(wi.getId()))
			{
				Content ct = getWorkItemById(wi.getId());
				if (ct != null)
					removeWorkItem(wi.getId());
			}
			
			// crete path from work item id
			String path = createPathFromId(wi.getId());
			log.info("workitem id = " + path);

			Content newc = ContentUtil.createPath(hm, path, ItemType.WORKITEM);

			ValueFactory vf = newc.getJCRNode().getSession().getValueFactory();
			String sId = wi.getLastExpressionId().toParseableString();

			newc.createNodeData("ID", vf.createValue(sId));
			log.info("ID=" + sId);
			newc.createNodeData("participant", vf.createValue(wi
                    .getParticipantName()));
			log.info("participant = " + wi.getParticipantName());
			StringAttribute assignTo = (StringAttribute) wi
					.getAttribute("assignTo");
			if (assignTo != null) {
				String s = assignTo.toString();
				if (s.length() > 0)
					newc.createNodeData("assignTo", vf.createValue(s));
				log.info("assignTo=" + s);
			}

			// convert to xml string
			Element encoded = XmlCoder.encode(wi);
			final org.jdom.Document doc = new org.jdom.Document(encoded);
			String s = XmlUtils.toString(doc, null);
			newc.createNodeData("value", vf.createValue(s));
			if (log.isDebugEnabled())
				log.debug("store work item: value=" + s);
			/*
			 * // store all attributes StringMapAttribute sma =
			 * wi.getAttributes(); Iterator it = sma.alphaStringIterator();
			 * while (it.hasNext()) { StringAttribute sa = (StringAttribute)
			 * it.next(); Attribute value = (Attribute) sma.get(sa);
			 * newc.createNodeData(sa.toString(), vf.createValue(value
			 * .toString())); }
			 */
			hm.save();

			// for testing
			// exportToFile("d:\\wi.xml", "/");

			log.info("store work item ok. ");
		} catch (Exception e) {
			log.error("store work item failed", e);
			throw new StoreException(e.toString());
		}

	}

	/**
	 * export the content to file (fot testing)
	 * @param fileName
	 * @param path
	 */
	public void exportToFile(String fileName, String path) {
		if (path == null || path.length() == 0)
			path = "/";
		int i = fileName.lastIndexOf(".");
		String pre = fileName;
		String ext = "";
		if (i > 0) {
			log.info("i = " + i);
			ext = fileName.substring(i + 1, fileName.length());
			pre = fileName.substring(0, i - 1);
		}

		try {
			FileOutputStream out_sv = new FileOutputStream(pre + "_sv" + "."
					+ ext);
			hm.getWorkspace().getSession().exportSystemView(path, out_sv,
					false, false);

			FileOutputStream out_dv = new FileOutputStream(pre + "_dv" + "."
					+ ext);
			hm.getWorkspace().getSession().exportDocumentView(path, out_dv,
					false, false);

		} catch (Exception e) {
			log.error("can not export to file " + fileName, e);
		}
	}

	/**
	 * export content to console (for testing purpose)
	 * @param path
	 */
	public void exportToConsole(String path) {

		if (path == null || path.length() == 0)
			path = "/";
		try {

			hm.getWorkspace().getSession().exportSystemView(path, System.out,
					false, false);

			hm.getWorkspace().getSession().exportDocumentView(path, System.out,
					false, false);

		} catch (Exception e) {
			log.error("can not export to console", e);
		}
	}

	
	/**
	 * execute the xPath Query
	 */
	public List doQuery(String queryString) {
		log.debug("enter doQuery");
		ArrayList list = new ArrayList();
		log.info("xpath query string: " + queryString);
		// storage.exportToFile("d:\\owfe_root.xml", null);
		Query q;
		try {
			// there is no query manager for config repo, so remove code
			MgnlContext.setInstance(MgnlContext.getSystemContext()); // for
			// testing
			// purpose

			q = MgnlContext.getQueryManager("owfe", "Store").createQuery(
					queryString, "xpath"); //$NON-NLS-1$

			QueryResult result = q.execute();
			if (result == null) {
				log.info("query result is null");
				return null;
			}
			// log.info("result size of mgnl:content = " +
			// result.getContent().size());
			// log.info("result size of workitem = " +
			// result.getContent("workItem").size());
			Iterator it = result.getContent("workItem").iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();
				String title = ct.getTitle();
				log.info("title=" + title);
				String sname = ct.getName();
				log.info("name=" + sname);
				// storage.exportToConsole(ct.getJCRNode().getPath());
				// storage.exportToFile("d:\\owfe_ct.xml",
				// ct.getJCRNode().getPath());
				InFlowWorkItem wi = loadWorkItem(ct);
				if (wi == null) {
					log.error("can not load found workitem");
					continue;
				}
				log.info("added workitem to return list ok");
				list.add(wi);
			}
		} catch (Exception e) {
			log.error("query flow failed", e);
			return null;
		}

		log.debug("leave doQuery");
		return list;

	}
	//
	// STATIC METHODS

}
