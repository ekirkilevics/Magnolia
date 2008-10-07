/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.filters.VirtualUriFilter;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.TransformationState;
import info.magnolia.content2bean.TypeDescriptor;
import info.magnolia.content2bean.impl.Content2BeanTransformerImpl;

public class FreemarkerTemplateLoaderManager extends ObservedManager {

    private static final Logger log = LoggerFactory.getLogger(FreemarkerTemplateLoaderManager.class);

    /**
     * All cached data.
     */
    private final List cachedTemplateLoaders = new ArrayList();
    private final ClassTemplateLoader classloaderTL = new ClassTemplateLoader(FreemarkerUtil.class, "/");
    private MultiTemplateLoader templateLoaders;
    
    public FreemarkerTemplateLoaderManager() {
        // add the default - make sure there is at least one loader no matter how screwed configuration is
        cachedTemplateLoaders.add(classloaderTL);
    }

    protected void onRegister(Content node) {
        try {
            log.info("Config : Loading TemplateLoaders - " + node.getHandle()); //$NON-NLS-1$
            Content2BeanUtil.setProperties(this.cachedTemplateLoaders, node, true, new Content2BeanTransformerImpl(){
                protected TypeDescriptor onResolveType(TransformationState state, TypeDescriptor resolvedType) {
                    if(state.getLevel()==2 && resolvedType == null){
                        return this.getTypeMapping().getTypeDescriptor(TemplateLoader.class);
                    }
                    return resolvedType;
                }
            });
            log.info("Config : TemplateLoaders loaded - " + node.getHandle()); //$NON-NLS-1$
        }
        catch (Exception e) {
            log.error("Config : Failed to load TemplateLoader - " + node.getHandle() + " - " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // move clTL to the end of the list
        cachedTemplateLoaders.remove(classloaderTL);
        cachedTemplateLoaders.add(classloaderTL);
    }

    protected void onClear() {
        this.cachedTemplateLoaders.clear();
        cachedTemplateLoaders.add(classloaderTL);
        templateLoaders = null;
    }

    TemplateLoader getMultiTemplateLoader() {
        if (templateLoaders == null) {
            templateLoaders = new MultiTemplateLoader((TemplateLoader[]) cachedTemplateLoaders.toArray(new TemplateLoader[cachedTemplateLoaders.size()]));
        }
        return templateLoaders;
    }

    /**
     * @return Returns the instance.
     */
    public static FreemarkerTemplateLoaderManager getInstance() {
        return (FreemarkerTemplateLoaderManager) FactoryUtil.getSingleton(FreemarkerTemplateLoaderManager.class);
    }

    public void addLoader(TemplateLoader loader) {
        // keep clTL always last;
        this.cachedTemplateLoaders.add(cachedTemplateLoaders.size() - 1, loader);
        this.templateLoaders = null;
    }
}
