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
package info.magnolia.objectfactory.guice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import javax.inject.Inject;

import info.magnolia.objectfactory.MgnlInstantiationException;


/**
 * Creates objects by dynamically resolving the parameters to use.
 *
 * @version $Id$
 */
public class ObjectManufacturer {

    /**
     * Holds details about a constructors parameter.
     */
    public class ConstructorParameter {

        private Constructor constructor;
        private int parameterIndex;
        private Class<?> parameterType;
        private Type genericParameterType;

        public ConstructorParameter(Constructor constructor, int parameterIndex) {
            this.constructor = constructor;
            this.parameterIndex = parameterIndex;
            this.parameterType = constructor.getParameterTypes()[parameterIndex];
            this.genericParameterType = constructor.getGenericParameterTypes()[parameterIndex];
        }

        public Constructor getConstructor() {
            return constructor;
        }

        public int getParameterIndex() {
            return parameterIndex;
        }

        public Class<?> getParameterType() {
            return parameterType;
        }

        public Type getGenericParameterType() {
            return genericParameterType;
        }
    }

    /**
     * Used to resolve a parameter when invoking a constructor.
     */
    public interface ParameterResolver {

        public static final Object UNRESOLVED = new Object();

        /**
         * Returns the instance to use for the parameter or <code>UNRESOLVED</code> if it cannot provider a value for
         * this parameter.
         */
        Object resolveParameter(ConstructorParameter constructorParameter);
    }

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

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {

            ConstructorParameter constructorParameter = new ConstructorParameter(constructor, parameterIndex);

            Object parameter = ParameterResolver.UNRESOLVED;

            for (ParameterResolver parameterResolver : parameterResolvers) {
                parameter = parameterResolver.resolveParameter(constructorParameter);
                if (parameter != ParameterResolver.UNRESOLVED) {
                    break;
                }
            }

            if (parameter == ParameterResolver.UNRESOLVED) {
                return null;
            }

            parameters[parameterIndex] = parameter;
        }
        return parameters;
    }

}
