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
package info.magnolia.objectfactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Provider;

/**
 * Parameter resolver which looks for suitable parameters in a set of candidates. Cannot handle null values since its
 * unable to match them to a type. Will create javax.inject.Provider instances that match the parameter's type.
 *
 * @version $Id$
 */
public class CandidateParameterResolver implements ParameterResolver {

    private final Object[] candidates;

    public CandidateParameterResolver(Object[] candidates) {
        this.candidates = candidates;
    }

    @Override
    public Object resolveParameter(ParameterInfo parameter) {

        Type genericParameterType = parameter.getGenericParameterType();
        Class<?> parameterType = parameter.getParameterType();

        // If the parameter is javax.inject.Provider<T> we will look for T instead and return a provider for it
        if (genericParameterType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
            if (parameterizedType.getRawType() == javax.inject.Provider.class) {
                Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                for (final Object extraCandidate : candidates) {
                    if (actualType.isAssignableFrom(extraCandidate.getClass())) {
                        return new Provider() {
                            @Override
                            public Object get() {
                                return extraCandidate;
                            }
                        };
                    }
                }
                return UNRESOLVED;
            }
        }
        for (Object extraCandidate : candidates) {
            if (parameterType.isAssignableFrom(extraCandidate.getClass())) {
                return extraCandidate;
            }
        }
        return UNRESOLVED;
    }
}
