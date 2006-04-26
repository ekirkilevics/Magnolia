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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class ContentVersion extends Content {

    /**
     * version node (nt:version)
     */
    private Version state;

    /**
     * package private constructor
     * @param thisVersion
     * @throws RepositoryException
     */
    public ContentVersion(Version thisVersion) throws RepositoryException {
        this.state = thisVersion;
        this.init();
    }

    /**
     * Set frozen node of this version as working node
     * @throws RepositoryException
     */
    private void init() throws RepositoryException {
        this.setNode(this.state.getNode(ItemType.JCR_FROZENNODE));
    }

    /**
     * Get creation date of this version
     * @throws RepositoryException
     * @return creation date as calendar
     */
    public Calendar getCreated() throws RepositoryException {
        return this.state.getCreated();
    }

    /**
     * Return the name of the version represented by this object
     * @return the versions name
     * @throws RepositoryException
     */
    public String getVersionLabel() throws RepositoryException{
        return this.state.getName();
    }

    /**
     * Get containing version history
     * @throws RepositoryException
     * @return version history associated to this version
     */
    public VersionHistory getContainingHistory() throws RepositoryException {
        return this.state.getContainingHistory();
    }

    /**
     * The original name of the node.
     */
    public String getName() {
        return this.getMetaData().getStringProperty(MetaData.NAME);
    }

    /**
     * The name of the user who created this version
     */
    public String getUserName() {
        return this.getMetaData().getStringProperty(MetaData.VERSION_USER);
    }

}
