package info.magnolia.module.owfe.commands;

import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
import openwfe.org.engine.launch.LaunchException;
import openwfe.org.engine.workitem.LaunchItem;
import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: niko
 * Date: Mar 22, 2006
 * Time: 1:11:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class IFlowCommand implements ITreeCommand {

    public boolean execute(Context context) {
        HashMap params = (HashMap) context.get(PARAMS);
        log.info("- Flow command -" + this.getClass().toString() + "- Start");

        // Get the references
        LaunchItem li = new LaunchItem();
        JCRPersistedEngine engine = OWFEEngine.getEngine();

        // start activation
        onExecute(context, params, engine, li);

        // Launch the item
        try {
            engine.launch(li, true);
        } catch (LaunchException e) {
            log.error("Launching failed", e);
        }

        // End execution
        log.info("- Flow command -" + this.getClass().toString() + "- End");
        return true;
    }

    public abstract void onExecute(Context context, HashMap params, JCRPersistedEngine engine, LaunchItem launchItem);
}
