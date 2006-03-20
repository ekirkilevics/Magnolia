package info.magnolia.module.owfe.jcr;

import openwfe.org.Service;
import openwfe.org.ServiceException;
import openwfe.org.embed.impl.worklist.AbstractWorklist;
import openwfe.org.engine.workitem.WorkItem;
import openwfe.org.worklist.WorkListException;
import openwfe.org.worklist.store.WorkItemStorage;


public class JCRWorkList extends AbstractWorklist {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JCRWorkList.class.getName());

    private WorkItemStorage storage = null;

    public JCRWorkList(final String participantName) {
        this(participantName, participantName);
    }

    public void consume(WorkItem wi) throws Exception {
        log.info("enter consume()..");
        String parName = wi.getAttributes().get("__participant__").toString();
        log.info("paritcipant name = " + parName);
        if (parName.startsWith("command")) // handle commands
        {
            // @Todo execute command
        } else {
            super.consume(wi); // save
        }
        log.info("leave consume()..");
    }

    public JCRWorkList(final String participantRegex, final String listName) {
        super(participantRegex, listName);

        //
        // ready storage

        this.storage = new JCRWorkItemStorage();
        try {
            ((Service) this.storage).init(listName, null, new java.util.HashMap(0));
        }
        catch (final ServiceException e) {
            log.warn("() failed to init underlying InMemoryWorkItemStorage", e);
        }
    }

    public WorkItemStorage getStorage() throws WorkListException {
        // TODO Auto-generated method stub
        return storage;
    }

}
