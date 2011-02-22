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
package info.magnolia.module.admincentral.place;

import org.apache.commons.lang.StringUtils;

import info.magnolia.module.vaadin.place.CompositePlace;
import info.magnolia.module.vaadin.place.Prefix;


/**
 * Edit a workspace's content.
 */
@Prefix("wks")
public class EditWorkspacePlace extends CompositePlace {

    /**
     * Serializes and deserializes EditWorkspacePlace(s).
     *
     * @author fgrilli
     *
     */
    public static class Tokenizer extends CompositePlace.Tokenizer {

        private static final String SEPARATOR = ";";

        public EditWorkspacePlace getPlace(String token) {
            final String[] bits = token.split(SEPARATOR);
            if(bits.length == 2) {
                EditWorkspacePlace place = new EditWorkspacePlace(bits[0]);
                //FIXME get the Region id
                place.setSubPlace("edit-workspace", new ItemSelectedPlace(bits[0],bits[1]));
                return place;
            }
            return new EditWorkspacePlace(token);
        }

        public String getToken(EditWorkspacePlace place) {
            final String superClassToken = super.getToken(place);
            if(StringUtils.isNotBlank(superClassToken)) {
                return place.getWorkspace() + SEPARATOR + superClassToken;
            }
            return place.getWorkspace() + SEPARATOR +"/";
        }
    }

    private String workspace;

    public EditWorkspacePlace(String workspace) {
        this.workspace = workspace;
    }

    public String getWorkspace() {
        return workspace;
    }

    public int hashCode() {
        return workspace.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EditWorkspacePlace)) {
            return false;
        }
        EditWorkspacePlace other = (EditWorkspacePlace) obj;

        if (workspace == null) {
            if (other.workspace != null) {
                return false;
            }
        } else if (!workspace.equals(other.workspace)) {
            return false;
        }
        return super.equals(obj);
    }
}
