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
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.owfe.servlets.FlowDefServlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;


/**
 * a class to wrapper the manipulation of flow definition in JCR repository
 * @author jackie
 */
public class JCRFlowDefinition {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(FlowDefServlet.class);

    private final static String ROOT_PATH_FOR_FLOW = "/modules/workflow/config/flows/";

    private final static String FLOW_VALUE = "value";

    /**
     * find one flow node by flow name
     * @param name
     * @return Content node in JCR store for specified flow definition
     */
    public Content findFlowDef(String name) {
        if (name == null)
            return null;
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);

        try {
            Content root = hm.getContent(ROOT_PATH_FOR_FLOW);
            Collection c = root.getChildren(ItemType.CONTENT);
            Iterator it = c.iterator();
            while (it.hasNext()) {
                Content ct = (Content) it.next();
                // String title = ct.getTitle();
                // log.info("title="+title);
                String sname = ct.getName();
                // log.info("name="+sname);
                if (sname.equals(name)) {
                    return ct;
                }
            }
        }
        catch (Exception e) {
            log.error("Error while finding flow definition:" + e, e);
        }
        return null;
    }

    /**
     * get flow definition as string of xml
     * @param flowName
     * @return the string defining the flow in xml format
     */
    public String getflowDefAsString(String flowName) {
        Content node = findFlowDef(flowName);
        if (node == null)
            return null;
        return node.getNodeData("value").getString();
    }

    /**
     * get all flows' url
     * @param request
     * @return a list of string representing the URL of each workflow
     */
    public List getFlows(HttpServletRequest request) {

        String url_base = "http://";
        if (request.isSecure())
            url_base = "https://";
        url_base += request.getServerName() + ":" + request.getServerPort() + request.getRequestURI();
        ArrayList list = new ArrayList();
        log.info(url_base);
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content root = hm.getContent(ROOT_PATH_FOR_FLOW);
            Collection c = root.getChildren(ItemType.CONTENT);
            Iterator it = c.iterator();
            while (it.hasNext()) {
                String name = ((Content) (it.next())).getName();
                if (name != null)
                    list.add(url_base + "?name=" + name);
            }
        }
        catch (Exception e) {
            log.error("error", e);
        }
        return list;
    }

    /**
     * export all flows to xml
     * @param xmlFileName
     */
    public void exportAll(String xmlFileName) {
        if (xmlFileName == null || xmlFileName.length() == 0)
            return;
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            File outputFile = new File(xmlFileName);
            FileOutputStream out = new FileOutputStream(outputFile);
            hm.getWorkspace().getSession().exportSystemView("/", out, false, false);

            log.info("exorpt flow def ok");
        }
        catch (Exception e) {
            log.error("exorpt flow failed", e);

        }
    }

    /**
     * add one flow definition to JCR store
     * @param flowDef
     * @return
     * @throws Exception
     */
    public List addFlow(String flowDef) throws Exception {
        if (flowDef == null)
            return null;
        String name;
        // jdom
        final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
        Document doc = builder.build(new StringReader(flowDef));
        Element process_definition = doc.getRootElement();
        name = process_definition.getAttribute("name").getValue();

        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content root = hm.getContent(ROOT_PATH_FOR_FLOW);

            /*
             * check if the node already exist, and if it does update the value of the the NodeData FLOW_VALUE with the
             * new flow. This is to allow duplication of flow node.
             */
            boolean exist = hm.isExist(root.getHandle() + "/" + name);
            Content c;
            if (exist)
                c = hm.getContent(root.getHandle() + "/" + name);
            else
                c = root.createContent(name, ItemType.CONTENT);

            ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
            Value value = vf.createValue(flowDef);
            if (!exist)
                c.createNodeData(FLOW_VALUE, value);
            else
                ((NodeData) c.getNodeDataCollection(FLOW_VALUE).iterator().next()).setValue(value);

            hm.save();
            log.info("add ok");
        }
        catch (Exception e) {
            log.error("add flow failed", e);
        }
        return null;
    }

}
