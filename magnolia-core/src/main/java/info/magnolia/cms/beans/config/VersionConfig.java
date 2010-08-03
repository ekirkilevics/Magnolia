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
package info.magnolia.cms.beans.config;

import info.magnolia.objectfactory.Components;


/**
 * @author Sameer Charles
 * $Id$
 */
public class VersionConfig {

    /**
     * maximum index to keep.
     */
    public static final String MAX_VERSION_INDEX = "maxVersionIndex";

    /**
     * is versioning is active. (at application level, JCR is always configured to version if implemented)
     */
    public static final String ACTIVE = "active";

    /**
     * maximum number of version index.
     */
    private static long maxVersions = 2; // default is 3 which could be overwritten through config

    public VersionConfig() {
        // do not instantiate
    }

    public static VersionConfig getInstance() {
        return Components.getSingleton(VersionConfig.class);
    }

    /**
     * Initialize bean.
     */
    public void init() {
        load();
    }

    /**
     * Its a fixed config bean.
     */
    public void load() {
        // nothing to do
    }

    /**
     * Read config and reload bean.
     */
    public void reload() {
        load();
    }

    /**
     * Checks active flag in version config.
     * @return true if versioning is active at application level
     */
    public boolean isActive() {
        return true;
    }

    /**
     * Get maximum number of versions allowed in version history.
     * @return max version index
     */
    public long getMaxVersionAllowed() {
        return maxVersions;
    }

}
