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

import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceTokenizer;


/**
 * Edit a workspace's content.
 */
public class EditWorkspacePlace extends Place {

    /**
     * Serializes and deserializes EditWorkspacePlace(s).
     *
     * @author fgrilli
     *
     */
    public static class Tokenizer implements PlaceTokenizer<EditWorkspacePlace> {
        static final String SEPARATOR = ";";

        public EditWorkspacePlace getPlace(String token) {
            String bits[] = token.split(SEPARATOR);

            if (bits.length == 1) {
                return new EditWorkspacePlace(token, null);
            }
            if (bits.length == 2) {
                return new EditWorkspacePlace(bits[0], bits[1]);
            }
            return new EditWorkspacePlace(token, null);
        }

        public String getToken(EditWorkspacePlace place) {
            if (place.getPath() != null)
                return place.getWorkspace() + SEPARATOR + place.getPath();
            return place.getWorkspace();
        }
    }

    private String workspace;

    private String path;

    public EditWorkspacePlace(String workspace, String path) {
        this.workspace = workspace;
        this.path = path;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((workspace == null) ? 0 : workspace.hashCode());
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
        return true;
    }
}
