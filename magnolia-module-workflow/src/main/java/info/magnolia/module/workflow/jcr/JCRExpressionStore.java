/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.workflow.jcr;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.LifeTimeJCRSessionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.workflow.WorkflowConstants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

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

    private static final String ENGINE_ID = "ee";

    private boolean useLifeTimeJCRSession = true;

    private boolean cleanUp = false;

    public JCRExpressionStore(boolean useLifeTimeJCRSession, boolean cleanUp) {
        super();
        this.useLifeTimeJCRSession = useLifeTimeJCRSession;
        this.cleanUp = cleanUp;
    }

    public void init(final String serviceName, final ApplicationContext context, final Map serviceParams) throws ServiceException {
        super.init(serviceName, context, serviceParams);
    }

    /**
     * Stores one expression.
     */
    public synchronized void storeExpression(final FlowExpression fe) throws PoolException {
        boolean release = !useLifeTimeJCRSession && !MgnlContext.hasInstance();
        HierarchyManager hm = null;
        try {
            hm = getHierarchyManager();
            final Content cExpression = findOrCreateExpression(fe, hm);

            log.debug("storeExpression() handle is " + cExpression.getHandle());

            // set expressionId as attribte id
            ValueFactory vf = cExpression.getJCRNode().getSession().getValueFactory();
            String value = fe.getId().toParseableString();

            cExpression.createNodeData(WorkflowConstants.NODEDATA_ID, vf.createValue(value));

            // serializeExpressionWithBeanCoder(ct, fe);
            serializeExpressionAsXml(cExpression, fe);

            hm.save();
        } catch (Exception e) {
            log.error("storeExpression() store exception failed", e);
            try {
                if (hm.hasPendingChanges()) {
                    hm.refresh(true);
                }
            } catch (RepositoryException e1) {
                log.error("Corrupted HM during WKF access", e);
            }
            throw new PoolException("storeExpression() store exception failed", e);
        } finally {
            if (release) {
                MgnlContext.release();
            }
        }
    }

    /**
     * Removes the expression from the JCR storage.
     */
    public synchronized void unstoreExpression(final FlowExpression fe) throws PoolException {
        boolean release = !useLifeTimeJCRSession && !MgnlContext.hasInstance();
        try {
            final HierarchyManager hm = getHierarchyManager();
            final Content cExpression = findOrCreateExpression(fe, hm);

            if (cExpression != null) {
                if (cleanUp) {
                    ContentUtil.deleteAndRemoveEmptyParents(cExpression, 1);
                } else {
                    cExpression.delete();
                }
                hm.save();
            } else {
                log.info("unstoreExpression() " + "didn't find content node for fe " + fe.getId().toParseableString());
            }
        } catch (Exception e) {
            log.error("unstoreExpression() unstore exception failed", e);
            throw new PoolException("unstoreExpression() unstore exception failed", e);
        } finally {
            if (release) {
                MgnlContext.release();
            }
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

        // TODO : does this need Iterator need to be modifiable? otherwise just
        // return Collections.emptyList()
        // return null;
        return new ArrayList(0).iterator();
    }

    /**
     * Loads an expression given its id.
     */
    public synchronized FlowExpression loadExpression(final FlowExpressionId fei) throws PoolException {
        try {
            Content cExpression = findExpression(fei, getHierarchyManager());

            if (cExpression != null) {
                final FlowExpression expression = deserializeExpressionAsXml(cExpression);
                if (expression != null) {
                    expression.setApplicationContext(getContext());
                    return expression;
                }
            }
        } catch (final Exception e) {
            log.error("loadExpression() failed for " + fei.asStringId(), e);

            throw new PoolException("loadExpression() failed for " + fei.asStringId(), e);
        }

        // this is normal after clean installation or manual cleanup of the expressions workspace
        log.info("Expected expression " + fei.asStringId() + " was not found in the repository.");

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

    private Content findOrCreateExpression(final FlowExpression fe, HierarchyManager hm) throws Exception {
        Content content = findExpression(fe.getId(), hm);
        if (content == null) {
            content = ContentUtil.createPath(hm, toXPathFriendlyString(fe.getId()), ItemType.EXPRESSION);
        }
        return content;
    }

    private Content findExpression(final FlowExpressionId fei, HierarchyManager hm) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("findExpression() looking for " + fei.toParseableString());
        }

        final String path = toXPathFriendlyString(fei);

        if (hm.isExist(path)) {
            return hm.getContent(path);
        } else {
            return null;
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

    protected HierarchyManager getHierarchyManager() {
        HierarchyManager hm;
        if (useLifeTimeJCRSession) {
            hm = LifeTimeJCRSessionUtil.getHierarchyManager(WorkflowConstants.WORKSPACE_EXPRESSION);
        } else {
            hm = MgnlContext.getSystemContext().getHierarchyManager(WorkflowConstants.WORKSPACE_EXPRESSION);
        }
        try {
            if (hm.hasPendingChanges()) {
                // If this happens it might be related to MAGNOLIA-2172
                // the methods of the expression store are synchronized so this
                // should not happen!
                log.warn("The workflow expression session has pending changes while " + (useLifeTimeJCRSession ? "" : "not ") + "using Life Time session. Will clean the session",
                        new Exception());
                hm.refresh(true);
            }
        } catch (RepositoryException e) {
            // should really not happen
            log.error("Can't check/refresh worflow expression session.", e);
        }
        return hm;
    }

    /**
     * 'lightweight' storeIterator. The previous version were stuffing all the
     * expression within a collection and returning an iterator on it.
     * <p>
     * The remaining question is : what's behind Magnolia's Content.iterator()
     * method ?
     */
    protected final class StoreIterator implements Iterator {
        private final Class assignClass;
        private Iterator rootIterator = null;
        private FlowExpression next = null;

        public StoreIterator(final Class assignClass) throws Exception {
            super();

            this.assignClass = assignClass;

            final QueryManager qm = LifeTimeJCRSessionUtil.getQueryManager(WorkflowConstants.WORKSPACE_EXPRESSION);

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

        // TODO : this was copied from ExpoolUtils, adding a fix for
        // MAGNOLIA-1131
        private boolean isAssignableFromClass(final FlowExpression fe, final Class expClass) {
            if (expClass == null) {
                return true;
            }

            Class c = fe.getClass();

            if (fe instanceof RawExpression) {
                c = fe.getExpressionClass();
                if (c == null) {
                    // TODO : fe.getDefinitionName() does not return the xml's
                    // root name as I expected ... (but its name attribute
                    // instead)
                    log.warn("Skipping expression " + fe.getId() + " (" + ((RawExpression) fe).getDefinitionName() + ")");
                    return false;
                }
            }

            return expClass.isAssignableFrom(c);
        }
    }

}
