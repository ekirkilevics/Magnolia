/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.module.templating;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.beans.config.ObservedManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public class TemplateRendererManager extends ObservedManager {

    private static final String ND_RENDERER = "renderer";

    private static final String ND_TYPE = "type";

    private Map renderers = new HashMap();

    /**
     * @return Returns the instance.
     */
    public static TemplateRendererManager getInstance() {
        return (TemplateRendererManager) FactoryUtil.getSingleton(TemplateRendererManager.class);
    }

    /**
     * @see info.magnolia.cms.beans.config.ObservedManager#onRegister(info.magnolia.cms.core.Content)
     */
    protected void onRegister(Content node) {
        Collection list = node.getChildren(ItemType.CONTENTNODE);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Content tr = (Content) iter.next();
            String type = tr.getNodeData(ND_TYPE).getString();
            String rendererClass = tr.getNodeData(ND_RENDERER).getString();

            if (StringUtils.isEmpty(type)) {
                type = tr.getName();
            }

            if (StringUtils.isBlank(type) || StringUtils.isBlank(rendererClass)) {
                log.warn("Can't register template render at {}, type=\"{}\" renderer=\"{}\"", new Object[]{tr.getHandle(), type, rendererClass});
                continue;
            }

            TemplateRenderer renderer;

            try {
                renderer = (TemplateRenderer) ClassUtil.newInstance(rendererClass);
            }
            catch (Exception e) {
                log.warn("Can't register template render at {}, type=\"{}\" renderer=\"{}\" due to a {} exception: {}", new Object[]{tr.getHandle(), type, rendererClass, e.getClass().getName(), e.getMessage()}, e);
                continue;
            }

            log.debug("Registering template render [{}] for type {}", rendererClass, type);
            registerTemplateRenderer(type, renderer);
        }

    }

    /**
     * @see info.magnolia.cms.beans.config.ObservedManager#onClear()
     */
    protected void onClear() {
        this.renderers.clear();
    }

    public void registerTemplateRenderer(String type, TemplateRenderer instance) {
        synchronized (renderers) {
            renderers.put(type, instance);
        }
    }

    public TemplateRenderer getRenderer(String type) {
        return (TemplateRenderer) renderers.get(type);
    }

}
