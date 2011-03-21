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
package info.magnolia.ui.model.builder;

import info.magnolia.objectfactory.ComponentProvider;

import java.util.HashMap;
import java.util.Map;



/**
 * A base class to implement factories which instantiate implementations based on definition
 * objects.
 * @param <D> definition parent type
 * @param <I> implementation parent type
 */
public abstract class FactoryBase<D, I> {

    private ComponentProvider componentProvider;

    private Map<Class< ? extends D>, Class< ? extends I>> mapping = new HashMap<Class< ? extends D>, Class< ? extends I>>();

    public FactoryBase(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    protected void addMapping(Class< ? extends D> definitionClass, Class< ? extends I> implementationClass) {
        mapping.put(definitionClass, implementationClass);
    }

    protected I create(D definition, Object... parameters) {
        Class< ? extends I> implementationClass = null;
        final Class< ? > definitionClass = definition.getClass();
        if (mapping.containsKey(definitionClass)) {
            implementationClass = mapping.get(definitionClass);
        }
        else {
            // test if we are a sub class
            for (Class< ? extends D> keyClass : mapping.keySet()) {
                if (keyClass.isInstance(definition)) {
                    implementationClass = mapping.get(keyClass);
                    break;
                }
            }
        }
        if (implementationClass != null) {
            // TODO: check whether this is satisfying enough - check TODO in FactoryBaseTest.Impl for details.
            Object[] combinedParameters = new Object[parameters.length + 1];
            combinedParameters[0] = definition;
            for (int i = 0; i < parameters.length; i++) {
                combinedParameters[i + 1] = parameters[i];
            }
            return componentProvider.newInstance(implementationClass, combinedParameters);
        }
        return null;
    }

}
