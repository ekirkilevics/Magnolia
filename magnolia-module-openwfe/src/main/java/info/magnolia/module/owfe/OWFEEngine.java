package info.magnolia.module.owfe;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;

import javax.jcr.Repository;

import org.apache.log4j.Logger;


/**
 * A Wrapper of jcr work flow engine 
 * @author jackie
 *
 */
public class OWFEEngine{
	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(OWFEEngine.class);
	
	static JCRPersistedEngine wfEngine;
	// repository id
	public static final String REPO_OWFE = "owfe"; 
	
	OWFEEngine() throws Exception
	{	
		wfEngine = new JCRPersistedEngine();
		log.debug("create worklist...");

		//JCRWorkList wl_pl = new JCRWorkList("project-leader");	
		//OWFEEngine.getEngine().registerParticipant(wl_pl);
		OWFEEngine.getEngine().registerParticipant(
				new MgnlParticipant("user-.*"));
		OWFEEngine.getEngine().registerParticipant(
				new MgnlParticipant("group-.*"));
		OWFEEngine.getEngine().registerParticipant(
				new MgnlParticipant("role-.*"));
		OWFEEngine.getEngine().registerParticipant(
				new MgnlParticipant("command-.*"));		
	}
		
	/**
	 * return the global work flow engine
	 * @return
	 */
	static public JCRPersistedEngine getEngine(){
		return wfEngine;		
	}
	
	/**
	 * return repository for owfe
	 * @return
	 */
	static public Repository getOWFERepository() {
		Repository repo = ContentRepository.getRepository(REPO_OWFE);
		log.info("get repository for " + REPO_OWFE + "=" + repo);
		return repo;
	}
	
	/**
	 * get hierarchy mananger for owfe repository
	 * @param workspace
	 * @return
	 */
	public static HierarchyManager getOWFEHierarchyManager(String workspace) {
		HierarchyManager hm = null;
		if (workspace != null)
		 hm = ContentRepository
				.getHierarchyManager(REPO_OWFE, workspace);
		else
			 hm = ContentRepository
			.getHierarchyManager(REPO_OWFE);
//		log.info("get HierarchyManager for " + REPO_OWFE + "=" + hm);
		return hm;
	}


 

}
