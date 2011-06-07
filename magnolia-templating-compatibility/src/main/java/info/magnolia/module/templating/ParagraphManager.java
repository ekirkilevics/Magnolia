/**
 * This file Copyright (c) 2003-2011 Magnolia International
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

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.objectfactory.Components;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Manages the paragraphs on the system. Modules can register the nodes where
 * the paragraphs are defined.
 *
 * @deprecated since 5.0, use the {@link TemplateDefinitionRegistry} instead
 */
public class ParagraphManager extends ObservedManager {

    private static final String DEFAULT_PARA_TYPE = "jsp";

    /**
     * Gets the current singleton instance.
     */
    public static ParagraphManager getInstance() {
        return Components.getSingleton(ParagraphManager.class);
    }

    /**
     * Cached paragraphs.
     */
    private Map<String, Paragraph> paragraphs = new Hashtable<String, Paragraph>();

    /**
     * Returns the cached content of the requested template. TemplateInfo
     * properties :
     * <ol>
     * <li>title - title describing template</li>
     * <li>type - jsp / servlet</li>
     * <li>path - jsp / servlet path</li>
     * <li>description - description of a template</li>
     * </ol>
     *
     * @return a Paragraph instance
     */
    public Paragraph getParagraphDefinition(String key) {
        return paragraphs.get(key);
    }

    /**
     * Get a map of all registered paragraphs.
     */
    public Map<String, Paragraph> getParagraphs() {
        return paragraphs;
    }

    /**
     * Register all the paragraphs under this and subnodes.
     */
    @Override
    protected void onRegister(Content node) {
        // register a listener

        Collection<Content> paragraphNodes = node.getChildren(ItemType.CONTENTNODE);
        for (Content paragraphNode : paragraphNodes) {
            try {
                addParagraphToCache(paragraphNode);
            } catch (Exception e) {
                log.error("Can't reload the node " + paragraphNode.getUUID() + " on location: " + paragraphNode.getHandle());
            }
        }

        Collection<Content> subDefinitions = node.getChildren(ItemType.CONTENT);
        for (Content subNode : subDefinitions) {
            // do not register other observations
            onRegister(subNode);
        }
    }

    /**
     * Adds paragraph definition to ParagraphInfo cache.
     */
    protected void addParagraphToCache(Content c) {
        try {
            final Paragraph p = (Paragraph) Content2BeanUtil.toBean(c, true, Paragraph.class);
            addParagraphToCache(p);
        } catch (Content2BeanException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    /**
     * @param paragraph
     */
    public void addParagraphToCache(final Paragraph paragraph) {
        if (StringUtils.isEmpty(paragraph.getType())) {
            paragraph.setType(DEFAULT_PARA_TYPE);
        }
        if (StringUtils.isEmpty(paragraph.getDialog())) {
            paragraph.setDialog(paragraph.getName());
        }
        log.debug("Registering paragraph [{}] of type [{}]", paragraph.getName(), paragraph.getType()); //$NON-NLS-1$
        paragraphs.put(paragraph.getName(), paragraph);
    }

    @Override
    public void onClear() {
        this.paragraphs.clear();
    }

}
