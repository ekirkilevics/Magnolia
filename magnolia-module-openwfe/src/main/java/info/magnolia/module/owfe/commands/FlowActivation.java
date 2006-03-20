package info.magnolia.module.owfe.commands;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import openwfe.org.embed.impl.engine.PersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import java.util.HashMap;

/**
 * @author jackie
 */
public class FlowActivation extends AbstractTreeCommand {

    String pathSelected = "";
    boolean recursive = false;

    public boolean execute(HashMap params) {
        log.info("enter execute");
        //request = (HttpServletRequest)params.get("request");
        recursive = ((Boolean) params.get("recursive")).booleanValue(); //$NON-NLS-1$
        pathSelected = (String) params.get("pathSelected");
        activate();
        log.info("leave execute");
        return true;
    }

    private String activate() {
        //boolean recursive = (request.getParameter("recursive") != null); //$NON-NLS-1$

        try {
            log.debug("Launch Activate Flow");
            //Get a reference to the workflow engine
            PersistedEngine engine = OWFEEngine.getEngine();

            // Create a new LaunchItem
            LaunchItem li = new LaunchItem();
            li.setWorkflowDefinitionUrl("field:__definition__");
            li.addAttribute("recursive", new StringAttribute(recursive ? "true"
                    : "false"));
            li.addAttribute("pathSelected", new StringAttribute(pathSelected));
            li.addAttribute("OK", new StringAttribute("false"));
            li.addAttribute("Action", new StringAttribute("Activate"));

            // Retrieve and add the flow definition to the LaunchItem

//			String flowDef = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
//					+ "<process-definition "
//					+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
//					+ "xsi:noNamespaceSchemaLocation=\"http://www.openwfe.org/flowdef_r1.5.0.xsd\" "
//					+ "name=\"docflow\" "
//					+ "revision=\"1.0\">"
//					+ "<description language=\"default\"> "
//					+ "This just the complete flow definition of docflow process. "
//					+ "</description>" + "<sequence>"
//					+ "<participant ref=\"user-superuser\"/>" + "</sequence>"
//					+ "</process-definition>";
//			li.getAttributes().puts("__definition__", flowDef);

            //String url_base = "http://"+ request.getServerName()+":" + request.getServerPort() + "/magnolia/FlowDef";
            //String flowURL = url_base+"?name=activation";
            //log.info("flow URL:" + flowURL);
            //li.setWorkflowDefinitionUrl(flowURL);

            String flowDef = new JCRFlowDefinition().getflowDefAsString("webActivation");
            li.getAttributes().puts("__definition__", flowDef);

            // Start the engine
            engine.launch(li, true);
//			engine.launch(flowURL, false);

            // Notify by email ? Should be in the flow itself, not here.
            //new Thread(new MailSender(pathSelected)).start();

            // Alert the user using a javascript notification
            String message = "Your request has been dispatched, please wait for approval";
            AlertUtil.setMessage(message);
        } catch (Exception e) {
            log.error("can't launch activate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
        return "tree";
    }


}
