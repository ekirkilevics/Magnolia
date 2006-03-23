package info.magnolia.module.owfe;

import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.module.owfe.commands.CommandsMap;
import info.magnolia.module.owfe.commands.MgnlCommand;
import info.magnolia.module.owfe.jcr.JCRWorkItemAPI;
import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class MgnlParticipant extends AbstractEmbeddedParticipant {
    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(AbstractEmbeddedParticipant.class);
    JCRWorkItemAPI storage = null;

    public MgnlParticipant() throws Exception {

        super();
        storage = new JCRWorkItemAPI();
        log.debug("storage = " + storage);
    }

    public MgnlParticipant(String arg0) throws Exception {
        super(arg0);
        storage = new JCRWorkItemAPI();
        log.debug("storage = " + storage);
    }

    public void consume(WorkItem wi) throws Exception {

        // get participant name
        log.info("enter consume()..");
        if (wi == null) {
            log.error("work item is null");
            return;
        }
        String parName = ((InFlowWorkItem) (wi)).getParticipantName();
        log.info("paritcipant name = " + parName);
        if (parName.startsWith("command-")) // handle commands
        {
            //	commandAgent.use((InFlowWorkItem)wi);
            String cmd = parName.substring(8, parName.length());
            log.info("command name is " + cmd);
            MgnlCommand tc = new CommandsMap().getFlowCommand(cmd);
            if (tc == null) { // not found, do in the old ways
                log.warn("can not find command named " + cmd + "in tree command map");
            } else {
                log.info("find command for " + cmd);
                // set parameters in the context
                HashMap params = new HashMap();
                params.put("workItem", wi);
                Context context = MgnlContext.getInstance();
                context.put(MgnlCommand.PARAMS, params);

                // execute
                tc.execute(context);
            }
        } else {
            log.debug("storage = " + storage);
            storage.storeWorkItem("", (InFlowWorkItem) wi);
        }

        log.info("leave consume()..");

    }


}
