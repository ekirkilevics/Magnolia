package info.magnolia.module.owfe.commands;

import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * @author jackie
 * @author nicolas
 */
public class FlowActivation extends IFlowCommand {

    public void onExecute(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem li) {
        try {

            // Retrieve parameters
            String pathSelected = (String) params.get(ITreeCommand.P_PATH);
            boolean recursive = ((Boolean) params.get(ITreeCommand.P_RECURSIVE)).booleanValue();

            // Parameters for the flow item
            li.setWorkflowDefinitionUrl("field:__definition__");
            li.addAttribute(ITreeCommand.P_RECURSIVE, new StringAttribute(recursive ? "true" : "false"));
            li.addAttribute(ITreeCommand.P_RECURSIVE, new StringAttribute(pathSelected));
            li.addAttribute("OK", new StringAttribute("false"));

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString("webActivation");
            li.getAttributes().puts("__definition__", flowDef);

        } catch (Exception e) {
            log.error("can't launch activate flow", e);
            AlertUtil.setMessage(AlertUtil.getExceptionMessage(e));
        }
    }

    /*
    String flowDef = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<process-definition "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xsi:noNamespaceSchemaLocation=\"http://www.openwfe.org/flowdef_r1.5.0.xsd\" "
            + "name=\"docflow\" "
            + "revision=\"1.0\">"
            + "<description language=\"default\"> "
            + "This just the complete flow definition of docflow process. "
            + "</description>" + "<sequence>"
            + "<participant ref=\"user-superuser\"/>" + "</sequence>"
            + "</process-definition>";
    li.getAttributes().puts("__definition__", flowDef);

    String url_base = "http://"+ request.getServerName()+":" + request.getServerPort() + "/magnolia/FlowDef";
    String flowURL = url_base+"?name=activation";
    log.info("flow URL:" + flowURL);
    li.setWorkflowDefinitionUrl(flowURL);

    engine.launch(flowURL, false);

     Notify by email ? Should be in the flow itself, not here.
    new Thread(new MailSender(pathSelected)).start();

     Alert the user using a javascript notification
    String message = "Your request has been dispatched, please wait for approval";
    AlertUtil.setMessage(message);
    */


}
