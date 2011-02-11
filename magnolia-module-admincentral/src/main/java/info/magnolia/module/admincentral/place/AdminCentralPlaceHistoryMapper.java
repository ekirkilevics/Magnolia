/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admincentral.place;


import info.magnolia.module.vaadin.place.AbstractPlaceHistoryMapper;
import info.magnolia.module.vaadin.place.Place;
import info.magnolia.module.vaadin.place.PlaceTokenizer;

/**
 * This class is the hub of your application's navigation system. It links
 * the {@link Place}s your user navigates to
 * with the browser history system &mdash; that is, it makes the browser's back
 * and forth buttons work for you, and also makes each spot in your app
 * bookmarkable.
 * TODO: do it better. This is a first sketchy attempt to write an implementation for {@link AbstractPlaceHistoryMapper}.
 * In GWT this is usually carried out by the {@link PlaceHistoryMapperGenerator} during gwt compilation phase.
 * @author fgrilli
 *
 */
public class AdminCentralPlaceHistoryMapper extends AbstractPlaceHistoryMapper<TokenizerFactory> {
       public AdminCentralPlaceHistoryMapper() {
           setFactory(new TokenizerFactory());
       }

        @Override
        protected PrefixAndToken getPrefixAndToken(Place newPlace) {
            if(newPlace instanceof EditWorkspacePlace){
                return new PrefixAndToken("", new EditWorkspacePlace.Tokenizer().getToken((EditWorkspacePlace)newPlace));
            }
            return null;
        }

        @Override
        protected PlaceTokenizer<?> getTokenizer(String prefix) {
            return factory.getEditWorkspaceTokenizer();
        }

}

        /**
         * TODO: write javadoc.
         *
         * @author fgrilli
         *
         */
        class TokenizerFactory {
            public EditWorkspacePlace.Tokenizer getEditWorkspaceTokenizer() {
                return new EditWorkspacePlace.Tokenizer();
            }
}

