package info.magnolia.module.data;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.admininterface.AbstractAdminModule;
import info.magnolia.module.admininterface.DialogHandlerManager;
import info.magnolia.module.admininterface.TreeHandlerManager;

public class DataModule extends AbstractAdminModule {

	protected String repository;
	
	public DataModule(){
        setRepository("data"); //warum hier noch mal ?!?
	}
	
	protected void onInit() throws InitializationException {
		Content cfgNode = getConfigNode();
		Collection types = cfgNode.getChildByName("types").getChildren();
		for(Iterator it = types.iterator(); it.hasNext();){
			Content type = (Content)it.next();
            Content node;
            // register the dialogs
            node = ContentUtil.getCaseInsensitive(type, "dialogs");
            if (node != null) {
                DialogHandlerManager.getInstance().register(node);
            }
			// register trees
            node = ContentUtil.getCaseInsensitive(type, "trees");
            if (node != null) {
                TreeHandlerManager.getInstance().register(node);
            }
		}
	}
    /**
     * Make some specific configuration.
     */
    public void onRegister(int registerState) {
        configureMenu();
        ModuleUtil.subscribeRepository(this.getRepository());
        ModuleUtil.grantRepositoryToSuperuser(this.getRepository()); 
    }

    /**
     * Order the menu
     */
    private void configureMenu() {
        // move menu point
        Content menu = ContentUtil.getContent(ContentRepository.CONFIG, "/modules/adminInterface/config/menu");
        try {
            menu.orderBefore("data", "security");
            menu.save();
        }
        catch (RepositoryException e) {
            log.warn("can't move menupoint", e);
        }
    }

    /**
     * @return Returns the repository.
     */
    public String getRepository() {
        return repository;
    }

    
    /**
     * @param repository The repository to set.
     */
    protected void setRepository(String repository) {
        this.repository = repository;
    }


}
