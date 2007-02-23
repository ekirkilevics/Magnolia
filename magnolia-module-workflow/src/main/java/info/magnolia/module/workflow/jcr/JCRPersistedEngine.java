/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.workflow.jcr;

import info.magnolia.module.workflow.WorkflowConstants;
import openwfe.org.ServiceException;
import openwfe.org.embed.impl.engine.PersistedEngine;
import openwfe.org.engine.Definitions;
import openwfe.org.engine.expool.ExpressionPool;
import openwfe.org.engine.participants.Participant;
import openwfe.org.engine.participants.ParticipantMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;


/**
 * Implement openwfe.org.embed.engine.Engine to use JCRWorkItemStore and JCRExpressionStore
 *
 * @author jackie_juju@hotmail.com
 */
public class JCRPersistedEngine extends PersistedEngine {
    private final static Logger log = LoggerFactory.getLogger(JCRPersistedEngine.class.getName());

    private final JCRExpressionStore eStore;

    public JCRPersistedEngine(final boolean storageDeferred) throws ServiceException {
        this(WorkflowConstants.ENGINE_NAME, true, storageDeferred);
    }

    /**
     * Instantiates a JCR persisted engine with the given name
     */
    public JCRPersistedEngine(final String engineName, final boolean cached, final boolean storageDeferred) throws ServiceException {
        super(engineName, cached);
        super.setDaemon(true);

        // create expression store and add it to context
        this.eStore = new JCRExpressionStore(storageDeferred);

        this.eStore.init(Definitions.S_EXPRESSION_STORE, getContext(), Collections.EMPTY_MAP);

        getContext().add(this.eStore);
    }

    // TODO : this doesnt seem to be used ... ?
    public Participant getParticipant(String name) {
        ParticipantMap pm = Definitions.getParticipantMap(getContext());
        if (pm == null) {
            log.error("get participant failed, the map retrieved was null for:" + name);
            return null;
        }
        return pm.get(name);
    }

    // TODO : this doesnt seem to be used ... ?
    public JCRExpressionStore getExpStore() {
        return this.eStore;
    }

    // TODO : this is useless since engine.stop() already stops the expression pool, like the WFModule tries to do ... ?
    public ExpressionPool getExpressionPool() {
        return super.getExpressionPool();
    }

}
