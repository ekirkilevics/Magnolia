package info.magnolia.module.owfe;

import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;
import openwfe.org.engine.workitem.InFlowWorkItem;
import openwfe.org.engine.workitem.WorkItem;

import org.apache.log4j.Logger;


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
        if (parName.startsWith("command")) // handle commands
        {
            // @Todo
        }
        else {
            log.debug("storage = " + storage);
            storage.storeWorkItem("", (InFlowWorkItem) wi);
        }

        log.info("leave consume()..");

    }

}
