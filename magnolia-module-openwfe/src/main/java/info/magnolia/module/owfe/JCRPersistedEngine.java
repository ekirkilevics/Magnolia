package info.magnolia.module.owfe;
import openwfe.org.ServiceException;
import openwfe.org.embed.impl.engine.FsPersistedEngine;
import openwfe.org.embed.impl.engine.PersistedEngine;
import openwfe.org.engine.Definitions;
import openwfe.org.engine.impl.expool.XmlExpressionStore;
import openwfe.org.engine.participants.Participant;
import openwfe.org.engine.participants.ParticipantMap;

import com.ns.log.Log;


/**
 * Implement openwfe.org.embed.engine.Engine to use JCRWorkItemStore and JCRExpressionStore
 * @author jackie_juju@hotmail.com
 *
 */
public class JCRPersistedEngine extends PersistedEngine {
	
    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger
        .getLogger(FsPersistedEngine.class.getName());
    
	public JCRPersistedEngine() throws ServiceException {
		this("owfe", true);
		

	}

	public Participant getParticipant(String name){
		ParticipantMap pm = Definitions.getParticipantMap(getContext());
		if (pm == null)
		{
			Log.error("owfe", "get particiaptn failed");
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
        ServiceException
    {
        super(engineName, cached);

        // create expression store and add it to context
        final java.util.Map esParams = new java.util.HashMap(1);
    
        JCRExpressionStore eStore = new JCRExpressionStore();;
 
        eStore.init(Definitions.S_EXPRESSION_STORE, getContext(), esParams);
        
        getContext().add(eStore);

        //
        // expression pool

        // is initted in parent class PersistedEngine

    }
    
//	public JCRPersistedEngine(String engineName, boolean cached) throws ServiceException {
//		super(engineName, cached);
//		// TODO Auto-generated constructor stub
//	}



}
