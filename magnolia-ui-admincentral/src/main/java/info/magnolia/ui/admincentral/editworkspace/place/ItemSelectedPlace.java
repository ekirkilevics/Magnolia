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
package info.magnolia.ui.admincentral.editworkspace.place;

import org.apache.commons.lang.builder.ToStringBuilder;

import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceTokenizer;
import info.magnolia.ui.framework.place.Prefix;

/**
 * A sub-place of {@link EditWorkspacePlace} if an item got selected.
 */
@Prefix("item-selected")
public class ItemSelectedPlace extends Place {
    /**
     * Tokenizer for ItemSelectedPlace.
     * @author fgrilli
     *
     */
    public static class Tokenizer implements PlaceTokenizer<ItemSelectedPlace> {

        public ItemSelectedPlace getPlace(String token) {
            final String[] bits = token.split(":");
            if(bits.length != 2){
                throw new IllegalArgumentException("Invalid token: " + token);
            }
            return new ItemSelectedPlace(bits[0], bits[1]);
        }

        public String getToken(ItemSelectedPlace place) {
            return place.getWorkspace() + ":" + place.getPath();
        }
    }

    private String workspace;

    private String path;

    public ItemSelectedPlace(String workspace, String path) {
        this.workspace = workspace;
        this.path = path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result
                + ((workspace == null) ? 0 : workspace.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemSelectedPlace other = (ItemSelectedPlace) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (workspace == null) {
            if (other.workspace != null)
                return false;
        } else if (!workspace.equals(other.workspace))
            return false;
        return true;
    }


    public String getWorkspace() {
        return workspace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }


}
