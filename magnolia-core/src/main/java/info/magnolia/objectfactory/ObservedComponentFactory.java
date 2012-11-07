/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
import info.magnolia.cms.security.SilentSessionOp;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.factory.cglib.CglibProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic observed singleton factory.
 * @param <T> the type of component this factory instantiates.
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
    @Deprecated
    protected final Class<T> interf;

    /**
     * The object delivered by this factory.
     * @deprecated since 4.3 - this should be private - use {@link #getObservedObject()} instead.
     */
    @Deprecated
    protected T observedObject;

    private ComponentProvider componentProvider;

    public ObservedComponentFactory(String repository, String path, Class<T> type) {
        this(repository, path, type, Components.getComponentProvider());
    }

    public ObservedComponentFactory(String repository, String path, Class<T> type, ComponentProvider componentProvider) {
        this.repository = repository;
        this.path = path;
        this.interf = type;
        this.componentProvider = componentProvider;
        load();
        startObservation(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    // until commons-proxy becomes generics-aware, we have to ignore this warning
    public T newInstance() {
        if (getObservedObject() == null) {
            // TODO - replace this by a default implementation or some form of null proxy
            // this only happens if load() did not set observedObject
            log.warn("An instance of {} couldn't be loaded from {}:{} yet, returning null.", new Object[]{interf, repository, path});
            return null;
        }

        return (T) new CglibProxyFactory().createDelegatorProxy(new ObjectProvider() {
            @Override
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

    @Override
    public void onEvent(EventIterator events) {
        reload();
    }

    protected void reload() {
        load();
    }

    protected void load() {
        MgnlContext.doInSystemContext(new SilentSessionOp<Void>(repository) {

            @Override
            public Void doExec(Session session) throws RepositoryException {
                session = MgnlContext.getJCRSession(session.getWorkspace().getName());
                if (session.nodeExists(path)) {
                    try {
                        // TODO: change this once c2b is n2b ...
                        final Node node = session.getNode(path);
                        onRegister(ContentUtil.asContent(node));
                    } catch (RepositoryException e) {
                        log.error("Can't read configuration for " + interf + " from [" + repository + ":" + path + "], will return null.", e);
                    }
                } else {
                    log.debug("{} does not exist, will return a default implementation for {}.", path, interf);
                    instantiateDefault();
                }
                return null;
            }

            @Override
            public String toString() {
                return " load repository [" + repository + "] path [" + path + "].";
            }
        });
    }

    protected void instantiateDefault() {
        if (Classes.isConcrete(interf)) {
            log.info("{} does not exist, will return a new instance of {}.", path, interf);
            final ClassFactory classFactory = Classes.getClassFactory();
            this.observedObject = classFactory.newInstance(interf);
        } else {
            log.warn("{} does not exist, default implementation for {} is unknown, will return null.", path, interf);
        }
    }

    /**
     * @deprecated since 4.5, use {@link Classes#isConcrete(Class)}
     */
    @Deprecated
    protected boolean isConcrete(Class<?> clazz) {
        return Classes.isConcrete(clazz);
    }

    /**
     * @deprecated since 4.5, use {@link #onRegister(Node)} instead
     */
    @Deprecated
    protected void onRegister(Content node) {
        try {
            Node n = node.getJCRNode();
            final T instance = transformNode(n);

            if (this.observedObject != null) {
                log.info("Re-loaded {} from {}", interf.getName(), node.getHandle());
            } else {
                log.debug("Loading {} from {}", interf.getName(), node.getHandle());
            }
            this.observedObject = instance;

        } catch (Exception e) {
            log.error("Can't transform [" + repository + ":" + path + "] to " + interf, e);
        }
    }

    protected T transformNode(Node node) throws Node2BeanException, RepositoryException {
        return (T) Components.getComponent(Node2BeanProcessor.class).toBean(node, true, getNode2BeanTransformer(), componentProvider);
    }

    protected Node2BeanTransformer getNode2BeanTransformer() {
        return new Node2BeanTransformerImpl();
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
    @Deprecated
    public T getObservedObject() {
        return this.observedObject;
    }

    @Override
    public String toString() {
        return super.toString() + ":" + interf + "(Observing: " + repository + ":" + path + ")";
    }
}
