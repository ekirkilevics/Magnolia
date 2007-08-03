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

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.module.admininterface.AbstractAdminModule;
import info.magnolia.module.workflow.flows.FlowDefinitionManager;
import info.magnolia.module.workflow.jcr.JCRPersistedEngine;
import info.magnolia.module.workflow.jcr.JCRWorkItemStore;
import openwfe.org.ServiceException;
import openwfe.org.engine.impl.expool.SimpleExpressionPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Module "workflow" entry class.
 *
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @author Nicolas Modrzyk
 * @version 3.0
 */
public class WorkflowModule extends AbstractAdminModule {
    private static final Logger log = LoggerFactory.getLogger(WorkflowModule.class);

    private static WorkflowModule instance;

    /**
     * The current used engine
     */
    private JCRPersistedEngine wfEngine;

    private JCRWorkItemStore workItemStore;

    /**
     * Do we backup the deleted workitems?
     */
    private boolean backupWorkItems = false;

    /**
     * Is the saving deferred? This increases the response time.
     */
    private boolean deferredExpressionStorage = false;

    protected void onInit() {
        try {
            Content2BeanUtil.setProperties(this, getConfigNode());
        }
        catch (Content2BeanException e) {
            log.error("can't initialize workflow module",e);
        }
        instance = this;
        startEngine();
        initializeWorkItemStore();
    }

    protected void startEngine() {
        try {
            log.info("Starting openwfe engine");
            wfEngine = new JCRPersistedEngine(deferredExpressionStorage);
            wfEngine.registerParticipant(new MgnlParticipant(WorkflowConstants.PARTICIPANT_PREFIX_USER+".*"));
            wfEngine.registerParticipant(new MgnlParticipant(WorkflowConstants.PARTICIPANT_PREFIX_GROUP+".*"));
            wfEngine.registerParticipant(new MgnlParticipant(WorkflowConstants.PARTICIPANT_PREFIX_ROLE+".*"));
            wfEngine.registerParticipant(new MgnlParticipant(WorkflowConstants.PARTICIPANT_PREFIX_COMMAND+".*"));
        }
        catch (Exception e) {
            log.error("An exception arised when creating the workflow engine", e);
        }
    }

    protected void initializeWorkItemStore() {
        try {
            workItemStore = new JCRWorkItemStore();
        }
        catch (Exception e) {
            log.error("can't initialize the workflow util", e);
        }
    }

    public void destroy() {
        JCRPersistedEngine engine = getEngine();
        if (engine != null && engine.isRunning()) {
            log.info("Stopping workflow engine..");
            try {
                // before try to stop purge and scheduling tasks
                // TODO : this is already done by engine.stop() ... ?
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
        return instance.wfEngine;
    }

    /**
     * return the global work flow engine
     */
    static public JCRWorkItemStore getWorkItemStore() {
        return instance.workItemStore;
    }

    // an easy and ugly way to access this config param
    public static boolean backupWorkitems() {
        return instance.backupWorkItems;
    }

    public static FlowDefinitionManager getFlowDefinitionManager() {
        return (FlowDefinitionManager) FactoryUtil.getSingleton(FlowDefinitionManager.class);
    }


    public boolean isBackupWorkItems() {
        return this.backupWorkItems;
    }


    public void setBackupWorkItems(boolean backupWorkItems) {
        this.backupWorkItems = backupWorkItems;
    }


    public boolean isDeferredExpressionStorage() {
        return this.deferredExpressionStorage;
    }


    public void setDeferredExpressionStorage(boolean deferredExpressionStorage) {
        this.deferredExpressionStorage = deferredExpressionStorage;
    }

}