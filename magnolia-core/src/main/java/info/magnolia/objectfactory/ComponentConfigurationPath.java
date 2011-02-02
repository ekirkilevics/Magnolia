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
package info.magnolia.objectfactory;

import info.magnolia.cms.beans.config.ContentRepository;
import org.apache.commons.lang.StringUtils;

/**
 * A string-based representation of a node path, in the form of <tt>repository:/path/to/node</tt>,
 * or <tt>/path/to/node</tt> (where the repository defaults to "config").
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ComponentConfigurationPath {

    public static boolean isComponentConfigurationPath(String s) {
        return s.startsWith("/") || s.indexOf(":/") >= 0;
    }

    private final String repository;
    private final String path;

    public ComponentConfigurationPath(String value) {
        if (value.indexOf(':') >= 0) {
            this.repository = StringUtils.substringBefore(value, ":");
            this.path = StringUtils.substringAfter(value, ":");
        } else {
            this.repository = ContentRepository.CONFIG;
            this.path = value;
        }
    }

    public String getRepository() {
        return repository;
    }

    public String getPath() {
        return path;
    }

}
