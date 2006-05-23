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
package info.magnolia.module.owfe.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.owfe.WorkflowConstants;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JCRWorkItemAPI {

    public final static Logger log = LoggerFactory.getLogger(JCRWorkItemAPI.class.getName());

    HierarchyManager hm;

    public JCRWorkItemAPI() throws Exception {
        this.hm = ContentRepository.getHierarchyManager(WorkflowConstants.WORKSPACE_STORE);
        if (this.hm == null) {
        	Exception e = new Exception("Can't get HierarchyManager Object for workitems repository");
            log.error(e.getMessage(),e);
        	throw e;
        }
    }

    /**
     * remove one workItem by its ID
     */
    public void removeWorkItem(FlowExpressionId fei) throws StoreException {
        try {
            Content ct = getWorkItemById(fei);
            if (ct != null) {
                ct.delete();
                this.hm.save();
                if (log.isDebugEnabled()) {
                    log.debug("work item removed");
                }
            }

        }
        catch (Exception e) {
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
    public InFlowWorkItem retrieveWorkItem(final String storeName, final FlowExpressionId fei) throws StoreException {
        if (log.isDebugEnabled()) {
            log.debug("starting retrieve work item. this = " + this);
            log.debug("retrieve work item for ID = " + fei.toParseableString());
        }

        Content ct = getWorkItemById(fei);

        if (ct == null) {
            throw new StoreException("cannot find workitem " + fei);
        }

        try {
            return loadWorkItem(ct);
        }
        catch (Exception e) {
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
        InputStream s = ct.getNodeData(WorkflowConstants.NODEDATA_VALUE).getStream();
        if (log.isDebugEnabled()) {
            log.debug("retrieve work item: value = " + s.toString());
        }
        final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
        Document doc = builder.build(s);
        wi = (InFlowWorkItem) XmlCoder.decode(doc);

        Iterator itt = wi.getAttributes().alphaStringIterator();
        while (itt.hasNext()) {
            Object o = itt.next();
            String name1 = (String) o;
            if (log.isDebugEnabled()) {
                log.debug(name1 + "=" + wi.getAttribute(name1).toString());
            }
        }
        return wi;
    }

    /**
     * retrieve a work item by participant name
     * @param participant the full participant name (for example, user-superuser)
     */
    public Content getWorkItemByParticipant(String participant) {
        String queryString = "//*[@participant=\"" + participant + "\"]";
        if(log.isDebugEnabled())
        log.debug("xpath query string = " + queryString);
        List list = doQuery(queryString);
        if (list != null && list.size() > 0) {
            return (Content) list.get(0);
        }

        return null;
    }

    /**
     * get work item by id
     * @param fei
     */
    public Content getWorkItemById(FlowExpressionId fei) {
        String path = createPathFromId(fei);
        try {
            return this.hm.getContent(path, false, ItemType.WORKITEM);
        }
        catch (Exception e) {
            log.error("get work item by id failed, path = " + path, e);
        }
        return null;
    }

    /**
     * check whether the specified work item exists
     * @param fei expression id of work item
     * @return true if exist, false if not
     */
    public boolean hasWorkItem(FlowExpressionId fei) throws AccessDeniedException, RepositoryException {
        String path = createPathFromId(fei);
        Content c;
        try {
            c = this.hm.getContent(path, false, ItemType.WORKITEM);
        }
        catch (PathNotFoundException e) {
            return false;
        }

        return (c != null);
    }

    /**
     * check if the content contains the right work Item with same id
     * @param ct JCR content
     * @param eid id of work item
     */
    public boolean checkContentWithEID(Content ct, FlowExpressionId eid) {
        String cid = ct.getNodeData(WorkflowConstants.NODEDATA_ID).getString();
        if (log.isDebugEnabled()) {
            log.debug("checkContentWithEID: ID = " + cid);
        }
        FlowExpressionId id = FlowExpressionId.fromParseableString(cid);
        return id.equals(eid);
    }

    /**
     * convert the name to valid path
     * @param id
     */
    public final String convertPath(String id) {
        return StringUtils.replace(StringUtils.replace(id, WorkflowConstants.BAR, StringUtils.EMPTY), WorkflowConstants.COLON, WorkflowConstants.DOT);
    }

    /**
     * create the jcr node path for work Item by its id
     * @param eid
     */
    public String createPathFromId(FlowExpressionId eid) {
		String wlInstId = eid.getWorkflowInstanceId();
		int groupNumber = Integer.valueOf(
				wlInstId.substring(wlInstId.length() - 3)).intValue() % 100;
		StringBuffer buffer = new StringBuffer(eid.getWorkflowDefinitionName());
		buffer.append(WorkflowConstants.SLASH);
		buffer.append(eid.getWorkflowDefinitionRevision());
		buffer.append(WorkflowConstants.SLASH);
		buffer.append(groupNumber);
		buffer.append(WorkflowConstants.SLASH);
		buffer.append(eid.getWorkflowInstanceId());
		buffer.append(WorkflowConstants.SLASH);
		buffer.append(eid.getExpressionName());
		buffer.append(WorkflowConstants.SLASH);
		buffer.append(eid.getExpressionId());

		return convertPath(buffer.toString());
	}

    /**
	 * store work Item
	 * 
	 * @param arg0
	 * @param wi
	 *            the work item intends to be stored
	 * @throws StoreException
	 */
    public void storeWorkItem(String arg0, InFlowWorkItem wi) throws StoreException {
        try {

            // delete it if already exist
            if (hasWorkItem(wi.getId())) {
                Content ct = getWorkItemById(wi.getId());
                if (ct != null) {
                    removeWorkItem(wi.getId());
                }
            }

            // crete path from work item id
            String path = createPathFromId(wi.getId());
            if(log.isDebugEnabled())
            	log.debug("storing workitem with path = " + path);

            Content newc = ContentUtil.createPath(this.hm, path, ItemType.WORKITEM);

            ValueFactory vf = newc.getJCRNode().getSession().getValueFactory();
            String sId = wi.getLastExpressionId().toParseableString();

            newc.createNodeData(WorkflowConstants.NODEDATA_ID, vf.createValue(sId));
            newc.createNodeData(WorkflowConstants.NODEDATA_PARTICIPANT, vf.createValue(wi.getParticipantName()));
            
            if(log.isDebugEnabled()){
            	log.debug("ID=" + sId);
                log.debug("participant = " + wi.getParticipantName());
            }
            
            StringAttribute assignTo = (StringAttribute) wi.getAttribute(WorkflowConstants.ATT_ASSIGN_TO);
            if (assignTo != null) {
                String s = assignTo.toString();
                if (s.length() > 0) {
                    newc.createNodeData(WorkflowConstants.ATT_ASSIGN_TO, vf.createValue(s));
                }
                if(log.isDebugEnabled()) log.debug("assignTo=" + s);
            }

            // convert to xml string
            Element encoded = XmlCoder.encode(wi);
            final org.jdom.Document doc = new org.jdom.Document(encoded);
            String s = XmlUtils.toString(doc, null);
            newc.createNodeData(WorkflowConstants.NODEDATA_VALUE, vf.createValue(s));

            if(log.isDebugEnabled()) log.debug("store work item: value=" + s);

            this.hm.save();

            if(log.isDebugEnabled()) log.debug("store work item ok. ");
        }
        catch (Exception e) {
            log.error("store work item failed", e);
            throw new StoreException(e.toString());
        }

    }

    /**
     * execute the xPath Query
     */
    public List doQuery(String queryString) {
        ArrayList list = new ArrayList();
        if(log.isDebugEnabled())
        log.debug("xpath query string: " + queryString);
        try {
			final QueryManager queryManager = MgnlContext.getSystemContext().getQueryManager(WorkflowConstants.WORKSPACE_STORE);
			final Query q = queryManager.createQuery(queryString, Query.XPATH); //$NON-NLS-1$

			QueryResult result = q.execute();
			if (result == null) {
				log.info("query result was null");
				return null;
			}

			Iterator it = result.getContent(WorkflowConstants.NODENAME_WORKITEM).iterator();
			while (it.hasNext()) {
				Content ct = (Content) it.next();
				String title = ct.getTitle();
				String sname = ct.getName();

				if (log.isDebugEnabled()) {
					log.debug("title=" + title);
					log.debug("name=" + sname);
				}

				InFlowWorkItem wi = loadWorkItem(ct);
				if (wi == null) {
					log.error("can not load found workitem");
					continue;
				}
				if (log.isDebugEnabled())
					log.debug("added workitem to return list ok");
				list.add(wi);
			}
		} catch (Exception e) {
			log.error("query flow failed", e);
			return null;
		}
        return list;

    }

}
