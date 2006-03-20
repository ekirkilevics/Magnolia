package info.magnolia.module.owfe;

import info.magnolia.module.owfe.commands.AbstractTreeCommand;
import info.magnolia.module.owfe.commands.CommandsMap;
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
//    CommandAgent commandAgent = new CommandAgent();
//    JavaAgentBuilder jab = new JavaAgentBuilder();

    public MgnlParticipant() throws Exception {

        super();
        storage = new JCRWorkItemAPI();
        log.debug("storage = " + storage);

//		commandAgent.init("command-.*", new ApplicationContext(),  new java.util.HashMap(1));
        // TODO Auto-generated constructor stub
    }

    public MgnlParticipant(String arg0) throws Exception {
        super(arg0);
        storage = new JCRWorkItemAPI();
        log.debug("storage = " + storage);
        // TODO Auto-generated constructor stub
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
            AbstractTreeCommand tc = new CommandsMap().getFlowCommand(cmd);
            if (tc == null) { // not found, do in the old ways
                log.warn("can not find command named " + cmd + "in tree command map");
            } else {
                log.info("find command for " + cmd);
                // set parameters
                HashMap params = new HashMap();
                params.put("workItem", wi);

                // execute
                tc.execute(params);
            }
        } else {
            log.debug("storage = " + storage);
            storage.storeWorkItem("", (InFlowWorkItem) wi);
        }

        log.info("leave consume()..");

    }


}
