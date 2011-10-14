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
package info.magnolia.module;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.ObservationUtil;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;

/**
 * Provider that creates the module class and populates it from repository configuration and updates it if
 * the configuration changes.
 *
 * @param <T> the type of the module class
 * @version $Id$
 */
public class ModuleInstanceProvider<T> extends AbstractModuleLifecycleListener implements Provider<T> {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModuleInstanceProvider.class);

    private static final int DEFAULT_MODULE_OBSERVATION_DELAY = 5000;
    private static final int DEFAULT_MODULE_OBSERVATION_MAX_DELAY = 30000;

    @Inject
    private ComponentProvider componentProvider;
    private ModuleManagerImpl moduleManager;

    @Inject
    private void initialize(ModuleManager moduleManager) {
        this.moduleManager = (ModuleManagerImpl) moduleManager;
        this.moduleManager.addListener(moduleName, this);
    }

    private final String moduleName;
    private final String path;
    private T instance;

    public ModuleInstanceProvider(String moduleName) {
        this.moduleName = moduleName;
        this.path = "/modules/" + moduleName + "/config";
    }

    @Override
    public synchronized T get() {

        if (instance != null) {
            return instance;
        }

        Class<?> moduleClass = moduleManager.getModuleClass(moduleName);

        instance = (T) componentProvider.newInstance(moduleClass);

        populateModuleInstance();

        return instance;
    }

    @Override
    public void onModuleStarted(final ModuleDefinition moduleDefinition, Object moduleInstance) {
        ObservationUtil.registerDeferredChangeListener(ContentRepository.CONFIG, path, new EventListener() {

            @Override
            public void onEvent(EventIterator events) {
                synchronized (ModuleInstanceProvider.this) {
                    MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {
                        @Override
                        public void doExec() {
                            moduleManager.restartModule(moduleDefinition.getName(), new ModuleRestartOperation() {

                                @Override
                                public void exec(ModuleDefinition moduleDefinition, Object moduleInstance) {
                                    populateModuleInstance();
                                }
                            });
                        }
                    }, true);
                }
            }
        }, DEFAULT_MODULE_OBSERVATION_DELAY, DEFAULT_MODULE_OBSERVATION_MAX_DELAY);

        moduleManager.removeListener(this);
    }

    protected void populateModuleInstance() {

        Node node;
        try {
            Session session = MgnlContext.getJCRSession(ContentRepository.CONFIG);
            if (!session.nodeExists(path)) {
                return;
            }
            node = session.getNode(path);
        } catch (RepositoryException e) {
            log.error("Can't read {}, can not populate {}.", new Object[]{path, instance, e});
            return;
        }

        try {
            Content2BeanUtil.setProperties(instance, ContentUtil.asContent(node), true);
        } catch (Content2BeanException e) {
            log.error("Can't read {}, can not populate {}.", new Object[]{path, instance, e});
        } catch (Throwable e) {
            log.error("Can't initialize module " + instance + ": " + e.getMessage(), e);
        }
    }
}
