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

import info.magnolia.cms.util.FactoryUtil;


/**
 * @author Sameer Charles
 * $Id$
 */
public class VersionConfig {

    /**
     * maximum index to keep
     */
    public static final String MAX_VERSION_INDEX = "maxVersionIndex";

    /**
     * is versioning is active (at application level, JCR is always configured to version if implemented)
     */
    public static final String ACTIVE = "active";

    /**
     * maximum number of version index
     */
    private static long maxVersions = 2; // default is 3 which could be overwritten through config

    public VersionConfig() {
        // do not instantiate
    }

    /**
     * get instance
     */
    public static VersionConfig getInstance() {
        return (VersionConfig) FactoryUtil.getSingleton(VersionConfig.class);
    }

    /**
     * Initialize bean
     */
    public void init() {
        load();
    }

    /**
     * Its a fixed config bean
     */
    public void load() {
        // nothing to do
    }

    /**
     * Read config and reload bean
     */
    public void reload() {
        load();
    }

    /**
     * Checks active flag in version config
     * @return true if versioning is active at application level
     */
    public boolean isActive() {
        return true;
    }

    /**
     * Get maximum number of versions allowed in version history
     * @return max version index
     */
    public long getMaxVersionAllowed() {
        return maxVersions;
    }

}
