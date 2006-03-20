package info.magnolia.module.owfe.flow;

import info.magnolia.cms.core.Content;
import info.magnolia.module.owfe.OWFEBean;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet implementation class for Servlet: FlowDef
 */
public class FlowDefServlet extends javax.servlet.http.HttpServlet implements
        javax.servlet.Servlet {
    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(FlowDefServlet.class);

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

        if (flowName == null) { // no parameter for name
            response
                    .getWriter()
                    .println(
                            "<html><body><form action=\"FlowDefUpload\" method=\"post\">"
                                    + "<textArea  cols=\"80\" rows=\"25\"  name=\"flow\">"
                                    + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><process-definition xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://www.openwfe.org/flowdef_r1.5.0.xsd\" name=\"webActivation\" revision=\"1.0\"><description language=\"default\">worflow for web activation process.</description><sequence><participant ref=\"user-superuser\" /></sequence></process-definition>"
                                    + "</textArea>"
                                    + "<br/><input type=\"submit\" value=\"upload\"></input>"
                                    + "</form>");


            response
                    .getWriter()
                    .println("<hr/><hr/><p/>" +
                            "<form action=\"FlowDef\" method=\"get\">query string:<input name=\"query\"></input>"
                            + "<br/><input type=\"submit\" value=\"query\"></input>"
                            + "<p/>");

            response.getWriter().println("<hr/><hr/><p/>"
                    + "<form action=\"FlowDef\" method=\"get\">" +
                    "<input type=\"hidden\" name=\"test\" value=\"yes\"/>"

                    + "parameter:<input name=\"testp\"></input>"
                    + "<br/><input type=\"submit\" value=\"test\"></input>"
                    + "</form>"
                    //+ "<p><a href=\"FlowDef?name=*\">List all flows</a></body></html>"
            );

        } else if (flowName.equalsIgnoreCase("*")) // name=* -> list all flow
        {
            List list = new JCRFlowDefinition().getFlows(request);
            for (int i = 0; i < list.size(); i++) {
                response.getWriter().println("<table>");
                response.getWriter().println(
                        "<tr><td><a href=\"" + list.get(i) + "\">");
                response.getWriter().println(list.get(i));
                response.getWriter().println("</a></td></tr>");
                response.getWriter().println("</table>");
            }
            //new JCRFlowDefinition().exportAll("d:\\flowdef");

        } else { // name = "xxx"

            // get flow by name
            Content flowdef = new JCRFlowDefinition().findFlowDef(flowName);
            if (flowdef == null) {
                log.error("can not find flow definition for " + flowName);
                log.error("add one for testing");
                // try {
                // new JCRFlowDefinition().addFlow(flowName);
                // } catch (Exception e) {
                // log.error("add flow failed", e);
                // }
                return;
            } else {
                response.getWriter().println(
                        flowdef.getNodeData("value").getString());
            }
        }

        // for testing
        String query = request.getParameter("query");
        if (query != null && query.length() > 0) {
            try {
                response.getWriter().println("<hr>" +
                        (new JCRWorkItemAPI().doQuery(query).toString())
                );
            } catch (Exception e) {
                log.error("testing query failed", e);
            }

        }

        String test = request.getParameter("test");
        if (test != null && test.equalsIgnoreCase("yes")) {
            try {
                String p = request.getParameter("testp");
                if (p == null)
                    p = "";

                new OWFEBean().doTest(p);
            }
            catch (Exception e) {
                response.getWriter().println("test failed<p>");
                e.printStackTrace(response.getWriter());
            }
        }

    }

    /*
      * (non-Java-doc)
      *
      * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
      *      HttpServletResponse response)
      */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
    }
}