/**
 * This file Copyright (c) 2011-2012 Magnolia International
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.inject.Inject;



/**
 * Creates objects by dynamically resolving the parameters to use.
 *
 * @version $Id$
 */
public class ObjectManufacturer {

    /**
     * Creates an object of the given type using parameters provided by a set of parameter resolvers. Will first look
     * for a constructor annotated with &#64;Inject, it can be public, private, protected or package private. It will
     * then look at public constructors and use the greediest. The greediest constructor is the one that has the most
     * number of arguments that the parameter resolvers can fulfill. If there is more than one constructor that
     * qualifies as greediest it is unspecified which one will be used.
     */
    public Object newInstance(Class<?> clazz, ParameterResolver... parameterResolvers) {

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        Constructor<?> selectedConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (selectedConstructor != null) {
                    throw new MgnlInstantiationException("Only one constructor can use @Inject [" + clazz + "]");
                }
                selectedConstructor = constructor;
            }
        }
        if (selectedConstructor != null) {
            selectedConstructor.setAccessible(true);
            Object[] parameters = resolveParameters(selectedConstructor, parameterResolvers);
            if (parameters == null) {
                throw new MgnlInstantiationException("Unable to resolve parameters for constructor " + selectedConstructor);
            }
            return newInstance(selectedConstructor, parameters);
        }

        // Find greediest satisfiable constructor
        int bestScore = -1;
        Object[] bestParameters = null;
        for (Constructor<?> constructor : constructors) {
            if (!Modifier.isPublic(constructor.getModifiers())) {
                continue;
            }
            int score = constructor.getParameterTypes().length;
            if (score < bestScore) {
                continue;
            }
            Object[] parameters = resolveParameters(constructor, parameterResolvers);
            if (parameters == null) {
                continue;
            }
            selectedConstructor = constructor;
            bestScore = score;
            bestParameters = parameters;
        }
        if (selectedConstructor != null) {
            return newInstance(selectedConstructor, bestParameters);
        }
        throw new MgnlInstantiationException("No suitable constructor found for class [" + clazz + "]");
    }

    private Object newInstance(Constructor constructor, Object[] parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException e) {
            throw new MgnlInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new MgnlInstantiationException(e);
        } catch (InvocationTargetException e) {
            throw new MgnlInstantiationException(e);
        }
    }

    private Object[] resolveParameters(Constructor<?> constructor, ParameterResolver[] parameterResolvers) {
        Object[] parameters = new Object[constructor.getParameterTypes().length];
        for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
            ParameterInfo constructorParameter = new ParameterInfo(constructor, parameterIndex);
            Object parameter = resolveParameter(constructorParameter, parameterResolvers);
            if (parameter == ParameterResolver.UNRESOLVED) {
                return null;
            }
            parameters[parameterIndex] = parameter;
        }
        return parameters;
    }

    private Object resolveParameter(ParameterInfo constructorParameter, ParameterResolver[] parameterResolvers) {
        for (ParameterResolver parameterResolver : parameterResolvers) {
            Object parameter = parameterResolver.resolveParameter(constructorParameter);
            if (parameter != ParameterResolver.UNRESOLVED) {
                return parameter;
            }
        }
        return ParameterResolver.UNRESOLVED;
    }

}
