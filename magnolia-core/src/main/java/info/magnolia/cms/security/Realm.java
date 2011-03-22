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

import java.security.Principal;

/**
 * Provides the name for the default realm. A realm is a independent set of users.
 * @author philipp
 * @version $Id$
 */
public interface Realm extends Principal {

    /**
     * The realm for the admin interface.
     */
    public static final Realm REALM_ADMIN = new RealmImpl("admin");

    /**
     * No realm --> all users.
     */
    public static final Realm REALM_ALL = new RealmImpl("all");

    /**
     * Contains not removable system users: anonymous, superuser.
     */
    public static final Realm REALM_SYSTEM = new RealmImpl("system");

    /**
     * The default realm is {@link Realm#REALM_ALL}.
     */
    public static final Realm DEFAULT_REALM = REALM_ALL;

    /**
     * Implementation of the realm. Enum would be easier to read, but would not be backward compatible.
     * @author had
     * @version $Id$
     */
    class RealmImpl implements Realm {
        private final String name;
        public RealmImpl(String name) {
            if (name == null) {
                throw new NullPointerException("Realm name can't be null!");
            }
            this.name = name;
        }
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Realm) ) {
                return false;
            }
            return this.name.equals(((Realm) o).getName());
        }
    }
}
