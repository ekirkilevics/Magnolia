/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
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

    private static final String MAIN_CONTENT = "main_content"; //$NON-NLS-1$

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
