/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */

package info.magnolia.module.data;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.ModuleDefinition;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RepositoryDefinition;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AbstractAdminModule;
import info.magnolia.module.admininterface.DialogHandlerManager;
import info.magnolia.module.admininterface.TreeHandlerManager;
import info.magnolia.module.data.tools.DataImportManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;

/**
 * 
 *
 * @author Christoph Hoffmann (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class DataModule extends AbstractAdminModule {

	protected static String repository;
	
	protected Timer registrationTimer;
	protected final HashSet registrationTypes;
	private final Object lock = new Object();
	
	public DataModule(){
		registrationTypes = new HashSet();
	}
	
	/** 
	 * Initialization of the module, means registration of all types and the corresponding
	 * dialogs and trees. 
	 *
	 * @see info.magnolia.module.admininterface.AbstractAdminModule#onInit()
	 */
	protected void onInit() throws InitializationException {
		Content cfgNode = getConfigNode();
		Collection types = cfgNode.getChildByName(Constants.TYPES_NODE).getChildren();
		for(Iterator it = types.iterator(); it.hasNext();){
			Content type = (Content)it.next();
			registerDialogs(type);
			registerTrees(type);
		}
		Content importConfig = moduleNode.getChildByName(Constants.IMPORT_NODE);
		if(importConfig != null){
			DataImportManager.getInstance().register(importConfig);
		}
		
		registerEventListener();
	}
	
	/**
	 * Schedules the registration of all dialogs and trees. This is nessessary 
	 * because on creation time of the node the nodeData of the dialog/tree are not available.
	 * 
	 * @param type the type to add to scheduled registration list.
	 */
	public void scheduleDialogAndTreeRegistration(String typeName){
		synchronized(lock){
			log.info("scheduling registration of " + typeName);
			registrationTypes.add(typeName);
			if(registrationTimer != null){
				registrationTimer.cancel();
			}
			registrationTimer = new Timer();
			registrationTimer.schedule(new TimerTask(){
				public void run() {
					synchronized(lock){
						log.info("executing scheduled registration of datatype dialogs and trees.");
						Content typesNode = getConfigNode().getChildByName(Constants.TYPES_NODE);
						for(Iterator types = registrationTypes.iterator(); types.hasNext();){
							Content type = typesNode.getChildByName((String)types.next());
							registerDialogs(type);
							registerTrees(type);
						}
						registrationTypes.clear();
					}
				}}, 10000);
		}
	}
	
	
	/**
	 * Registration of all dialogs belonging to the given type. 
	 * 
	 * @param type the type to register the dialogs for
	 */
	protected void registerDialogs(Content type){
        Content dialogs = ContentUtil.getCaseInsensitive(type, Constants.TYPE_DIALOGS_NODE);
        if (dialogs != null) {
            DialogHandlerManager.getInstance().register(dialogs);
        }
	}
	
	/**
	 * Registration of all trees belonging to the given type. 
	 * 
	 * @param type the type to register the trees for
	 */
	protected void registerTrees(Content type){
        Content trees = ContentUtil.getCaseInsensitive(type, Constants.TYPE_TREES_NODE);
        if (trees != null) {
            TreeHandlerManager.getInstance().register(trees);
        }
	}

	/**
	 * EventListener for observation of type creation.
	 */
	protected void registerEventListener(){
		// registring type event listener
		try {
			HierarchyManager config = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
			ObservationManager observationManager = config.getWorkspace().getObservationManager();
			observationManager.addEventListener(new TypeEventListener(this), Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED, Constants.TYPES_NODE_PATH, true, null, null, false);
		} catch (UnsupportedRepositoryOperationException e) {
			log.error("can't add event listener", e);
		} catch (RepositoryException e) {
			log.error("can't add event listener", e);
		}
	}
	
	
    /**
     * Make some specific configuration.
     */
    public void onRegister(int registerState) {
    	configureMenu();
        ModuleUtil.subscribeRepository(DataModule.repository);
	    ModuleUtil.grantRepositoryToSuperuser(DataModule.repository); 
    }

    /**
     * Order the menu
     */
    private void configureMenu() {
        // move menu point
        Content menu = ContentUtil.getContent(ContentRepository.CONFIG, "/modules/adminInterface/config/menu");
        try {
            menu.orderBefore(getModuleDefinition().getName(), "security");
            menu.save();
        }
        catch (RepositoryException e) {
            log.warn("can't move menupoint", e);
        }
    }

    /**
     * @return Returns the repository.
     */
    public static String getRepository() {
        return repository;
    }
    
    /**
     * Hack for providing the repository name in a static way.
     */
    protected void setDefinition(ModuleDefinition definition) {
    	super.setDefinition(definition);
    	
    	Iterator repositories = definition.getRepositories().iterator();
    	if(!repositories.hasNext()){
			throw new RuntimeException("No repository defined. Please check the data.xml file");
    	}
    	
    	RepositoryDefinition repositoryDefinition = (RepositoryDefinition)repositories.next();
    	Iterator workspaces = repositoryDefinition.getWorkspaces().iterator();
    	if(!workspaces.hasNext()){
			throw new RuntimeException("No workspaces defined. Please check the data.xml file");
    	}
    	DataModule.repository = (String)workspaces.next();
    }
   
}
