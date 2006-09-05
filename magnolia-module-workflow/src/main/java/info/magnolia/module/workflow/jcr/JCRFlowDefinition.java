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
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.module.workflow.WorkflowConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import openwfe.org.engine.launch.LaunchException;


/**
 * a class to wrapper the manipulation of flow definition in JCR repository
 * @author jackie
 */
public class JCRFlowDefinition {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(JCRFlowDefinition.class);

    /**
     * find one flow node by flow name
     * @param name
     * @return Content node in JCR store for specified flow definition
     * @throws RepositoryException
     * @throws PathNotFoundException
     * @throws AccessDeniedException
     */
    public Content findFlowDef(String name) throws RepositoryException {
        if (name == null) {
            return null;
        }
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);

        Content root = hm.getContent(WorkflowConstants.ROOT_PATH_FOR_FLOW);
        Collection c = root.getChildren(ItemType.CONTENT);
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Content ct = (Content) it.next();

            String sname = ct.getName();
            if (sname.equals(name)) {
                return ct;
            }
        }

        return null;
    }

    /**
     * get flow definition as string of xml
     * @param flowName
     * @return the string defining the flow in xml format
     * @throws RepositoryException if error why accessing the repository
     * @throws LaunchException if the node for that flowName does not exist
     */
    public String getflowDefAsString(String flowName) throws LaunchException,RepositoryException {
        Content node = findFlowDef(flowName);
        if (node == null)
            throw new LaunchException("Could not access the node definition for flowName:"+flowName);
        return node.getNodeData("value").getString();
    }

    /**
     * get all flows' names
     * @param request
     * @return a list of string representing the name of each workflow
     * @throws RepositoryException
     */
    public List getFlows(HttpServletRequest request) throws RepositoryException {

        ArrayList list = new ArrayList();

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        Content root = hm.getContent(WorkflowConstants.ROOT_PATH_FOR_FLOW);
        Collection c = root.getChildren(ItemType.CONTENT);
        Iterator it = c.iterator();
        while (it.hasNext()) {
            String name = ((Content) (it.next())).getName();
            if (name != null) {
                list.add(name);
            }
        }

        return list;
    }

    /**
     * export all flows to xml
     * @param xmlFileName
     * @throws RepositoryException
     * @throws PathNotFoundException
     */
    public void exportAll(String xmlFileName) throws IOException, RepositoryException {
        if (xmlFileName == null || xmlFileName.length() == 0) {
            return;
        }

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        File outputFile = new File(xmlFileName);
        FileOutputStream out = new FileOutputStream(outputFile);
        hm.getWorkspace().getSession().exportSystemView("/", out, false, false);

    }

    /**
     * add one flow definition to JCR store
     * @param flowDef
     * @return
     * @throws IOException
     * @throws JDOMException
     * @throws RepositoryException
     */
    public List addFlow(String flowDef) throws JDOMException, IOException, RepositoryException {
        if (flowDef == null) {
            return null;
        }
        String name;
        // jdom
        final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
        Document doc = builder.build(new StringReader(flowDef));
        Element process_definition = doc.getRootElement();
        name = process_definition.getAttribute("name").getValue();

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        Content root = hm.getContent(WorkflowConstants.ROOT_PATH_FOR_FLOW);

        // check if the node already exist, and if it does update the value of the the NodeData FLOW_VALUE with the
        // new flow. This is to allow duplication of flow node.

        final boolean exist = hm.isExist(root.getHandle() + "/" + name);
        Content c;
        if (exist) {
            c = hm.getContent(root.getHandle() + "/" + name);
        }
        else {
            c = root.createContent(name, ItemType.CONTENT);
        }

        ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
        Value value = vf.createValue(flowDef);
        if (!exist) {
            c.createNodeData(WorkflowConstants.FLOW_VALUE, value);
        }
        else {
            ((NodeData) c.getNodeDataCollection(WorkflowConstants.FLOW_VALUE).iterator().next()).setValue(value);
        }

        hm.save();
        log.info("new flow added");

        return null;
    }

}
