/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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
     * Use life time jcr sessions or a session per operation
     */
    private boolean useLifeTimeJCRSession = true;

    /**
     * Cleanup empty parent nodes (for expressions, workitems)
     */
    private boolean cleanup = true;

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
            wfEngine = new JCRPersistedEngine();
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
            workItemStore = new JCRWorkItemStore(this.isUseLifeTimeJCRSession(), this.isCleanup(), this.isBackupWorkItems());
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

    public static FlowDefinitionManager getFlowDefinitionManager() {
        return (FlowDefinitionManager) FactoryUtil.getSingleton(FlowDefinitionManager.class);
    }


    public boolean isBackupWorkItems() {
        return this.backupWorkItems;
    }


    public void setBackupWorkItems(boolean backupWorkItems) {
        this.backupWorkItems = backupWorkItems;
    }

    public boolean isUseLifeTimeJCRSession() {
        return useLifeTimeJCRSession;
    }

    public void setUseLifeTimeJCRSession(boolean useLifeTimeJCRSession) {
        log.warn("Setting useLifeTimeJCRSession flag should not be used unless you really know what you do.");

        this.useLifeTimeJCRSession = useLifeTimeJCRSession;
    }

    public boolean isCleanup() {
        return cleanup;
    }

    public void setCleanup(boolean cleanup) {
        log.warn("Setting cleanup flag should not be used unless you really know what you do.");
        this.cleanup = cleanup;
    }

    public static WorkflowModule getInstance() {
        return instance;
    }

}
