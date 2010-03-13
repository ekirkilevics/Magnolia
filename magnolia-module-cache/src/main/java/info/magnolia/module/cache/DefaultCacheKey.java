/**
 * This file Copyright (c) 2010-2010 Magnolia International
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
package info.magnolia.module.cache;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author had
 * @version $Id:$
 */
public class DefaultCacheKey implements Serializable, Comparable<DefaultCacheKey> {

    // Keep the svuid fixed to prevent deserialization errors. Keep in mind that when adding new properties they will be deserialized to null!
    private static final long serialVersionUID = 2699497852929596651L;

    private String uri;
    private String serverName;
    private String locale;
    private Map<String, String> params;

    /**
     * @param key
     * @param serverName
     * @param locale
     */
    public DefaultCacheKey(String uri, String serverName, String locale, Map<String, String> params) {
        this.uri = uri;
        this.serverName = serverName;
        this.locale = locale;
        this.params = params;
    }

    @Override
    public int hashCode() {
        return (uri == null ? 13 : uri.hashCode())
            + (serverName == null ? 17 : serverName.hashCode())
            + (locale == null ? 23 : locale.hashCode())
            + (params != null ? 27 :  params.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof DefaultCacheKey)) {
            return false;
        }
        DefaultCacheKey that = (DefaultCacheKey) obj;
        return (this.uri == null) ? that.uri == null : this.uri.equals(that.uri)
            && (this.serverName == null ? that.serverName == null : this.serverName.equals(that.serverName))
            && (this.locale == null ? that.locale == null : this.locale.equals(that.locale))
            && (this.params == null ? that.params == null : this.params.equals(that.params));
    }

    public String getUri() {
        return this.uri;
    }

    public String getDomain() {
        return this.serverName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int compareTo(DefaultCacheKey o) {
        return CompareToBuilder.reflectionCompare(this, o);
    }
}
