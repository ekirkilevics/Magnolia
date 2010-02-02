/**
 * This file Copyright (c) 2003-2010 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.context.MgnlContext;
import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.Map;

/**
 * Generic observed singleton factory.
 *
 * @author philipp
 * @version $Id: $
 */
public class ObservedComponentFactory<T> implements ComponentFactory<T>, EventListener {
    private static final Logger log = LoggerFactory.getLogger(ObservedComponentFactory.class);

    private static final int DEFAULT_MAX_DELAY = 5000;
    private static final int DEFAULT_DELAY = 1000;

    /**
     * Repository name used.
     */
    private final String repository;

    /**
     * Path to the node in the config repository.
     */
    private final String path;

    /**
     * @deprecated since 4.3 - this should be private - use {@link #getComponentType()} instead.
     * (rename to "type" once made private)
     */
    protected final Class<T> interf;

    /**
     * The object delivered by this factory.
     * @deprecated since 4.3 - this should be private - use {@link #getObservedObject()} instead.
     */
    protected T observedObject;

    public ObservedComponentFactory(String repository, String path, Class<T> type) {
        this.repository = repository;
        this.path = path;
        this.interf = type;
        load();
        startObservation(path);
    }

    @SuppressWarnings("unchecked") // until commons-proxy becomes generics-aware, we have to ignore this warning
    public T newInstance() {
        if (getObservedObject() == null) {
            // TODO - replace this by a default implementation or some form of null proxy
            log.debug("An instance of {} couldn't be loaded from {}:{} yet, returning null.", new Object[]{interf, repository, path});
            return null;
        }
        
        return (T) new CglibProxyFactory().createDelegatorProxy(new ObjectProvider() {
            public Object getObject() {
                return getObservedObject();
            }
        }, new Class[]{
                // we want to expose the observed object's concrete class and interfaces so that client code can cast if they want 
                getObservedObject().getClass()
        });
    }

    protected void startObservation(String handle) {
        ObservationUtil.registerDeferredChangeListener(repository, handle, this, DEFAULT_DELAY, DEFAULT_MAX_DELAY);
    }

    public void onEvent(EventIterator events) {
        reload();
    }

    protected void reload() {
        load();
    }

    protected void load() {
        final HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(repository);
        if (hm.isExist(path)) {
            try {
                final Content node = hm.getContent(path);
                onRegister(node);
            } catch (RepositoryException e) {
                log.error("Can't read configuration for " + interf + " from [" + repository + ":" + path + "], will return null.", e);
            }
        } else {
            log.warn("Configuration for " + interf + " does not exist at [" + repository + ":" + path + "], will return null.");
        }
    }

    protected void onRegister(Content node) {
        try {
            final T instance = transformNode(node);

            if (this.observedObject != null) {
                log.info("Re-loaded {} from {}", interf.getName(), node.getHandle());
            } else {
                log.debug("Loading {} from {}", interf.getName(), node.getHandle());
            }
            this.observedObject = instance;

        } catch (Content2BeanException e) {
            log.error("Can't transform [" + repository + ":" + path + "] to " + interf, e);
        }
    }

    protected T transformNode(Content node) throws Content2BeanException {
        return (T) Content2BeanUtil.toBean(node, true, getContent2BeanTransformer());
    }

    protected Content2BeanTransformer getContent2BeanTransformer() {
        // we can not discover again the same class we are building
        return new Content2BeanTransformerImpl() {
            public Object newBeanInstance(TransformationState state, Map properties) throws Content2BeanException {
                if (state.getCurrentType().getType().equals(interf)) {
                    final ClassFactory classFactory = Classes.getClassFactory();
                    return classFactory.newInstance(interf);
                }
                return super.newBeanInstance(state, properties);
            }
        };
    }

    protected Class<T> getComponentType() {
        return interf;
    }

    /**
     * Returns the latest converted object observed by this factory.
     * Since 4.3, if you are using {@link info.magnolia.objectfactory.DefaultClassFactory}, calling this shouldn't be needed,
     * {@link #newInstance()} returned a proxy, so you'll always see this object.
     *
     * @deprecated since 4.3 - {@link info.magnolia.objectfactory.DefaultComponentProvider#newInstance(Class)} returns a proxy of the observed object instead of this factory, so this method shouldn't be needed publicly.
     */
    public T getObservedObject() {
        return this.observedObject;
    }

    public String toString() {
        return super.toString() + ":" + interf + "(Observing: " + repository + ":" + path + ")";
    }
}
