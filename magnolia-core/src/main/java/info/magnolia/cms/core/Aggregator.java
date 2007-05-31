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

import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.context.MgnlContext;

/**
 * As of Magnolia 3.0, this class only contains constants and some methods to avoid direct access to the attributes.
 *
 * @author Sameer Charles $Id$
 * @deprecated use WebContext.getAggregationState()
 */
public class Aggregator {
    private static final String FILE = "file"; //$NON-NLS-1$

    /**
     * @deprecated use FileProperties.PROPERTY_TEMPLATE
     */
    public static final String NODE_DATA_TEMPLATE = "nodeDataTemplate";

    private static final String CURRENT_CONTENT = "current_content"; //$NON-NLS-1$

    private static final String EXTENSION = "extension";

    private static final String HANDLE = "handle"; //$NON-NLS-1$

    private static final String MAIN_CONTENT = "main_content"; //$NON-NLS-1$

    private static final String REPOSITORY = "repository";

    private static final String SELECTOR = "selector"; //$NON-NLS-1$

    private static final String TEMPLATE = "mgnl_Template"; //$NON-NLS-1$

    /**
     * @deprecated use MgnlContext.getAggrigationStatus()
     */
    public static final String ACTPAGE = MAIN_CONTENT;

    /**
     * @deprecated use MgnlContext.getAggrigationStatus()
     */
    public static final String CURRENT_ACTPAGE = CURRENT_CONTENT;

    /**
     * Don't instantiate.
     */
    private Aggregator() {
    }


    public static Content getCurrentContent() {
        return getAggregationState().getCurrentContent();
    }

    /**
     * Get the current extesion of the request
     * @return
     */
    public static String getExtension() {
        return getAggregationState().getExtension();
    }

    public static File getFile() {
        return getAggregationState().getFile();
    }

    /**
     * Returns the URI of the current request, but uses the uri to repository mapping to remove any prefix.
     *
     * @return request URI without servlet context and without repository mapping prefix
     */
    public static String getHandle() {
        return  getAggregationState().getHandle();
    }

    public static Content getMainContent() {
        return  getAggregationState().getMainContent();
    }

    public static String getRepository() {
        return  getAggregationState().getRepository();
    }

    public static String getSelector() {
        return  getAggregationState().getSelector();
    }

    public static Template getTemplate() {
        return  getAggregationState().getTemplate();
    }

    public static void setCurrentContent(Content node) {
        getAggregationState().setCurrentContent(node);
    }

    public static void setExtension(String extension) {
        getAggregationState().setExtension(extension);
    }

    public static void setFile(File file) {
        getAggregationState().setFile(file);
    }

    public static void setHandle(String handle) {
        // set the new handle pointing to the real node
        getAggregationState().setHandle(handle);
    }

    public static void setMainContent(Content node) {
        getAggregationState().setMainContent(node);
    }

    public static void setRepository(String repository) {
        getAggregationState().setRepository(repository);
    }

    public static void setSelector(String selector) {
        getAggregationState().setSelector(selector);
    }

    public static void setTemplate(Template template) {
        getAggregationState().setTemplate(template);
    }

    private static AggregationState getAggregationState() {
        return MgnlContext.getAggregationState();
    }


}
