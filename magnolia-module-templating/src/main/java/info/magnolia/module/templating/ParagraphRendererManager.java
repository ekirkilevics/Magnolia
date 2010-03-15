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
package info.magnolia.module.templating;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.objectfactory.Classes;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.MgnlInstantiationException;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphRendererManager extends ObservedManager {

    public static ParagraphRendererManager getInstance() {
        return Components.getSingleton(ParagraphRendererManager.class);
    }

    private final Map paragraphRenderers;

    public ParagraphRendererManager() {
        paragraphRenderers = Collections.synchronizedMap(new LinkedHashMap());
    }

    public ParagraphRenderer getRenderer(String name) {
        final ParagraphRenderer renderer = (ParagraphRenderer) paragraphRenderers.get(name);
        if (renderer == null) {
            throw new IllegalArgumentException("No paragraph renderer registered with name " + name);
        }
        return renderer;
    }

    // TODO : this should allow util pages to get info about the renderer's nodes path and configuration instead of the renderers' impls.
    public Map getRenderers() {
        return paragraphRenderers;
    }

    protected void onRegister(Content node) {
        final Collection list = node.getChildren(ItemType.CONTENTNODE);
        final Iterator it = list.iterator();
        while (it.hasNext()) {
            final Content paragraphRendererNode = (Content) it.next();
            String name = paragraphRendererNode.getNodeData("name").getString();
            String clazz = paragraphRendererNode.getNodeData("class").getString();

            if (StringUtils.isEmpty(name)) {
                name = paragraphRendererNode.getName();
            }

            if (StringUtils.isBlank(name) || StringUtils.isBlank(clazz)) {
                log.warn("Can't register template renderer at {}, missing name or class property.", paragraphRendererNode.getHandle());
                continue;
            }

            try {
                final ParagraphRenderer renderer = Classes.newInstance(clazz);
                registererParagraphRenderer(name, renderer);
                log.debug("Registered template render [{}] with name {}.", clazz, name);
            } catch (MgnlInstantiationException e) {
                throw newInstanciationException(name, clazz, e);
            } catch (ClassNotFoundException e) {
                throw newInstanciationException(name, clazz, e);
            }
        }
    }

    protected void registererParagraphRenderer(String name, ParagraphRenderer renderer) {
        if (paragraphRenderers.containsKey(name)) {
            throw new IllegalStateException("Duplicate paragraph name \"" + name + "\"");
        }
        paragraphRenderers.put(name, renderer);
    }

    protected void onClear() {
        paragraphRenderers.clear();
    }

    private IllegalStateException newInstanciationException(String name, String clazz, Exception e) {
        return new IllegalStateException("Can't register paragraph renderer with name [" + name + "] and class [" + clazz + "] : " + e.getClass().getName() + " : " + e.getMessage());
    }
}
