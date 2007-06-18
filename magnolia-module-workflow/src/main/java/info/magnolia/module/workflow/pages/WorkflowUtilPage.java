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
package info.magnolia.module.workflow.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowModule;
import info.magnolia.module.workflow.WorkflowUtil;
import info.magnolia.module.workflow.flows.FlowDefinitionException;
import info.magnolia.module.workflow.flows.FlowDefinitionManager;
import info.magnolia.cms.core.HierarchyManager;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet implementation class for Servlet: FlowDef This servlet is mainly for testing (add flow def, list flow xml
 * ...)
 */
public class WorkflowUtilPage extends TemplatedMVCHandler {

    /**
     *
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(WorkflowUtilPage.class);

    protected FlowDefinitionManager fdm = WorkflowModule.getFlowDefinitionManager();

    /**
     * @param name
     * @param request
     * @param response
     */
    public WorkflowUtilPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public String getCurrentTimeStamp() {
        return new Timestamp(System.currentTimeMillis()).toString();
    }

    private String flowName;

    private String query;

    private String path;

    private String startDate;

    private String stopDate;

    private String result;

    private String repository;

    private String flow;

    /**
     * Getter for <code>flow</code>.
     * @return Returns the flow.
     */
    public String getFlow() {
        return this.flow;
    }

    /**
     * Setter for <code>flow</code>.
     * @param flow The flow to set.
     */
    public void setFlow(String flow) {
        this.flow = flow;
    }

    /**
     * Getter for <code>flowName</code>.
     * @return Returns the flowName.
     */
    public String getFlowName() {
        return this.flowName;
    }

    /**
     * Setter for <code>flowName</code>.
     * @param flowName The flowName to set.
     */
    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    /**
     * Getter for <code>path</code>.
     * @return Returns the path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Setter for <code>path</code>.
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Getter for <code>query</code>.
     * @return Returns the query.
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * Setter for <code>query</code>.
     * @param query The query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Getter for <code>repository</code>.
     * @return Returns the repository.
     */
    public String getRepository() {
        return this.repository;
    }

    /**
     * Setter for <code>repository</code>.
     * @param repository The repository to set.
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Getter for <code>startDate</code>.
     * @return Returns the startDate.
     */
    public String getStartDate() {
        return this.startDate;
    }

    /**
     * Setter for <code>startDate</code>.
     * @param startDate The startDate to set.
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Getter for <code>stopDate</code>.
     * @return Returns the stopDate.
     */
    public String getStopDate() {
        return this.stopDate;
    }

    /**
     * Setter for <code>stopDate</code>.
     * @param stopDate The stopDate to set.
     */
    public void setStopDate(String stopDate) {
        this.stopDate = stopDate;
    }

    /**
     * Getter for <code>result</code>.
     * @return Returns the result.
     */
    public String getResult() {
        return this.result;
    }

    public List getFlowList() throws FlowDefinitionException {
        return fdm.getDefinitionNames();
    }

    public String showFlow() throws FlowDefinitionException {
        String flow;
        flow = fdm.readDefinition(flowName);

        // get flow by name
        if (StringUtils.isEmpty(flow)) {
            log.error("can not find flow definition for {}", flowName);
            result = "can not find flow definition for " + flowName;
        }
        else {
            // Download the flow as xml
            response.setContentType("text/xml");
            try {
                response.getWriter().print(flow);
                response.flushBuffer();
            }
            catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public String doQuery() throws Exception {
        if (query != null && query.length() > 0) {
            result = WorkflowUtil.getWorkItemStore().doQuery(query).toString();
        }
        return VIEW_SHOW;
    }

    public String updateDate() throws Exception {
        result = setDate(path, startDate, stopDate);
        return VIEW_SHOW;
    }

    public String launchFlow() throws Exception {

        WorkflowUtil.launchFlow(repository, path, flowName);

        return VIEW_SHOW;
    }

    public String upload() throws Exception {

        if (flow == null) {
            return null;
        }
        flow = flow.trim();

        fdm.saveDefinition(flow);

        result = "add flow OK.";
        return VIEW_SHOW;
    }

    private String setDate(String pathSelected, String start, String stop) throws Exception {
        // add start date and end date
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.WEBSITE);
        Content ct;
        try {
            ct = hm.getContent(pathSelected);
            Calendar start_c = Calendar.getInstance();
            Calendar stop_c = Calendar.getInstance();
            start_c.setTime(new Date(Timestamp.valueOf(start).getTime()));
            ct.getNodeData(WorkflowConstants.ATTRIBUTE_START_DATE).setValue(start_c);
            stop_c.setTime(new Date(Timestamp.valueOf(stop).getTime()));
            ct.getNodeData(WorkflowConstants.ATTRIBUTE_END_DATE).setValue(stop_c);
            hm.save();
            return "set date ok. path " + pathSelected + ", start date " + start + ", stop date " + stop;
        }
        catch (Exception e) {
            log.error("can not get content node for path " + pathSelected, e);
        }
        return "set date failed";
    }

}