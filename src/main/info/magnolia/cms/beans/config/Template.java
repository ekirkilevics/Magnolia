/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.beans.config;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 1.1
 */
public class Template {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Template.class);

    private static Iterator templates;

    private static List visibleTemplates = new ArrayList();

    private static Map cachedContent = new Hashtable();

    /**
     * Mandatatory.
     */
    private String name;

    /**
     * Mandatatory.
     */
    private String path;

    private Map alternativePaths;

    /**
     * Mandatatory.
     */
    private String type;

    /**
     * Mandatatory.
     */
    private boolean visible;

    /**
     * Optional fields.
     */
    private String description;

    private String image;

    private String title;

    /**
     * Load all temple definitions available as a collection of Content objects.
     */
    public static void init() {
        log.info("Config : initializing Template info");
        Template.cachedContent.clear();
        Template.visibleTemplates.clear();
    }

    public static void update(String modulePath) {
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            log.info("Config : loading Template info - " + modulePath);
            Content startPage = configHierarchyManager.getPage(modulePath);
            Collection children = startPage.getContentNode("Templates").getChildren();
            if ((children != null) && !(children.isEmpty())) {
                Template.templates = children.iterator();
            }
            Template.cacheContent();
            log.info("Config : Template info loaded - " + modulePath);
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Template info - " + modulePath);
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : re-initializing Template info");
        Template.init();
    }

    /**
     * Get templates collection.
     * @param type , type could be TemplateInfo.CUSTOM_TEMPLATES or TemplateInfo.ADMIN_TEMPLATES
     * @return Collection list containing templates as Content objects
     * @deprecated
     * @see Template#getAvailableTemplates()
     */
    public static Iterator getAvailableTemplates(int type) {
        return getAvailableTemplates();
    }

    /**
     * Get templates collection.
     * @return Collection list containing templates as Content objects
     */
    public static Iterator getAvailableTemplates() {
        return Template.visibleTemplates.iterator();
    }

    /**
     * Load content of this template info page in a hash table caching at the system load, this will save lot of time on
     * every request while matching template info.
     */
    private static void cacheContent() {
        if (Template.templates != null) {
            addTemplatesToCache(Template.templates, Template.visibleTemplates);
            Template.templates = null;
        }
    }

    /**
     * Adds templates definition to TemplatesInfo cache.
     * @param templates iterator as read from the repository
     * @param visibleTemplates List in with all visible templates will be added
     */
    private static void addTemplatesToCache(Iterator templates, List visibleTemplates) {
        while (templates.hasNext()) {
            ContentNode c = (ContentNode) templates.next();
            try {
                Template ti = new Template();
                ti.name = c.getNodeData("name").getValue().getString();
                ti.path = c.getNodeData("path").getValue().getString();
                Template.addAlternativePaths(c, ti);
                ti.type = c.getNodeData("type").getValue().getString();
                ti.visible = c.getNodeData("visible").getBoolean();
                ti.title = c.getNodeData("title").getString();
                ti.description = c.getNodeData("description").getString();
                ti.image = c.getNodeData("image").getString();
                Template.cachedContent.put(ti.name, ti);
                if (ti.visible) {
                    visibleTemplates.add(ti);
                }
            }
            catch (RepositoryException re) {
                log.fatal("Failed to cache TemplateInfo");
            }
        }
    }

    /**
     * Add alternative extention paths to templates cache.
     * @param node
     * @param ti TemplateInfo
     */
    private static void addAlternativePaths(ContentNode node, Template ti) {
        try {
            ContentNode cl = node.getContentNode("SubTemplates");
            Iterator it = cl.getChildren().iterator();
            ti.alternativePaths = new Hashtable();
            while (it.hasNext()) {
                ContentNode c = (ContentNode) it.next();
                ti.alternativePaths.put(c.getNodeData("extension").getString(), c.getNodeData("path").getString());
            }
        }
        catch (RepositoryException re) {
        }
    }

    /**
     * <p>
     * returns the cached content of the requested template <br>
     * TemplateInfo properties :<br>
     * 1. title - title describing template <br>
     * 2. type - jsp / servlet <br>
     * 3. path - jsp / servlet path <br>
     * 4. description - description of a template
     * </p>
     * @return TemplateInfo
     */
    public static Template getInfo(String key) throws Exception {
        return (Template) Template.cachedContent.get(key);
    }

    /**
     *
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     */
    public String getTitle() {
        return this.title;
    }

    /**
     *
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return default template path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @param extension
     * @return template path for the specified extension
     */
    public String getPath(String extension) {
        try {
            String path = (String) this.alternativePaths.get(extension);
            if (path == null) {
                return this.getPath();
            }
            return path;
        }
        catch (Exception e) {
            return this.getPath();
        }
    }

    /**
     *
     */
    public String getType() {
        return this.type;
    }

    /**
     *
     */
    public String getImage() {
        return this.image;
    }

    /**
     *
     */
    public boolean isVisible() {
        return this.visible;
    }
}
