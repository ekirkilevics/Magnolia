/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Default cache key implementation. Key is based on the URI, server name, parameters and request headers. Since the server name is likely to change from server to server, copying cached items around will most likely not help to avoid generating cache entries.
 *
 * @author had
 * @version $Id:$
 */
public class DefaultCacheKey implements Serializable {

    // Keep the svuid fixed to prevent deserialization errors. Keep in mind that when adding new properties they will be deserialized to null!
    private static final long serialVersionUID = 2699497852929596651L;

    private String uri;
    private String serverName;
    private String locale;
    private Map<String, String> params;
    private Boolean isSecure;

    /**
     * @deprecated since 4.4.6 use info.magnolia.module.cache.DefaultCacheKey.DefaultCacheKey(String, String, String, Map<String, String>, Boolean)
     */
    @Deprecated
    public DefaultCacheKey(String uri, String serverName, String locale, Map<String, String> params) {
        this.uri = uri;
        this.serverName = serverName;
        this.locale = locale;
        this.params = params == null ? null : Collections.unmodifiableMap(params);
        this.isSecure = false;
    }
    
    public DefaultCacheKey(String uri, String serverName, String locale, Map<String, String> params, Boolean isSecure){
        this.uri = uri;
        this.serverName = serverName;
        this.locale = locale;
        this.params = params == null ? null : Collections.unmodifiableMap(params);
        this.isSecure = isSecure;
    }

    @Override
    public int hashCode() {
        return (uri == null ? 13 : uri.hashCode())
            + (serverName == null ? 17 : serverName.hashCode())
            + (locale == null ? 23 : locale.hashCode())
            + (params == null ? 29 :  params.hashCode())
            + (isSecure == null ? 31 : isSecure.hashCode());
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
            && (this.params == null ? that.params == null : this.params.equals(that.params))
            && (this.isSecure == null ? that.isSecure == null : this.isSecure.equals(that.isSecure));
    }

    public String getUri() {
        return this.uri;
    }

    public String getDomain() {
        return this.serverName;
    }
    
    public String getLocale() {
        return this.locale;
    }
    
    public Map<String, String> getParams() {
        return params;
    }
    
    public Boolean getIsSecured(){
        return this.isSecure;
    }

    // generated toString() method
    @Override
    public String toString() {
        return "DefaultCacheKey{" +
                "uri='" + uri + '\'' +
                ", serverName='" + serverName + '\'' +
                ", locale='" + locale + '\'' +
                ", params=" + params + '\'' +
                ", secure='" + isSecure + "'" +
                '}';
    }

}
