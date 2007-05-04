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
package info.magnolia.cms.core;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.context.MgnlContext;


/**
 * As of Magnolia 3.0, this class only contains constants and some methods to avoid direct access to the attributes.
 * @author Sameer Charles $Id$
 */
public class Aggregator {
    private static final String FILE = "file"; //$NON-NLS-1$

    public static final String NODE_DATA_TEMPLATE = "nodeDataTemplate";

    private static final String CURRENT_CONTENT = "current_content"; //$NON-NLS-1$

    private static final String EXTENSION = "extension";

    private static final String HANDLE = "handle"; //$NON-NLS-1$

    private static final String MAIN_CONTENT = "main_content"; //$NON-NLS-1$

    private static final String REPOSITORY = "repository";

    private static final String SELECTOR = "selector"; //$NON-NLS-1$

    private static final String TEMPLATE = "mgnl_Template"; //$NON-NLS-1$

    /**
     * @deprecated
     */
    private static final String ACTPAGE = MAIN_CONTENT;

    /**
     * @deprecated
     */
    private static final String CURRENT_ACTPAGE = CURRENT_CONTENT;

    /**
     * Don't instantiate.
     */
    private Aggregator() {
    }


    public static Content getCurrentContent() {
        return (Content) MgnlContext.getAttribute(CURRENT_CONTENT);
    }

    /**
     * Get the current extesion of the request
     * @return
     */
    public static String getExtension() {
        String ext = (String) MgnlContext.getAttribute(EXTENSION);
        if (ext == null) {
            String fileName = StringUtils.substringAfterLast(Path.getURI(), "/");
            ext = StringUtils.substringAfterLast(fileName, ".");

            if(StringUtils.isEmpty(ext)){
                fileName = StringUtils.substringAfterLast(Path.getOriginalURI(), "/");
                ext = StringUtils.substringAfterLast(fileName, ".");
            }
            MgnlContext.setAttribute(EXTENSION, ext);
        }
        return ext;
    }

    public static File getFile() {
        return (File) MgnlContext.getAttribute(FILE);
    }

    /**
     * Returns the URI of the current request, but uses the uri to repository mapping to remove any prefix.
     * @param req request
     * @return request URI without servlet context and without repository mapping prefix
     */
    public static String getHandle() {
        return (String) MgnlContext.getAttribute(HANDLE);
    }

    public static Content getMainContent() {
        return (Content) MgnlContext.getAttribute(MAIN_CONTENT);
    }

    public static String getRepository() {
        return (String) MgnlContext.getAttribute(REPOSITORY);
    }

    public static String getSelector() {
        return (String) MgnlContext.getAttribute(SELECTOR);
    }

    public static Template getTemplate() {
        return (Template) MgnlContext.getAttribute(TEMPLATE);
    }

    public static void setCurrentContent(Content node) {
        MgnlContext.setAttribute(CURRENT_CONTENT, node);
    }

    public static void setExtension(String extension) {
        MgnlContext.setAttribute(EXTENSION, extension);
    }

    public static void setFile(File file) {
        MgnlContext.setAttribute(FILE, file);
    }

    public static void setHandle(String handle) {
        // set the new handle pointing to the real node
        MgnlContext.setAttribute(HANDLE, handle);
    }

    public static void setMainContent(Content node) {
        MgnlContext.setAttribute(MAIN_CONTENT, node);
    }

    public static void setRepository(String repository) {
        MgnlContext.setAttribute(REPOSITORY, repository);
    }

    public static void setSelector(String selector) {
        MgnlContext.setAttribute(SELECTOR, selector);
    }

    public static void setTemplate(Template template) {
        MgnlContext.setAttribute(TEMPLATE, template);
    }

}
