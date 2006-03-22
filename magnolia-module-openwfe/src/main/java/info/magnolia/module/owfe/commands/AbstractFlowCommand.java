package info.magnolia.module.owfe.commands;

import info.magnolia.module.owfe.OWFEEngine;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;

import java.util.HashMap;

import openwfe.org.engine.workitem.LaunchItem;
import openwfe.org.engine.launch.LaunchException;

/**
 * Created by IntelliJ IDEA.
 * User: niko
 * Date: Mar 22, 2006
 * Time: 1:11:29 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractFlowCommand extends AbstractTreeCommand {

    public boolean execute(HashMap params) {
        log.info("- Flow command -" + this.getClass().toString() + "- Start");

        // Get the references
        LaunchItem li = new LaunchItem();
        JCRPersistedEngine engine = OWFEEngine.getEngine();

        // start activation
        executeImpl(params, engine, li);

        // Launch the item
        try {
            engine.launch(li, true);
        } catch (LaunchException e) {
            log.error("Launching failed",e);
        }

        // End execution
        log.info("- Flow command -" + this.getClass().toString() + "- End");
        return true;
    }

    public abstract void executeImpl(HashMap params, JCRPersistedEngine engine, LaunchItem launchItem);
}
