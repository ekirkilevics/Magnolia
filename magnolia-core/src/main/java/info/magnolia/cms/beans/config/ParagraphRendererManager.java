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

import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.FactoryUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.Writer;
import java.io.IOException;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphRendererManager extends ObservedManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ParagraphRendererManager.class);

    private final Map paragraphRenderers;

    public ParagraphRendererManager() {
        paragraphRenderers = Collections.synchronizedMap(new LinkedHashMap());
    }

    public static ParagraphRendererManager getInstance() {
        return (ParagraphRendererManager) FactoryUtil.getSingleton(ParagraphRendererManager.class);
    }

    public void render(Paragraph paragraph, Writer out) throws IOException {
        final ParagraphRenderer renderer = getRenderer(paragraph.getType());
        renderer.render(paragraph, out);
    }

    protected ParagraphRenderer getRenderer(String name) {
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
                log.warn("Can't register template renderer at {},missing name or class property.", paragraphRendererNode.getHandle());
                continue;
            }

            try {
                final ParagraphRenderer renderer = (ParagraphRenderer) ClassUtil.newInstance(clazz);
                registererParagraphRenderer(name, renderer);
                log.debug("Registered template render [{}] with name {}", clazz, name);
            } catch (InstantiationException e) {
                throw new IllegalStateException("Can't register paragraph renderer with name [" + name + "] and class [" + clazz + "] : " + e.getClass().getName() + " : " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Can't register paragraph renderer with name [" + name + "] and class [" + clazz + "] : " + e.getClass().getName() + " : " + e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Can't register paragraph renderer with name [" + name + "] and class [" + clazz + "] : " + e.getClass().getName() + " : " + e.getMessage(), e);
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
}
