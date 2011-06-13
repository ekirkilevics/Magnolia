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
package info.magnolia.objectfactory.pico;

import java.lang.annotation.Annotation;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.lifecycle.CompositeLifecycleStrategy;
import org.picocontainer.lifecycle.JavaEE5LifecycleStrategy;

import info.magnolia.objectfactory.AtStartup;


/**
 * Magnolia's LifecycleStrategy strategy for PicoContainer: delegates to {@link JavaEE5LifecycleStrategy} then
 * {@link StartableLifecycleStrategy}. Components will be started eagerly (i.e when their container starts) if they
 * have the @AtStartup annotation, otherwise they will be started lazily (i.e when they are first requested).
 *
 * @version $Id$
 * @see AtStartup
 * @see javax.annotation.PostConstruct
 * @see javax.annotation.PreDestroy
 * @see info.magnolia.objectfactory.Startable
 * @see info.magnolia.objectfactory.Disposable
 * @see JavaEE5LifecycleStrategy
 * @see StartableLifecycleStrategy
 */
public class PicoLifecycleStrategy extends CompositeLifecycleStrategy {

    public PicoLifecycleStrategy(ComponentMonitor componentMonitor) {
        super(
                new JavaEE5LifecycleStrategy(componentMonitor),
                new StartableLifecycleStrategy(componentMonitor)
        );
    }

    @Override
    public boolean isLazy(ComponentAdapter<?> adapter) {
        return findAnnotation(adapter.getComponentImplementation(), AtStartup.class) == null;
    }

    private static <T extends Annotation> T findAnnotation(Class<?> clazz, Class<T> annotationClass) {
        T annotation = clazz.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            return findAnnotation(superClass, annotationClass);
        }
        return null;
    }
}
