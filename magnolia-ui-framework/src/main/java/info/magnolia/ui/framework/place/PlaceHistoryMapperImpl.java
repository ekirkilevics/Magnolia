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
package info.magnolia.ui.framework.place;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is the hub of the application's navigation system. It links
 * the {@link Place}s a user navigates to with the browser history system &mdash; that is, it makes the browser's back
 * and forth buttons work, and also makes each spot in the app bookmarkable.
 * @author fgrilli
 *
 */
public class PlaceHistoryMapperImpl extends AbstractPlaceHistoryMapper {
    private static final Logger log = LoggerFactory.getLogger(PlaceHistoryMapperImpl.class);

    private Map<String, PlaceTokenizer<Place>> tokenizers = new HashMap<String, PlaceTokenizer<Place>>();

    public PlaceHistoryMapperImpl(Class<? extends Place>... places) {
        registerTokenizers(places);
    }

    @SuppressWarnings("unchecked")
    private void registerTokenizers(Class<? extends Place>... places) {
        log.debug("Starting registering tokenizers for places...");
        for(Class<? extends Place> clazz: places){
            Prefix prefix = clazz.getAnnotation(Prefix.class);
            if(prefix == null){
                log.info("No @Prefix annotation found for place {}. Skipping it...", clazz.getName());
                continue;
            }

            boolean foundTokenizer = false;
            final Class<?>[] declaredClasses = clazz.getDeclaredClasses();

            for(Class<?> declaredClass : declaredClasses){
                if(PlaceTokenizer.class.isAssignableFrom(declaredClass)){
                    try {
                        final PlaceTokenizer<Place> tokenizer = (PlaceTokenizer<Place>) declaredClass.newInstance();
                        tokenizers.put(prefix.value(), tokenizer);
                        log.debug("Added tokenizer for place {}", clazz.getName());
                        foundTokenizer = true;
                    } catch (InstantiationException e) {
                        throw new IllegalStateException(e);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            if(!foundTokenizer){
                log.warn("A @Prefix annotation was detected for {} but a PlaceTokenizer implementation was not found.", clazz.getName());
            }
        }
    }

    @Override
    protected PrefixAndToken getPrefixAndToken(Place newPlace) {
        final String prefix = newPlace.getPrefixValue();
        if(prefix != null){
            return new PrefixAndToken(prefix, tokenizers.get(prefix).getToken(newPlace));
        }
        return null;
    }

    @Override
    protected PlaceTokenizer<?> getTokenizer(String prefix) {
        return tokenizers.get(prefix);
    }
}
