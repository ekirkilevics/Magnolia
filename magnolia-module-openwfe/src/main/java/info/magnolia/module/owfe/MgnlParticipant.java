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
    private static final String COMMAND_PREFIX = "command-";
    private static final int COMMAND_PREFIX_LEN = COMMAND_PREFIX.length();

    public MgnlParticipant() throws Exception {
        super();
        storage = new JCRWorkItemAPI();
        if (log.isDebugEnabled())
            log.debug("storage = " + storage);
    }

    public MgnlParticipant(String arg0) throws Exception {
        super(arg0);
        storage = new JCRWorkItemAPI();
        if (log.isDebugEnabled())
            log.debug("storage = " + storage);
    }

    public void consume(WorkItem wi) throws Exception {

        // get participant name
        if (log.isDebugEnabled())
            log.debug("enter consume()..");
        if (wi == null) {
            log.error("work item is null");
            return;
        }
        String parName = ((InFlowWorkItem) (wi)).getParticipantName();
        if (log.isDebugEnabled())
            log.debug("participant name = " + parName);
        if (parName.startsWith(COMMAND_PREFIX)) // handle commands
        {
            String cmd = parName.substring(COMMAND_PREFIX_LEN, parName.length());
            log.debug("command name is " + cmd);
            MgnlCommand tc = new CommandsMap().getFlowCommand(cmd);
            if (tc == null) { // not found, do in the old ways
                if (log.isDebugEnabled())
                    log.debug("can not find command named " + cmd + "in tree command map");
            } else {
                if (log.isDebugEnabled())
                    log.debug("found command for " + cmd);

                // set parameters in the context
                HashMap params = new HashMap();
                params.put(MgnlCommand.P_WORKITEM, wi);
                Context context = MgnlContext.getInstance();
                context.put(MgnlCommand.PARAMS, params);

                // execute
                tc.execute(context);
            }
        } else {
            if (log.isDebugEnabled())
                log.debug("storage = " + storage);
            storage.storeWorkItem("", (InFlowWorkItem) wi);
        }
        if (log.isDebugEnabled())
            log.debug("leave consume()..");

    }


}
