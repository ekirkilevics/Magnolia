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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the paragraph ot the system. The modules register the nodes where the paragraph are defined.
 * @author philipp
 */
public class ParagraphManager extends ObservedManager {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ParagraphManager.class);

    /**
     * The current implementation of the ParagraphManager. Defeined in magnolia.properties.
     */
    private static ParagraphManager instance = (ParagraphManager) FactoryUtil.getSingleton(ParagraphManager.class);

    /**
     * Cached paragraphs
     */
    protected Map paragraphs = new Hashtable();

    /**
     * Returns the cached content of the requested template. TemplateInfo properties :
     * <ol>
     * <li>title - title describing template</li>
     * <li>type - jsp / servlet</li>
     * <li>path - jsp / servlet path</li>
     * <li>description - description of a template</li>
     * </ol>
     * @return TemplateInfo
     */
    public Paragraph getInfo(String key) {
        return (Paragraph) paragraphs.get(key);
    }

    /**
     * Get a map of all registered paragraphs.
     * @return
     */
    public Map getParagraphs() {
        return paragraphs;
    }

    /**
     * Register all the paragraphs under this and subnodes.
     * @param node
     * @param observate true if an eventlistener should get registered
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
    protected Paragraph addParagraphToCache(Content c) {
        Paragraph pi = new Paragraph();

        String name = c.getNodeData("name").getString();//$NON-NLS-1$
        if (StringUtils.isEmpty(name)) {
            name = c.getName();
        }

        pi.setName(name);
        pi.setTemplatePath(c.getNodeData("templatePath").getString()); //$NON-NLS-1$
        pi.setDialogPath(c.getNodeData("dialogPath").getString()); //$NON-NLS-1$
        pi.setTemplateType(c.getNodeData("type").getString()); //$NON-NLS-1$
        pi.setTitle(c.getNodeData("title").getString()); //$NON-NLS-1$
        pi.setDescription(c.getNodeData("description").getString()); //$NON-NLS-1$
        if (Paragraph.log.isDebugEnabled()) {
            Paragraph.log.debug("Registering paragraph [" + pi.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        paragraphs.put(pi.getName(), pi);
        log.info("paragraph added [{}]", pi.getName());
        return pi;
    }

    /**
     * Get the current singleton object
     * @return
     */
    public static ParagraphManager getInstance() {
        return instance;
    }

    public void onClear() {
        this.paragraphs.clear();
    }

}
