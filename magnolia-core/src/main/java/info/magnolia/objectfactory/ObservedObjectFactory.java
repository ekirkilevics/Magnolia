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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanTransformer;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.Map;

/**
 * Generic observerd singleton factory.
 *
 * @author philipp
 * @version $Id: FactoryUtil.java 25238 2009-05-25 09:48:34Z pbaerfuss $
 */
public class ObservedObjectFactory implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(ObservedObjectFactory.class);

    private static final int DEFAULT_MAX_DELAY = 5000;
    private static final int DEFAULT_DELAY = 1000;

    /**
     * Path to the node in the config repository.
     */
    private final String path;

    /**
     * Repository name used.
     */
    private final String repository;

    protected final Class interf;

    /**
     * The object delivered by this factory.
     */
    protected Object observedObject;

    public ObservedObjectFactory(String repository, String path, Class interf) {
        this.path = path;
        this.repository = repository;
        this.interf = interf;
        load();
        startObservation(path);
    }

    protected void startObservation(String handle) {
        ObservationUtil.registerDeferredChangeListener(ContentRepository.CONFIG, handle, this, DEFAULT_DELAY, DEFAULT_MAX_DELAY);
    }

    public void onEvent(EventIterator events) {
        reload();
    }

    protected void reload() {
        load();
    }

    protected void load() {
        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(repository);
        if (hm.isExist(path)) {
            Content node;
            try {
                node = hm.getContent(path);
                onRegister(node);
            }
            catch (RepositoryException e) {
                log.error("Can't read configuration for object " + interf + " from [" + repository + ":" + path + "]", e);
            }
        } else {
            log.warn("Can't read configuration for object " + interf + " from [" + repository + ":" + path + "]");
        }
    }

    protected void onRegister(Content node) {
        try {
            Object instance = transformNode(node);

            if (this.observedObject != null) {
                log.info("Loaded {} from {}", interf.getName(), node.getHandle());
            }
            this.observedObject = instance;
        }
        catch (Exception e) {
            log.error("Can't instantiate object from [" + repository + ":" + path + "]", e);
        }
    }

    protected Object transformNode(Content node) throws Content2BeanException {
        return Content2BeanUtil.toBean(node, true, getContent2BeanTransformer());
    }

    protected Content2BeanTransformer getContent2BeanTransformer() {
        // we can not discover again the same class we are building
        return new Content2BeanTransformerImpl() {
            public Object newBeanInstance(TransformationState state, Map properties) throws Content2BeanException {
                if (state.getCurrentType().getType().equals(interf)) {
                    return FactoryUtil.newInstanceWithoutDiscovery(interf.getName());
                }
                return super.newBeanInstance(state, properties);
            }
        };
    }

    /**
     * Returns the object observed by this factory. Not synchronized.
     */
    public Object getObservedObject() {
        return this.observedObject;
    }

    public String toString() {
        return super.toString() + ":" + interf + "(" + repository + ":" + path + ")";
    }
}
