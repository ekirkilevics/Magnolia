package info.magnolia.module.owfe.jcr;

import openwfe.org.ServiceException;
import openwfe.org.embed.impl.engine.FsPersistedEngine;
import openwfe.org.embed.impl.engine.PersistedEngine;
import openwfe.org.engine.Definitions;
import openwfe.org.engine.participants.Participant;
import openwfe.org.engine.participants.ParticipantMap;


/**
 * Implement openwfe.org.embed.engine.Engine to use JCRWorkItemStore and JCRExpressionStore
 *
 * @author jackie_juju@hotmail.com
 */
public class JCRPersistedEngine extends PersistedEngine {

    private JCRExpressionStore eStore = null;
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
            .getLogger(FsPersistedEngine.class.getName());

    public JCRPersistedEngine() throws ServiceException {
        this("owfe", true);


    }

    public Participant getParticipant(String name) {
        ParticipantMap pm = Definitions.getParticipantMap(getContext());
        if (pm == null) {
            log.error("get participant failed, the map retrieved was null for:" + name);
            return null;
        }
        return pm.get(name);
    }


    /**
     * Instantiates a JCR persisted engine with the given name
     */
    public JCRPersistedEngine
            (final String engineName, final boolean cached)
            throws
            ServiceException {
        super(engineName, cached);

        // create expression store and add it to context
        final java.util.Map esParams = new java.util.HashMap(1);

        eStore = new JCRExpressionStore();

        eStore.init(Definitions.S_EXPRESSION_STORE, getContext(), esParams);

        getContext().add(eStore);

        //
        // expression pool

        // is initted in parent class PersistedEngine

    }




    public JCRExpressionStore getExpStore() {
        return eStore;
    }

}
