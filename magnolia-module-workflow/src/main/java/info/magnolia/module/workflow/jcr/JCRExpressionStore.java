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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.cms.core.HierarchyManager;
import openwfe.org.ApplicationContext;
import openwfe.org.ServiceException;
import openwfe.org.engine.expool.PoolException;
import openwfe.org.engine.expressions.FlowExpression;
import openwfe.org.engine.expressions.FlowExpressionId;
import openwfe.org.engine.expressions.raw.RawExpression;
import openwfe.org.engine.impl.expool.AbstractExpressionStore;
import openwfe.org.util.beancoder.XmlBeanCoder;
import openwfe.org.xml.XmlUtils;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ValueFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * The JCR implementation of the expression store.
 *
 * @author Jackie Ju
 * @author Nicolas Modrzyk
 * @author John Mettraux
 * @author gjoseph
 */
public class JCRExpressionStore extends AbstractExpressionStore {
    private static final Logger log = LoggerFactory.getLogger(JCRExpressionStore.class);
    private static final long SLEEP_DELAY_MS = 3 * 1000;
    private static final long NOPING_DELAY_MS = 6 * 1000;
    private static final long MAX_SAVE_DELAY_MS = 30 * 1000;

    private static final String ENGINE_ID = "ee";

    private final HierarchyManagerWrapper hmWrapper;

    public JCRExpressionStore(boolean isStorageDeferred) throws ServiceException {
        final HierarchyManager hierarchyManager = MgnlContext.getSystemContext().getHierarchyManager(WorkflowConstants.WORKSPACE_EXPRESSION);

        if (hierarchyManager == null) {
            throw new ServiceException("Can't access HierarchyManager for workitems");
        }

        if (isStorageDeferred) {
            this.hmWrapper = HierarchyManagerDeferredSaver.startInThread(hierarchyManager, SLEEP_DELAY_MS, NOPING_DELAY_MS, MAX_SAVE_DELAY_MS);
        } else {
            this.hmWrapper = new HierarchyManagerWrapperDelegator(hierarchyManager);
        }

    }

    public void init(final String serviceName, final ApplicationContext context, final Map serviceParams) throws ServiceException {
        super.init(serviceName, context, serviceParams);
    }

    /**
     * Stores one expresion
     */
    public void storeExpression(final FlowExpression fe) throws PoolException {
        try {
            synchronized (hmWrapper) {
                final Content cExpression = findExpression(fe);

                log.debug("storeExpression() handle is " + cExpression.getHandle());

                // set expressionId as attribte id
                ValueFactory vf = cExpression.getJCRNode().getSession().getValueFactory();
                String value = fe.getId().toParseableString();

                cExpression.createNodeData(WorkflowConstants.NODEDATA_ID, vf.createValue(value));

                //serializeExpressionWithBeanCoder(ct, fe);
                serializeExpressionAsXml(cExpression, fe);

                hmWrapper.save();
            }
        } catch (final Exception e) {
            log.error("storeExpression() store exception failed", e);
            throw new PoolException("storeExpression() store exception failed", e);
        }
    }

    /**
     * Removes the expression from the JCR storage.
     */
    public void unstoreExpression(final FlowExpression fe) throws PoolException {
        try {
            synchronized (hmWrapper) {
                final Content cExpression = findExpression(fe);

                if (cExpression != null) {
                    // TODO : we could delete this node's parent's parent here. find a good/safe way to do this, for a cleaner repository.
                    cExpression.delete();
                    hmWrapper.save();
                } else {
                    log.info("unstoreExpression() " + "didn't find content node for fe " + fe.getId().toParseableString());
                }
            }
        } catch (final Exception e) {
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
        } catch (final Throwable t) {
            log.error("contentIterator() failed to set up an iterator", t);
        }

        // TODO : does this need Iterator need to be modifiable? otherwise just return Collections.emptyList()
        //return null;
        return new ArrayList(0).iterator();
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
        } catch (final Exception e) {
            log.debug("loadExpression() failed for " + fei.asStringId(), e);

            throw new PoolException("loadExpression() failed for " + fei.asStringId(), e);
        }

        log.error("loadExpression() " + "didn't find expression " + fei.asStringId() + " in the repository");

        throw new PoolException("loadExpression() " + "didn't find expression " + fei.asStringId() + " in the repository");
    }

    /**
     * Returns the number of expressions currently stored in that store.
     */
    public int size() {
        try {
            final QueryManager qm = MgnlContext.getSystemContext().getQueryManager(WorkflowConstants.WORKSPACE_EXPRESSION);
            Query q = qm.createQuery(WorkflowConstants.STORE_ITERATOR_QUERY, Query.SQL);
            QueryResult qr = q.execute();

            return qr.getContent().size();
        } catch (final Exception e) {
            log.error("size() failed", e);
            return -1;
        }
    }

    private void serializeExpressionAsXml(Content c, FlowExpression fe) throws Exception {
        final org.jdom.Document doc = XmlBeanCoder.xmlEncode(fe);
        String s = XmlUtils.toString(doc, null);
        ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
        c.createNodeData(WorkflowConstants.NODEDATA_VALUE, vf.createValue(s));
    }

    /*
    private void serializeExpressionWithBeanCoder(Content c,FlowExpression fr) throws Exception {

        OwfeJcrBeanCoder coder = new OwfeJcrBeanCoder(null,new MgnlNode(c),WorkflowConstants.NODEDATA_VALUE);
        coder.encode(fr);
    }
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
        buffer.append(fei.getExpressionId());
        buffer.append("__");
        buffer.append(fei.getExpressionName());

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

        if (hmWrapper.isExist(path)) {
            return hmWrapper.getContent(path);
        } else {
            return hmWrapper.createPath(path, ItemType.EXPRESSION);
        }
    }

    private FlowExpression deserializeExpressionAsXml(final Content c) throws Exception {
        final InputStream is = c.getNodeData(WorkflowConstants.NODEDATA_VALUE).getStream();

        if (is == null) {
            return null;
        }

        final SAXBuilder builder = new SAXBuilder();
        final Document doc = builder.build(is);
        return (FlowExpression) XmlBeanCoder.xmlDecode(doc);
    }

    /*
    protected FlowExpression deserializeExpressionWithBeanCoder(Content ret) throws Exception {
        OwfeJcrBeanCoder coder = new OwfeJcrBeanCoder(null, new MgnlNode(ret.getContent(WorkflowConstants.NODEDATA_VALUE)));
        return (FlowExpression)coder.decode();
    }
    */

    /**
     * 'lightweight' storeIterator. The previous version were stuffing all
     * the expression within a collection and
     * returning an iterator on it.
     * <p>
     * The remaining question is : what's behind
     * Magnolia's Content.iterator() method ?
     */
    protected final class StoreIterator implements Iterator {
        private final Class assignClass;
        private Iterator rootIterator = null;
        private FlowExpression next = null;

        public StoreIterator(final Class assignClass) throws Exception {
            super();

            this.assignClass = assignClass;

            final QueryManager qm = MgnlContext.getSystemContext().getQueryManager(WorkflowConstants.WORKSPACE_EXPRESSION);

            final Query query = qm.createQuery(WorkflowConstants.STORE_ITERATOR_QUERY, Query.SQL);

            final QueryResult qr = query.execute();

            if (log.isDebugEnabled()) {
                log.debug("() query found " + qr.getContent("expression").size() + " elements");
            }

            this.rootIterator = qr.getContent("expression").iterator();

            this.next = fetchNext();
        }

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

                fe.setApplicationContext(getContext());

                if (!isAssignableFromClass(fe, this.assignClass)) {
                    return fetchNext();
                }

                return fe;
            } catch (final Exception e) {
                log.error("fetchNext() problem", e);
                return null;
            }
        }

        public Object next() throws java.util.NoSuchElementException {
            final FlowExpression current = this.next;

            if (current == null) {
                throw new NoSuchElementException();
            }

            this.next = fetchNext();

            if (log.isDebugEnabled()) {
                log.debug("next() is  " + (next != null ? next.getId().toString() : "'null'"));
            }

            return current;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        // TODO : this was copied from ExpoolUtils, adding a fix for MAGNOLIA-1131
        private boolean isAssignableFromClass(final FlowExpression fe, final Class expClass) {
            if (expClass == null) {
                return true;
            }

            Class c = fe.getClass();

            if (fe instanceof RawExpression) {
                c = fe.getExpressionClass();
                if (c == null) {
                    // TODO : fe.getDefinitionName() does not return the xml's root name as I expected ... (but its name attribute instead)
                    log.warn("Skipping expression " + fe.getId() + " (" + ((RawExpression) fe).getDefinitionName() + ")");
                    return false;
                }
            }

            return expClass.isAssignableFrom(c);
        }
    }

}
