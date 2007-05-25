/**
 * This code is licensed under the Magnolia Visible Source License (MVSL).
 * Please make sure you understand the terms of the license, as you are
 * legally bound to it when you make use of this code.
 *
 * The MVSL is part of the Magnolia Visible Source Software distribution.
 * To obtain an additional copy of the license text, please contact
 * Magnolia International - see www.magnolia.info for current contact details
 *
 * Copyright 2005, 2006 Magnolia International Ltd. All rights reserved.
 */
package info.magnolia.module.workflow.flows;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.api.HierarchyManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.servlet.http.HttpServletRequest;

import openwfe.org.engine.workitem.LaunchItem;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read and store flows in the repositories
 * @author philipp
 *
 */
public class DefaultFlowDefinitionManager implements FlowDefinitionManager {

    private static Logger log = LoggerFactory.getLogger(DefaultFlowDefinitionManager.class);

    private boolean saveWorkflowDefinitionInWorkItem = true;

    private String flowDefinitionURLPattern;

    public void configure(LaunchItem launchItem, String workflowName) throws FlowDefinitionException {
        if(saveWorkflowDefinitionInWorkItem){
            launchItem.setWorkflowDefinitionUrl(WorkflowConstants.ATTRIBUTE_WORKFLOW_DEFINITION_URL);
            String flowDef = readDefinition(workflowName);
            launchItem.getAttributes().puts(WorkflowConstants.ATTRIBUTE_DEFINITION, flowDef);
        }
        else{
            InputStream is = null;
            String surl = getFlowDefinitionURLPattern();
            surl = MessageFormat.format(surl, new String[]{workflowName});
            try {
                URL url = new URL(surl);
                URLConnection connection = url.openConnection();
                connection.connect();
                is = connection.getInputStream();
                launchItem.setWorkflowDefinitionUrl(surl);
            }
            catch (MalformedURLException e) {
                throw new FlowDefinitionException("can't use workflow name [" + workflowName + "] because the url[" + surl + "] is not an url", e);
            }
            catch (IOException e) {
                throw new FlowDefinitionException("can't use workflow name [" + workflowName + "] because the url[" + surl + "] is not accessible", e);
            }
            finally {
                try {if(is!=null) is.close();} catch(Exception e) {/*just try to close any open stream*/}
            }

        }
    }

    public String readDefinition(String workflowName) throws FlowDefinitionException {
        Content node;
        try {
            node = getDefinitionNode(workflowName);
        }
        catch (RepositoryException e) {
            throw new FlowDefinitionException("could not access the node definition for name:" + workflowName, e);
        }
        if (node == null){
            throw new FlowDefinitionException("could not access the node definition for name:" + workflowName);
        }
        return node.getNodeData("value").getString();
    }

    /**
     * find one flow node by flow name
     * @return Content node in JCR store for specified flow definition
     */
    public Content getDefinitionNode(String name) throws RepositoryException {
        if (name == null) {
            return null;
        }
        return ContentUtil.getContent(ContentRepository.CONFIG, WorkflowConstants.ROOT_PATH_FOR_FLOW + "/"+ name);
    }

    public void saveDefinition(String definition) throws FlowDefinitionException {
        saveDefinition(extractWorkflowName(definition), definition);
    }

    protected void saveDefinition(String workflowName, String definition) throws FlowDefinitionException {
        if (definition == null) {
            return;
        }

        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content root = hm.getContent(WorkflowConstants.ROOT_PATH_FOR_FLOW);

            // check if the node already exist, and if it does update the value of the the NodeData FLOW_VALUE with the
            // new flow. This is to allow duplication of flow node.

            final boolean exist = hm.isExist(root.getHandle() + "/" + workflowName);
            Content c;
            if (exist) {
                c = hm.getContent(root.getHandle() + "/" + workflowName);
            }
            else {
                c = root.createContent(workflowName, ItemType.CONTENTNODE);
            }

            ValueFactory vf = c.getJCRNode().getSession().getValueFactory();
            Value value = vf.createValue(definition);
            if (!exist) {
                c.createNodeData(WorkflowConstants.FLOW_VALUE, value);
            }
            else {
                ((NodeData) c.getNodeDataCollection(WorkflowConstants.FLOW_VALUE).iterator().next()).setValue(value);
            }

            hm.save();
            log.info("new flow added");
        }
        catch (Exception e) {
            throw new FlowDefinitionException("can't add flow", e);
        }
    }

    protected String extractWorkflowName(String definition) throws FlowDefinitionException {
        try {
            String name;
            // jdom
            final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
            Document doc = builder.build(new StringReader(definition));
            Element process_definition = doc.getRootElement();
            name = process_definition.getAttribute("name").getValue();
            return name;
        }
        catch(Exception e){
            throw new FlowDefinitionException("can't extract name out of the definition", e);
        }
    }

    public List getDefinitionNames(){
        ArrayList list = new ArrayList();

        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        Content root;
        try {
            root = hm.getContent(WorkflowConstants.ROOT_PATH_FOR_FLOW);
        }
        catch (RepositoryException e) {
            log.error("can't read flow definitions", e);
            return Collections.EMPTY_LIST;
        }
        // get the leaves
        Collection c =  ContentUtil.getAllChildren(root);

        Iterator it = c.iterator();
        while (it.hasNext()) {
            String name = ((Content) (it.next())).getName();
            if (name != null) {
                list.add(name);
            }
        }

        return list;
    }

    public String getFlowDefinitionURLPattern() {
        if (StringUtils.isEmpty(flowDefinitionURLPattern) || flowDefinitionURLPattern.equals("auto")) {
            WebContextImpl impl = (WebContextImpl) MgnlContext.getInstance();
            HttpServletRequest request = impl.getRequest();

            StringBuffer baseurl = new StringBuffer();
            baseurl.append("http");
            if(request.isSecure()){
                baseurl.append("s");
            }
            baseurl.append("://");
            baseurl.append(request.getServerName());
            baseurl.append(":");
            baseurl.append(request.getServerPort());
            baseurl.append(impl.getContextPath());
            baseurl.append("/.magnolia/pages/flows.html");
            baseurl.append("?command=showFlow&flowName=");
            baseurl.append("{0}");
            flowDefinitionURLPattern = baseurl.toString();
        }

        return this.flowDefinitionURLPattern;
    }


    public void setFlowDefinitionURLPattern(String flowDefinitionURLPattern) {
        this.flowDefinitionURLPattern = flowDefinitionURLPattern;
    }

}
