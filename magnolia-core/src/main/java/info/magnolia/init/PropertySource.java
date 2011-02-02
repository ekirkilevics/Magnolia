/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.init;

import java.util.Set;

/**
 * Basic Magnolia configuration typically happens with a set of files typically called "magnolia.properties",
 * loaded in a given order, from different location. This class derives its name from that, but properties
 * can be loaded from different types of sources. (module descriptors, ...)
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface PropertySource {

    // TODO - do we need this for anything else than info.magnolia.init.MagnoliaServletContextListener.populateMainContainer() ?
    Set<String> getKeys();

    /**
     * Returns a value corresponding to the given key, or null if not known.
     */
    String getProperty(String key);

    boolean hasProperty(String key);

    /**
     * Provides a description of this source, typically the location of the file
     * that was used to load the properties this source holds.
     * Useful for debugging / logging.
     */
    String describe();

}
