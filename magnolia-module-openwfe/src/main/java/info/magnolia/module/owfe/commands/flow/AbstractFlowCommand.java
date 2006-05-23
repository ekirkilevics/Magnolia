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

import info.magnolia.commands.ContextAttributes;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.module.owfe.WorkflowModule;
import info.magnolia.module.owfe.jcr.JCRFlowDefinition;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.workitem.AttributeUtils;
import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.workitem.StringAttribute;
import openwfe.org.engine.workitem.StringMapAttribute;

import org.apache.commons.chain.Context;


public abstract class AbstractFlowCommand extends MgnlCommand {

    public boolean execute(Context ctx) {

        log.debug("- Flow command -" + this.getClass().toString() + "- Start");
        try {
            // Get the references
            LaunchItem li = new LaunchItem();
            
            prepareLaunchItem(ctx, li);
            
            li.addAttribute(ContextAttributes.P_ACTION, new StringAttribute(this.getClass().getName()));
            li.setWorkflowDefinitionUrl(ContextAttributes.P_WORKFLOW_DEFINITION_URL);

            // Retrieve and add the flow definition to the LaunchItem
            String flowDef = new JCRFlowDefinition().getflowDefAsString(getFlowName());
            li.getAttributes().puts(ContextAttributes.P_DEFINITION, flowDef);
            JCRPersistedEngine engine = WorkflowModule.getEngine();

            // Launch the item
            engine.launch(li, true);

        }
        catch (Exception e) {
            log.error("Launching failed", e);
        }

        // End execution

        log.debug("- Flow command -" + this.getClass().toString() + "- End");
        return true;
    }

    /**
     * FIXME: don't be that rough
     * @param context
     * @param launchItem
     */
    public void prepareLaunchItem(Context context, LaunchItem launchItem){
        StringMapAttribute attrs = AttributeUtils.java2attributes(context);
        launchItem.setAttributes(attrs);
    }

    public abstract String getFlowName();
}
