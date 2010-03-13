/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.core.version;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.beans.config.VersionConfig;
import info.magnolia.objectfactory.Components;

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
        return Components.getSingleton(VersionManager.class);
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
