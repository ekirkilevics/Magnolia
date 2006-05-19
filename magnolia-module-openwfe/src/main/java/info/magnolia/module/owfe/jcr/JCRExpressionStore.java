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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.owfe.MgnlConstants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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
 * 
 * @author jackie
 */
public class JCRExpressionStore extends AbstractExpressionStore {

    private static final String ENGINE_ID = "ee";

    private static Logger log = LoggerFactory.getLogger(JCRExpressionStore.class.getName());

    HierarchyManager hm;

    public void init(final String serviceName, final ApplicationContext context, final java.util.Map serviceParams)
            throws ServiceException {
        super.init(serviceName, context, serviceParams);
        this.hm = ContentRepository.getHierarchyManager(MgnlConstants.WORKSPACE_EXPRESSION);
        if (this.hm == null) {
            throw new ServiceException("Can't access HierarchyManager for workitems");
        }
    }

    /**
     * stroe one expresion
     */
    public synchronized void storeExpression(FlowExpression fe) throws PoolException {
        try {
            synchronized (hm) {
                Content ct = findExpression(fe);
                if (log.isDebugEnabled())
                    log.debug("Handle for store expression" + ct.getHandle());

                // set expressionId as attribte id
                ValueFactory vf = ct.getJCRNode().getSession().getValueFactory();
                String value = fe.getId().toParseableString();
                ct.createNodeData(MgnlConstants.NODEDATA_ID, vf.createValue(value));

                if (log.isDebugEnabled())
                    log.debug("id_value=" + value);
                serializeExpressionAsXml(ct, fe);
                hm.save();
            }
        } catch (Exception e) {
            log.error("store exception failed,", e);
        }
    }

    private void serializeExpressionAsXml(Content c, FlowExpression fe) throws Exception {
        Element encoded = XmlCoder.encode(fe);
        final org.jdom.Document doc = new org.jdom.Document(encoded);
        String s = XmlUtils.toString(doc, null);
        ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
        c.createNodeData(MgnlConstants.NODEDATA_VALUE, vf.createValue(s));
    }

    /**
     * remove one expresion
     */
    public synchronized void unstoreExpression(FlowExpression fe) throws PoolException {
        try {
            Content ret = findExpression(fe);
            if (ret != null) {
                synchronized (hm) {
                    ret.delete();
                    hm.save();
                }
            }
        } catch (Exception e) {
            log.error("unstore exception failed,", e);
        }

    }

    public final String toXPathFriendlyString(final FlowExpressionId fei) {
        final StringBuffer buffer = new StringBuffer();
        final String engineId = fei.getEngineId();

        buffer.append(MgnlConstants.SLASH);
        buffer.append(engineId);
        // engine storage
        if (engineId.equals(ENGINE_ID))
            return buffer.toString();

        buffer.append(MgnlConstants.SLASH);
        buffer.append(fei.getWorkflowDefinitionName());
        buffer.append(MgnlConstants.SLASH);
        buffer.append(fei.getWorkflowInstanceId());
        buffer.append(MgnlConstants.SLASH);
        buffer.append(fei.getExpressionId());
        return buffer.toString();
    }

    private Content findExpression(FlowExpression fe) throws Exception {
        return findExpression(fe.getId());
    }

    private Content findExpression(FlowExpressionId fei) throws Exception {
        String local = toXPathFriendlyString(fei);
        if (log.isDebugEnabled())
            log.debug("accessing expresion: expression id = " + fei.toParseableString());
        if (hm.isExist(local))
            return hm.getContent(local);
        else
            return ContentUtil.createPath(hm, local, ItemType.EXPRESSION);
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
        } catch (Exception e) {
            // ignore. The expression cannot be found. This is reported to the
            // calling method
        }

        // no expression found. Throw an exception ?
        throw new PoolException("can not get this expression (id=" + fei.asStringId() + ")");
    }

    private FlowExpression deserializeExpressionAsXml(Content ret) throws Exception {
        InputStream s = ret.getNodeData(MgnlConstants.NODEDATA_VALUE).getStream();
        final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
        Document doc = builder.build(s);
        final FlowExpression decode = (FlowExpression) XmlCoder.decode(doc);
        return decode;
    }

    /**
     * return a iterator of content
     */
    public synchronized Iterator contentIterator(Class assignClass) {
        ArrayList ret = new ArrayList();
        try {
            Content root = this.hm.getRoot();
            Collection c = root.getChildren(ItemType.EXPRESSION);

            Iterator it = c.iterator();
            while (it.hasNext()) {
                try {
                    Content ct = (Content) it.next();

                    InputStream s = ct.getNodeData(MgnlConstants.NODEDATA_VALUE).getStream();
                    final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();

                    Document doc = builder.build(s);
                    FlowExpression fe = (FlowExpression) XmlCoder.decode(doc);

                    fe.setApplicationContext(getContext());

                    if (!ExpoolUtils.isAssignableFromClass(fe, assignClass))
                        continue;

                    ret.add(fe);

                } catch (RuntimeException e) {
                    e.printStackTrace();
                    // ignore and skip to next item
                }
            }

            return ret.iterator();

        } catch (Exception e) {
            log.error("Read access to expression store failed:" + e.getMessage(), e);
            return ret.iterator();
        }
    }

    /**
     * //TODO what's this suppose to do ? return size of expresion
     */
    public int size() {
        try {
            Content root = this.hm.getRoot();
            Collection c = root.getChildren(ItemType.EXPRESSION);
            // @fix it
            return c.size();
        } catch (Exception e) {
            log.error("exception:" + e);
            return 0;
        }

    }

}
