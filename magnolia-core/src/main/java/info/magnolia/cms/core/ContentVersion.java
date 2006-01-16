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
package info.magnolia.cms.core;

import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.Version;
import java.util.Calendar;

/**
 * @author Sameer Charles
 * @version $Revision: 1871 $ ($Author: scharles $)
 */
public class ContentVersion extends Content {

    /**
     * version node (nt:version)
     * */
    private Version state;

    /**
     * package private constructor
     * @param thisVersion
     * @throws RepositoryException
     * */
    ContentVersion(Version thisVersion) throws RepositoryException {
        this.state = thisVersion;
        this.init();
    }

    /**
     * Set frozen node of this version as working node
     * @throws RepositoryException
     * */
    private void init() throws RepositoryException {
        this.setNode(this.state.getNode(ItemType.JCR_FROZENNODE.getSystemName()));
    }

    /**
     * Get creation date of this version
     * @throws RepositoryException
     * @return creation date as calendar
     * */
    public Calendar getCreated() throws RepositoryException {
        return this.state.getCreated();
    }

    /**
     * Get containing version history
     * @throws RepositoryException
     * @return version history associated to this version
     * */
    public VersionHistory getContainingHistory() throws RepositoryException {
        return this.state.getContainingHistory();
    }
}
