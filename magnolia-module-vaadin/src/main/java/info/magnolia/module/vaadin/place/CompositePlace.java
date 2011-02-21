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
package info.magnolia.module.vaadin.place;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * A {@link Place} which is is build of several sub place. Used in combination of sub MVP containers
 * build by {@link info.magnolia.module.vaadin.activity.MVPSubContainerActivity}.
 */
public class CompositePlace extends Place implements Cloneable{
    /**
     * Composite places can extend this tokenizer and delegate to it the correct construction of tokens for sub places.
     * @author fgrilli
     *
     */
    public static class Tokenizer implements PlaceTokenizer<CompositePlace> {
        private String token;

        public CompositePlace getPlace(String token) {
            //FIXME is this correct?
            return new CompositePlace();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public String getToken(CompositePlace place) {
            if(token != null){
                return token;
            }
            final StringBuilder sb = new StringBuilder();
            for(Entry<String, Place> subPlace: place.subPlaces.entrySet()){
                final Class<?>[] declaredClasses = subPlace.getValue().getClass().getDeclaredClasses();
                for(Class<?> clazz : declaredClasses){
                    if(PlaceTokenizer.class.isAssignableFrom(clazz)){
                        try {
                            final PlaceTokenizer tokenizer = (PlaceTokenizer) clazz.newInstance();
                            sb.append(tokenizer.getToken(subPlace.getValue()));
                        } catch (InstantiationException e) {
                            throw new IllegalStateException(e);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            }
            token = sb.toString();
            return token;
        }
    }


    private Map<String, Place> subPlaces = new HashMap<String, Place>();

    public Place getSubPlace(String regionId) {
        return subPlaces.get(regionId);
    }

    public void setSubPlace(String regionId, Place newPlace) {
        subPlaces.put(regionId, newPlace);
    }

    @Override
    public boolean equals(Object obj) {
        return subPlaces.equals(((CompositePlace)obj).subPlaces);
    }

    @Override
    public Object clone() {
        try {
            CompositePlace clone = (CompositePlace)super.clone();
            clone.subPlaces = new HashMap(subPlaces);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
