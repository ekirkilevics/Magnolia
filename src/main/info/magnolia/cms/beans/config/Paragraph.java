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
import info.magnolia.cms.security.AccessDeniedException;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 */
public class Paragraph {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Paragraph.class);

    private static final String DIALOGS_DIR = "/dialogs/";

    private static Map cachedContent = new Hashtable();

    private String name;

    private String title;

    private String templatePath;

    private String dialogPath;

    private String type;

    private String description;

    private Content dialogContent;

    /**
     * constructor
     */
    public Paragraph() {
    }

    protected static void init() {
        log.info("Config : Initializing Paragraph info");
        Paragraph.cachedContent.clear();
    }

    /**
     * <p>
     * load all paragraph definitions available as a collection of Content objects
     * </p>
     */
    public static void update(String modulePath) {
        HierarchyManager configHierarchyManager = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {
            log.info("Config : loading Paragraph info - " + modulePath);
            Content startPage = configHierarchyManager.getPage(modulePath);
            Content paragraphDefinition = startPage.getContent("Paragraphs");
            Paragraph.cacheContent(paragraphDefinition, modulePath);
            log.info("Config : Paragraph info loaded - " + modulePath);
        }
        catch (RepositoryException re) {
            log.error("Config : Failed to load Paragraph info - " + modulePath);
            log.error(re.getMessage(), re);
        }
    }

    protected static void reload() {
        log.info("Config : re-initializing Paragraph info");
        Paragraph.init();
    }

    /**
     * <p>
     * load content of this paragraph info page in a hash table caching at the system load, this will save lot of time
     * on every request while matching paragraph info <br>
     * paragraphs of Paragraphs.xml overwrite same named paras of AdminParagraphs.xml
     * </p>
     */
    private static void cacheContent(Content content, String modulePath) throws AccessDeniedException {
        Collection contentNodes = content.getChildren(ItemType.NT_CONTENTNODE);
        Iterator definitions = contentNodes.iterator();
        addParagraphsToCache(definitions, modulePath);
        Collection subDefinitions = content.getChildren(ItemType.NT_CONTENT);
        Iterator it = subDefinitions.iterator();
        while (it.hasNext()) {
            Content c = (Content) it.next();
            cacheContent(c, modulePath);
        }
    }

    /**
     * <p>
     * adds paragraph definition to ParagraphInfo cache
     * </p>
     * @param paragraphs iterator as read from the repository
     */
    private static void addParagraphsToCache(Iterator paragraphs, String startPage) {
        while (paragraphs.hasNext()) {
            ContentNode c = (ContentNode) paragraphs.next();
            Paragraph pi = new Paragraph();
            pi.name = c.getNodeData("name").getString();
            pi.templatePath = c.getNodeData("templatePath").getString();
            pi.dialogPath = c.getNodeData("dialogPath").getString();
            pi.type = c.getNodeData("type").getString();
            pi.title = c.getNodeData("title").getString();
            pi.description = c.getNodeData("description").getString();
            // get remaining from dialog definition
            try {
                String dialog = pi.dialogPath;
                if (dialog.lastIndexOf(".") != -1) {
                    dialog = dialog.substring(0, dialog.lastIndexOf("."));
                }
                if (dialog.indexOf("/") != 0) {
                    dialog = startPage + DIALOGS_DIR + dialog; // dialog: pars/text.xml -> /info/dialogs/pars/text.xml
                }
                Content dialogPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getPage(dialog);
                pi.dialogContent = dialogPage;
            }
            catch (RepositoryException re) {
            }
            Paragraph.cachedContent.put(pi.name, pi);
        }
    }

    /**
     * @return String, name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return String, title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return String, templatePath
     */
    public String getTemplatePath() {
        return this.templatePath;
    }

    /**
     * @return String, dialogPath
     */
    public String getDialogPath() {
        return this.dialogPath;
    }

    /**
     * @return String, template type (jsp / servlet)
     */
    public String getTemplateType() {
        return this.type;
    }

    /**
     * @return String, description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return Content, Content holding information for the paragraph dialog
     */
    public Content getDialogContent() {
        return this.dialogContent;
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
    public static Paragraph getInfo(String key) throws Exception {
        return (Paragraph) Paragraph.cachedContent.get(key);
    }
    /**
     * <p>
     * add uuid in paragraph container
     * </p>
     * @param container
     * @param pi , paragraph info
     */
    // private static void updateContainer(ContentNode container, ParagraphInfo pi) {
    // NodeData uuid = container.getNodeData("UUID");
    // if (uuid.isExist()) {
    // ParagraphInfo.cachedContent.put(uuid.getString(),pi);
    // } else {
    // try {
    // String id = ParagraphInfo.getUUID();
    // container.createNodeData("UUID").setValue(id);
    // ParagraphInfo.cachedContent.put(id,pi);
    // } catch (RepositoryException re) {
    // log.fatal("Failed to add UUID in paragraph - "+pi.getName());
    // }
    // }
    // }
    /**
     * @return UUID
     */
    /*
     * private static String getUUID() { return (new UUID()).toString(); }
     */
}
