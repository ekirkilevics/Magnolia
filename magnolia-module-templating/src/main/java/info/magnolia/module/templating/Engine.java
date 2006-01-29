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
package info.magnolia.module.templating;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.RegisterException;

import java.util.Collection;
import java.util.Iterator;
import java.util.jar.JarFile;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Module "templating" main class.
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class Engine implements Module {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Engine.class);

    /**
     * base path jcr property.
     */
    private static final String ATTRIBUTE_BASE_PATH = "basePath"; //$NON-NLS-1$

    /**
     * Module name.
     */
    protected String moduleName;

    /**
     * Base path in configuration.
     */
    protected String basePath;

    /*
     * (non-Javadoc)
     * @see info.magnolia.cms.module.Module#register(java.lang.String, java.lang.String, info.magnolia.cms.core.Content,
     * java.util.jar.JarFile, int)
     */
    public void register(String moduleName, String version, Content moduleNode, JarFile jar, int registerState)
        throws RegisterException {
        // nothing to do
    }

    /**
     * @see info.magnolia.cms.module.Module#init(info.magnolia.cms.module.ModuleConfig)
     */
    public void init(ModuleConfig config) {
        this.moduleName = config.getModuleName();
        this.basePath = (String) config.getInitParameters().get(ATTRIBUTE_BASE_PATH);

        // set local store to be accessed via admin interface classes or JSP

        Store.getInstance().setStore(config.getLocalStore());

        log.info("Module: " + this.moduleName); //$NON-NLS-1$
        log.info(this.moduleName + ": updating Template list"); //$NON-NLS-1$
        Template.update(this.basePath);
        log.info(this.moduleName + ": updating Paragraph list"); //$NON-NLS-1$
        registerParagraphs();

        registerEventListeners();
    }

    /**
     * @see info.magnolia.cms.module.Module#destroy()
     */
    public void destroy() {
        // do nothing
        // @todo remove event listeners?
    }

    /**
     * Add jcr event listeners for automatic reloading of templates and paragraphs when content changes.
     */
    private void registerEventListeners() {

        // automatically reload paragraphs
        registerEventListeners("/" + this.basePath + "/Paragraphs", new EventListener() { //$NON-NLS-1$ //$NON-NLS-2$

                public void onEvent(EventIterator iterator) {
                    // reload everything, should we handle single-paragraph reloading?
                    registerParagraphs();
                }
            });

        // automatically reload templates
        registerEventListeners("/" + this.basePath + "/Templates", new EventListener() { //$NON-NLS-1$ //$NON-NLS-2$

                public void onEvent(EventIterator iterator) {
                    // reload everything, should we handle single-template reloading?
                    Template.reload();
                }
            });
    }

    /**
     * Register a single event listener, bound to the given path.
     * @param observationPath repository path
     * @param listener event listener
     */
    private void registerEventListeners(String observationPath, EventListener listener) {

        log.info("Registering event listener for path [" + observationPath + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        try {

            ObservationManager observationManager = ContentRepository
                .getHierarchyManager(ContentRepository.CONFIG)
                .getWorkspace()
                .getObservationManager();

            observationManager.addEventListener(listener, Event.NODE_ADDED
                | Event.PROPERTY_ADDED
                | Event.PROPERTY_CHANGED, observationPath, true, null, null, false);
        }
        catch (RepositoryException e) {
            log.error("Unable to add event listeners for " + observationPath, e); //$NON-NLS-1$
        }

    }

    /**
     * Load all paragraph definitions available as a collection of Content objects.
     */
    protected void registerParagraphs() {
        // simply overwrite (it's a map, clear is not needed)
        // Paragraph.cachedContent.clear();

        log.info(this.moduleName + ": initializing Paragraph info"); //$NON-NLS-1$
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            log.info(this.moduleName + ": loading Paragraph info - " + this.basePath); //$NON-NLS-1$
            Content startPage = configHierarchyManager.getContent(this.basePath);
            Content paragraphDefinition = startPage.getContent("Paragraphs"); //$NON-NLS-1$

            cacheParagraphsContent(paragraphDefinition);
            log.info(this.moduleName + ": Paragraph info loaded - " + this.basePath); //$NON-NLS-1$
        }
        catch (RepositoryException re) {
            log.error(this.moduleName + ": Failed to load Paragraph info - " + this.basePath); //$NON-NLS-1$
            log.error(re.getMessage(), re);
        }
    }

    /**
     * Adds paragraph definition to ParagraphInfo cache.
     * @param paragraphs iterator as read from the repository
     */
    private void addParagraphsToCache(Iterator paragraphs) {
        while (paragraphs.hasNext()) {
            Content c = (Content) paragraphs.next();
            Paragraph.addParagraphToCache(c, this.basePath);
        }
    }

    /**
     * Load content of this paragraph info page in a hash table caching at the system load, this will save lot of time
     * on every request while matching paragraph info.
     * @param content paragraph node
     */
    private void cacheParagraphsContent(Content content) {
        Collection contentNodes = content.getChildren(ItemType.CONTENTNODE);
        Iterator definitions = contentNodes.iterator();
        addParagraphsToCache(definitions);
        Collection subDefinitions = content.getChildren(ItemType.CONTENT);
        Iterator it = subDefinitions.iterator();
        while (it.hasNext()) {
            Content c = (Content) it.next();
            cacheParagraphsContent(c);
        }
    }

}