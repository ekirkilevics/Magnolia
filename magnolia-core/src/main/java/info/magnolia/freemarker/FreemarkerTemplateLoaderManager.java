/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import info.magnolia.cms.util.FactoryUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Observed manager handling Freemarker template loaders.
 * 
 */
public class FreemarkerTemplateLoaderManager {
    public static FreemarkerTemplateLoaderManager getInstance() {
        return (FreemarkerTemplateLoaderManager) FactoryUtil.getSingleton(FreemarkerTemplateLoaderManager.class);
    }

    private MultiTemplateLoader multiTL;
    private List templateLoaders;

    public FreemarkerTemplateLoaderManager() {
        templateLoaders = new ArrayList();
    }

    TemplateLoader getMultiTemplateLoader() {
        if (multiTL == null) {
            // add a ClassTemplateLoader as our last loader
            final int s = templateLoaders.size();
            final TemplateLoader[] tl = (TemplateLoader[]) templateLoaders.toArray(new TemplateLoader[s + 1]);
            tl[s] = new ClassTemplateLoader(getClass(), "/");
            multiTL = new MultiTemplateLoader(tl);
        }
        return multiTL;
    }

    public List getTemplateLoaders() {
        return templateLoaders;
    }

    public void setTemplateLoaders(List templateLoaders) {
        this.templateLoaders = templateLoaders;
    }

    public void addTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoaders.add(templateLoader);
    }

}
