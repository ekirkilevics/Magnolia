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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the expresion store using JCR
 * @author jackie
 */
public class JCRExpressionStore extends AbstractExpressionStore {

    private static Logger log = LoggerFactory.getLogger(JCRExpressionStore.class.getName());

    public final static String REPO_OWFE = "owfe";

    public final static String WORKSPACEID = "Expressions";

    public final static String WORKITEM_NODENAME = "expression";

    HierarchyManager hm;

    public void init(final String serviceName, final ApplicationContext context, final java.util.Map serviceParams)
        throws ServiceException {
        super.init(serviceName, context, serviceParams);
        this.hm = ContentRepository.getHierarchyManager(REPO_OWFE, WORKSPACEID);
        if (this.hm == null) {
            throw new ServiceException("Can't get HierarchyManager Object for workitems repository");
        }

    }

    /**
     * convert the id to a valid node name
     * @param id
     */
    private String convertId(String id) {
        return StringUtils.replace(StringUtils.replace(id, "|", ""), ":", ".");
    }

    /**
     * stroe one expresion
     */
    public synchronized void storeExpression(FlowExpression fe) throws PoolException {
        try {
            Content root = this.hm.getRoot();

            String id = fe.getId().toParseableString();
            log.debug("store expresion: expression id = " + id);
            String nid = convertId(id);
            Content ct = root.createContent(nid, ItemType.EXPRESSION);

            // set expressionId as attribte id
            ValueFactory vf = ct.getJCRNode().getSession().getValueFactory();
            String value = fe.getId().toParseableString();
            if (log.isDebugEnabled()) {
                log.debug("id_value=" + value);
            }
            ct.createNodeData("ID", vf.createValue(value));

            // convert to xml string
            Element encoded = XmlCoder.encode(fe);
            final org.jdom.Document doc = new org.jdom.Document(encoded);
            String s = XmlUtils.toString(doc, null);

            // store it as attribute value
            ct.createNodeData("value", vf.createValue(s));
            this.hm.save();

        }
        catch (Exception e) {
            log.error("store exception failed,", e);
        }

    }

    /**
     * remove one expresion
     */
    public synchronized void unstoreExpression(FlowExpression fe) throws PoolException {
        try {
            // get root
            Content ret = findExpression(fe.getId());
            if (ret != null) {
                ret.delete();
                this.hm.save();
            }

        }
        catch (Exception e) {
            log.error("unstore exception faled,", e);
        }

    }

    /**
     * Find expression by id
     * @param fei flow expression id
     * @return
     * @throws Exception
     */
    private Content findExpression(FlowExpressionId fei) throws Exception {
        Content ret;
        String s_fei = fei.toParseableString();

        Content root = this.hm.getRoot();
        log.debug("load expresion, expression id = " + s_fei);
        ret = root.getContent(convertId(s_fei));
        if (ret == null) { // if not found the id directly
            Collection c = root.getChildren(ItemType.EXPRESSION);
            Iterator it = c.iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();
                // String name = ct.getName();
                String sid = ct.getNodeData("ID").getString();
                FlowExpressionId id;
                // compare the expression id
                try {
                    id = FlowExpressionId.fromParseableString(sid);
                }
                catch (Exception e) {
                    log.error("parse expresion id failed", e);
                    ct.delete();
                    this.hm.save();
                    continue;
                }

                if (id.equals(fei))// find the target one, just load it
                {
                    ret = ct;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * load expreson by id
     */
    public FlowExpression loadExpression(FlowExpressionId fei) throws PoolException {
        FlowExpression ret_fe;
        String s_fei = "";
        try {
            Content ret = findExpression(fei);
            if (ret != null) {
                InputStream s = ret.getNodeData("value").getStream();
                final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();

                Document doc = builder.build(s);
                ret_fe = (FlowExpression) XmlCoder.decode(doc);
                return ret_fe;
            }

        }
        catch (PathNotFoundException e) {
            log.error("Path not found while loading expression : {}", e.getMessage());
        }
        catch (Exception e) {
            log.error("load exception faled,", e);
        }
        throw new PoolException("can not get this expression (id=" + s_fei + ")");
    }

    /**
     * return a iterator of content
     */
    public Iterator contentIterator(Class assignClass) {
        ArrayList ret = new ArrayList();
        try {
            Content root = this.hm.getRoot();
            Collection c = root.getChildren(ItemType.EXPRESSION);

            Iterator it = c.iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();

                InputStream s = ct.getNodeData("value").getStream();
                final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();

                Document doc = builder.build(s);
                FlowExpression fe = (FlowExpression) XmlCoder.decode(doc);
                if (!ExpoolUtils.isAssignableFromClass(fe, assignClass)) {

                    continue;
                }
                fe.setApplicationContext(getContext());
                ret.add(fe);
            }

            return ret.iterator();

        }
        catch (Exception e) {
            log.error("exception:" + e.getMessage(), e);
            return ret.iterator();
        }
    }

    /**
     * return size of expresion
     */
    public int size() {
        try {
            Content root = this.hm.getRoot();
            Collection c = root.getChildren(ItemType.EXPRESSION);
            // @fix it
            return c.size();
        }
        catch (Exception e) {
            log.error("exception:" + e);
            return 0;
        }

    }

}
