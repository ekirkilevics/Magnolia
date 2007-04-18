/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
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
 *
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
            final Paragraph p = (Paragraph) Content2BeanUtil.toBean(c, Paragraph.class);
            if (StringUtils.isEmpty(p.getType())) {
                p.setType(DEFAULT_PARA_TYPE);
            }
            if (StringUtils.isEmpty(p.getDialog())) {
                p.setDialog(p.getName());
            }
            log.debug("Registering pi [{}] of type [{}]", p.getName(), p.getType()); //$NON-NLS-1$
            paragraphs.put(p.getName(), p);
        } catch (Content2BeanException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public void onClear() {
        this.paragraphs.clear();
    }

}
