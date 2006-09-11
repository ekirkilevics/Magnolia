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
package info.magnolia.module.workflow;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.module.admininterface.AbstractAdminModule;
import info.magnolia.module.workflow.jcr.JCRPersistedEngine;

import java.util.Iterator;

import javax.jcr.RepositoryException;

import openwfe.org.ServiceException;
import openwfe.org.engine.impl.expool.SimpleExpressionPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Module "workflow" entry class.
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @author Nicolas Modrzyk
 * @version 3.0
 */
public class WorkflowModule extends AbstractAdminModule {

    /**
     * 
     */
    public static final String COMMANDS_CATALOG_PATH = "/modules/workflow/config/commands";

    public static final String CACHE_URL_DEFINITION = "/modules/workflow/config/cache";

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(WorkflowModule.class);

    /**
     * The current used engine
     */
    private static JCRPersistedEngine wfEngine;
    private static String cacheURL;

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onRegister(int)
     */
    protected void onRegister(int registerState) throws RegisterException {
        Content menu = ContentUtil.getContent(ContentRepository.CONFIG, "/modules/adminInterface/config/menu");
        try {
            menu.orderBefore("inbox", "security");
            menu.save();
        }
        catch (RepositoryException e) {
            log.warn("can't move menupoint", e);
        }
    }

    /**
     * register commands and start OWFE engine
     */
    protected void onInit() {
        registerCacheUrl();
        startEngine();
    }

    public static String getCacheURL() {
        return cacheURL;
    }

    private void registerCacheUrl() {
        Content node = ContentUtil.getContent(ContentRepository.CONFIG, CACHE_URL_DEFINITION);
        try {
            if(node!=null && node.hasNodeData("serverURL")) {
                String serverURL = node.getNodeData("serverURL").getString();
                if(serverURL !=null) {
                    cacheURL = serverURL;
                    log.info("Cache server base url for flows is set to:"+cacheURL);
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * 
     */
    private void startEngine() {
        try {
            log.info("Starting openwfe engine");
            wfEngine = new JCRPersistedEngine();
            wfEngine.registerParticipant(new MgnlParticipant("user-.*"));
            wfEngine.registerParticipant(new MgnlParticipant("group-.*"));
            wfEngine.registerParticipant(new MgnlParticipant("role-.*"));
            wfEngine.registerParticipant(new MgnlParticipant("command-.*"));
        }
        catch (Exception e) {
            log.error("An exception arised when creating the workflow engine", e);
        }
    }

    public void destroy() {
        JCRPersistedEngine engine = getEngine();
        if (engine != null && engine.isRunning()) {
            log.info("Stopping workflow engine..");
            try {
                // before try to stop purge and scheduling tasks
                ((SimpleExpressionPool) engine.getExpressionPool()).stop();
                engine.stop();
            }
            catch (ServiceException se) {
                log.error("Failed to stop Open WFE engine");
                log.error(se.getMessage(), se);
            }
        }
    }

    /**
     * return the global work flow engine
     */
    static public JCRPersistedEngine getEngine() {
        return wfEngine;
    }

}