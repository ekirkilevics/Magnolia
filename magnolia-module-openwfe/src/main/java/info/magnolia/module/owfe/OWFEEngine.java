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
package info.magnolia.module.owfe;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.owfe.jcr.JCRPersistedEngine;

import javax.jcr.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Wrapper of jcr work flow engine
 * @author jackie
 */
public class OWFEEngine {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(OWFEEngine.class);

    static JCRPersistedEngine wfEngine;

    // repository id
    public static final String REPO_OWFE = "owfe";

    OWFEEngine() throws Exception {
        wfEngine = new JCRPersistedEngine();
        log.debug("create worklist...");

        OWFEEngine.getEngine().registerParticipant(new MgnlParticipant("user-.*"));
        OWFEEngine.getEngine().registerParticipant(new MgnlParticipant("group-.*"));
        OWFEEngine.getEngine().registerParticipant(new MgnlParticipant("role-.*"));
        OWFEEngine.getEngine().registerParticipant(new MgnlParticipant("command-.*"));
    }

    /**
     * return the global work flow engine
     */
    static public JCRPersistedEngine getEngine() {
        return wfEngine;
    }

    /**
     * return repository for owfe
     */
    static public Repository getOWFERepository() {
        Repository repo = ContentRepository.getRepository(REPO_OWFE);
        log.info("get repository for " + REPO_OWFE + "=" + repo);
        return repo;
    }

    /**
     * get hierarchy mananger for owfe repository
     * @param workspace
     */
    public static HierarchyManager getOWFEHierarchyManager(String workspace) {
        HierarchyManager hm;
        if (workspace != null) {
            hm = ContentRepository.getHierarchyManager(REPO_OWFE, workspace);
        }
        else {
            hm = ContentRepository.getHierarchyManager(REPO_OWFE);
        }
        // log.info("get HierarchyManager for " + REPO_OWFE + "=" + hm);
        return hm;
    }

}
