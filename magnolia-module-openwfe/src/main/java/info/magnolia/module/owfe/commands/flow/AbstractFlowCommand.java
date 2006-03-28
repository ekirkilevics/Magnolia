package info.magnolia.module.owfe.commands.flow;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.module.owfe.MgnlConstants;
import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: niko
 * Date: Mar 22, 2006
 * Time: 1:11:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractFlowCommand extends MgnlCommand {

    public boolean exec(HashMap params, Context ctx) {
        if (log.isDebugEnabled())
            log.debug("- Flow command -" + this.getClass().toString() + "- Start");
        try {
            // Get the references
            LaunchItem li = new LaunchItem();
            li.addAttribute(MgnlConstants.P_ACTION, new StringAttribute(this.getClass().getName()));
            li.setWorkflowDefinitionUrl(MgnlConstants.P_WORKFLOW_DEFINITION_URL);

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString(getFlowName());
            li.getAttributes().puts(MgnlConstants.P_DEFINITION, flowDef);
            JCRPersistedEngine engine = OWFEEngine.getEngine();

            // start activation
            preLaunchFlow(ctx, params, engine, li);

            // Launch the item
            engine.launch(li, true);

        } catch (Exception e) {
            log.error("Launching failed", e);
        }

        // End execution
        if (log.isDebugEnabled())
            log.debug("- Flow command -" + this.getClass().toString() + "- End");
        return true;
    }

    public abstract void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem launchItem);

    public abstract String getFlowName();
}
