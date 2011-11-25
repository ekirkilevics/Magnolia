/**
 * This file Copyright (c) 2008-2011 Magnolia International
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
package info.magnolia.beanmerger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;

import net.sf.cglib.proxy.Enhancer;


/**
 * Proxy-based bean merger.
 *
 * @version $Id$
 */
public class ProxyBasedBeanMerger extends BeanMergerBase {

    @Override
    protected Object mergeBean(List sources) {
        Set<Class> types = new HashSet<Class>();
        Class< ? > mostSpecificAssignableClass = sources.get(0).getClass();

        for (Object source : sources) {

            // Skip up the class hierarchy to avoid proxying cglib proxy classes
            Class clazz = source.getClass();
            while (Enhancer.isEnhanced(clazz)) {
                clazz = clazz.getSuperclass();
            }

            Collections.addAll(types, clazz.getInterfaces());

            if (mostSpecificAssignableClass.isAssignableFrom(clazz)) {
                mostSpecificAssignableClass = clazz;
            }
        }
        types.add(mostSpecificAssignableClass);

        return new CglibProxyFactory().createInvokerProxy(newInvoker(sources), types.toArray(new Class< ? >[types.size()]));
    }

    protected MergeInvoker newInvoker(List sources) {
        return new MergeInvoker(this, sources);
    }

    /**
     * Merge Invoker.
     *
     * @version $Id$
     */
    protected static final class MergeInvoker implements Invoker {

        private final List sources;

        private BeanMerger merger;

        private MergeInvoker(BeanMerger merger, List sources) {
            this.merger = merger;
            this.sources = sources;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            // only merge calls to property getters
            if (arguments.length > 0) {
                return method.invoke(sources.get(0), arguments);
            }

            List values = new ArrayList();
            for (Object obj : sources) {
                values.add(evaluate(obj, method, arguments));
            }

            return merger.merge(values);
        }

        private Object evaluate(Object obj, Method method, Object[] arguments) throws IllegalAccessException, InvocationTargetException {

            // direct method call
            if (method.getDeclaringClass().isAssignableFrom(obj.getClass())) {
                return method.invoke(obj, arguments);
            }

            // ducking
            try {
                return MethodUtils.invokeMethod(obj, method.getName(), arguments);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
    }

}
