package info.magnolia.module.owfe;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Servlet implementation class for Servlet: FlowDefUpload
 */
public class FlowDefUpload extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /*
     * (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public FlowDefUpload() {
        super();
    }

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(FlowDefUpload.class);

    /*
     * (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /*
     * (non-Java-doc)
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
}