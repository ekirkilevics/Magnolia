/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.workflow.flows;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContextImpl;
import info.magnolia.module.workflow.WorkflowConstants;

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
import javax.servlet.http.HttpServletRequest;

import openwfe.org.engine.workitem.LaunchItem;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read and store flows in the repositories.
 * @author philipp
 */
public class DefaultFlowDefinitionManager implements FlowDefinitionManager {

    private static Logger log = LoggerFactory.getLogger(DefaultFlowDefinitionManager.class);

    private boolean saveWorkflowDefinitionInWorkItem = true;

    private String flowDefinitionURLPattern;

    @Override
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
                try {if(is!=null) {
                    is.close();
                }} catch(Exception e) {/*just try to close any open stream*/}
            }

        }
    }

    @Override
    public String readDefinition(String workflowName) throws FlowDefinitionException {
        Content node;
        try {
            node = getDefinitionNode(workflowName);
        }
        catch (RepositoryException e) {
            throw new FlowDefinitionException("can't read workflow definition [" + workflowName + "]", e);
        }
        if (node == null){
            throw new FlowDefinitionException("can't read workflow definition [" + workflowName + "]") ;
        }
        return node.getNodeData("value").getString();
    }

    /**
     * find one flow node by flow name.
     * @return Content node in JCR store for specified flow definition
     */
    public Content getDefinitionNode(String name) throws RepositoryException {
        if (name == null) {
            return null;
        }
        return ContentUtil.getContent(ContentRepository.CONFIG, WorkflowConstants.ROOT_PATH_FOR_FLOW + "/"+ name);
    }

    @Override
    public void saveDefinition(String definition) throws FlowDefinitionException {
        saveDefinition(extractWorkflowName(definition), definition);
    }

    protected void saveDefinition(String workflowName, String definition) throws FlowDefinitionException {
        if (definition == null) {
            return;
        }

        try {
            HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);
            Content root = hm.getContent(WorkflowConstants.ROOT_PATH_FOR_FLOW);

            // check if the node already exist, and if it does update the value of the the NodeData FLOW_VALUE with the
            // new flow. This is to allow duplication of flow node.

            final Content wfNode = ContentUtil.getOrCreateContent(root, workflowName, ItemType.CONTENTNODE);
            NodeDataUtil.getOrCreateAndSet(wfNode, WorkflowConstants.FLOW_VALUE, definition);

            root.save();
            log.info("New flow added: " + workflowName);
        }
        catch (Exception e) {
            throw new FlowDefinitionException("can't add flow", e);
        }
    }

    protected String extractWorkflowName(String definition) throws FlowDefinitionException {
        try {
            // jdom
            final org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder();
            Document doc = builder.build(new StringReader(definition));
            Element process_definition = doc.getRootElement();
            return process_definition.getAttribute("name").getValue();
        }
        catch(Exception e){
            throw new FlowDefinitionException("can't extract name out of the definition", e);
        }
    }

    @Override
    public List getDefinitionNames(){
        ArrayList list = new ArrayList();

        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.CONFIG);
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
