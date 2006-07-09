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
package info.magnolia.module.workflow.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowUtil;
import info.magnolia.module.workflow.jcr.JCRFlowDefinition;
import info.magnolia.module.workflow.jcr.JCRWorkItemAPI;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet implementation class for Servlet: FlowDef This servlet is mainly for testing (add flow def, list flow xml
 * ...)
 */
public class FlowDefServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    /**
     * 
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(FlowDefServlet.class);

    static final String DEFAULT_FLOW = "<!-- the name of the definition will get used as the upload name-->";

    /**
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String flowName = request.getParameter("name");

        PrintWriter writer = response.getWriter();
        StringBuffer sb = new StringBuffer();

        if (flowName == null) { // no parameter for name
            sb.append("<html><body>");

            // Flow edit area
            sb.append("<form action=\"\" method=\"post\">"
                + "<textArea  cols=\"80\" rows=\"25\"  name=\"flow\">"
                + DEFAULT_FLOW
                + "</textArea>"
                + "<br/>"
                + "<input type=\"submit\" value=\"upload\"></input>"
                + "</form>");

            sb.append("<hr/><hr/><p/>");

            // set start/end date
            sb.append("set date for node<br>" + "<form action=\"\" method=\"get\">");
            sb.append("<input type=\"hidden\" name=\"method\" value=\"setDate\"/>");

            sb.append("path:<input name=\"path\" />");
            sb.append("start date:<input name=\"startDate\" value=\"");
            sb.append(new Timestamp(System.currentTimeMillis()).toString());
            sb.append("\" />");
            sb.append("stop date:<input name=\"stopDate\" value=\"");
            sb.append(new Timestamp(System.currentTimeMillis()).toString());
            sb.append("\" />");

            sb.append("<br/>");
            sb.append("<input type=\"submit\" value=\"submit\"></input>");
            sb.append("</form>");

            sb.append("<hr/><hr/><p/>");

            // launch flow
            sb.append("launch flow<br>" + "<form action=\"\" method=\"get\">");
            sb.append("<input type=\"hidden\" name=\"method\" value=\"launchFlow\"/>"
                + "path:<input name=\"path\" />"
                + "Flow name:<input name=\"flowName\" />"
                + "Repository name:<input name=\"repository\" />"
                + "workspace name:<input name=\"workspace\" />");
            sb.append("<br/>");
            sb.append("<input type=\"submit\" value=\"submit\"></input>");
            sb.append("</form>");

            sb.append("<hr/><hr/><p/>");

            // Query
            sb.append("set date for node<br>" + "<form action=\"\" method=\"get\">");
            sb.append("query string:<input name=\"query\"></input>");
            sb.append("<br/>");
            sb.append("<input type=\"submit\" value=\"query\"></input>");
            sb.append("</form>");

            sb.append("<hr/><hr/><p/>");

            // Test
            sb.append("test<br>" + "<form action=\"\" method=\"get\">");
            sb.append("<input type=\"hidden\" name=\"test\" value=\"yes\"/>parameter:<input name=\"testp\"></input>");
            sb.append("<br/>");
            sb.append("<input type=\"submit\" value=\"test\"></input>");
            sb.append("</form>");

            sb.append("<hr/><hr/><p/>");

            // List all flows
            sb.append("<a href=\"?name=*\">List all flows</a>");

        }
        else if (flowName.equalsIgnoreCase("*")) // name=* -> list all flow
        {
            List list = new JCRFlowDefinition().getFlows(request);
            for (int i = 0; i < list.size(); i++) {
                sb.append("<table>");
                sb.append("<tr><td><a href=\"").append(list.get(i)).append("\">");
                sb.append(list.get(i));
                sb.append("</a></td></tr>");
                sb.append("</table>");
            }
            // new JCRFlowDefinition().exportAll("d:\\flowdef");

        }
        else { // name = "xxx"

            // get flow by name
            Content flowdef = new JCRFlowDefinition().findFlowDef(flowName);
            if (flowdef == null) {
                log.error("can not find flow definition for " + flowName);

            }
            else {
                // Download the flow as xml
                response.setContentType("text/xml");
                response.getWriter().print(flowdef.getNodeData("value").getString());
                response.flushBuffer();
                return;
            }
        }
        String query = request.getParameter("query");
        String method = request.getParameter("method");

        if (query != null || method != null) {
            sb.append("<hr><p>result<p>");
        }

        if (query != null && query.length() > 0) {
            try {
                sb.append("<hr>").append(new JCRWorkItemAPI().doQuery(query).toString());
            }
            catch (Exception e) {
                log.error("testing query failed", e);
            }

        }

        // set date

        if (method != null) {
            if (method.equalsIgnoreCase("setDate")) {
                try {

                    String path = request.getParameter("path");
                    String start = request.getParameter("startDate");
                    String stop = request.getParameter("stopDate");
                    sb.append("<hr>").append(setDate(path, start, stop));

                }
                catch (Exception e) {
                    log.error("testing query failed", e);
                }

            }
            else if (method.equalsIgnoreCase("launchFlow")) {
                String repo = request.getParameter("repository");
                String launchFlowName = request.getParameter("flowName");
                String path = request.getParameter("path");

                try {
                    WorkflowUtil.launchFlow(repo, path, launchFlowName);
                }
                catch (Exception e) {
                    log.error("launch flow failed", e);
                }
            }

        }
        writer.write(sb.toString());
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        String flowDef = request.getParameter("flow");

        if (flowDef == null) {
            return;
        }
        flowDef = flowDef.trim();
        try {
            new JCRFlowDefinition().addFlow(flowDef);
        }
        catch (Exception e) {
            log.error("add flow failed", e);
            response.getWriter().println("add flow failed: exception" + e);
            return;
        }
        response.getWriter().println("add flow OK.");
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