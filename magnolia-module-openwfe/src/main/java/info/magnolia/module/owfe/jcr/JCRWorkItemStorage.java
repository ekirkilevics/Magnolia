package info.magnolia.module.owfe.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import openwfe.org.ApplicationContext;
import openwfe.org.ServiceException;
import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.worklist.impl.store.AbstractStorage;
import openwfe.org.worklist.store.StoreException;
import openwfe.org.xml.XmlCoder;
import openwfe.org.xml.XmlUtils;
import org.jdom.Document;
import org.jdom.Element;

import javax.jcr.ValueFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//public class JCRWorkItemStore implements WorkItemStorage {

public class JCRWorkItemStorage extends AbstractStorage {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JCRWorkItemStorage.class
            .getName());

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
    public void init(final String serviceName, final ApplicationContext context, final java.util.Map serviceParams)
            throws ServiceException {
        super.init(serviceName, context, serviceParams);

        //
        // determine work directory

        // repository = ContentRepository.getRepository(REPO_WORKITMES);
        // log.info("Repository for workitmes = " + repository);
        // if (repository == null){
        // throw new ServiceException("Can't get repository for workitems");
        // }
        hm = ContentRepository.getHierarchyManager(REPO_OWFE, WORKSPACEID);
        if (hm == null) {
            throw new ServiceException("Can't get HierarchyManager Object for workitems repository");
        }

        //
        // done

        log.info("init() storage '" + serviceName + "' ready.");
    }

    //
    // METHODS
    public int countWorkItems(String arg0) throws StoreException {
        try {
            Content root = hm.getRoot();
            Collection c = root.getChildren(ItemType.WORKITEM);
            // @fix it
            return c.size();
        }
        catch (Exception e) {
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
                 * InFlowWorkItem wi = new InFlowWorkItem(); String name = ct.getName(); if (true) { wi = new
                 * InFlowWorkItem(); wi.setId(FlowExpressionId.fromParseableString(ct .getNodeData("ID").getString()));
                 * wi.setParticipantName(this.getName()); // add attributes Collection attrs =
                 * ct.getNodeDataCollection(); Iterator a_it = attrs.iterator(); while (a_it.hasNext()) { NodeData nd =
                 * (NodeData) a_it.next(); wi.addAttribute(nd.getName(), new StringAttribute(nd .getString())); } }
                 */
                InFlowWorkItem wi = loadWorkItem(ct);
                ret.add(wi);

            }

            return ret;
        }
        catch (Exception e) {
            log.error("exception:" + e);
            throw new StoreException(e.toString());
        }

    }

    public void removeWorkItem(String arg0, FlowExpressionId fei) throws StoreException {
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
        }
        catch (Exception e) {
            log.error("exception:" + e);
            throw new StoreException(e.toString());
        }

    }

    public InFlowWorkItem retrieveWorkItem(final String storeName, final FlowExpressionId fei) throws StoreException {
        if (log.isDebugEnabled()) {
            log.debug("starting retrieve work item. this = " + this);
            log.debug("retrieve work item for ID = " + fei.toParseableString());
        }
        // String fileName = determineFileName(storeName, fei, false);
        //
        // fileName = Utils.getCanonicalPath
        // (getContext().getApplicationDirectory(), fileName);
        Content ct = findWorkItem(fei);

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

    public static InFlowWorkItem loadWorkItem(Content ct) throws Exception {
        InFlowWorkItem wi = null;
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

    public Content findWorkItem(FlowExpressionId fei) {
        try {
            Content root = hm.getRoot();
            Collection c = root.getChildren(ItemType.WORKITEM);
            Iterator it = c.iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();

                if (checkContentWithEID(ct, fei)) {
                    /*
                     * wi = new InFlowWorkItem(); wi.setId(fei); wi.setParticipantName(this.getName()); // add
                     * attributes Collection attrs = ct.getNodeDataCollection(); Iterator a_it = attrs.iterator(); while
                     * (a_it.hasNext()){ NodeData nd = (NodeData)a_it.next(); wi.addAttribute(nd.getName(), new
                     * StringAttribute(nd.getString())); }
                     */
                    return ct;
                }
            }

        }
        catch (Exception e) {
            log.error("exception:" + e);
        }
        return null;

    }

    public boolean checkContentWithEID(Content ct, FlowExpressionId eid) {
        String cid = ct.getNodeData("ID").getString();
        if (log.isDebugEnabled())
            log.debug("checkContentWithEID: ID = " + cid);
        FlowExpressionId id = null;

        id = FlowExpressionId.fromParseableString(cid);

        return id.equals(eid);
    }

    public void storeWorkItem(String arg0, InFlowWorkItem wi) throws StoreException {
        try {

            // delete it if already exist
            Content ct = findWorkItem(wi.getId());
            if (ct != null) {
                removeWorkItem("", wi.getId());
            }

            Content root = hm.getRoot();

            // @fix it
            Content newc = root.createContent("workItem", ItemType.WORKITEM);

            ValueFactory vf = newc.getJCRNode().getSession().getValueFactory();
            String sId = wi.getLastExpressionId().toParseableString();
            if (log.isDebugEnabled())
                log.debug("store work item: ID = " + sId);
            newc.createNodeData("ID", vf.createValue(sId));
            // convert to xml string
            Element encoded = XmlCoder.encode(wi);
            final org.jdom.Document doc = new org.jdom.Document(encoded);
            String s = XmlUtils.toString(doc, null);
            newc.createNodeData("value", vf.createValue(s));
            if (log.isDebugEnabled())
                log.debug("store work item: value=" + s);
            /*
             * // store all attributes StringMapAttribute sma = wi.getAttributes(); Iterator it =
             * sma.alphaStringIterator(); while (it.hasNext()) { StringAttribute sa = (StringAttribute) it.next();
             * Attribute value = (Attribute) sma.get(sa); newc.createNodeData(sa.toString(), vf.createValue(value
             * .toString())); }
             */
            hm.save();
        }
        catch (Exception e) {
            log.error("exception:" + e);
            throw new StoreException(e.toString());
        }

    }

    //
    // STATIC METHODS

}
