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
package info.magnolia.jaas.principal;

import info.magnolia.cms.security.auth.Entity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;


/**
 * Implementation fo the security entity.
 * @author Sameer Charles $Id$
 * @deprecated
 */
@Deprecated
public class EntityImpl implements Entity {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String DEFAULT_NAME = "person";

    private final Map properties;

    public EntityImpl() {
        this.properties = new HashMap();
    }

    /**
     * Gets name given to this principal.
     *
     * @return name
     */
    @Override
    public String getName() {
        final String name = (String) getProperty(NAME);
        if (StringUtils.isEmpty(name)) {
            return DEFAULT_NAME;
        }
        return name;
    }

    /**
     * @deprecated use addProperty(NAME, name)
     */
    @Override
    @Deprecated
    public void setName(String name) {
        addProperty(NAME, name);
    }

    @Override
    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", getName()).toString();
    }

}
