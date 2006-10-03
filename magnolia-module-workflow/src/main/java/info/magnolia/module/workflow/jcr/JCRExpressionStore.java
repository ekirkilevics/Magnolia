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
import openwfe.org.util.beancoder.XmlBeanCoder;
import openwfe.org.xml.XmlUtils;

import org.jdom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The JCR implementation of the expression store.
 * @author Jackie Ju
 * @author Nicolas Modrzyk
 * @author John Mettraux
 */
public class JCRExpressionStore extends AbstractExpressionStore {

    //
    // CONSTANTS & co

    private static final String ENGINE_ID = "ee";

    protected static Logger log = LoggerFactory.getLogger(JCRExpressionStore.class.getName());

    // private static final Object HM_LOCK = new Object();

    //
    // FIELDS

    private HierarchyManager hierarchyManager = null;

    //
    // CONSTRUCTORS

    public void init(final String serviceName, final ApplicationContext context, final java.util.Map serviceParams)
        throws ServiceException {

        super.init(serviceName, context, serviceParams);
        this.hierarchyManager = ContentRepository.getHierarchyManager(WorkflowConstants.WORKSPACE_EXPRESSION);

        if (this.hierarchyManager == null) {
            throw new ServiceException("Can't access HierarchyManager for workitems");
        }
    }

    //
    // METHODS from ExpressionStore

    //
    // METHODS

    /**
     * Stores one expresion
     */
    public synchronized void storeExpression(final FlowExpression fe) throws PoolException {

        try {
            synchronized (this.getClass()) {

                Content cExpression = findExpression(fe);

                if (log.isDebugEnabled()) {

                    log.debug("storeExpression() handle is " + cExpression.getHandle());
                }

                // set expressionId as attribte id

                ValueFactory vf = cExpression.getJCRNode().getSession().getValueFactory();
                String value = fe.getId().toParseableString();

                cExpression.createNodeData(WorkflowConstants.NODEDATA_ID, vf.createValue(value));

                // serializeExpressionWithBeanCoder(ct, fe);

                serializeExpressionAsXml(cExpression, fe);

                this.hierarchyManager.save();
            }
        }
        catch (final Exception e) {

            log.error("storeExpression() store exception failed", e);

            throw new PoolException("storeExpression() store exception failed", e);
        }
    }

    /**
     * Removes the expression from the JCR storage.
     */
    public synchronized void unstoreExpression(final FlowExpression fe) throws PoolException {

        try {
            Content cExpression = findExpression(fe);

            if (cExpression != null) {

                synchronized (this.getClass()) {

                    cExpression.delete();

                    this.hierarchyManager.save();
                }
            }
            else {
                log.info("unstoreExpression() " + "didn't find content node for fe " + fe.getId().toParseableString());
            }
        }
        catch (final Exception e) {

            log.error("unstoreExpression() unstore exception failed", e);

            throw new PoolException("unstoreExpression() unstore exception failed", e);
        }
    }

    /**
     * Returns an iterator on the content of that expression store.
     */
    public synchronized Iterator contentIterator(final Class assignClass) {

        try {
            return new StoreIterator(assignClass);
        }
        catch (final Throwable t) {
            log.error("contentIterator() failed to set up an iterator", t);
        }

        // return null;
        return new java.util.ArrayList(0).iterator();
    }

    /**
     * Loads an expression given its id.
     */
    public FlowExpression loadExpression(final FlowExpressionId fei) throws PoolException {

        try {

            Content cExpression = findExpression(fei);

            if (cExpression != null) {

                final FlowExpression expression = deserializeExpressionAsXml(cExpression);

                expression.setApplicationContext(getContext());

                return expression;
            }
        }
        catch (final Exception e) {

            log.debug("loadExpression() failed for " + fei.asStringId(), e);

            throw new PoolException("loadExpression() failed for " + fei.asStringId(), e);
        }

        if (log.isDebugEnabled()) {

            log.debug("loadExpression() " + "didn't find expression " + fei.asStringId() + " in the repository");
        }

        throw new PoolException("loadExpression() "
            + "didn't find expression "
            + fei.asStringId()
            + " in the repository");

    }

    /**
     * Returns the number of expressions currently stored in that store.
     */
    public int size() {

        try {

            QueryManager qm = MgnlContext.getSystemContext().getQueryManager(WorkflowConstants.WORKSPACE_EXPRESSION);

            Query q = qm.createQuery(WorkflowConstants.STORE_ITERATOR_QUERY, Query.SQL);
            QueryResult qr = q.execute();

            return qr.getContent().size();
        }
        catch (final Exception e) {

            log.error("size() failed", e);

            return -1;
        }
    }

    //
    // METHODS

    private void serializeExpressionAsXml(Content c, FlowExpression fe) throws Exception {

        final org.jdom.Document doc = XmlBeanCoder.xmlEncode(fe);
        String s = XmlUtils.toString(doc, null);
        ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
        c.createNodeData(WorkflowConstants.NODEDATA_VALUE, vf.createValue(s));
    }

    /*
     * private void serializeExpressionWithBeanCoder(Content c,FlowExpression fr) throws Exception { OwfeJcrBeanCoder
     * coder = new OwfeJcrBeanCoder(null,new MgnlNode(c),WorkflowConstants.NODEDATA_VALUE); coder.encode(fr); }
     */

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
        buffer.append(fei.getExpressionId()).append("__").append(fei.getExpressionName());

        return buffer.toString();
    }

    private Content findExpression(final FlowExpression fe) throws Exception {

        return findExpression(fe.getId());
    }

    private Content findExpression(final FlowExpressionId fei) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("findExpression() looking for " + fei.toParseableString());
        }

        final String path = toXPathFriendlyString(fei);

        if (this.hierarchyManager.isExist(path)) {
            return this.hierarchyManager.getContent(path);
        }
        else {
            return ContentUtil.createPath(this.hierarchyManager, path, ItemType.EXPRESSION);
        }
    }

    private FlowExpression deserializeExpressionAsXml(final Content c) throws Exception {

        final InputStream is = c.getNodeData(WorkflowConstants.NODEDATA_VALUE).getStream();

        if (is == null)
            return null;
        //
        // not an expression

        final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();

        final Document doc = builder.build(is);

        return (FlowExpression) XmlBeanCoder.xmlDecode(doc);
    }

    /*
     * could be useful once again later... private void debugNodeData(final Content c) throws Exception { final
     * InputStream is = c .getNodeData(WorkflowConstants.NODEDATA_VALUE) .getStream(); if (is == null) return; final
     * java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(); final byte[] buffer = new byte[70];
     * final int i = is.read(buffer); if (i > -1) { baos.write(buffer); baos.flush(); } log.warn
     * ("deserializeExpressionAsXml() nodeData is >"+baos.toString()+"<"); }
     */

    /*
     * protected FlowExpression deserializeExpressionWithBeanCoder(Content ret) throws Exception { OwfeJcrBeanCoder
     * coder = new OwfeJcrBeanCoder(null, new MgnlNode(ret.getContent(WorkflowConstants.NODEDATA_VALUE))); return
     * (FlowExpression)coder.decode(); }
     */

    /**
     * 'lightweight' storeIterator. The previous version were stuffing all the expression within a collection and
     * returning an iterator on it.
     * <p>
     * The remaining question is : what's behind Magnolia's Content.iterator() method ?
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

            super();

            this.assignClass = assignClass;

            final QueryManager qm = MgnlContext.getSystemContext().getQueryManager(
                WorkflowConstants.WORKSPACE_EXPRESSION);

            final Query query = qm.createQuery(WorkflowConstants.STORE_ITERATOR_QUERY, Query.SQL);

            final QueryResult qr = query.execute();

            if (log.isDebugEnabled()) {
                log.debug("() query found " + qr.getContent("expression").size() + " elements");
            }

            this.rootIterator = qr.getContent("expression").iterator();

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

                if (fe == null) {
                    return fetchNext();
                }

                fe.setApplicationContext(JCRExpressionStore.this.getContext());

                if (!ExpoolUtils.isAssignableFromClass(fe, this.assignClass)) {
                    return fetchNext();
                }

                return fe;
            }
            catch (final Exception e) {

                log.error("fetchNext() problem", e);
                return null;
            }
        }

        public Object next() throws java.util.NoSuchElementException {

            final FlowExpression current = this.next;

            if (current == null) {
                throw new java.util.NoSuchElementException();
            }

            this.next = fetchNext();

            if (log.isDebugEnabled()) {
                log.debug("next() is  " + this.next.getId());
            }

            return current;
        }

        public void remove() {
            // not necessary
        }
    }

}
