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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.0
 */
public class LocalStore {

    private static Logger log = Logger.getLogger(LocalStore.class);

    private Content localStore;

    private String configPath;

    protected static LocalStore getInstance(String path) {
        LocalStore store = new LocalStore();
        store.configPath = path;
        store.init();
        return store;
    }

    private void init() {
        log.info("Config : Initializing LocalStore for - " + configPath); //$NON-NLS-1$
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            localStore = configHierarchyManager.getContent(configPath);
            log.info("Config : LocalStore initialized for -" + configPath); //$NON-NLS-1$
        }
        catch (PathNotFoundException e) {
            log.error("Config : No LocalStore defined for - " + configPath); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to initialize LocalStore for - " + configPath); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    protected Content getStore() {
        return localStore;
    }
}
