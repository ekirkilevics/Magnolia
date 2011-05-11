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
package info.magnolia.ui.admincentral.search.place;

import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.search.view.SearchParameters;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.framework.place.PlaceTokenizer;
import info.magnolia.ui.framework.place.Prefix;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * TODO write javadoc.
 * @author fgrilli
 *
 */
@Prefix("search")
public class SearchPlace extends ItemSelectedPlace {

    /**
     * Tokenizer for SearchPlace.
     * @author fgrilli
     *
     */
    public static class Tokenizer implements PlaceTokenizer<SearchPlace> {

        @Override
        public SearchPlace getPlace(String token) {
            final String[] bits = token.split(":");
            if(bits.length != 3){
                throw new IllegalArgumentException("Invalid token: " + token);
            }
            return new SearchPlace(new SearchParameters(bits[0], bits[1]));
        }

        @Override
        public String getToken(SearchPlace place) {
            return place.getWorkspace() + ":" + place.getSearchParameters().getQuery() + ":" + place.getViewType().getText();
        }
    }

    private SearchParameters searchParameters;
    private JcrView jcrView;

    public SearchPlace(SearchParameters parameters) {
        super(parameters.getWorkspace(), "/", ViewType.LIST);
        this.searchParameters = parameters;
    }

    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((searchParameters == null) ? 0 : searchParameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof SearchPlace)) {
            return false;
        }
        SearchPlace other = (SearchPlace) obj;
        if (searchParameters == null) {
            if (other.searchParameters != null) {
                return false;
            }
        } else if (!searchParameters.equals(other.searchParameters)) {
            return false;
        }
        //FIXME we need to return false here, else search is triggered only once
        return false;
    }

    public JcrView getJcrView() {
        return jcrView;
    }
    //FIXME A workaround so that in the end search activity knows which jcr view to update.
    public void setJcrView(JcrView jcrView) {
        this.jcrView = jcrView;
    }
}
