package info.magnolia.module.admininterface;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.commands.CommandsManager;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TreeHandlerManager extends ObservedManager {

    private static final String ND_CLASS = "class";

	private static final String ND_REPOSITORY = "repository";

	private static final String ND_NAME = "name";

	/**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TreeHandlerManager.class);

    /**
     * The current implementation of the ParagraphManager. Defeined in magnolia.properties.
     */
    private static TreeHandlerManager instance = (TreeHandlerManager) FactoryUtil
        .getSingleton(TreeHandlerManager.class);

    /**
     * Map with repository name/handler class for admin tree. When this servlet will receive a call with a parameter
     * <code>repository</code>, the corresponding handler will be used top display the admin tree.
     */
    private final Map treeHandlers = new HashMap();

    /**
     * Get the tree handler registered under a particular name.
     * @param name
     * @param request
     * @param response
     * @return
     */
    public AdminTreeMVCHandler getTreeHandler(String name, HttpServletRequest request, HttpServletResponse response) {

        TreeHandlerConfig th = (TreeHandlerConfig) treeHandlers.get(name);

        if (th == null) {
            throw new InvalidTreeHandlerException(name);
        }

        Class treeHandlerClass = th.getHandler();
        String repository = th.getRepository();

        try {
            Constructor constructor = treeHandlerClass.getConstructor(new Class[]{
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            AdminTreeMVCHandler newInstance = (AdminTreeMVCHandler) constructor.newInstance(new Object[]{repository, request, response});
            ContentUtil.setProperties(newInstance, th.getTreeDefinition());
            newInstance.initialize();
			return newInstance;
        }
        catch (Exception e) {
            throw new InvalidTreeHandlerException(name, e);
        }
    }

    protected void registerTreeHandler(String name, String repository, Class treeHandler, Content treeDefinition) {
        log.info("Registering tree handler {}", name); //$NON-NLS-1$ //$NON-NLS-2$
        treeHandlers.put(name, new TreeHandlerConfig(treeHandler, repository, treeDefinition));
    }

    protected void onRegister(Content defNode) {
        Collection trees = defNode.getChildren(ItemType.CONTENTNODE.getSystemName());
        for (Iterator iter = trees.iterator(); iter.hasNext();) {
            Content tree = (Content) iter.next();
            String name = tree.getNodeData(ND_NAME).getString(); //$NON-NLS-1$

            if (StringUtils.isEmpty(name)) {
                name = tree.getName();
            }

            String repository = tree.getNodeData(ND_REPOSITORY).getString(); //$NON-NLS-1$
            String className = tree.getNodeData(ND_CLASS).getString(); //$NON-NLS-1$

            if (StringUtils.isEmpty(repository)) {
                repository = name;
            }

            try {
                this.registerTreeHandler(name, repository, Class.forName(className), tree);
            }
            catch (ClassNotFoundException e) {
                log.error("Can't register tree handler [{}]: class [{}] not found", name, className);
            }
            
            // register commands if defined
            try {
                if(tree.hasContent("commands")){
                    Content commands = tree.getContent("commands");
                    CommandsManager.getInstance().register(commands);
                }
            }
            catch (RepositoryException e) {
                log.error("can't register commands catalog for tree handler {}", name, e);
            }
        }
    }

    /**
     * @return Returns the instance.
     */
    public static TreeHandlerManager getInstance() {
        return instance;
    }

    /**
     * Clear the handlers
     */
    protected void onClear() {
        this.treeHandlers.clear();
    }

    class TreeHandlerConfig {

        private Class handler;

        private String repository;
        
        private Content treeDefinition;

        TreeHandlerConfig(Class handler, String repository, Content treeDefinition) {
            this.handler = handler;
            this.repository = repository;
            this.treeDefinition = treeDefinition;
        }

        public Class getHandler() {
            return this.handler;
        }

        public String getRepository() {
            return this.repository;
        }
        
        public Content getTreeDefinition(){
        	return treeDefinition;
        }
    }

}
