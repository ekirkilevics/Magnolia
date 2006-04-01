package info.magnolia.module.owfe.servlets;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.owfe.OWFEBean;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class for Servlet: FlowDef
 */
public class FlowDefServlet extends javax.servlet.http.HttpServlet implements
        javax.servlet.Servlet {
    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(FlowDefServlet.class);

    static final String DEFAULT_FLOW = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><process-definition xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://www.openwfe.org/flowdef_r1.5.0.xsd\" name=\"webActivation\" revision=\"1.0\"><description language=\"default\">worflow for web activation process.</description><sequence><participant ref=\"command-SendMail\" /><set field=\"mailTo\" value=\"user-superuser\" /><participant ref=\"user-superuser\" /><participant ref=\"command-activate\" /></sequence></process-definition>";

    /*
      * (non-Java-doc)
      *
      * @see javax.servlet.http.HttpServlet#HttpServlet()
      */
    public FlowDefServlet() {
        super();
    }

    /*
      * (non-Java-doc)
      *
      * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
      *      HttpServletResponse response)
      */
    /*
      * <?xml version="1.0" encoding="UTF-8" ?><process-definition xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.openwfe.org/flowdef_r1.5.0.xsd" name="webActivation" revision="1.0"><description language="default">worflow for web activation process.</description><sequence><participant ref="user-superuser" /><participant ref="command-activate" /></sequence></process-definition>
      *
      */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String flowName = request.getParameter("name");

        PrintWriter writer = response.getWriter();
        StringBuffer sb = new StringBuffer();

        if (flowName == null) { // no parameter for name
            sb.append("<html><body>");

            // Flow edit area
            sb.append("<form action=\"FlowDef\" method=\"post\">"
                    + "<textArea  cols=\"80\" rows=\"25\"  name=\"flow\">" + DEFAULT_FLOW + "</textArea>"
                    + "<br/>"
                    + "<input type=\"submit\" value=\"upload\"></input>"
                    + "</form>");

            sb.append("<hr/><hr/><p/>");

            // set start/end date
            sb.append("<form action=\"FlowDef\" method=\"get\">");
            sb.append("<input type=\"hidden\" name=\"method\" value=\"setDate\"/>" +
            		"path:<input name=\"path\" />" +
            		"start date:<input name=\"startDate\" value=\""+new Timestamp(System.currentTimeMillis()).toString()+"\" />" +
            				"stop date:<input name=\"stopDate\" value=\""+new Timestamp(System.currentTimeMillis()).toString()+"\" />"); 
            sb.append("<br/>");
            sb.append("<input type=\"submit\" value=\"test\"></input>");
            sb.append("</form>");

            sb.append("<hr/><hr/><p/>");
            
            // Query
            sb.append("<form action=\"FlowDef\" method=\"get\">");
            sb.append("query string:<input name=\"query\"></input>");
            sb.append("<br/>");
            sb.append("<input type=\"submit\" value=\"query\"></input>");
            sb.append("</form>");

            sb.append("<hr/><hr/><p/>");

            // Test
            sb.append("<form action=\"FlowDef\" method=\"get\">");
            sb.append("<input type=\"hidden\" name=\"test\" value=\"yes\"/>parameter:<input name=\"testp\"></input>");
            sb.append("<br/>");
            sb.append("<input type=\"submit\" value=\"test\"></input>");
            sb.append("</form>");

            sb.append("<hr/><hr/><p/>");

            // List all flows
            sb.append("<a href=\"FlowDef?name=*\">List all flows</a>");


        } else if (flowName.equalsIgnoreCase("*")) // name=* -> list all flow
        {
            List list = new JCRFlowDefinition().getFlows(request);
            for (int i = 0; i < list.size(); i++) {
                sb.append("<table>");
                sb.append(
                        "<tr><td><a href=\"" + list.get(i) + "\">");
                sb.append(list.get(i));
                sb.append("</a></td></tr>");
                sb.append("</table>");
            }
            //new JCRFlowDefinition().exportAll("d:\\flowdef");

        } else { // name = "xxx"

            // get flow by name
            Content flowdef = new JCRFlowDefinition().findFlowDef(flowName);
            if (flowdef == null) {
                log.error("can not find flow definition for " + flowName);

            } else {
                // Download the flow as xml
                response.setContentType("text/xml");
                response.getWriter().print(flowdef.getNodeData("value").getString());
                response.flushBuffer();
            }
        }
        sb.append("<hr><p>result<p>");
        // for testing
        String query = request.getParameter("query");
        if (query != null && query.length() > 0) {
            try {
            	String path = request.getParameter("path");
            	
                sb.append("<hr>" +
                        (new JCRWorkItemAPI().doQuery(query).toString())
                );
            } catch (Exception e) {
                log.error("testing query failed", e);
            }

        }
        
        // set date
        String method = request.getParameter("method");
        
        if (method != null && method.equalsIgnoreCase("setDate") ) {
            try {
            	
            	String path = request.getParameter("path");
            	String start = request.getParameter("startDate");
            	String stop = request.getParameter("stopDate");
                sb.append("<hr>" +
                        setDate(path, start, stop));
                
            } catch (Exception e) {
                log.error("testing query failed", e);
            }

        }
        
        // test
        String test = request.getParameter("test");
        if (test != null && test.equalsIgnoreCase("yes")) {
            try {
                String p = request.getParameter("testp");
                if (p == null)
                    p = "";

                new OWFEBean().doTest(p);
            }
            catch (Exception e) {
                sb.append("test failed<p>");
                e.printStackTrace(writer);
            }
        }
        writer.write(sb.toString());

    }

    /*
      * (non-Java-doc)
      *
      * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
      *      HttpServletResponse response)
      */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
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
    
    private String setDate(String pathSelected, String start, String stop) throws Exception{
        // add start date and end date
        HierarchyManager hm =  ContentRepository
		.getHierarchyManager(ContentRepository.WEBSITE);
        Content ct = null;
        try{
        	ct = hm.getContent(pathSelected);
        }catch(Exception e){
        	log.error("can not get content node for path " + pathSelected, e);
        }        
		//params.put(MgnlConstants.P_PATH, pathSelected);		
        Calendar start_c = Calendar.getInstance();
        Calendar stop_c = Calendar.getInstance();
        start_c.setTime(new Date(Timestamp.valueOf(start).getTime()));
        ct.getMetaData().setStartTime(start_c);
        stop_c.setTime(new Date(Timestamp.valueOf(stop).getTime()));		
		ct.getMetaData().setEndTime(stop_c);
		hm.save();
		return "set date ok. path " + pathSelected +", start date " + start + ", stop date " + stop;
    }
}