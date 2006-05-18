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
package info.magnolia.module.owfe;

import info.magnolia.cms.module.RegisterException;
import info.magnolia.module.admininterface.AbstractAdminModule;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;
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
public class Engine extends AbstractAdminModule {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Engine.class);

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onRegister(int)
     */
    protected void onRegister(int registerState) throws RegisterException {
        // nothing todo
    }

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onInit()
     */
    protected void onInit() {
        try {
			new OWFEEngine();
		} catch (Exception e) {
			log.error("An exception arised when creating the workflow engine",e);
		}
	}

    public void destroy() {
        JCRPersistedEngine engine = OWFEEngine.getEngine();
		if (engine!=null && engine.isRunning()) {
            log.info("Stopiing workflow engine..");
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

}