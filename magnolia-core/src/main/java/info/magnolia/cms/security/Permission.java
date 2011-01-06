/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.cms.security;

import java.io.Serializable;

import info.magnolia.cms.util.UrlPattern;


/**
 * A permission is a collection of rights and can match paths. Defines various permission constants which can be used to build right bitsets.
 */
public interface Permission extends Serializable {

    // list of permissions

    long NONE = 0;

    long ADD = 1;

    long SET = 2;

    long REMOVE = 4;

    long READ = 8;

    long EXECUTE = 16;

    long SYNDICATE = 32;

    // permission names are not localized, they are never displayed in the GUI but only used for exception messages

    String PERMISSION_NAME_ADD = "Add"; //$NON-NLS-1$

    String PERMISSION_NAME_SET = "Set"; //$NON-NLS-1$

    String PERMISSION_NAME_REMOVE = "Remove"; //$NON-NLS-1$

    String PERMISSION_NAME_READ = "Read"; //$NON-NLS-1$

    String PERMISSION_NAME_EXECUTE = "Execute"; //$NON-NLS-1$

    String PERMISSION_NAME_SYNDICATE = "Syndicate"; //$NON-NLS-1$

    String PERMISSION_NAME_ALL = "(Add, Set, Read, Execute, Syndicate)"; //$NON-NLS-1$

    String PERMISSION_NAME_WRITE = "(Add, Set, Read)"; //$NON-NLS-1$

    /**
     * All permissions combined.
     */
    long ALL = ADD | REMOVE | SET | READ | EXECUTE | SYNDICATE;

    /**
     * All write permissions.
     */
    long WRITE = ADD | SET | READ;

    void setPattern(UrlPattern value);

    UrlPattern getPattern();

    void setPermissions(long value);

    long getPermissions();

    /**
     * True if this permission matches the path.
     */
    boolean match(String path);
}
