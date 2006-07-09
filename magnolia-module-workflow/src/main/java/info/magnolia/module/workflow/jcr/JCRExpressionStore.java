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
package info.magnolia.module.workflow.jcr;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.WorkflowConstants;

import java.io.InputStream;
import java.util.Iterator;

import javax.jcr.ValueFactory;

import openwfe.org.ApplicationContext;
import openwfe.org.ServiceException;
import openwfe.org.engine.expool.PoolException;
import openwfe.org.engine.expressions.FlowExpression;
import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.impl.expool.AbstractExpressionStore;
import openwfe.org.engine.impl.expool.ExpoolUtils;
import openwfe.org.xml.XmlCoder;
import openwfe.org.xml.XmlUtils;

import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the expresion store using JCR
 * @author jackie
 */
public class JCRExpressionStore extends AbstractExpressionStore {

    private static final String ENGINE_ID = "ee";

    private static Logger log = LoggerFactory.getLogger(JCRExpressionStore.class.getName());

    static final Object HM_LOCK = new Object();

    HierarchyManager hm;

    public void init(final String serviceName, final ApplicationContext context, final java.util.Map serviceParams)
        throws ServiceException {
        super.init(serviceName, context, serviceParams);
        this.hm = ContentRepository.getHierarchyManager(WorkflowConstants.WORKSPACE_EXPRESSION);
        if (this.hm == null) {
            throw new ServiceException("Can't access HierarchyManager for workitems");
        }
    }

    /**
     * stroe one expresion
     */
    public synchronized void storeExpression(FlowExpression fe) throws PoolException {
        try {
            synchronized (HM_LOCK) {
                Content ct = findExpression(fe);
                if (log.isDebugEnabled()) {
                    log.debug("Handle for store expression" + ct.getHandle());
                }

                // set expressionId as attribte id
                ValueFactory vf = ct.getJCRNode().getSession().getValueFactory();
                String value = fe.getId().toParseableString();
                ct.createNodeData(WorkflowConstants.NODEDATA_ID, vf.createValue(value));

                if (log.isDebugEnabled()) {
                    log.debug("id_value=" + value);
                }
                serializeExpressionAsXml(ct, fe);
                hm.save();
            }
        }
        catch (Exception e) {
            log.error("store exception failed,", e);
        }
    }

    private void serializeExpressionAsXml(Content c, FlowExpression fe) throws Exception {
        Element encoded = XmlCoder.encode(fe);
        final org.jdom.Document doc = new org.jdom.Document(encoded);
        String s = XmlUtils.toString(doc, null);
        ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
        c.createNodeData(WorkflowConstants.NODEDATA_VALUE, vf.createValue(s));
    }

    /**
     * remove one expresion
     */
    public synchronized void unstoreExpression(FlowExpression fe) throws PoolException {
        try {
            Content ret = findExpression(fe);
            if (ret != null) {
                synchronized (HM_LOCK) {
                    ret.delete();
                    hm.save();
                }
            }
        }
        catch (Exception e) {
            log.error("unstore exception failed,", e);
        }

    }

    public final String toXPathFriendlyString(final FlowExpressionId fei) {
        final StringBuffer buffer = new StringBuffer();
        final String engineId = fei.getEngineId();

        buffer.append(WorkflowConstants.SLASH);
        buffer.append(engineId);
        // engine storage
        if (engineId.equals(ENGINE_ID)) {
            return buffer.toString();
        }

        buffer.append(WorkflowConstants.SLASH);
        buffer.append(fei.getWorkflowDefinitionName());
        buffer.append(WorkflowConstants.SLASH);
        buffer.append(fei.getWorkflowInstanceId());
        buffer.append(WorkflowConstants.SLASH);
        buffer.append(fei.getExpressionId());
        return buffer.toString();
    }

    private Content findExpression(FlowExpression fe) throws Exception {
        return findExpression(fe.getId());
    }

    private Content findExpression(FlowExpressionId fei) throws Exception {
        String local = toXPathFriendlyString(fei);
        if (log.isDebugEnabled()) {
            log.debug("accessing expresion: expression id = " + fei.toParseableString());
        }
        if (hm.isExist(local)) {
            return hm.getContent(local);
        }
        else {
            return ContentUtil.createPath(hm, local, ItemType.EXPRESSION);
        }
    }

    /**
     * load expreson by id
     */
    public FlowExpression loadExpression(FlowExpressionId fei) throws PoolException {
        try {
            Content ret = findExpression(fei);
            if (ret != null) {
                final FlowExpression decode = deserializeExpressionAsXml(ret);
                decode.setApplicationContext(getContext());
                return decode;
            }
        }
        catch (Exception e) {
            // ignore. The expression cannot be found. This is reported to the
            // calling method
        }

        // no expression found. Throw an exception ?
        throw new PoolException("can not get this expression (id=" + fei.asStringId() + ")");
    }

    private FlowExpression deserializeExpressionAsXml(Content ret) throws Exception {
        InputStream s = ret.getNodeData(WorkflowConstants.NODEDATA_VALUE).getStream();
        final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
        Document doc = builder.build(s);
        return (FlowExpression) XmlCoder.decode(doc);
    }

    /**
     * return a iterator of content
     */
    public synchronized Iterator contentIterator(Class assignClass) {
        try {
            return new StoreIterator(assignClass);
        }
        catch (Exception e) {
            log.error("Could not get a content iterator");
            return null;
        }

    }

    /**
     * //TODO what's this suppose to do ? return size of expresion
     */
    public int size() {
        try {
            QueryManager qm = MgnlContext.getSystemContext().getQueryManager(WorkflowConstants.WORKSPACE_EXPRESSION);
            Query query = qm.createQuery(WorkflowConstants.STORE_ITERATOR_QUERY, Query.SQL);
            QueryResult qr = query.execute();
            return qr.getContent().size();
        }
        catch (Exception e) {
            log.error("Error while getting the size of the expression store:" + e.getMessage());
            return -1;
        }
    }

    /**
     * 'lightweight' storeIterator. The previous version were stuffing all the expression within a collection and
     * returning an iterator on it.
     * <p>
     * The remainaing question is : what's behind Magnolia's Content.iterator() method ?
     */
    protected final class StoreIterator implements Iterator {

        //
        // FIELDS

        private Class assignClass;

        private Iterator rootIterator = null;

        private FlowExpression next = null;

        //
        // CONSTRUCTORS

        public StoreIterator(final Class assignClass) throws Exception {
            this.assignClass = assignClass;
            QueryManager qm = MgnlContext.getSystemContext().getQueryManager(WorkflowConstants.WORKSPACE_EXPRESSION);
            Query query = qm.createQuery(WorkflowConstants.STORE_ITERATOR_QUERY, Query.SQL);
            if (log.isDebugEnabled()) {
                log.debug("xx-->query executed:" + query.getStatement());
            }
            QueryResult qr = query.execute();
            this.rootIterator = qr.getContent().iterator();
            this.next = fetchNext();
        }

        //
        // METHODS

        public boolean hasNext() {
            return (this.next != null);
        }

        public FlowExpression fetchNext() {

            if (!this.rootIterator.hasNext()) {
                return null;
            }

            final Content content = (Content) this.rootIterator.next();
            try {

                final FlowExpression fe = deserializeExpressionAsXml(content);

                if (!ExpoolUtils.isAssignableFromClass(fe, this.assignClass)) {
                    return fetchNext();
                }

                return fe;

            }
            catch (Exception e) {
                return null;
            }
        }

        public Object next() throws java.util.NoSuchElementException {

            final FlowExpression current = this.next;

            if (current == null) {
                throw new java.util.NoSuchElementException();
            }

            this.next = fetchNext();

            return current;
        }

        public void remove() {
            // not necessary
        }
    }

}
