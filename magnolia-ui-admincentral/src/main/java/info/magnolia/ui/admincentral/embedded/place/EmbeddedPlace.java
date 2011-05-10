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
package info.magnolia.ui.admincentral.embedded.place;

import org.apache.commons.lang.StringUtils;

import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceTokenizer;
import info.magnolia.ui.framework.place.Prefix;


/**
 * Show a target page in the main region.
 */
@Prefix("target")
public class EmbeddedPlace extends Place {

    private String url;

    public EmbeddedPlace(String url) {
        if(StringUtils.isBlank(url)){
            throw new IllegalArgumentException("url cannot be null");
        }
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Serializes and deserializes EmbeddedPlace(s).
     *
     * @author fgrilli
     *
     */
    public static class Tokenizer implements PlaceTokenizer<EmbeddedPlace>{

        @Override
        public EmbeddedPlace getPlace(String token) {
            return new EmbeddedPlace(token);
        }

        @Override
        public String getToken(EmbeddedPlace place) {
            return place.getUrl();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EmbeddedPlace other = (EmbeddedPlace) obj;
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }
}
