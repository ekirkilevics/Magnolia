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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.content2bean.Content2BeanException;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the paragraphs on the system. Modules can register the nodes where the paragraph are defined.
 * @author philipp
 */
public class ParagraphManager extends ObservedManager {

    private static final Logger log = LoggerFactory.getLogger(Paragraph.class);

    private static final String DEFAULT_PARA_TYPE = "jsp";

    /**
     * Gets the current singleton instance.
     */
    public static ParagraphManager getInstance() {
        return (ParagraphManager) FactoryUtil.getSingleton(ParagraphManager.class);
    }

    /**
     * Cached paragraphs
     */
    private Map paragraphs = new Hashtable();

    /**
     * Returns the cached content of the requested template. TemplateInfo properties :
     * <ol>
     * <li>title - title describing template</li>
     * <li>type - jsp / servlet</li>
     * <li>path - jsp / servlet path</li>
     * <li>description - description of a template</li>
     * </ol>
     * @return a Paragraph instance
     */
    public Paragraph getInfo(String key) {
        return (Paragraph) paragraphs.get(key);
    }

    /**
     * Get a map of all registered paragraphs.
     */
    public Map getParagraphs() {
        return paragraphs;
    }

    /**
     * Register all the paragraphs under this and subnodes.
     */
    protected void onRegister(Content node) {
        // register a listener

        Collection paragraphNodes = node.getChildren(ItemType.CONTENTNODE);
        for (Iterator iter = paragraphNodes.iterator(); iter.hasNext();) {
            Content paragraphNode = (Content) iter.next();
            addParagraphToCache(paragraphNode);
        }

        Collection subDefinitions = node.getChildren(ItemType.CONTENT);
        Iterator it = subDefinitions.iterator();
        while (it.hasNext()) {
            Content subNode = (Content) it.next();
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
        }
        catch (Content2BeanException e) {
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
        log.debug("Registering pi [{}] of type [{}]", paragraph.getName(), paragraph.getType()); //$NON-NLS-1$
        paragraphs.put(paragraph.getName(), paragraph);
    }

    public void onClear() {
        this.paragraphs.clear();
    }

}
