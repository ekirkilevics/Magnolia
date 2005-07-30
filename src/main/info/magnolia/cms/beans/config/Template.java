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
import info.magnolia.cms.core.ContentHandler;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;

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

    private static List visibleTemplates = new ArrayList();

    private static Map cachedContent = new Hashtable();

    /**
     * Template name.
     */
    private String name;

    /**
     * Template path.
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

    private String location;

    /**
     * Load all temple definitions available as a collection of Content objects.
     */
    public static void init() {
        log.info("Config : initializing Template info"); //$NON-NLS-1$
        Template.cachedContent.clear();
        Template.visibleTemplates.clear();
    }

    public static void update(String modulePath) {
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            log.info("Config : loading Template info - " + modulePath); //$NON-NLS-1$
            Content startPage = configHierarchyManager.getContent(modulePath);
            Collection children = startPage.getContent("Templates") //$NON-NLS-1$
                .getChildren(ItemType.CONTENTNODE, ContentHandler.SORT_BY_SEQUENCE);

            if ((children != null) && !(children.isEmpty())) {
                Iterator templates = children.iterator();
                Template.cacheContent(templates);
            }
            log.info("Config : Template info loaded - " + modulePath); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Template info - " + modulePath); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    public static void reload() {
        log.info("Config : re-initializing Template info"); //$NON-NLS-1$
        Template.init();
        update("modules/templating"); //$NON-NLS-1$
    }

    /**
     * Get templates collection.
     * @return Collection list containing templates as Template objects
     */
    public static Iterator getAvailableTemplates() {
        return Template.visibleTemplates.iterator();
    }

    /**
     * Get templates collection after access control filter applied using specified AccessManager
     * @return Collection list containing templates as Template objects
     */
    public static Iterator getAvailableTemplates(AccessManager accessManager) {
        List templateList = new ArrayList();
        Iterator it = Template.visibleTemplates.iterator();
        while (it.hasNext()) {
            Template template = (Template) it.next();
            if (accessManager.isGranted(template.getLocation(), Permission.READ)) {
                templateList.add(template);
            }
        }
        return templateList.iterator();
    }

    /**
     * Load content of this template info page in a hash table caching at the system load, this will save lot of time on
     * every request while matching template info.
     */
    private static void cacheContent(Iterator templates) {
        if (templates != null) {
            addTemplatesToCache(templates, Template.visibleTemplates);
        }
    }

    /**
     * Adds templates definition to TemplatesInfo cache.
     * @param templates iterator as read from the repository
     * @param visibleTemplates List in with all visible templates will be added
     */
    private static void addTemplatesToCache(Iterator templates, List visibleTemplates) {
        while (templates.hasNext()) {
            Content c = (Content) templates.next();
            try {
                Template ti = new Template();
                ti.name = c.getNodeData("name").getValue().getString(); //$NON-NLS-1$
                ti.path = c.getNodeData("path").getValue().getString(); //$NON-NLS-1$
                Template.addAlternativePaths(c, ti);
                ti.type = c.getNodeData("type").getValue().getString(); //$NON-NLS-1$
                ti.visible = c.getNodeData("visible").getBoolean(); //$NON-NLS-1$
                ti.title = c.getNodeData("title").getString(); //$NON-NLS-1$
                ti.description = c.getNodeData("description").getString(); //$NON-NLS-1$
                ti.image = c.getNodeData("image").getString(); //$NON-NLS-1$
                Template.cachedContent.put(ti.name, ti);
                ti.setLocation(c.getHandle());
                if (ti.visible) {
                    visibleTemplates.add(ti);
                }
            }
            catch (RepositoryException re) {
                log.fatal("Failed to cache TemplateInfo"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Add alternative extention paths to templates cache.
     * @param node
     * @param ti TemplateInfo
     */
    private static void addAlternativePaths(Content node, Template ti) {
        try {
            Content cl = node.getContent("SubTemplates"); //$NON-NLS-1$
            Iterator it = cl.getChildren().iterator();
            ti.alternativePaths = new Hashtable();
            while (it.hasNext()) {
                Content c = (Content) it.next();
                ti.alternativePaths.put(c.getNodeData("extension").getString(), c.getNodeData("path").getString()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (RepositoryException re) {
            log.error("RepositoryException caught while loading alternative templates path configuration: " //$NON-NLS-1$
                + re.getMessage(), re);
        }
    }

    /**
     * Returns the cached content of the requested template. TemplateInfo properties:
     * <ol>
     * <li> title - title describing template</li>
     * <li> type - jsp / servlet</li>
     * <li> path - jsp / servlet path</li>
     * <li> description - description of a template</li>
     * </ol>
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
