package info.magnolia.module.owfe.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.util.ContentUtil;
import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.worklist.store.StoreException;
import openwfe.org.xml.XmlCoder;
import openwfe.org.xml.XmlUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import javax.jcr.ValueFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
    public int countWorkItems(String arg0) throws StoreException {
        try {
            Content root = hm.getRoot();
            Collection c = root.getChildren(ItemType.WORKITEM);
            // @fix it
            return c.size();
        } catch (Exception e) {
            log.error("exception:" + e);
            throw new StoreException(e.toString());
        }

    }

    public List listWorkItems(String s, int limit) throws StoreException {
        ArrayList ret = new ArrayList();
        try {
            Content root = hm.getRoot();
            Collection c = root.getChildren(ItemType.WORKITEM);
            // @fix it
            Iterator it = c.iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();
                /*
                     * InFlowWorkItem wi = new InFlowWorkItem(); String name =
                     * ct.getName(); if (true) { wi = new InFlowWorkItem();
                     * wi.setId(FlowExpressionId.fromParseableString(ct
                     * .getNodeData("ID").getString()));
                     * wi.setParticipantName(this.getName()); // add attributes
                     * Collection attrs = ct.getNodeDataCollection(); Iterator a_it =
                     * attrs.iterator(); while (a_it.hasNext()) { NodeData nd =
                     * (NodeData) a_it.next(); wi.addAttribute(nd.getName(), new
                     * StringAttribute(nd .getString())); } }
                     */
                InFlowWorkItem wi = loadWorkItem(ct);
                ret.add(wi);

            }

            return ret;
        } catch (Exception e) {
            log.error("exception:" + e);
            throw new StoreException(e.toString());
        }

    }

    public void removeWorkItem(String arg0, FlowExpressionId fei)
            throws StoreException {
        try {
            Content root = hm.getRoot();
            Collection c = root.getChildren(ItemType.WORKITEM);
            // @fix it
            Iterator it = c.iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();
                if (checkContentWithEID(ct, fei)) {
                    ct.delete();
                    hm.save();
                    return;
                }
            }
        } catch (Exception e) {
            log.error("exception:" + e);
            throw new StoreException(e.toString());
        }

    }

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

    //
    // public Content findWorkItem(FlowExpressionId fei) {
    // try {
    // Content root = hm.getRoot();
    // Collection c = root.getChildren(ItemType.WORKITEM);
    // Iterator it = c.iterator();
    // while (it.hasNext()) {
    // Content ct = (Content) it.next();
    // String name = ct.getName();
    //
    // if (checkContentWithEID(ct, fei)) {
    // /*
    // * wi = new InFlowWorkItem(); wi.setId(fei);
    // * wi.setParticipantName(this.getName()); // add attributes
    // * Collection attrs = ct.getNodeDataCollection(); Iterator
    // * a_it = attrs.iterator(); while (a_it.hasNext()){ NodeData
    // * nd = (NodeData)a_it.next(); wi.addAttribute(nd.getName(),
    // * new StringAttribute(nd.getString())); }
    // */
    // return ct;
    // }
    // }
    //
    // } catch (Exception e) {
    // log.error("exception:" + e);
    // }
    // return null;
    //
    // }
//    public Content findWorkItem(FlowExpressionId fei) {
//        String sFei = fei.toParseableString();
//        // String queryString = "//*[@name=\'workitem\']/*[/jcr:contains(ID,
//        // \'"+fei+"\')]";
//        String queryString = "//*[@ID=\"" + sFei + "\"]";
//        log.info("xpath query string = " + queryString);
//        return doQuery(queryString);
//    }
    
    public Content getWorkItemByParticipant(String participant) {  
        String queryString = "//*[@participant=\"" + participant + "\"]";
        log.info("xpath query string = " + queryString);
        List list =  doQuery(queryString);
        if (list != null && list.size()>0)
        	return (Content)list.get(0);
        else
        	return null;
    }
    
    
    
    /**
     * get work item by id
     * @param fei
     * @return
     */
    public Content getWorkItemById(FlowExpressionId fei){
    	String path = createPathFromId(fei);
    	
    	log.info("path = " + path);
    	 try {
             Content c = hm.getContent(path, false, ItemType.WORKITEM);
             return c;
    	 }catch (Exception e){
    		 log.error("get work item by id failed, path = " + path, e);
    	 }
    	 
    	 return null;
    	 
    }

    public boolean checkContentWithEID(Content ct, FlowExpressionId eid) {
        String cid = ct.getNodeData("ID").getString();
        if (log.isDebugEnabled())
            log.debug("checkContentWithEID: ID = " + cid);
        FlowExpressionId id = FlowExpressionId.fromParseableString(cid);
        return id.equals(eid);
    }

    private String convertPath(String id) {
        return StringUtils.replace(StringUtils.replace(id, "|", ""), ":", ".");
    }
    
    private String createPathFromId(FlowExpressionId eid){
    	String ret = "";
//    	FlowExpressionId eid = wi.getId();
    	ret = eid.getWorkflowDefinitionName() + "/" + eid.getWorkflowDefinitionRevision()
    			+ "/" + eid.getWorkflowInstanceId()
    			+ "/" + eid.getExpressionName()
    			+ "/" + eid.getExpressionId();
    	
    	return convertPath(ret);    	
    }
    

    public void storeWorkItem(String arg0, InFlowWorkItem wi)
            throws StoreException {
        try {

            // delete it if already exist
            Content ct = getWorkItemById(wi.getId());
            if (ct != null)
                removeWorkItem("", wi.getId());

            Content root = hm.getRoot();
            //Collection c = root.getChildren(ItemType.WORKITEM);

            String path = createPathFromId(wi.getId());
            log.info("workitem id = " + path);           
            //Content newc = root.createContent(path, ItemType.WORKITEM);
            Content newc = ContentUtil.createPath(hm, path, ItemType.WORKITEM);
            
            ValueFactory vf = newc.getJCRNode().getSession().getValueFactory();
            String sId = wi.getLastExpressionId().toParseableString();  
             
            newc.createNodeData("ID", vf.createValue(sId));
            log.info("ID=" + sId);
            newc.createNodeData("participant", vf.createValue(wi.getParticipantName().toString()));
            log.info("participant = " + wi.getParticipantName().toString());
            StringAttribute assignTo = (StringAttribute)wi.getAttribute("assignTo");
            if (assignTo != null){
            	String s = assignTo.toString();
            	if (s.length() >0)
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
            exportToFile("d:\\wi.xml", "/");

            log.info("store work item ok. ");
        } catch (Exception e) {
            log.error("store work item failed", e);
            throw new StoreException(e.toString());
        }

    }

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
            FileOutputStream out_sv = new FileOutputStream(pre + "_sv" + "." + ext);
            hm.getWorkspace().getSession().exportSystemView(path, out_sv, false,
                    false);

            FileOutputStream out_dv = new FileOutputStream(pre + "_dv" + "." + ext);
            hm.getWorkspace().getSession().exportDocumentView(path, out_dv, false,
                    false);

        } catch (Exception e) {
            log.error("can not export to file " + fileName, e);
        }
    }

    public void exportToConsole(String path) {

        if (path == null || path.length() == 0)
            path = "/";
        try {

            hm.getWorkspace().getSession().exportSystemView(path, System.out, false,
                    false);


            hm.getWorkspace().getSession().exportDocumentView(path, System.out, false,
                    false);

        } catch (Exception e) {
            log.error("can not export to console");
        }
    }

    public List doQuery(String queryString) {
    	ArrayList list = new ArrayList();
        log.info("xpath query string: " + queryString);
        //storage.exportToFile("d:\\owfe_root.xml", null);
        Query q;
        try {
            // there is no query manager for config repo, so remove code
            MgnlContext.setInstance(MgnlContext.getSystemContext()); // for
            // testing
            // purpose

            q = MgnlContext.getQueryManager("owfe", "Store").createQuery(queryString,
                    "xpath"); //$NON-NLS-1$

            QueryResult result = q.execute();
            if (result == null) {
                log.info("query result is null");
                return null;
            }
            //log.info("result size of mgnl:content = " + result.getContent().size());
            //log.info("result size of workitem = " + result.getContent("workItem").size());
            Iterator it = result.getContent("workItem").iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();
                String title = ct.getTitle();
                log.info("title=" + title);
                String sname = ct.getName();
                log.info("name=" + sname);
                //storage.exportToConsole(ct.getJCRNode().getPath());
                //storage.exportToFile("d:\\owfe_ct.xml", ct.getJCRNode().getPath());
                list.add(ct);
            }
        } catch (Exception e) {
            log.error("query flow failed", e);
            return null;
        }

        log.info("query return null");
        return list;

    }
    //
    // STATIC METHODS

}
