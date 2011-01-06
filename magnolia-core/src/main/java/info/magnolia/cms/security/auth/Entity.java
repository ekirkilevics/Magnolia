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
package info.magnolia.cms.security.auth;

import java.io.Serializable;
import java.security.Principal;


/**
 * A user entity. Defines some standard properties as user name or language.
 * @author Sameer Charles $Id$
 */
public interface Entity extends Principal, Serializable {

    public static final String FULL_NAME = "fullName";

    public static final String NAME = "name";

    public static final String EMAIL = "email";

    public static final String LANGUAGE = "language";

    public static final String LOCALE = "locale";

    public static final String ADDRESS_LINE = "address";

    public static final String PASSWORD = "password";

    /**
     * @return the name of the entity, or a default value if no name was set.
     */
    public String getName();

    /**
     * @deprecated not used - use addProperty(Entity.NAME)
     */
    public void setName(String name);

    public void addProperty(String key, Object value);

    /**
     * @return the property's value, or null if undefined
     */
    public Object getProperty(String key);
}
