package info.magnolia.module.owfe;

import info.magnolia.cms.core.Content;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Servlet implementation class for Servlet: FlowDef
 */
public class FlowDefServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(FlowDefServlet.class);

    /*
     * (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public FlowDefServlet() {
        super();
    }

    /*
     * (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String flowName = request.getParameter("name");

        if (flowName == null) {
            response
                .getWriter()
                .println(
                    "<html><body><form action=\"FlowDefUpload\" method=\"post\">"
                        + "<textArea  cols=\"80\" rows=\"25\"  name=\"flow\">"
                        + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><process-definition xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://www.openwfe.org/flowdef_r1.5.0.xsd\" name=\"webActivation\" revision=\"1.0\"><description language=\"default\">worflow for web activation process.</description><sequence><participant ref=\"user-superuesr\" /></sequence></process-definition>"
                        + "</textArea>"
                        + "<br/><input type=\"submit\"></input>"
                        + "</form>"
                        + "<p><a href=\"FlowDef?name=*\">List all flows</a></body></html>");
            return;
        }
        if (flowName.equalsIgnoreCase("*")) // list all flow
        {
            List list = new JCRFlowDefinition().getFlows(request);
            for (int i = 0; i < list.size(); i++) {
                response.getWriter().println("<table>");
                response.getWriter().println("<tr><td><a href=\"" + list.get(i) + "\">");
                response.getWriter().println(list.get(i));
                response.getWriter().println("</a></td></tr>");
                response.getWriter().println("</table>");
            }
            // new JCRFlowDefinition().exportAll("d:\\flowdef");

        }
        else {

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
            }
            response.getWriter().println(flowdef.getNodeData("value").getString());
        }

    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {
        // TODO Auto-generated method stub
    }
}