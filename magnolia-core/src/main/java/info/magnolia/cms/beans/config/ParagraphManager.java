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


/**
 * Manages the paragraphs on the system. Modules can register the nodes where the paragraph are defined.
 *
 * @author philipp
 */
public class ParagraphManager extends ObservedManager {

    private static final String ND_I18N_BASENAME = "i18nBasename";

    private static final String ND_DESCRIPTION = "description";

    private static final String ND_TITLE = "title";

    private static final String ND_TYPE = "type";

    private static final String ND_DIALOG_PATH = "dialogPath";

    private static final String ND_TEMPLATE_PATH = "templatePath";

    private static final String ND_DIALOG = "dialog";

    private static final String ND_NAME = "name";

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
    protected Paragraph addParagraphToCache(Content c) {
        Paragraph pi = new Paragraph();

        String name = c.getNodeData(ND_NAME).getString();
        if (StringUtils.isEmpty(name)) {
            name = c.getName();
        }

        pi.setName(name);

        // by default just use the dialog with the same name of the paragraph
        String dialog = c.getNodeData(ND_DIALOG).getString();
        if (StringUtils.isEmpty(dialog)) {
            dialog = c.getName();
        }
        pi.setDialog(dialog);

        pi.setTemplatePath(c.getNodeData(ND_TEMPLATE_PATH).getString());
        pi.setDialogPath(c.getNodeData(ND_DIALOG_PATH).getString());
        pi.setTemplateType(c.getNodeData(ND_TYPE).getString());
        pi.setTitle(c.getNodeData(ND_TITLE).getString());
        pi.setDescription(c.getNodeData(ND_DESCRIPTION).getString());
        pi.setI18nBasename(c.getNodeData(ND_I18N_BASENAME).getString());
        if (Paragraph.log.isDebugEnabled()) {
            Paragraph.log.debug("Registering paragraph [{}]", pi.getName()); //$NON-NLS-1$ 
        }

        paragraphs.put(pi.getName(), pi);
        return pi;
    }

    /**
     * Get the current singleton object
     * @return
     */
    public static ParagraphManager getInstance() {
        return (ParagraphManager) FactoryUtil.getSingleton(ParagraphManager.class);
    }

    public void onClear() {
        this.paragraphs.clear();
    }

}
