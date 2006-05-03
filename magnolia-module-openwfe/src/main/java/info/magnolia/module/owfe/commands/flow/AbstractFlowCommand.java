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
package info.magnolia.module.owfe.commands.flow;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.module.owfe.MgnlConstants;
import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;

import java.util.HashMap;

import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;

import org.apache.commons.chain.Context;

public abstract class AbstractFlowCommand extends MgnlCommand {

    public boolean exec(HashMap params, Context ctx) {
       // if (log.isDebugEnabled())
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
            log.info("Params for command:" + getClass() + ":" + params);
            preLaunchFlow(ctx, params, engine, li);

            // Launch the item
            engine.launch(li, true);

        } catch (Exception e) {
            log.error("Launching failed", e);
        }

        // End execution
        //if (log.isDebugEnabled())
            log.debug("- Flow command -" + this.getClass().toString() + "- End");
        return true;
    }

    public abstract void preLaunchFlow(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem launchItem);

    public abstract String getFlowName();
}
