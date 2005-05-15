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

import java.util.Hashtable;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 */
public final class Paragraph {

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

    private String templateType;

    private String description;

    private Content dialogContent;

    /**
     * constructor
     */
    private Paragraph() {
    }

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
    public static Paragraph getInfo(String key) {
        return (Paragraph) Paragraph.cachedContent.get(key);
    }

    /**
     * Adds paragraph definition to ParagraphInfo cache.
     * @param paragraphs iterator as read from the repository
     */
    public static Paragraph addParagraphToCache(Content c, String startPage) {

        Paragraph pi = new Paragraph();
        pi.name = c.getNodeData("name").getString();
        pi.templatePath = c.getNodeData("templatePath").getString();
        pi.dialogPath = c.getNodeData("dialogPath").getString();
        pi.templateType = c.getNodeData("type").getString();
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
            Content dialogPage = ContentRepository.getHierarchyManager(ContentRepository.CONFIG).getContent(dialog);
            pi.dialogContent = dialogPage;

            // this is registered in module
            // classes in info.magnolia.cms.beans.config should NEVER depends from classes in info.magnolia.module!!
            // Store.getInstance().registerParagraphDialogHandler(pi.name, pi.dialogContent);

        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }

        if (log.isDebugEnabled()) {
            log.debug("Registering paragraph [" + pi.name + "]");
        }
        cachedContent.put(pi.name, pi);
        return pi;
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
        return this.templateType;
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
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
            //
            .append("name", this.name)
            .append("templateType", this.templateType)
            .append("description", this.description)
            .append("dialogPath", this.dialogPath)
            .append("title", this.title)
            .append("templatePath", this.templatePath)
            .append("dialogContent", this.dialogContent)
            .toString();
    }
}