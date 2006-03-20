package info.magnolia.module.owfe.flow;

import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.commands.Command;
import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import openwfe.org.embed.impl.engine.PersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * This is a subclass of the regular MVCHandler to plug in flow events.
 * <p/>
 * In this case, only the activate method is part of a flow. We should find a way to plug in flow on the different methods.
 *
 * @author jackie
 * @author Niko
 */

public abstract class FlowAdminTreeMVCHandler extends AdminTreeMVCHandler {

    private static Logger log = Logger.getLogger(info.magnolia.module.owfe.flow.FlowAdminTreeMVCHandler.class);

    public FlowAdminTreeMVCHandler(String name, HttpServletRequest request,
                                   HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * execute dynamic command
     */
    public String execute(String command) {
        // get command from command map in JCR repository
        Command tc = new CommandsMap().getTreeCommand(command);
        if (tc == null) { // not found, do in the old ways
            //throw new Exception("can not find command named " + command);
            log.warn("can not find command named " + command + "in tree command map");
            return super.execute(command);
        }
        log.info("find command for " + command + ": " + tc);

        // set parameters
        HashMap params = new HashMap();
        params.put(Command.P_REQUEST, request);
        params.put("pathSelected", pathSelected);
        params.put("recursive", Boolean.valueOf((request.getParameter("recursive") != null)));

        // execute
        tc.execute(params);
        return VIEW_TREE;
    }

    public String activate() {
        boolean recursive = (request.getParameter("recursive") != null); //$NON-NLS-1$
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
        return VIEW_TREE;
    }

    /**
     * Override this method to configure the tree control (define the columns,
     * ...)
     *
     * @param tree
     * @param request
     */
    protected abstract void prepareTree(Tree tree, HttpServletRequest request);

    /**
     * Prepare the context menu of the tree. This is called during renderTree
     *
     * @param tree
     * @param request
     */
    protected abstract void prepareContextMenu(Tree tree,
                                               HttpServletRequest request);

}
