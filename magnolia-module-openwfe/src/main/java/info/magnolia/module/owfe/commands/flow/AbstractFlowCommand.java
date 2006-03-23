package info.magnolia.module.owfe.commands.flow;

import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.commands.MgnlCommand;
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
public abstract class AbstractFlowCommand implements MgnlCommand {

    public boolean execute(Context context) {
        HashMap params = (HashMap) context.get(PARAMS);
        log.info("- Flow command -" + this.getClass().toString() + "- Start");
        try {
            // Get the references
            LaunchItem li = new LaunchItem();
            li.addAttribute(P_ACTION, new StringAttribute(this.getClass().getName()));
            JCRPersistedEngine engine = OWFEEngine.getEngine();

            // start activation
            onExecute(context, params, engine, li);

            // Launch the item
            engine.launch(li, true);

        } catch (Exception e) {
            log.error("Launching failed", e);
        }

        // End execution
        log.info("- Flow command -" + this.getClass().toString() + "- End");
        return true;
    }

    public abstract void onExecute(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem launchItem);
}
