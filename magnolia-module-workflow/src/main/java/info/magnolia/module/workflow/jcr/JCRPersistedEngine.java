/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.workflow.jcr;

import info.magnolia.module.workflow.WorkflowConstants;
import info.magnolia.module.workflow.WorkflowModule;

import java.util.Collections;

import openwfe.org.ServiceException;
import openwfe.org.embed.impl.engine.PersistedEngine;
import openwfe.org.engine.Definitions;
import openwfe.org.engine.expool.ExpressionPool;
import openwfe.org.engine.participants.Participant;
import openwfe.org.engine.participants.ParticipantMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implement openwfe.org.embed.engine.Engine to use JCRWorkItemStore and JCRExpressionStore
 *
 * @author jackie_juju@hotmail.com
 */
public class JCRPersistedEngine extends PersistedEngine {
    private final static Logger log = LoggerFactory.getLogger(JCRPersistedEngine.class.getName());

    private final JCRExpressionStore eStore;

    public JCRPersistedEngine() throws ServiceException {
        this(WorkflowConstants.ENGINE_NAME, true);
    }

    /**
     * Instantiates a JCR persisted engine with the given name
     */
    public JCRPersistedEngine(final String engineName, final boolean cached) throws ServiceException {
        super(engineName, cached);
        super.setDaemon(true);

        // create expression store and add it to context
        WorkflowModule module = WorkflowModule.getInstance();
        this.eStore = new JCRExpressionStore(module.isUseLifeTimeJCRSession(), module.isCleanup());

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
