/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core.version;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.beans.config.VersionConfig;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton class which should be used for any operation related to versioning VersionManager synchronizes all
 * operations like add version, restore version and remove version but it does not synchronize between operations
 * @author Sameer Charles
 * $Id:VersionManager.java 6430 2006-09-20 11:25:35Z scharles $
 */
public final class VersionManager extends BaseVersionManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(VersionManager.class);

    /**
     * do not instanciate
     */
    public VersionManager() {
        try {
            this.createInitialStructure();
        }
        catch (RepositoryException re) {
            log.error("Failed to initialize VersionManager");
            log.error(re.getMessage(), re);
        }
    }

    /**
     * get instance
     */
    public static VersionManager getInstance() {
        return (VersionManager) FactoryUtil.getSingleton(VersionManager.class);
    }

    /**
     * since version is set "only revert" always return true
     * */
    public boolean isInvalidMaxVersions() {
        return VersionConfig.getInstance().getMaxVersionAllowed() < 1;
    }

    /**
     * set version history to max version possible
     * @param node
     * @throws RepositoryException if failed to get VersionHistory or fail to remove
     */
    public void setMaxVersionHistory(Content node) throws RepositoryException {
        VersionHistory history = node.getJCRNode().getVersionHistory();
        VersionIterator versions = history.getAllVersions();
        // size - 2 to skip root version
        long indexToRemove = (versions.getSize() - 2) - VersionConfig.getInstance().getMaxVersionAllowed();
        if (indexToRemove > 0) {
            // skip root version
            versions.nextVersion();
            // remove the version after rootVersion
            for (; indexToRemove > 0; indexToRemove--) {
                history.removeVersion(versions.nextVersion().getName());
            }
        }
    }

}
